package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigResource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

public class ReservedIfTopicNotMatchingProps implements ReservedTopic {
    private final Set<Map.Entry<String, String>> matchingProps;

    public ReservedIfTopicNotMatchingProps(Map<String, String> matchingProps) {
        if (matchingProps == null) {
            this.matchingProps = emptySet();
        } else {
            this.matchingProps = unmodifiableSet(matchingProps.entrySet());
        }
    }

    @Override
    public Set<String> filter(AdminClient client, Set<String> topicNames) throws ExecutionException, InterruptedException {
        if (matchingProps.isEmpty()) {
            return emptySet();
        }

        List<ConfigResource> topicResources = topicNames.stream()
            .map(t -> new ConfigResource(ConfigResource.Type.TOPIC, t))
            .toList();

        Map<String, Map<String, String>> topicProps = client.describeConfigs(topicResources).all().get().entrySet().stream()
            .collect(Collectors.toMap(
                    entry -> entry.getKey().name(),
                    entry -> entry.getValue().entries().stream().collect(
                        Collectors.toMap(ConfigEntry::name, ConfigEntry::value)
                    )
                )
            );

        return topicNames.stream()
            .filter(t -> topicProps.get(t).entrySet().containsAll(matchingProps))
            .collect(Collectors.toSet());
    }
}
