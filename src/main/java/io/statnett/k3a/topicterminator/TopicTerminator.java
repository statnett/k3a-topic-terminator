package io.statnett.k3a.topicterminator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.statnett.k3a.topicterminator.strategy.AndOperation;
import io.statnett.k3a.topicterminator.strategy.BlessedTopic;
import io.statnett.k3a.topicterminator.strategy.ConsumedTopic;
import io.statnett.k3a.topicterminator.strategy.InternalTopic;
import io.statnett.k3a.topicterminator.strategy.NonEmptyTopic;
import io.statnett.k3a.topicterminator.strategy.ReservedIfTopicNotMatchingProps;
import io.statnett.k3a.topicterminator.strategy.ReservedTopic;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;


@Component
public class TopicTerminator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ApplicationProperties props;
    private final KafkaAdmin kafkaAdmin;
    private final Counter deletedCounter;

    public TopicTerminator(ApplicationProperties props, KafkaAdmin kafkaAdmin, MeterRegistry meterRegistry) {
        this.props = props;
        this.kafkaAdmin = kafkaAdmin;
        deletedCounter = Counter.builder("topic.deleted.total")
            .description("Number of topics deleted")
            .register(meterRegistry);
    }

    @Scheduled(fixedRateString = "${app.fixed-rate-string}")
    public void terminateUnusedTopics() throws ExecutionException, InterruptedException {
        log.atInfo()
            .setMessage("Terminating unused topics")
            .addKeyValue("dry-run", props.isDryRun())
            .log();

        ReservedTopic nonEmptyTopic = nonEmptyTopics();
        if (props.getNonEmptyTopics().isWithoutTimeRetention()) {
            nonEmptyTopic = and(nonEmptyTopic, reservedTopicsNotMatchingProps(
                Map.of(TopicConfig.RETENTION_MS_CONFIG, "-1")
            ));
        }

        from(allTopics())
            .remove(internalTopics())
            .remove(blessedTopics(props.getBlessedTopics()))
            .remove(consumedTopics())
            .remove(nonEmptyTopic)
            .terminate();
    }

    private TopicTerminatorChain from(TopicProvider topicProvider) {
        return new TopicTerminatorChain(topicProvider);
    }

    private static AndOperation and(ReservedTopic... reservedTopic) {
        return new AndOperation(reservedTopic);
    }

    private class TopicTerminatorChain {
        private final TopicProvider topicProvider;
        private final List<ReservedTopic> reservedTopics;

        public TopicTerminatorChain(TopicProvider topicProvider) {
            this.topicProvider = topicProvider;
            this.reservedTopics = new ArrayList<>();
        }

        public void terminate() throws ExecutionException, InterruptedException {
            try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
                Set<String> unusedTopics = topicProvider.getNames(client);

                for (ReservedTopic reservedTopic : reservedTopics) {
                    unusedTopics = reservedTopic.filter(client, unusedTopics);
                }

                log.atInfo()
                    .setMessage("Start deleting unused topics")
                    .addKeyValue("count", unusedTopics.size())
                    .log();
                if (props.isDryRun()) {
                    unusedTopics.forEach(t -> log.atInfo()
                        .setMessage("NOT deleting unused topic in dry-run mode")
                        .addKeyValue("topic", t)
                        .log());
                } else {
                    unusedTopics.forEach(t -> log.atInfo()
                        .setMessage("Deleting unused topic")
                        .addKeyValue("topic", t)
                        .log());
                    client.deleteTopics(unusedTopics);
                    deletedCounter.increment(unusedTopics.size());
                }
            }
        }

        public TopicTerminatorChain remove(ReservedTopic reservedTopic) {
            reservedTopics.add(reservedTopic);
            return this;
        }
    }

    public interface TopicProvider {
        Set<String> getNames(AdminClient client) throws ExecutionException, InterruptedException;
    }

    public static class AllTopicsProvider implements TopicProvider {
        @Override
        public Set<String> getNames(AdminClient client) throws ExecutionException, InterruptedException {
            return new HashSet<>(client.listTopics().names().get());
        }
    }

    public static TopicProvider allTopics() {
        return new AllTopicsProvider();
    }

    private static InternalTopic internalTopics() {
        return new InternalTopic();
    }

    private static BlessedTopic blessedTopics(Collection<Pattern> blessedTopics) {
        return new BlessedTopic(blessedTopics);
    }

    private static ConsumedTopic consumedTopics() {
        return new ConsumedTopic();
    }

    private static NonEmptyTopic nonEmptyTopics() {
        return new NonEmptyTopic();
    }

    private static ReservedIfTopicNotMatchingProps reservedTopicsNotMatchingProps(Map<String, String> props) {
        return new ReservedIfTopicNotMatchingProps(props);
    }
}
