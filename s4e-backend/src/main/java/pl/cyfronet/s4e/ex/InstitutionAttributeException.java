package pl.cyfronet.s4e.ex;

public class InstitutionAttributeException extends Exception {
    public InstitutionAttributeException() {
    }

    public InstitutionAttributeException(String message) {
        super(message);
    }

    public InstitutionAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstitutionAttributeException(Throwable cause) {
        super(cause);
    }

    public InstitutionAttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
