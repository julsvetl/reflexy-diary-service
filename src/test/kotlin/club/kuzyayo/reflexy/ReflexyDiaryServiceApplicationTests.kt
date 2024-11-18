package club.kuzyayo.reflexy

import club.kuzyayo.reflexy.config.H2JpaConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(classes = [ReflexyDiaryServiceApplication::class, H2JpaConfig::class])
class ReflexyDiaryServiceApplicationTests {

    @Test
    fun contextLoads() {
    }
}
