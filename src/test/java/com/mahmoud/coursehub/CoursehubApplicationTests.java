package com.mahmoud.coursehub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CoursehubApplicationTests {

    // Fails if the Spring context cannot start (bad mapping, bad query, missing bean...)
    @Test
    void contextLoads() {
    }
}
