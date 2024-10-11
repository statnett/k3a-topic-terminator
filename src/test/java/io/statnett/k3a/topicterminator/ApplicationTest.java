package io.statnett.k3a.topicterminator;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureObservability
// Using profile to disable scheduling for tests
@ActiveProfiles("spring-boot-test")
public class ApplicationTest {
    public static final String TOPIC_INTERNAL = "_schemas";
    public static final String TOPIC_UNUSED = "topic-unused";
    public static final String TOPIC_WITH_DATA = "topic-with-data";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Autowired
    private TopicTerminator topicTerminator;

    @Test
    void testTerminateUnusedTopics() throws Exception {
        // Put some data on topic-with-data topic
        kafkaTemplate.send(TOPIC_WITH_DATA, "foo").get();

        topicTerminator.terminateUnusedTopics();

        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> allTopics = client.listTopics().names().get();

            assertThat(allTopics)
                .contains(TOPIC_INTERNAL)
                .contains(TOPIC_WITH_DATA)
                .doesNotContain(TOPIC_UNUSED);
        }
    }

    @TestConfiguration
    static class TestTopicConfiguration {
        @Bean
        public NewTopic topicInternal() {
            return TopicBuilder.name(TOPIC_INTERNAL)
                .build();
        }

        @Bean
        public NewTopic topicUnused() {
            return TopicBuilder.name(TOPIC_UNUSED)
                .build();
        }

        @Bean
        public NewTopic topicWithData() {
            return TopicBuilder.name(TOPIC_WITH_DATA)
                .build();
        }
    }
}
