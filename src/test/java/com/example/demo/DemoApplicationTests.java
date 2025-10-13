package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.demo.infrastructure.BoxClient;

@SpringBootTest
class DemoApplicationTests {

    // BoxClientをMock化
    BoxClient boxClient;

    @Test
    void contextLoads() {
        // ここでは何もしなくてOK
    }
}
