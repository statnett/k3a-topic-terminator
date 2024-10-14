package io.statnett.k3a.topicterminator.strategy;

import org.apache.kafka.clients.admin.AdminClient;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface ReservedTopic {
    Set<String> getNames(AdminClient client) throws ExecutionException, InterruptedException;
}
