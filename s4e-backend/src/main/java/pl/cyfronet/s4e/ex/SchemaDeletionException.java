package pl.cyfronet.s4e.ex;

public class SchemaDeletionException extends Exception {
    public SchemaDeletionException() {
    }

    public SchemaDeletionException(String message) {
        super(message);
    }

    public SchemaDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaDeletionException(Throwable cause) {
        super(cause);
    }

    public SchemaDeletionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
