package com.civicbuild;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
class CivicBuildApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true, "Spring Boot Application context loads successfully with Zeebe fallback and H2 database");
    }
}
