package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class BlessedTopic implements ReservedTopic {
    private final Collection<Pattern> patterns;

    public BlessedTopic(Collection<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public Set<String> filter(AdminClient client, Set<String> topicNames) {
        if (patterns == null || patterns.isEmpty()) {
            return topicNames;
        }
        return topicNames.stream()
            .filter(not(this::isBlessed))
            .collect(Collectors.toSet());
    }

    private boolean isBlessed(String topicName) {
        return patterns.stream()
            .anyMatch(pattern -> pattern.matcher(topicName).matches());
    }
}
