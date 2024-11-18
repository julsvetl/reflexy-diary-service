package club.kuzyayo.reflexy.service

import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * Helper service to retrieve user id from OAuth2User.
 *
 * @author ysvetlichnaia
 * @since 0.0.1
 */
interface AuthenticationService {

    /**
     * Returns user id retrieved from OAuth2User.
     *
     * @param principal OAuth2User from security context
     * @return user id
     */
    fun getUserId(principal: OAuth2User): Long
}