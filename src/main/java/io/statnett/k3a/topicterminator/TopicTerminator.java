package io.statnett.k3a.topicterminator;

import io.statnett.k3a.topicterminator.strategy.ConsumedTopic;
import io.statnett.k3a.topicterminator.strategy.InternalTopic;
import io.statnett.k3a.topicterminator.strategy.NonEmptyTopic;
import io.statnett.k3a.topicterminator.strategy.ReservedTopic;
import org.apache.kafka.clients.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Component
public class TopicTerminator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ApplicationProperties props;
    private final KafkaAdmin kafkaAdmin;

    public TopicTerminator(ApplicationProperties props, KafkaAdmin kafkaAdmin) {
        this.props = props;
        this.kafkaAdmin = kafkaAdmin;
    }

    @Scheduled(fixedRateString = "${app.fixed-rate-string}")
    public void terminateUnusedTopics() throws ExecutionException, InterruptedException {
        log.atInfo()
            .setMessage("Terminating unused topics")
            .addKeyValue("dry-run", props.isDryRun())
            .log();
        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            final Set<String> allTopics = client.listTopics().names().get();

            final Set<String> unusedTopics = new HashSet<>(allTopics);

            Collection<ReservedTopic> reservedTopics = List.of(
                new ConsumedTopic(),
                new InternalTopic(allTopics),
                new NonEmptyTopic()
            );

            for (ReservedTopic reservedTopic : reservedTopics) {
                unusedTopics.removeAll(reservedTopic.getNames(client));
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
            }
        }
    }

}
