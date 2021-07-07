package com.itblare.archetype.support.config;

import com.itblare.archetype.support.interceptor.ResponseWrapperInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;

/**
 * WebMvc 信息配置
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/7 15:40
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Resource
    private ResponseWrapperInterceptor responseWrapperInterceptor;

    /**
     * 注册拦截器
     *
     * @param registry 注册机
     * @author Blare
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 设置拦截器
        registry.addInterceptor(responseWrapperInterceptor);
    }
}