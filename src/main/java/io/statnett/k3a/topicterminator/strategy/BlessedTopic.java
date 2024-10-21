package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlessedTopic implements ReservedTopic {
    private final Set<String> allNames;
    private final Collection<Pattern> patterns;

    public BlessedTopic(Set<String> allNames, Collection<Pattern> patterns) {
        this.allNames = allNames;
        this.patterns = patterns;
    }

    @Override
    public Set<String> getNames(AdminClient client) {
        if (patterns == null || patterns.isEmpty()) {
            return Collections.emptySet();
        }
        return allNames.stream()
            .filter(this::isBlessed)
            .collect(Collectors.toSet());
    }

    private boolean isBlessed(String topicName) {
        return patterns.stream()
            .anyMatch(pattern -> pattern.matcher(topicName).matches());
    }
}
