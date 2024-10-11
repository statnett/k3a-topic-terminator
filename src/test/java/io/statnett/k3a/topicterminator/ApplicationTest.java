package io.statnett.k3a.topicterminator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureObservability
// Using profile to disable scheduling for tests
@ActiveProfiles("spring-boot-test")
public class ApplicationTest {
    @Test
    void testTerminate() {
    }
}
