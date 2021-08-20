package com.itblare.architecture.support.result;

import org.springframework.http.HttpStatus;

/**
 * 应用接口响应结果包装
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/5 16:00
 */
public class ResponseDataWrapper<T> extends BaseResponseWrapper<T> {

    private T data;

    protected ResponseDataWrapper() {
        super(HttpStatus.OK);
    }

    protected ResponseDataWrapper(HttpStatus status) {
        super(status);
    }

    protected ResponseDataWrapper(HttpStatus status, T data) {
        super(status);
        this.data = data;
    }

    protected ResponseDataWrapper(T data) {
        super(HttpStatus.OK);
        this.data = data;
    }

    protected ResponseDataWrapper(int status, String message) {
        super(status, message);
    }

    protected ResponseDataWrapper(int status, T data) {
        super(status);
        this.data = data;
    }

    protected ResponseDataWrapper(int status, String message, T data) {
        super(status, message);
        this.data = data;
    }

    public T getData() {
        return data;
    }
}