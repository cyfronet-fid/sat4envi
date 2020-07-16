package pl.cyfronet.s4e.ex;

import lombok.Getter;
import org.springframework.validation.BindingResult;

public class QueryException extends Exception {
    @Getter
    private final BindingResult bindingResult;

    public QueryException(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
    }

    public QueryException(String message, BindingResult bindingResult) {
        super(message);
        this.bindingResult = bindingResult;
    }

    public QueryException(String message, Throwable cause, BindingResult bindingResult) {
        super(message, cause);
        this.bindingResult = bindingResult;
    }
}
