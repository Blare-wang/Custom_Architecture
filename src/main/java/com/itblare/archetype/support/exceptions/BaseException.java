package com.itblare.archetype.support.exceptions;

/**
 * 自定义基础运行时异常类
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/6 9:51
 */
public class BaseException extends RuntimeException {

    private int status;
    private String message;

    public BaseException(int code, String message) {
        super(message);
        this.status = code;
    }

    public BaseException(int code, String message, Throwable ex) {
        super(message, ex);
        this.status = code;
        this.message = message;
    }

    public BaseException(Throwable exception) {
        super(exception);
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}