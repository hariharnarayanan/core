package net.securustech.embs.receive;

import org.apache.kafka.clients.consumer.internals.AbstractPartitionAssignor;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

public class BalancedRangeAssignor extends AbstractPartitionAssignor {
    @Override
    public String name() {
        return "range";
    }

    private Map<String, List<String>> consumersPerTopic(Map<String, Subscription> consumerMetadata) {
        Map<String, List<String>> res = new HashMap<>();
        for (Map.Entry<String, Subscription> subscriptionEntry : consumerMetadata.entrySet()) {
            String consumerId = subscriptionEntry.getKey();
            for (String topic : subscriptionEntry.getValue().topics())
                put(res, topic, consumerId);
        }
        return res;
    }

    @Override
    public Map<String, List<TopicPartition>> assign(Map<String, Integer> partitionsPerTopic,
                                                    Map<String, Subscription> subscriptions) {
        Map<String, List<String>> consumersPerTopic = consumersPerTopic(subscriptions);
        Map<String, List<TopicPartition>> assignment = new HashMap<>();
        for (String memberId : subscriptions.keySet())
            assignment.put(memberId, new ArrayList<TopicPartition>());

        for (Map.Entry<String, List<String>> topicEntry : consumersPerTopic.entrySet()) {
            String topic = topicEntry.getKey();
            List<String> consumersForTopic = topicEntry.getValue();

            Integer numPartitionsForTopic = partitionsPerTopic.get(topic);
            if (numPartitionsForTopic == null)
                continue;

            Map<String, List<String>> consumersPerHost = new HashMap<>();
            for (String consumer: consumersForTopic) {
                int firstIndexOfHost = consumer.indexOf('_') + 1;
                int lastIndexOfHost = consumer.indexOf('_', firstIndexOfHost);
                String host = consumer.substring(firstIndexOfHost, lastIndexOfHost);
                List<String> consumersForHost = consumersPerHost.get(host);
                if (consumersForHost == null) {
                    consumersForHost = new ArrayList<String>();
                    consumersForHost.add(consumer);
                    consumersPerHost.put(host, consumersForHost);
                }
                else
                    consumersPerHost.get(host).add(consumer);
            }

            int maxConsumersPerHost = consumersForTopic.size();
            consumersForTopic = new ArrayList<>();

            for(int i = 0; i < maxConsumersPerHost; i++) {
                for (List<String> consumersForHost: consumersPerHost.values()) {
                    if (i < consumersForHost.size())
                        consumersForTopic.add(consumersForHost.get(i));
                }
            }

            int numPartitionsPerConsumer = numPartitionsForTopic / consumersForTopic.size();
            int consumersWithExtraPartition = numPartitionsForTopic % consumersForTopic.size();

            List<TopicPartition> partitions = AbstractPartitionAssignor.partitions(topic, numPartitionsForTopic);
            for (int i = 0, n = consumersForTopic.size(); i < n; i++) {
                int start = numPartitionsPerConsumer * i + Math.min(i, consumersWithExtraPartition);
                int length = numPartitionsPerConsumer + (i + 1 > consumersWithExtraPartition ? 0 : 1);
                assignment.get(consumersForTopic.get(i)).addAll(partitions.subList(start, start + length));
            }
        }
        return assignment;
    }
}

