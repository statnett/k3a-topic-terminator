package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * Topic that is internal, i.e. used by Kafka components.
 */
public class InternalTopic implements ReservedTopic {
    @Override
    public Set<String> filter(AdminClient client, Set<String> topicNames) {
        return topicNames.stream()
            .filter(not(this::isInternal))
            .collect(Collectors.toSet());
    }

    private boolean isInternal(String name) {
        return name.startsWith("_");
    }
}
