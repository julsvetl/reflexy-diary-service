package club.kuzyayo.reflexy.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception is thrown when a resource is not found.
 *
 * @author ysvetlichnaia
 * @since 0.0.1
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class EntityNotFoundException(message: String) : RuntimeException(message)