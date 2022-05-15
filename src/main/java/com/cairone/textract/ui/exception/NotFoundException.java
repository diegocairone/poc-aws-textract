package com.cairone.textract.ui.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cairone.textract.AppException;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NotFoundException extends AppException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String format, Object... args) {
        super(format, args);
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }

    public NotFoundException(Throwable cause, String message) {
        super(cause, message);
    }
}
