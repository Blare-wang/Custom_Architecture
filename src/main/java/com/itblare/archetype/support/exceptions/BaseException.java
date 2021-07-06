package com.itblare.archetype.support.exceptions;

/**
 * 自定义基础运行时异常类
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/6 9:51
 */
public class BaseException extends RuntimeException {

    private String code;
    private String message;

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String code, String message, Throwable ex) {
        super(message, ex);
        this.code = code;
        this.message = message;
    }

    public BaseException(Throwable exception) {
        super(exception);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}