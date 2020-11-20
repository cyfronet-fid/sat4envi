package pl.cyfronet.s4e.ex;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.cyfronet.s4e.ex.product.ProductException;
import pl.cyfronet.s4e.ex.product.ProductValidationException;
import pl.cyfronet.s4e.ex.schema.SchemaException;

@ControllerAdvice("pl.cyfronet.s4e")
@RequiredArgsConstructor
public class ErrorHandler extends ResponseEntityExceptionHandler {
    private final ErrorHandlerHelper errorHandlerHelper;

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handle(DisabledException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handle(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handle(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(RecaptchaException.class)
    public ResponseEntity<?> handle(RecaptchaException e) {
        return ResponseEntity.badRequest().body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(RegistrationTokenExpiredException.class)
    public ResponseEntity<?> handle(RegistrationTokenExpiredException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handle(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(PasswordResetTokenExpiredException.class)
    public ResponseEntity<?> handle(PasswordResetTokenExpiredException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handle(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(InstitutionCreationException.class)
    public ResponseEntity<?> handle(InstitutionCreationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(InstitutionUpdateException.class)
    public ResponseEntity<?> handle(InstitutionUpdateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(InstitutionZkException.class)
    public ResponseEntity<?> handle(InstitutionZkException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(GroupCreationException.class)
    public ResponseEntity<?> handle(GroupCreationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(GroupUpdateException.class)
    public ResponseEntity<?> handle(GroupUpdateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(SchemaException.class)
    public ResponseEntity<?> handle(SchemaException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(QueryException.class)
    public ResponseEntity<?> handle(QueryException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(ProductValidationException.class)
    public ResponseEntity<?> handle(ProductValidationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<?> handle(ProductException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(LicenseGrantException.class)
    public ResponseEntity<?> handle(LicenseGrantException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(InvitationCreationException.class)
    public ResponseEntity<?> handle(InvitationCreationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @ExceptionHandler(InvitationInvalidInstitutionException.class)
    public ResponseEntity<?> handle(InvitationInvalidInstitutionException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorHandlerHelper.toResponseMap(e));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.badRequest().body(errorHandlerHelper.toResponseMap(e));
    }
}
