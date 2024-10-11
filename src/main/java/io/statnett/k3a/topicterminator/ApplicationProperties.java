package io.statnett.k3a.topicterminator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app")
@Validated
@EnableScheduling
public class ApplicationProperties {
}
