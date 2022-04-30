package com.cairone.textract;

public class AppException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AppException(Throwable cause, String message) {
        super(message, cause);
    }
    
    public AppException(Throwable cause, String format, Object... args) {
        super(String.format(format, args), cause);
    }

    public AppException(String message) {
        super(message);
    }
    
    public AppException(String format, Object... args) {
        super(String.format(format, args));
    }
}
