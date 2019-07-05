package pl.cyfronet.s4e.ex;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice("pl.cyfronet.s4e")
@RequiredArgsConstructor
public class ErrorHandler extends ResponseEntityExceptionHandler {
    private final ErrorHandlerHelper errorHandlerHelper;

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledException(DisabledException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleCannotAuthenticateException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleCannotAuthenticateException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(AppUserCreationException.class)
    public ResponseEntity<?> handleAppUserCreationException() {
        // don't return an error status in this case, as it would open the system to account enumeration attack
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ExceptionHandler(RecaptchaException.class)
    public ResponseEntity<?> handleRecaptchaException(RecaptchaException e) {
        return ResponseEntity.badRequest().body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(RegistrationTokenExpiredException.class)
    public ResponseEntity<?> handleRegistrationTokenExpiredException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        BindingResult bindingResult = e.getBindingResult();
        if (bindingResult == null) {
            // no BindingResult, just return an error code 400 without further info
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.badRequest().body(errorHandlerHelper.toResponseMap(e));
    }
}
