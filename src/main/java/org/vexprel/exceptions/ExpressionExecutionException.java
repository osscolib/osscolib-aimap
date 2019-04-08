package org.vexprel.exceptions;

public class ExpressionExecutionException extends RuntimeException {


    public ExpressionExecutionException() {
        super();
    }

    public ExpressionExecutionException(final String message) {
        super(message);
    }

    public ExpressionExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ExpressionExecutionException(final Throwable cause) {
        super(cause);
    }

    public ExpressionExecutionException(
            final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
