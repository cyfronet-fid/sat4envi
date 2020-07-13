package pl.cyfronet.s4e.ex.product;

public class ProductDeletionException extends ProductException {
    public ProductDeletionException() {
    }

    public ProductDeletionException(String message) {
        super(message);
    }

    public ProductDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductDeletionException(Throwable cause) {
        super(cause);
    }

    public ProductDeletionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
