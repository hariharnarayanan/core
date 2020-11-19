package net.securustech.embs.receive;

import org.apache.kafka.clients.consumer.internals.PartitionAssignor;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

public class BalancedRangeAssignorTest {

    @Test
    public void testAssign() {
        BalancedRangeAssignor balancedRangeAssignor = new BalancedRangeAssignor();

        Map<String, Integer> partitionsPerTopic = new HashMap<>();
        partitionsPerTopic.put("test_topic", 3);

        Map<String, PartitionAssignor.Subscription> subscriptions = new HashMap<>();

        // Consumers from Host-1
        subscriptions.put("AppName_Host-1_port-0-numbers", new PartitionAssignor.Subscription(Collections.singletonList("test_topic"), null));
        subscriptions.put("AppName_Host-1_port-1-numbers", new PartitionAssignor.Subscription(Collections.singletonList("test_topic"), null));
        subscriptions.put("AppName_Host-1_port-2-numbers", new PartitionAssignor.Subscription(Collections.singletonList("test_topic"), null));

        // Consumers from Host-2
        subscriptions.put("AppName_Host-2_port-0-numbers", new PartitionAssignor.Subscription(Collections.singletonList("test_topic"), null));
        subscriptions.put("AppName_Host-2_port-1-numbers", new PartitionAssignor.Subscription(Collections.singletonList("test_topic"), null));
        subscriptions.put("AppName_Host-2_port-2-numbers", new PartitionAssignor.Subscription(Collections.singletonList("test_topic"), null));

        Map<String, List<TopicPartition>> assignment = balancedRangeAssignor.assign(partitionsPerTopic, subscriptions);

        assertTrue(assignment.size() == 6);

        Supplier<Stream<Map.Entry<String, List<TopicPartition>>>> consumersWithAssignment = () -> assignment.entrySet().stream().filter(entry -> {
            return entry.getValue().size() == 1;
        });

        assertTrue(consumersWithAssignment.get().collect(Collectors.toList()).size() == 3);

        Set<String> hostsWithAssignments = consumersWithAssignment.get().map(entry -> {
            String consumer = entry.getKey();
            int firstIndexOfHost = consumer.indexOf('_') + 1;
            int lastIndexOfHost = consumer.indexOf('_', firstIndexOfHost);
            return consumer.substring(firstIndexOfHost, lastIndexOfHost);
        }).collect(Collectors.toSet());

        assertTrue(hostsWithAssignments.size() == 2);
    }
}
