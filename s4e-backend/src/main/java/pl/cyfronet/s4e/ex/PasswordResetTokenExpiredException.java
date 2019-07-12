package pl.cyfronet.s4e.ex;

public class PasswordResetTokenExpiredException extends Exception {
    public PasswordResetTokenExpiredException() {
    }

    public PasswordResetTokenExpiredException(String message) {
        super(message);
    }

    public PasswordResetTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordResetTokenExpiredException(Throwable cause) {
        super(cause);
    }

    public PasswordResetTokenExpiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
