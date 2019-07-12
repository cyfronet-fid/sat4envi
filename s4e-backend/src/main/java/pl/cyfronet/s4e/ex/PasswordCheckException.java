package pl.cyfronet.s4e.ex;

public class PasswordCheckException extends Exception {
    public PasswordCheckException() {
    }

    public PasswordCheckException(String message) {
        super(message);
    }

    public PasswordCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordCheckException(Throwable cause) {
        super(cause);
    }

    public PasswordCheckException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
