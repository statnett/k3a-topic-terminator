package io.statnett.k3a.topicterminator;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
// Using profile to disable scheduling for tests
@Profile({"!spring-boot-test"})
@EnableScheduling
public class SchedulingConfiguration {
}
