package io.statnett.k3a.topicterminator;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app")
@Validated
public class ApplicationProperties {
    /**
     * Execute the cleanup job with a fixed period between runs.
     * Use a {@link java.time.Duration#parse java.time.Duration}
     * compliant value, which is based on the ISO-8601 duration format.
     *
     * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Durations">ISO-8601 duration format</a>
     */
    @NotEmpty
    private String fixedRateString;

    /**
     * If set to `true` the application won't delete anything,
     * but just log the topics that would have been deleted if
     * dry-run was disabled.
     */
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
