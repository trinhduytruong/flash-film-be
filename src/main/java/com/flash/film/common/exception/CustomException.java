package com.flash.film.common.exception;

import com.flash.film.common.enums.AppCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final AppCode appCode;
    private final HttpStatus httpStatus;

    public CustomException(AppCode appCode, HttpStatus httpStatus, String message) {
        super(message);
        this.appCode = appCode;
        this.httpStatus = httpStatus;
    }

    public CustomException(AppCode appCode, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.appCode = appCode;
        this.httpStatus = httpStatus;
    }
}
