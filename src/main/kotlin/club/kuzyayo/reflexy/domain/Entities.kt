@file:Suppress("JpaObjectClassSignatureInspection")

package club.kuzyayo.reflexy.domain

import jakarta.persistence.*

@Entity
@Table(name = "dr_story")
class Story(
    @Column(name = "user_id", nullable = false) var userId: Long,
    @Column(name = "title", nullable = false) var title: String,
    @JoinTable(
        name = "dr_story_activity",
        joinColumns = [JoinColumn(name = "story_id")],
        inverseJoinColumns = [JoinColumn(name = "activity_id")]
    )
    @ManyToMany(fetch = FetchType.EAGER) var activities: List<Activity>,
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dr_story_seq")
    @SequenceGenerator(name = "dr_story_seq", sequenceName = "seq_dr_story", allocationSize = 1)
    @Column(name = "id")
    var id: Long? = null
)

@Entity
@Table(name = "dr_activity")
class Activity(
    @Column(name = "title", nullable = false) var title: String,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id") var category: ActivityCategory,
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dr_activity_seq")
    @SequenceGenerator(name = "dr_activity_seq", sequenceName = "seq_dr_activity", allocationSize = 1)
    @Column(name = "id") var id: Long? = null
)

@Entity
@Table(name = "dr_activity_category")
class ActivityCategory(
    @Column(name = "title", nullable = false) var title: String,
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dr_activity_category_seq")
    @SequenceGenerator(name = "dr_activity_category_seq", sequenceName = "seq_dr_activity_category", allocationSize = 1)
    @Column(name = "id") var id: Long? = null
)