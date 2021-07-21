package com.itblare.archetype.support.filter;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * 基础安全防御配置
 * OncePerRequestFilter：过滤器抽象类通常被用于继承实现并在每次请求时只执行一次过滤
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/20 14:36
 */
public class BasicSecurityDefenseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        /*XSS预防之Cookie的安全设置*/
        //设置http的cookie
        httpServletResponse.addHeader("Set-Cookie", "uid=112; Path=/; HttpOnly");
        httpServletResponse.addHeader("Set-Cookie", "timeout=30; Path=/test; HttpOnly");
        //设置https的cookie
        httpServletResponse.addHeader("Set-Cookie", "uid=112; Path=/; Secure; HttpOnly");
        /*XSS预防之Cookie的安全设置*/

        /*XSS预防之内容安全策略*/
        httpServletResponse.setHeader("Content-Security-Policy", "script-src https://felord.cn");
        /*XSS预防之内容安全策略*/

        /*XSS预防之阻止加载策略*/
        httpServletResponse.addHeader("X-XSS-Protection", "1; mode=block");
        /*XSS预防之阻止加载策略*/

        /*点击劫持响应配置*/
        httpServletResponse.setHeader("x-frame-options", "SAMEORIGIN");
        httpServletResponse.addHeader("X-Frame-Options", "SAMEORIGIN");
        httpServletResponse.addHeader("Referer-Policy", "origin");
        httpServletResponse.addHeader("Content-Security-Policy", "object-src 'self'");
        httpServletResponse.addHeader("X-Permitted-Cross-Domain-Policies", "master-only");
        httpServletResponse.addHeader("X-Content-Type-Options", "nosniff");
        httpServletResponse.addHeader("X-Download-Options", "noopen");
        /*点击劫持响应配置*/

        /*XSS预防之编码过滤转义策略*/
        filterChain.doFilter(new XssHttpServletRequestWrapper(httpServletRequest), httpServletResponse);
        /*XSS预防之编码过滤转义策略*/
    }

    static class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

        public XssHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return HtmlUtils.htmlEscape(value);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return HtmlUtils.htmlEscape(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            return values != null ? (String[]) Stream.of(values).map(HtmlUtils::htmlEscape).toArray() : super.getParameterValues(name);
        }

    }
}