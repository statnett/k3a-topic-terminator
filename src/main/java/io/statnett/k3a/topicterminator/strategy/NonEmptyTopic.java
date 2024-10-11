package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.LogDirDescription;
import org.apache.kafka.common.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Topic that contains data
 */
public class NonEmptyTopic implements ReservedTopic {

    @Override
    public Set<String> getNames(AdminClient client) throws ExecutionException, InterruptedException {
        Set<String> topics = new HashSet<>();

        List<Integer> brokers = client.describeCluster()
            .nodes().get().stream()
            .map(Node::id)
            .collect(Collectors.toList());

        client.describeLogDirs(brokers)
            .allDescriptions().get().values().stream()
            .flatMap(m -> m.values().stream())
            .map(LogDirDescription::replicaInfos)
            .forEach(log -> log.forEach((topicPartition, replicaInfo) -> {
                if (replicaInfo.size() > 0) {
                    topics.add(topicPartition.topic());
                }
            }));

        return topics;
    }
}