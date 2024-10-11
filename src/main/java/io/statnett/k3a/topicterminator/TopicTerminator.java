package io.statnett.k3a.topicterminator;

import org.apache.kafka.clients.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ExecutionException;

@Component
public class TopicTerminator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final KafkaAdmin admin;

    public TopicTerminator(KafkaAdmin admin) {
        this.admin = admin;
    }

    @Scheduled(fixedRateString = "${app.fixed-rate-string}")
    public void terminateUnusedTopics() throws ExecutionException, InterruptedException {
        log.info("Terminating unused topics");
        try (AdminClient client = AdminClient.create(admin.getConfigurationProperties())) {
            final Set<String> allTopicNames = client.listTopics().names().get();
            allTopicNames.forEach(t -> log.info("Terminating unused topic: {}", t));
        }
    }
}
