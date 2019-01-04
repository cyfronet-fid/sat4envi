package pl.cyfronet.s4e.ex;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@ControllerAdvice("pl.cyfronet.s4e")
@RequiredArgsConstructor
public class SecurityErrorHandler extends ResponseEntityExceptionHandler {
    private final ErrorHandlerHelper errorHandlerHelper;

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleCannotAuthenticateException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorHandlerHelper.toResponse(e));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleCannotAuthenticateException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorHandlerHelper.toResponse(e));
    }
}
