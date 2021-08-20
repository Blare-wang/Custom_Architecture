package com.itblare.architecture.support.interceptor;

import com.itblare.architecture.support.annotation.ResponseWrapper;
import com.itblare.architecture.support.constant.CommonConstant;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 统一响应包装处理拦截器
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/7 13:49
 */
@Component
public class ResponseWrapperInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (handler instanceof final HandlerMethod handlerMethod) {
            final Class<?> classType = handlerMethod.getBeanType();
            final Method method = handlerMethod.getMethod();
            // 确保当前类是RestController，不然不进行封装
            if (classType.isAnnotationPresent(RestController.class)) {

                // 从方法上到类上再到超类上查看是否有指定注解
                boolean hasResponseResultAnnotation = method.isAnnotationPresent(ResponseWrapper.class);

                if (!hasResponseResultAnnotation) {
                    // 此类上是否由注释
                    hasResponseResultAnnotation = hasAnnotationWithParent(classType);
                }

                if (!hasResponseResultAnnotation) {
                    // 方法和当前类上无ResponseResult注解，那么查看其实现接口是否由ResponseResult注解
                    final Class<?>[] interfaces = classType.getInterfaces();
                    if (interfaces.length > 0) {
                        for (Class<?> anInterface : interfaces) {
                            hasResponseResultAnnotation = hasAnnotationWithParent(anInterface);
                            if (hasResponseResultAnnotation) {
                                break;
                            }
                        }
                    }
                }
                request.setAttribute(CommonConstant.RESPONSE_WRAPPER_ANN, hasResponseResultAnnotation);
            }
        }

        return true;
    }

    /**
     * 检测当前类/接口及其父类是否包含该注解
     *
     * @param declaringClass 检测的类
     * @return {@link boolean}
     * @author Blare
     */
    private boolean hasAnnotationWithParent(Class<?> declaringClass) {

        boolean hasAnnotation = declaringClass.isAnnotationPresent(ResponseWrapper.class);
        while (!hasAnnotation) {
            final String className = declaringClass.getName();
            //已经是最顶层父类
            if ("java.lang.Object".equals(className)) {
                break;
            }

            declaringClass = declaringClass.getSuperclass();// 父类
            if (Objects.isNull(declaringClass)) {
                break;
            }
            hasAnnotation = declaringClass.isAnnotationPresent(ResponseWrapper.class);
        }
        return hasAnnotation;
    }

}