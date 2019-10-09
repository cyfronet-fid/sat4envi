package pl.cyfronet.s4e.ex;

public class InstitutionCreationException extends Exception {
    public InstitutionCreationException() {
    }

    public InstitutionCreationException(String message) {
        super(message);
    }

    public InstitutionCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstitutionCreationException(Throwable cause) {
        super(cause);
    }

    public InstitutionCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}