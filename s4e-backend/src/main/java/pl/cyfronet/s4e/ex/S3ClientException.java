package pl.cyfronet.s4e.ex;

public class S3ClientException extends Exception {
    public S3ClientException() {
    }

    public S3ClientException(String message) {
        super(message);
    }

    public S3ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public S3ClientException(Throwable cause) {
        super(cause);
    }

    public S3ClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
