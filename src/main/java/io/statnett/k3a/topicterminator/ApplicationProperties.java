package io.statnett.k3a.topicterminator;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app")
@Validated
public class ApplicationProperties {
    @NotEmpty
    private String fixedRateString;

    private boolean dryRun;

    public String getFixedRateString() {
        return fixedRateString;
    }

    public void setFixedRateString(String fixedRateString) {
        this.fixedRateString = fixedRateString;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
}
