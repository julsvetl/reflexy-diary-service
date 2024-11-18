package club.kuzyayo.reflexy.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface StoryRepository : JpaRepository<Story, Long> {

    @EntityGraph(attributePaths = ["activities", "activities.category"])
    override fun findAll(): MutableList<Story>

    fun findByUserId(userId: Long, pageable: Pageable): List<Story>
}

interface ActivityRepository : JpaRepository<Activity, Long>

interface ActivityCategoryRepository : JpaRepository<ActivityCategory, Long>