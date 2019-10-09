package pl.cyfronet.s4e.ex;

public class GroupUpdateException extends Exception {
    public GroupUpdateException() {
    }

    public GroupUpdateException(String message) {
        super(message);
    }

    public GroupUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroupUpdateException(Throwable cause) {
        super(cause);
    }

    public GroupUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
