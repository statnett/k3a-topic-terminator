package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.common.TopicPartition;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Topic that is consumed.
 * <p>
 * If a consumer group have not consumed anything for a while,
 * the consumer group is automatically cleaned up by Kafka
 * based on `offset.retention` setting , which is 7 days by default
 */
public class ConsumedTopic implements ReservedTopic {
    @Override
    public Set<String> getNames(AdminClient client) throws ExecutionException, InterruptedException {
        final Set<String> topics = new HashSet<>();

        for (ConsumerGroupListing group : client.listConsumerGroups().all().get()) {
            topics.addAll(getTopics(client.listConsumerGroupOffsets(group.groupId())));
        }

        return topics;
    }

    private Set<String> getTopics(ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult) throws ExecutionException, InterruptedException {
        return listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get().keySet().stream()
            .map(TopicPartition::topic)
            .collect(Collectors.toSet());
    }
}
