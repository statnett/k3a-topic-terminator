package io.statnett.k3a.topicterminator;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureObservability
// Using profile to disable scheduling for tests
@ActiveProfiles("spring-boot-test")
public class ApplicationTest {
    @Autowired
    private TopicTerminator topicTerminator;

    @Test
    void testTerminate() throws Exception {
        topicTerminator.terminateUnusedTopics();
    }

    @TestConfiguration
    static class TestTopicConfiguration {
        @Bean
        public NewTopic topic1() {
            return TopicBuilder.name("topic1")
                .build();
        }
    }
}
