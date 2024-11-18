package club.kuzyayo.reflexy.service.impl

import club.kuzyayo.reflexy.service.AuthenticationService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class AuthenticationServiceImpl : AuthenticationService {

    override fun getUserId(principal: OAuth2User): Long {
        TODO("Not yet implemented")
    }
}