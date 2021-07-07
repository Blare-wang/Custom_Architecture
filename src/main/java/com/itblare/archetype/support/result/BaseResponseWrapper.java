package com.itblare.archetype.support.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 基础响应封装
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/7 11:13
 */
public class BaseResponseWrapper<T> implements Serializable {

    private int status;
    private String message;


    protected BaseResponseWrapper(HttpStatus status) {
        this.status = status.value();
        this.message = status.name();
    }

    protected BaseResponseWrapper(int status) {
        this.status = status;
    }

    protected BaseResponseWrapper(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * 使之不在json序列化结果当中
     */
    @JsonIgnore
    public boolean isSuccess() {
        return this.status == HttpStatus.OK.value();
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BaseResponseWrapper{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}