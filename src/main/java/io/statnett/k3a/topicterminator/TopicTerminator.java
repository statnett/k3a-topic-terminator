package io.statnett.k3a.topicterminator;

import io.statnett.k3a.topicterminator.strategy.InternalTopic;
import io.statnett.k3a.topicterminator.strategy.NonEmptyTopic;
import io.statnett.k3a.topicterminator.strategy.ReservedTopic;
import org.apache.kafka.clients.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Component
public class TopicTerminator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ApplicationProperties props;
    private final KafkaAdmin admin;

    public TopicTerminator(ApplicationProperties props, KafkaAdmin admin) {
        this.props = props;
        this.admin = admin;
    }

    @Scheduled(fixedRateString = "${app.fixed-rate-string}")
    public void terminateUnusedTopics() throws ExecutionException, InterruptedException {
        log.info("Terminating unused topics{}", props.isDryRun() ? " in dry-run mode" : "");
        try (AdminClient client = AdminClient.create(admin.getConfigurationProperties())) {
            final Set<String> allTopics = client.listTopics().names().get();

            final Set<String> unusedTopics = new HashSet<>(allTopics);

            for (ReservedTopic reservedTopic : reservedTopics(allTopics)) {
                unusedTopics.removeAll(reservedTopic.getNames(client));
            }

            if (props.isDryRun()) {
                unusedTopics.forEach(t -> log.info("Topic {} is considered unused and would be deleted in non dry-run mode", t));
            } else {
                log.info("{} topic(s) to be deleted: ", unusedTopics.size());
                unusedTopics.forEach(t -> log.info("Delete unused topic: {}", t));
                client.deleteTopics(unusedTopics);
            }
        }
    }

    private List<ReservedTopic> reservedTopics(Set<String> allTopics) {
        return List.of(new NonEmptyTopic(), new InternalTopic(allTopics));
    }
}
