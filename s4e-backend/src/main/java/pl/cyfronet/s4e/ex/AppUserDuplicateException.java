package pl.cyfronet.s4e.ex;

public class AppUserDuplicateException extends Exception {
    public AppUserDuplicateException() {
    }

    public AppUserDuplicateException(String message) {
        super(message);
    }

    public AppUserDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppUserDuplicateException(Throwable cause) {
        super(cause);
    }

    public AppUserDuplicateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
