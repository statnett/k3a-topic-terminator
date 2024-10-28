package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class AndOperation implements ReservedTopic {

    private final ReservedTopic[] parts;

    public AndOperation(ReservedTopic... parts) {
        this.parts = parts;
    }

    @Override
    public Set<String> filter(AdminClient client, Set<String> topicNames) throws ExecutionException, InterruptedException {
        if (parts.length == 0) {
            return topicNames;
        }

        HashSet<String> reserved = new HashSet<>();
        for (ReservedTopic reservedTopic : parts) {
            reserved.addAll(reservedTopic.filter(client, new HashSet<>(topicNames)));
        }
        topicNames.retainAll(reserved);
        return topicNames;
    }
}
