package io.statnett.k3a.topicterminator;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.regex.Pattern;

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

    /**
     * Used to specify topics that should be retained in the cluster,
     * even if the topic is marked for termination by the other rules.
     * Can be specified as a list or a comma separated value of topic
     * name regular expressions.
     */
    private Collection<Pattern> blessedTopics;

    /**
     * Can be used to terminate topics even if the topic contains data
     * (destructive operation) if the topic is otherwise considered unused.
     * By default, no topics with data will be deleted.
     */
    private NonEmptyTopicCharacteristics nonEmptyTopics = new NonEmptyTopicCharacteristics();

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

    public Collection<Pattern> getBlessedTopics() {
        return blessedTopics;
    }

    public void setBlessedTopics(Collection<Pattern> blessedTopics) {
        this.blessedTopics = blessedTopics;
    }

    public NonEmptyTopicCharacteristics getNonEmptyTopics() {
        return nonEmptyTopics;
    }

    public void setNonEmptyTopics(NonEmptyTopicCharacteristics nonEmptyTopics) {
        this.nonEmptyTopics = nonEmptyTopics;
    }

    public static class NonEmptyTopicCharacteristics {
        /**
         * If set to ´true´, topics without time retention can be terminated.
         */
        private boolean withoutTimeRetention;

        public boolean isWithoutTimeRetention() {
            return withoutTimeRetention;
        }

        public void setWithoutTimeRetention(boolean withoutTimeRetention) {
            this.withoutTimeRetention = withoutTimeRetention;
        }
    }
}
