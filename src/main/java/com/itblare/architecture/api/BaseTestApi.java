package com.itblare.architecture.api;

import com.itblare.architecture.support.annotation.ResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口
 *
 * @author Blare
 * @version 1.0.0
 * @since 2021/7/7 15:24
 */
@RestController
public class BaseTestApi implements BaseApi {

    @GetMapping("test")
    public String testApi(int a, int b) {
        return String.valueOf(a + b);
    }

    @GetMapping("test2")
    @ResponseWrapper
    public String testApi2(int a, int b) {
        return String.valueOf(a + b);
    }
}