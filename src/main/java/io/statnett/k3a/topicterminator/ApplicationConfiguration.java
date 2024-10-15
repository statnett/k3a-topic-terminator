package io.statnett.k3a.topicterminator;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@EnableKafka
public class ApplicationConfiguration {

    @Bean
    public TopicTerminator topicTerminator(ApplicationProperties props, KafkaAdmin kafkaAdmin, MeterRegistry meterRegistry) {
        return new TopicTerminator(props, kafkaAdmin, meterRegistry);
    }
}
