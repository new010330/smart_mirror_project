package com.school_project.smart_mirror.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CustomValidationApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Map<String, String> errorMap;

    public CustomValidationApiException() {
        this("error", new HashMap<String, String>());
    }

    public CustomValidationApiException(String message) {
        this(message, new HashMap<String, String>());
    }

    public CustomValidationApiException(String message, Map<String, String> errorMap) {
        super(message);
        this.errorMap = errorMap;

    }
}