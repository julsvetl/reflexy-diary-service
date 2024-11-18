package club.kuzyayo.reflexy.service.impl

import club.kuzyayo.reflexy.domain.Activity
import club.kuzyayo.reflexy.domain.ActivityCategory
import club.kuzyayo.reflexy.domain.Story
import club.kuzyayo.reflexy.domain.StoryRepository
import club.kuzyayo.reflexy.dto.ActivityCategoryDto
import club.kuzyayo.reflexy.dto.ActivityDto
import club.kuzyayo.reflexy.dto.SaveStoryDto
import club.kuzyayo.reflexy.dto.StoryDto
import club.kuzyayo.reflexy.exception.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.*
import kotlin.test.assertTrue

class StoryServiceImplTest {
    private val storyRepository: StoryRepository = mock()

    private val storyService = StoryServiceImpl(storyRepository)

    @Test
    fun `Should find story by id when it exists`() {
        //given
        val storyId = 555L
        val foundStoryEntity = storyEntity(storyId)
        given(storyRepository.findById(storyId)).willReturn(Optional.of(foundStoryEntity))

        //when
        val actualStoryDto = storyService.findById(storyId)

        //then
        storyIsCorrect(foundStoryEntity, actualStoryDto)
    }

    @Test
    fun `Should throw exception when story not found by id`() {
        //given
        val storyId = 555L
        given(storyRepository.findById(storyId)).willReturn(Optional.empty())

        //when
        val exception = assertThrows<EntityNotFoundException> { storyService.findById(storyId) }

        //then
        assertEquals(exception.message, "Could not find story with id $storyId")
    }

    @Test
    fun `Should delete story by id when it exists`() {
        //given
        val storyId = 555L
        given(storyRepository.existsById(storyId)).willReturn(true)
        doNothing().`when`(storyRepository).deleteById(storyId)

        //when
        storyService.remove(storyId)

        //then
        verify(storyRepository).deleteById(storyId)
    }

    @Test
    fun `Should throw exception when it story to be removed does not exist`() {
        //given
        val storyId = 555L
        given(storyRepository.existsById(storyId)).willReturn(false)

        //when
        val exception = assertThrows<EntityNotFoundException> { storyService.remove(storyId) }

        //then
        assertEquals(exception.message, "Could not find story with id $storyId")
        verify(storyRepository, never()).deleteById(storyId)
    }

    @Test
    fun `Should create story`() {
        //given
        val requestUserId = 111L
        val requestStoryDto = saveStoryDto()
        val newStoryEntity = storyEntity(storyId = 222L, userId = requestUserId, baseStoryDto = requestStoryDto)
        given(storyRepository.save(check<Story> {
            storyIsCorrect(
                expectedStoryId = null,
                expectedUserId = requestUserId,
                expectedStory = requestStoryDto,
                actualStory = it
            )
        })).willReturn(newStoryEntity)

        //when
        val actualCreatedStoryDto = storyService.create(requestUserId, requestStoryDto)

        //then
        storyIsCorrect(expectedStory = newStoryEntity, actualStory = actualCreatedStoryDto)
    }

    @Test
    fun `Should update story`() {
        val requestStoryDto = saveStoryDto()
        val storyId: Long = 111
        val initialStoryEntity = storyEntity(storyId = storyId, userId = 222)
        val updatedStoryEntity = storyEntity(storyId = storyId, userId = 222, baseStoryDto = requestStoryDto)
        given(storyRepository.findById(storyId)).willReturn(Optional.of(initialStoryEntity))
        given(storyRepository.save(check<Story> {
            storyIsCorrect(
                expectedStoryId = storyId,
                expectedUserId = initialStoryEntity.userId,
                expectedStory = requestStoryDto,
                actualStory = it
            )
        })).willReturn(updatedStoryEntity)

        //when
        val actualUpdatedStoryDto = storyService.update(storyId, requestStoryDto)

        //then
        storyIsCorrect(expectedStory = updatedStoryEntity, actualStory = actualUpdatedStoryDto)
    }

    @Test
    fun `Should throw exception when story to be updated not found`() {
        val requestStoryDto = saveStoryDto()
        val storyId: Long = 111
        given(storyRepository.findById(storyId)).willReturn(Optional.empty())


        //when
        val exception = assertThrows<EntityNotFoundException> { storyService.update(storyId, requestStoryDto) }

        //then
        assertEquals(exception.message, "Could not find story with id $storyId")
    }

    @Test
    fun `should find stories by user id when they exist`() {
        //given
        val userId = 111L
        val pageNumber = 0
        val pageSize = 10
        val story1 = storyEntity(222L, userId)
        val story2 = storyEntity(333L, userId)
        given(
            storyRepository.findByUserId(
                eq(userId),
                check {
                    assertEquals(pageNumber, it.pageNumber)
                    assertEquals(pageSize, it.pageSize)
                })
        ).willReturn(listOf(story1, story2))

        //when
        val actualStories = storyService.findByUserId(userId, pageNumber, pageSize)

        //then
        assertEquals(2, actualStories.size)
        storyIsCorrect(story1, actualStories[0])
        storyIsCorrect(story2, actualStories[1])
    }

    @Test
    fun `should return empty collection when no stories found for user`() {
        //given
        val userId = 111L
        val pageNumber = 0
        val pageSize = 10
        given(
            storyRepository.findByUserId(
                eq(userId),
                check {
                    assertEquals(pageNumber, it.pageNumber)
                    assertEquals(pageSize, it.pageSize)
                })
        ).willReturn(emptyList())

        //when
        val actualStories = storyService.findByUserId(userId, pageNumber, pageSize)

        //then
        assertTrue { actualStories.isEmpty() }
    }

    private fun storyIsCorrect(expectedStory: Story, actualStory: StoryDto) {
        assertEquals(expectedStory.id, actualStory.id)
        assertEquals(expectedStory.userId, actualStory.userId)
        assertEquals(expectedStory.title, actualStory.title)
        assertEquals(expectedStory.activities.size, actualStory.activities.size)
        actualStory.activities.forEach { actualActivity ->
            val expectedActivity =
                expectedStory.activities.find { expectedActivity -> expectedActivity.id == actualActivity.id }
            assertNotNull(expectedActivity) { "Activity with id ${actualActivity.id} is not expected" }
            activityDtoIsCorrect(expectedActivity!!, actualActivity)
        }
    }

    private fun activityDtoIsCorrect(
        expectedActivity: Activity,
        actualActivity: ActivityDto
    ) {
        assertEquals(expectedActivity.id, actualActivity.id)
        assertEquals(expectedActivity.title, actualActivity.title)
        assertEquals(expectedActivity.category.id, actualActivity.category.id)
        assertEquals(expectedActivity.category.title, actualActivity.category.title)
    }

    private fun storyIsCorrect(
        expectedStoryId: Long? = null,
        expectedUserId: Long? = null,
        expectedStory: SaveStoryDto,
        actualStory: Story
    ) {
        assertEquals(expectedStoryId, actualStory.id)
        assertEquals(expectedUserId, actualStory.userId)
        assertEquals(expectedStory.title, actualStory.title)
        assertNotNull(actualStory.activities)
        assertEquals(expectedStory.activities, actualStory.activities.size)
        actualStory.activities.forEach { actualActivity ->
            val expectedActivity =
                expectedStory.activities.find { expectedActivity -> expectedActivity.id == actualActivity.id }
            assertNotNull(expectedActivity) { "Activity with id ${actualActivity.id} is not expected" }
            activityEntityIsCorrect(expectedActivity!!, actualActivity)
        }
    }

    private fun activityEntityIsCorrect(
        expectedActivity: ActivityDto,
        actualActivity: Activity
    ) {
        assertEquals(expectedActivity.id, actualActivity.id)
        assertEquals(expectedActivity.title, actualActivity.title)
        assertEquals(expectedActivity.category.id, actualActivity.category.id)
        assertEquals(expectedActivity.category.title, actualActivity.category.title)
    }


    private fun saveStoryDto() = SaveStoryDto(
        title = "New Story Title",
        activities = listOf(
            ActivityDto(
                id = 777,
                title = "New Activity Title",
                category = ActivityCategoryDto(
                    id = 888,
                    title = "New Activity Category Title"
                )
            )
        )
    )

    private fun storyEntity(
        storyId: Long? = null,
        userId: Long = 666,
        baseStoryDto: SaveStoryDto? = null
    ): Story {
        val baseActivityDto = baseStoryDto?.activities?.get(0)
        val baseActivityCategoryDto = baseActivityDto?.category
        val activityId = baseActivityDto?.id ?: 777
        val activityCategoryId = baseActivityCategoryDto?.id ?: 888
        return Story(
            id = storyId,
            userId = userId,
            title = baseStoryDto?.title ?: "Story ${storyId ?: ""} Title",
            activities = listOf(
                Activity(
                    id = activityId,
                    title = baseActivityDto?.title ?: "Activity $activityId Title",
                    category = ActivityCategory(
                        id = activityCategoryId,
                        title = baseActivityCategoryDto?.title ?: "Activity Category $activityCategoryId Title"
                    )
                )
            )
        )
    }
}