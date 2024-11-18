package club.kuzyayo.reflexy.dto

data class StoryDto(
    val id: Long,
    val userId: Long,
    val title: String,
    val activities: List<ActivityDto>,
) {
    constructor(id: Long, userId: Long, storyDto: SaveStoryDto) : this(
        id = id,
        userId = userId,
        title = storyDto.title,
        activities = storyDto.activities,
    )
}