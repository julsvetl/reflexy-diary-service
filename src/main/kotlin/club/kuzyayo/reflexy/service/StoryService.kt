package club.kuzyayo.reflexy.service

import club.kuzyayo.reflexy.dto.SaveStoryDto
import club.kuzyayo.reflexy.dto.StoryDto
import club.kuzyayo.reflexy.exception.EntityNotFoundException

/**
 * Service to manage stories.
 *
 * @author ysvetlichnaia
 * @since 0.0.1
 */
interface StoryService {

    /**
     * Returns dto for story found by provided id.
     *
     * @param id desired story id
     * @return story dto
     * @throws EntityNotFoundException if story has not been found by provided id
     */
    fun findById(id: Long): StoryDto

    /**
     * Removes story by provided id.
     *
     * @param id desired story id
     * @throws EntityNotFoundException if story has not been found by provided id
     */
    fun remove(id: Long)

    /**
     * Creates new story.
     *
     * @param userId id of user who creates story
     * @param story new story dto
     * @return dto for created story
     */
    fun create(userId: Long, story: SaveStoryDto): StoryDto

    /**
     * Updates story with provided id.
     *
     * @param id desired story id
     * @param updatedStory updated story dto
     * @return dto for updated story
     * @throws EntityNotFoundException if story has not been found by provided id
     */
    fun update(id: Long, updatedStory: SaveStoryDto): StoryDto

    /**
     * Returns stories for provided user.
     *
     * @param userId id of user for which stories are to be found
     * @param pageNumber desired page number
     * @param pageSize desired page size
     * @return list of stories dto
     */
    fun findByUserId(
        userId: Long,
        pageNumber: Int,
        pageSize: Int
    ): List<StoryDto>
}