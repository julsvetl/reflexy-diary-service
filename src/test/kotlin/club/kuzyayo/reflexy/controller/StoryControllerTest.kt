package club.kuzyayo.reflexy.controller

import club.kuzyayo.reflexy.ReflexyDiaryServiceApplication
import club.kuzyayo.reflexy.config.H2JpaConfig
import club.kuzyayo.reflexy.dto.SaveStoryDto
import club.kuzyayo.reflexy.dto.StoryDto
import club.kuzyayo.reflexy.service.AuthenticationService
import club.kuzyayo.reflexy.service.StoryService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [ReflexyDiaryServiceApplication::class, H2JpaConfig::class]
)
class StoryControllerTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockBean
    private lateinit var storyService: StoryService

    @MockBean
    private lateinit var authenticationService: AuthenticationService

    private lateinit var webClient: MockMvc

    @BeforeEach
    fun setUp() {
        webClient = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `Should return stories for user`() {
        //given
        val userId: Long = 6
        val story1 = story(55, userId)
        val story2 = story(66, userId)
        given(authenticationService.getUserId(anyOrNull())).willReturn(userId)
        given(storyService.findByUserId(userId, 0, 10)).willReturn(listOf(story1, story2))

        //when
        val actualResponse = webClient.get("/stories?page=0&pageSize=10") {
            with(oauth2Login())
        }

        //then
        actualResponse.andExpect { status { isOk() } }
        actualResponse.andExpect {
            content {
                string(
                    "[{\"id\":${story1.id}," +
                            "\"userId\":$userId," +
                            "\"title\":\"${story1.title}\"," +
                            "\"activities\":[]}," +
                            "{\"id\":${story2.id}," +
                            "\"userId\":$userId," +
                            "\"title\":\"${story2.title}\"," +
                            "\"activities\":[]}]"
                )
            }
        }
    }

    @Test
    fun `Should create story for user`() {
        // given
        val userId: Long = 6
        val story = SaveStoryDto(
            title = "Story Title",
            activities = listOf(),
        )
        val createdStory = StoryDto(555, userId, story)
        given(authenticationService.getUserId(anyOrNull())).willReturn(userId)
        given(storyService.create(userId, story)).willReturn(createdStory)

        //when
        val actualResponse = webClient.post("/stories") {
            with(csrf().asHeader())
            with(oauth2Login())
            contentType = MediaType.APPLICATION_JSON
            content =
                "{\"title\":\"${story.title}\"," +
                        "\"activities\":[]}"
        }

        //then
        actualResponse.andExpect { status { isOk() } }
        responseBodyContainsSingleStoryObject(actualResponse, createdStory)
    }

    @Test
    fun `Should return story by id when user is owner`() {
        // given
        val storyId: Long = 5
        val userId: Long = 6
        val story = story(storyId, userId)
        given(authenticationService.getUserId(anyOrNull())).willReturn(userId)
        given(storyService.findById(storyId)).willReturn(story)


        //when
        val actualResponse = webClient.get("/stories/$storyId") {
            with(oauth2Login())
        }

        //then
        actualResponse.andExpect { status { isOk() } }
        responseBodyContainsSingleStoryObject(actualResponse, story)
    }

    @Test
    fun `Should return 403 when user is not owner`() {
        // given
        val storyId: Long = 5
        val userId: Long = 6
        val story = story(storyId, 789)
        given(authenticationService.getUserId(anyOrNull())).willReturn(userId)
        given(storyService.findById(storyId)).willReturn(story)


        //when
        val actualResponse = webClient.get("/stories/$storyId") {
            with(oauth2Login())
        }

        //then
        actualResponse.andExpect { status { isForbidden() } }
    }

    @Test
    fun `Should update story when user is owner`() {
        // given
        val userId: Long = 6
        val storyId: Long = 555
        val story = SaveStoryDto(
            title = "New Story Title",
            activities = listOf(),
        )
        val updatedStory = StoryDto(storyId, userId, story)
        given(storyService.findById(storyId)).willReturn(story(storyId, userId))
        given(authenticationService.getUserId(anyOrNull())).willReturn(userId)
        given(storyService.update(storyId, story)).willReturn(updatedStory)

        //when
        val actualResponse = webClient.put("/stories/${storyId}") {
            with(csrf().asHeader())
            with(oauth2Login())
            contentType = MediaType.APPLICATION_JSON
            content =
                "{\"title\":\"${story.title}\"," +
                        "\"activities\":[]}"
        }

        //then
        actualResponse.andExpect { status { isOk() } }
        responseBodyContainsSingleStoryObject(actualResponse, updatedStory)
    }

    @Test
    fun `Should not update story when it belongs to another user`() {
        // given
        val requestUserId: Long = 6
        val foundStoryUserId: Long = 78
        val storyId: Long = 555
        val story = SaveStoryDto(
            title = "New Story Title",
            activities = listOf(),
        )
        given(storyService.findById(storyId)).willReturn(story(storyId, foundStoryUserId))
        given(authenticationService.getUserId(anyOrNull())).willReturn(requestUserId)

        //when
        val actualResponse = webClient.put("/stories/${storyId}") {
            with(csrf().asHeader())
            with(oauth2Login())
            contentType = MediaType.APPLICATION_JSON
            content =
                "{\"title\":\"${story.title}\"," +
                        "\"activities\":[]}"
        }

        //then
        actualResponse.andExpect { status { isForbidden() } }
    }

    @Test
    fun `Should remove story when user is owner`() {
        // given
        val userId: Long = 6
        val storyId: Long = 555
        given(storyService.findById(storyId)).willReturn(story(storyId, userId))
        given(authenticationService.getUserId(anyOrNull())).willReturn(userId)
        doNothing().`when`(storyService).remove(storyId)

        //when
        val actualResponse = webClient.delete("/stories/${storyId}") {
            with(csrf().asHeader())
            with(oauth2Login())
        }

        //then
        actualResponse.andExpect { status { isOk() } }
        verify(storyService).remove(storyId)
    }

    @Test
    fun `Should not remove story when user is not owner`() {
        // given
        val requestUserId: Long = 6
        val foundStoryUserId: Long = 78
        val storyId: Long = 555
        given(storyService.findById(storyId)).willReturn(story(storyId, foundStoryUserId))
        given(authenticationService.getUserId(anyOrNull())).willReturn(requestUserId)

        //when
        val actualResponse = webClient.delete("/stories/${storyId}") {
            with(csrf().asHeader())
            with(oauth2Login())
        }

        //then
        actualResponse.andExpect { status { isForbidden() } }
        verify(storyService, never()).remove(storyId)
    }

    private fun story(storyId: Long, userId: Long) = StoryDto(
        id = storyId,
        userId = userId,
        title = "Story $storyId Title",
        activities = emptyList(),
    )

    private fun responseBodyContainsSingleStoryObject(
        actualResponse: ResultActionsDsl, story: StoryDto
    ) {
        actualResponse.andExpect {
            content {
                string(
                    "{\"id\":${story.id}," +
                            "\"userId\":${story.userId}," +
                            "\"title\":\"${story.title}\"," +
                            "\"activities\":[]}"
                )
            }
        }
    }
}