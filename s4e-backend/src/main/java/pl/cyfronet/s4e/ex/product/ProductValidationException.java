package pl.cyfronet.s4e.ex.product;

import lombok.Getter;
import org.springframework.validation.BindingResult;

public class ProductValidationException extends ProductException {
    @Getter
    private final BindingResult bindingResult;

    public ProductValidationException(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
    }

    public ProductValidationException(String message, BindingResult bindingResult) {
        super(message);
        this.bindingResult = bindingResult;
    }

    public ProductValidationException(String message, Throwable cause, BindingResult bindingResult) {
        super(message, cause);
        this.bindingResult = bindingResult;
    }

    public ProductValidationException(Throwable cause, BindingResult bindingResult) {
        super(cause);
        this.bindingResult = bindingResult;
    }

    public ProductValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, BindingResult bindingResult) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.bindingResult = bindingResult;
    }
}
