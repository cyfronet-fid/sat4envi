package pl.cyfronet.s4e.ex;

import lombok.Getter;
import org.springframework.validation.BindingResult;

public class LicenseGrantException extends Exception implements BindingResultException {
    @Getter
    private final BindingResult bindingResult;

    public LicenseGrantException(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
    }

    public LicenseGrantException(String message, BindingResult bindingResult) {
        super(message);
        this.bindingResult = bindingResult;
    }

    public LicenseGrantException(String message, Throwable cause, BindingResult bindingResult) {
        super(message, cause);
        this.bindingResult = bindingResult;
    }

    public LicenseGrantException(Throwable cause, BindingResult bindingResult) {
        super(cause);
        this.bindingResult = bindingResult;
    }

    public LicenseGrantException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, BindingResult bindingResult) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.bindingResult = bindingResult;
    }
}
