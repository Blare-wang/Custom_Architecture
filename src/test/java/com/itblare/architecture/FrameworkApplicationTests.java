package com.itblare.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
class FrameworkApplicationTests {

    @Test
    void contextLoads() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("1","1");
        System.out.println(map);
    }

}
