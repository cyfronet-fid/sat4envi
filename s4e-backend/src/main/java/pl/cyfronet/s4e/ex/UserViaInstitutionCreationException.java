package pl.cyfronet.s4e.ex;

public class UserViaInstitutionCreationException extends Exception {
    public UserViaInstitutionCreationException() {
    }

    public UserViaInstitutionCreationException(String message) {
        super(message);
    }

    public UserViaInstitutionCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserViaInstitutionCreationException(Throwable cause) {
        super(cause);
    }

    public UserViaInstitutionCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
