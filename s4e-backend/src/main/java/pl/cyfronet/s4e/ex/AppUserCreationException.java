package pl.cyfronet.s4e.ex;

public class AppUserCreationException extends Exception {
    public AppUserCreationException() {
    }

    public AppUserCreationException(String message) {
        super(message);
    }

    public AppUserCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppUserCreationException(Throwable cause) {
        super(cause);
    }

    public AppUserCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
