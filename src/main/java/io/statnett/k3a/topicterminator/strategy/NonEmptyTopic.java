package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.LogDirDescription;
import org.apache.kafka.common.Node;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Topic that contains data
 */
public class NonEmptyTopic implements ReservedTopic {
    @Override
    public Set<String> filter(AdminClient client, Set<String> topicNames) throws ExecutionException, InterruptedException {
        final List<Integer> brokers = client.describeCluster()
            .nodes().get().stream()
            .map(Node::id)
            .collect(Collectors.toList());

        client.describeLogDirs(brokers)
            .allDescriptions().get().values().stream()
            .flatMap(m -> m.values().stream())
            .map(LogDirDescription::replicaInfos)
            .forEach(log -> log.forEach((topicPartition, replicaInfo) -> {
                if (replicaInfo.size() > 0) {
                    topicNames.remove(topicPartition.topic());
                }
            }));

        return topicNames;
    }
}
