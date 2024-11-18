package club.kuzyayo.reflexy.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception is thrown when access to a resource is denied for the current user.
 *
 * @author ysvetlichnaia
 * @since 0.0.1
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class AccessDeniedException(message: String) : RuntimeException(message)