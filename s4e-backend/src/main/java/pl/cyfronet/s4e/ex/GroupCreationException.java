package pl.cyfronet.s4e.ex;

public class GroupCreationException extends Exception {
    public GroupCreationException() {
    }

    public GroupCreationException(String message) {
        super(message);
    }

    public GroupCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroupCreationException(Throwable cause) {
        super(cause);
    }

    public GroupCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
