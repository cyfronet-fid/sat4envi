package pl.cyfronet.s4e.ex.schema;

public class SchemaTypeException extends SchemaException {
    public SchemaTypeException() {
    }

    public SchemaTypeException(String message) {
        super(message);
    }

    public SchemaTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaTypeException(Throwable cause) {
        super(cause);
    }

    public SchemaTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
