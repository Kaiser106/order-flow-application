package com.orderflow.common.result;

import lombok.Getter;

import javax.swing.*;

@Getter
public class Result<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private  final String errorCode;

    private Result(boolean success, T data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, null);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, data, message, null);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(false, null, message, null);
    }

    public static <T> Result<T> failure(String message, String errorCode) {
        return new Result<>(false, null, message, errorCode);
    }

}
