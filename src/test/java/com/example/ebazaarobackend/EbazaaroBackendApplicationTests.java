package com.example.ebazaarobackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfig.class) // <- I tutaj to samo
class EbazaaroBackendApplicationTests {

    @Test
    void contextLoads() {}
}
