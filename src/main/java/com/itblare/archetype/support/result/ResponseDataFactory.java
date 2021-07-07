package com.itblare.archetype.support.result;

import org.springframework.http.HttpStatus;

/**
 * 响应数据处理工厂
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/5 16:03
 */
public class ResponseDataFactory {

    /**
     * Create by success server response.
     *
     * @param <T> the type parameter
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> success() {
        return new ResponseDataWrapper<T>();
    }

    /**
     * Create by success message server response.
     *
     * @param <T>     the type parameter
     * @param message the message
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> successMessage(String message) {
        return new ResponseDataWrapper<T>(HttpStatus.OK.value(), message);
    }

    /**
     * Create by success server response.
     *
     * @param <T>  the type parameter
     * @param data the data
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> success(T data) {
        return new ResponseDataWrapper<T>(data);
    }

    /**
     * Create by success server response.
     *
     * @param <T>     the type parameter
     * @param message the message
     * @param data    the data
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> success(String message, T data) {
        return new ResponseDataWrapper<T>(HttpStatus.OK.value(), message, data);
    }

    /**
     * Create by error server response.
     *
     * @param <T> the type parameter
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> error() {
        return new ResponseDataWrapper<T>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Create by error message server response.
     *
     * @param <T>          the type parameter
     * @param errorMessage the error message
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> errorMessage(String errorMessage) {
        return new ResponseDataWrapper<T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage);
    }

    /**
     * Create by error code message server response.
     *
     * @param <T>          the type parameter
     * @param errorCode    the error code
     * @param errorMessage the error message
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> errorCodeMessage(int errorCode, String errorMessage) {
        return new ResponseDataWrapper<T>(errorCode, errorMessage);
    }

    /**
     * Create by error server response.
     *
     * @param <T>          the type parameter
     * @param errorMessage the error message
     * @param data         the data
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> errorMessageData(String errorMessage, T data) {
        return new ResponseDataWrapper<T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage, data);
    }

    /**
     * Create by error server response.
     *
     * @param <T>        the type parameter
     * @param httpStatus the error status
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> errorHttpStatus(HttpStatus httpStatus) {
        return new ResponseDataWrapper<T>(httpStatus);
    }

    /**
     * 数据+响应码+自定义msg
     *
     * @param data    数据
     * @param code    响应码
     * @param message 自定义msg
     * @return {@link ResponseDataWrapper <T>}
     * @author Blare
     */
    public static <T> ResponseDataWrapper<T> wrapper(int code, String message, T data) {
        return new ResponseDataWrapper<>(code, message, data);
    }

    /**
     * 响应状态+数据
     *
     * @param httpStatus 状态
     * @param data       数据
     * @return {@link ResponseDataWrapper<T>}
     * @author Blare
     */
    public static <T> ResponseDataWrapper<T> wrapper(HttpStatus httpStatus, T data) {
        return new ResponseDataWrapper<>(httpStatus, data);
    }

}