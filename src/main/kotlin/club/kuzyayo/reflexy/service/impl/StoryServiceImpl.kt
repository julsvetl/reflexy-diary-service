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
import club.kuzyayo.reflexy.service.StoryService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class StoryServiceImpl(val storyRepository: StoryRepository) : StoryService {

    override fun findById(id: Long): StoryDto {
        return storyRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("Could not find story with id $id") }
    }

    override fun remove(id: Long) {
        if (storyRepository.existsById(id)) {
            storyRepository.deleteById(id)
        } else {
            throw EntityNotFoundException("Could not find story with id $id")
        }
    }

    override fun create(userId: Long, story: SaveStoryDto): StoryDto {
        val storyEntity = story.toEntity(userId = userId)

        return storyRepository.save(storyEntity).toDto()
    }

    override fun update(id: Long, updatedStory: SaveStoryDto): StoryDto {
        val existingStory = storyRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Could not find story with id $id") }
        val storyEntity = updatedStory.toEntity(id = id, userId = existingStory.userId)
        return storyRepository.save(storyEntity).toDto()
    }

    override fun findByUserId(userId: Long, pageNumber: Int, pageSize: Int): List<StoryDto> {
        return storyRepository.findByUserId(userId, PageRequest.of(pageNumber, pageSize))
            .map { it.toDto() }
    }
}

private fun SaveStoryDto.toEntity(id: Long? = null, userId: Long): Story {
    return Story(
        id = id,
        userId = userId,
        title = title,
        activities = activities.map { it.toEntity() }
    )
}

private fun ActivityDto.toEntity(): Activity {
    return Activity(
        id = id,
        title = title,
        category = ActivityCategory(
            id = category.id,
            title = category.title
        )
    )
}

private fun Story.toDto(): StoryDto {
    return StoryDto(
        id = requireIdNotNull(id),
        userId = userId,
        title = title,
        activities = activities.map { it.toDto() },
    )
}

private fun Activity.toDto(): ActivityDto {
    return ActivityDto(
        id = requireIdNotNull(id),
        title = title,
        category = ActivityCategoryDto(
            id = requireIdNotNull(category.id),
            title = category.title,
        )
    )
}

private fun requireIdNotNull(id: Long?) =
    requireNotNull(id) { "Entity retrieved from DB is supposed to have id" }
