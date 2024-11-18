package club.kuzyayo.reflexy.dao.impl

import club.kuzyayo.reflexy.ReflexyDiaryServiceApplication
import club.kuzyayo.reflexy.config.H2JpaConfig
import club.kuzyayo.reflexy.domain.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


@ActiveProfiles("test")
@SpringBootTest(classes = [ReflexyDiaryServiceApplication::class, H2JpaConfig::class])
class StoryDaoImplTest {

    @Autowired
    lateinit var storyRepository: StoryRepository

    @Autowired
    lateinit var activityRepository: ActivityRepository

    @Autowired
    lateinit var activityCategoryRepository: ActivityCategoryRepository

    @Test
    fun `Should find story by id`() {
        // given
        val activityCategory1 = activityCategoryRepository.save(
            ActivityCategory(
                title = "Test Category 1"
            )
        )
        val activityCategory2 = activityCategoryRepository.save(
            ActivityCategory(
                title = "Test Category 2"
            )
        )
        val activity1 = activityRepository.save(
            Activity(
                title = "Test Activity 1",
                category = activityCategory1
            )
        )
        val activity2 = activityRepository.save(
            Activity(
                title = "Test Activity 2",
                category = activityCategory2
            )
        )

        val story1 = storyRepository.save(
            Story(
                userId = 5,
                title = "Test Story 1",
                activities = listOf(activity1)
            )
        )
        val story2 = storyRepository.save(
            Story(
                userId = 5,
                title = "Test Story 2",
                activities = listOf(activity2)
            )
        )

        // when
        val actualFoundStories = storyRepository.findAll()

        // then
        storyIsCorrect(expectedId = 1, expectedStory = story1, actualStory = actualFoundStories[0])
        storyIsCorrect(expectedId = 2, expectedStory = story2, actualStory = actualFoundStories[1])
    }

    private fun storyIsCorrect(
        expectedId: Long,
        expectedStory: Story,
        actualStory: Story,
    ) {
        assertEquals(expectedId, actualStory.id)
        assertEquals(expectedStory.title, actualStory.title)
        assertEquals(expectedStory.activities.size, actualStory.activities.size)
        actualStory.activities.forEach { actualActivity ->
            val expectedActivity =
                expectedStory.activities.find { expectedActivity -> expectedActivity.id == actualActivity.id }
            assertNotNull(expectedActivity) { "Activity not among expected" }
            activityIsCorrect(expectedActivity!!, actualActivity)
        }
    }

    private fun activityIsCorrect(expectedActivity: Activity, actualActivity: Activity) {
        assertEquals(expectedActivity.id, actualActivity.id)
        assertEquals(expectedActivity.title, actualActivity.title)
        assertEquals(expectedActivity.category.id, actualActivity.category.id)
        assertEquals(expectedActivity.category.title, actualActivity.category.title)
    }
}