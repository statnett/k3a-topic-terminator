package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Topic that is internal, i.e., used by Kafka components, e.g. stream, connect, schema-registry
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
        return isUsedByConnect(name) || isUsedInternal(name) || isUsedByStream(name);
    }

    private boolean isUsedInternal(String name) {
        return name.startsWith("_");
    }

    private boolean isUsedByConnect(String name) {
        return name.startsWith("stream.connect");
    }

    private boolean isUsedByStream(String name) {
        return name.endsWith("-changelog") || name.endsWith("-repartition") || name.endsWith("-rekey");
    }
}
