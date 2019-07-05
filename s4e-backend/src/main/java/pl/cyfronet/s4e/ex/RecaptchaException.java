package pl.cyfronet.s4e.ex;

import com.github.mkopylec.recaptcha.validation.ErrorCode;
import lombok.Getter;

import java.util.List;

public class RecaptchaException extends Exception {
    @Getter
    private final List<ErrorCode> errorCodes;

    public RecaptchaException(String message, List<ErrorCode> errorCodes, Throwable cause) {
        super(message, cause);
        this.errorCodes = errorCodes;
    }

    public RecaptchaException(String message, List<ErrorCode> errorCodes) {
        this(message, errorCodes, null);
    }
}
