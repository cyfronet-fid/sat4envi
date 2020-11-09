package pl.cyfronet.s4e.ex;

public class InstitutionZkException extends Exception {
    public InstitutionZkException() {
    }

    public InstitutionZkException(String message) {
        super(message);
    }

    public InstitutionZkException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstitutionZkException(Throwable cause) {
        super(cause);
    }

    public InstitutionZkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
