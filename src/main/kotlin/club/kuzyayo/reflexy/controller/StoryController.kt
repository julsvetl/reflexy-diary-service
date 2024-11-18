package club.kuzyayo.reflexy.controller

import club.kuzyayo.reflexy.dto.SaveStoryDto
import club.kuzyayo.reflexy.dto.StoryDto
import club.kuzyayo.reflexy.exception.AccessDeniedException
import club.kuzyayo.reflexy.service.AuthenticationService
import club.kuzyayo.reflexy.service.StoryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stories")
class StoryController @Autowired constructor(
    private val storyService: StoryService,
    private val authenticationService: AuthenticationService
) {

    @GetMapping
    fun getStories(
        @RequestParam page: Int,
        @RequestParam pageSize: Int,
        @AuthenticationPrincipal principal: OAuth2User
    ): List<StoryDto> {
        return storyService.findByUserId(
            authenticationService.getUserId(principal),
            page,
            pageSize
        )
    }

    @PostMapping
    fun createStory(
        @RequestBody story: SaveStoryDto,
        @AuthenticationPrincipal principal: OAuth2User
    ): StoryDto {
        return storyService.create(authenticationService.getUserId(principal), story)
    }

    @GetMapping("/{id}")
    fun getStory(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: OAuth2User
    ): StoryDto {
        val story = storyService.findById(id)
        checkStoryBelongsToUser(authenticationService.getUserId(principal), story)
        return story
    }

    @PutMapping("/{id}")
    fun updateStory(
        @PathVariable id: Long,
        @RequestBody story: SaveStoryDto,
        @AuthenticationPrincipal principal: OAuth2User
    ): StoryDto {
        checkStoryBelongsToUser(authenticationService.getUserId(principal), id)
        return storyService.update(id, story)
    }

    @DeleteMapping("/{id}")
    fun removeStory(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: OAuth2User
    ) {
        checkStoryBelongsToUser(authenticationService.getUserId(principal), id)
        return storyService.remove(id)
    }

    private fun checkStoryBelongsToUser(userId: Long, storyId: Long) {
        val story = storyService.findById(storyId)
        checkStoryBelongsToUser(userId, story)
    }

    private fun checkStoryBelongsToUser(userId: Long, story: StoryDto) {
        if (story.userId != userId) {
            throw AccessDeniedException("Story with id ${story.id} does not belong to user with id $userId")
        }
    }
}