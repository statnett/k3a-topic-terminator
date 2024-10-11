package io.statnett.k3a.topicterminator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TopicTerminator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Scheduled(fixedRateString = "${app.fixed-rate-string}")
    public void terminateUnusedTopics() {
        log.info("Terminating unused topics");
    }
}
