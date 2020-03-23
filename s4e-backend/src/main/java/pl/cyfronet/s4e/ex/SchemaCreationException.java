package pl.cyfronet.s4e.ex;

public class SchemaCreationException extends Exception {
    public SchemaCreationException() {
    }

    public SchemaCreationException(String message) {
        super(message);
    }

    public SchemaCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaCreationException(Throwable cause) {
        super(cause);
    }

    public SchemaCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
