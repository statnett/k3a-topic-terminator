package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Topic that is internal, i.e. used by Kafka components.
 */
public class InternalTopic implements ReservedTopic {
    private final Set<String> allNames;

    public InternalTopic(Set<String> allNames) {
        this.allNames = allNames;
    }

    @Override
    public Set<String> getNames(AdminClient client) throws ExecutionException, InterruptedException {
        return allNames.stream()
            .filter(this::isInternal)
            .collect(Collectors.toSet());
    }

    private boolean isInternal(String name) {
        return name.startsWith("_");
    }
}
