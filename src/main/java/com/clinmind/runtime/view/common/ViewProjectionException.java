package com.clinmind.runtime.view.common;

public class ViewProjectionException extends RuntimeException {

    private final String code;

    public ViewProjectionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
