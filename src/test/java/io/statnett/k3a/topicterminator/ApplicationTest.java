package io.statnett.k3a.topicterminator;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@AutoConfigureObservability
// Using profile to disable scheduling for tests
@ActiveProfiles("spring-boot-test")
public class ApplicationTest {
    public static final String GROUP_CONSUMER = "test-consumer";
    public static final String TOPIC_CONSUMED = "topic-consumed";
    public static final String TOPIC_INTERNAL = "_schemas";
    public static final String TOPIC_UNUSED = "topic-unused";
    public static final String TOPIC_WITH_DATA = "topic-with-data";
    public static final String TOPIC_WITH_DATA_WITHOUT_TIME_RETENTION = "topic-with-data-without-time-retention";
    public static final String TOPIC_BLESSED_BY_REGEX = "blessed-topic";
    public static final String TOPIC_BLESSED_BY_NAME = "topic-foo";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Autowired
    SimpleMeterRegistry meterRegistry;

    @Autowired
    private TopicTerminator topicTerminator;

    @Test
    void testTerminateUnusedTopics() throws Exception {
        // Put some data on topic-with-data topic
        kafkaTemplate.send(TOPIC_WITH_DATA, "foo").get();
        kafkaTemplate.send(TOPIC_WITH_DATA_WITHOUT_TIME_RETENTION, "key", "value").get();

        // Wait until consumer is started and registered in cluster
        try (AdminClient client1 = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            await().until(() -> client1.listConsumerGroupOffsets(GROUP_CONSUMER)
                .partitionsToOffsetAndMetadata().get().keySet().stream()
                .anyMatch(topicPartition -> topicPartition.topic().equals(TOPIC_CONSUMED)));
        }

        topicTerminator.terminateUnusedTopics();

        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Set<String> allTopics = client.listTopics(new ListTopicsOptions().listInternal(true)).names().get();

            assertThat(allTopics)
                .contains(TOPIC_CONSUMED, TOPIC_INTERNAL, TOPIC_WITH_DATA, TOPIC_BLESSED_BY_REGEX, TOPIC_BLESSED_BY_NAME)
                .doesNotContain(TOPIC_UNUSED, TOPIC_WITH_DATA_WITHOUT_TIME_RETENTION);
        }

        // Assert delete of topic increases metrics counter
        assertThat(meterRegistry.find("topic.deleted.total").counter())
            .isNotNull()
            .matches(counter -> counter.count() == 2);
    }

    @TestConfiguration
    static class TestTopicConfiguration {
        @KafkaListener(topics = TOPIC_CONSUMED, groupId = GROUP_CONSUMER)
        public void consumeTopicConsumed(String message) {
            System.out.println("Received Message in group foo: " + message);
        }

        @Bean
        public NewTopic topicConsumed() {
            return TopicBuilder.name(TOPIC_CONSUMED)
                .build();
        }

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

        @Bean
        public NewTopic topicWithDataWithoutTimeRetention() {
            return TopicBuilder.name(TOPIC_WITH_DATA_WITHOUT_TIME_RETENTION)
                .config(TopicConfig.RETENTION_MS_CONFIG, "-1")
                .build();
        }

        @Bean
        public NewTopic topicBlessedByRegex() {
            return TopicBuilder.name(TOPIC_BLESSED_BY_REGEX)
                .build();
        }

        @Bean
        public NewTopic topicBlessedByName() {
            return TopicBuilder.name(TOPIC_BLESSED_BY_NAME)
                .build();
        }
    }


    @TestConfiguration
    static class ObservationTestConfiguration {

        @Primary
        @Bean
        MeterRegistry registry() {
            return new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
        }
    }
}
