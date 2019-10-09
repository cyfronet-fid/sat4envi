package pl.cyfronet.s4e.ex;

public class InstitutionUpdateException extends Exception {
    public InstitutionUpdateException() {
    }

    public InstitutionUpdateException(String message) {
        super(message);
    }

    public InstitutionUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstitutionUpdateException(Throwable cause) {
        super(cause);
    }

    public InstitutionUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
