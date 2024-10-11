package io.statnett.k3a.topicterminator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app")
@Validated
public class ApplicationProperties {
}
