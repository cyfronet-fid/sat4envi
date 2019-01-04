package pl.cyfronet.s4e.security;

public class CannotAuthenticateException extends Exception {
    public CannotAuthenticateException() {
    }

    public CannotAuthenticateException(String message) {
        super(message);
    }

    public CannotAuthenticateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotAuthenticateException(Throwable cause) {
        super(cause);
    }

    public CannotAuthenticateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
