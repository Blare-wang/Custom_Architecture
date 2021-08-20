package com.itblare.architecture.support.config;

import com.itblare.architecture.support.filter.BasicSecurityDefenseFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/20 14:50
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<BasicSecurityDefenseFilter> registrationBasicSecurityDefenseFilterBean() {
        FilterRegistrationBean<BasicSecurityDefenseFilter> registrationBean = new FilterRegistrationBean<>();
        // 注册自定义过滤器
        registrationBean.setFilter(new BasicSecurityDefenseFilter());
        // 过滤所有路径
        registrationBean.addUrlPatterns("/filter/*");
        // 过滤器名称
        registrationBean.setName("BasicSecurityDefenseFilter");
        // 过滤器的级别，值越小级别越高越先执行
        registrationBean.setOrder(1);
        return registrationBean;
    }
}