package com.itblare.architecture.support.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itblare.architecture.support.constant.CommonConstant;
import com.itblare.architecture.support.exceptions.BaseException;
import com.itblare.architecture.support.result.ResponseDataFactory;
import com.itblare.architecture.support.result.ResponseDataWrapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * HTTP响应结果处理器
 * 全局处理增强版Controller，避免Controller里返回数据每次都要用响应体来包装
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/5 18:00
 */
@RestControllerAdvice("com.itblare.archetype.api") // 注意：这里要加上需要扫描的包
public class ResponseDataHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {

        // 如果接口返回的类型本身就是ResultVO那就没有必要进行额外的操作，返回false
        if (methodParameter.getGenericParameterType().equals(ResponseDataWrapper.class)) {
            return false;
        }
        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        final HttpServletRequest request = requestAttributes.getRequest();
        // 判定请求是否包含标记
        final Boolean hasMark = (Boolean) request.getAttribute(CommonConstant.RESPONSE_WRAPPER_ANN);
        return !Objects.isNull(hasMark) && hasMark;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request, ServerHttpResponse response) {

        // 响应的Content-Type为JSON格式
        if (mediaType.includes(MediaType.APPLICATION_JSON) || MediaType.APPLICATION_JSON.equals(mediaType)) {
            // JSON 返回
            // mediaType 根据实际类型获取的
            if (Objects.isNull(body)) {
                return ResponseDataFactory.success();
            }
            // 已经值指定类型或其他无需包装类型，无需重写
            if (body instanceof ResponseDataWrapper) {
                return body;
            }
            return ResponseDataFactory.success(body);
        }

        //处理返回值是String的情况
        if (Objects.nonNull(body) && body instanceof String) {
            try {
                return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(ResponseDataFactory.success(body));
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                throw new BaseException(ex);
            }
        }
        return body;
    }

}