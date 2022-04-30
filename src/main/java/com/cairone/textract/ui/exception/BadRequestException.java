package com.cairone.textract.ui.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cairone.textract.AppException;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestException extends AppException {

    private static final long serialVersionUID = 1L;

    public BadRequestException(String format, Object... args) {
        super(format, args);
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }

    public BadRequestException(Throwable cause, String message) {
        super(cause, message);
    }
}
