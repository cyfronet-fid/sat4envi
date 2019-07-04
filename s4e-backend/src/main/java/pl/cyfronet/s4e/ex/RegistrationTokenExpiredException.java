package pl.cyfronet.s4e.ex;

public class RegistrationTokenExpiredException extends Exception {
    public RegistrationTokenExpiredException() {
    }

    public RegistrationTokenExpiredException(String message) {
        super(message);
    }

    public RegistrationTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistrationTokenExpiredException(Throwable cause) {
        super(cause);
    }

    public RegistrationTokenExpiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
