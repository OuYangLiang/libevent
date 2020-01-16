package com.personal.oyl.event.kafka;

import com.personal.oyl.event.Event;
import com.personal.oyl.event.EventPusher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author OuYang Liang
 */
public class KafkaEventPusher implements EventPusher {

    private KafkaProducer<String, String> producer;
    public KafkaEventPusher() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfiguration.instance().getKafkaAddrs());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);
    }

    @Override
    public List<String> push(int tbNum, List<Event> events) throws ExecutionException, InterruptedException {

        int partition = tbNum % KafkaConfiguration.instance().getProduceTopicPartitions();

        List<String> eventIds = new LinkedList<>();
        List<Future<RecordMetadata>> futures = new LinkedList<>();


        for (Event event : events) {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    KafkaConfiguration.instance().getProduceTopic(), partition, event.getEventTime().getTime(), null, event.json(), null);
            futures.add(producer.send(record));
            eventIds.add(event.getEventId());
        }

        for (Future<RecordMetadata> future : futures) {
            future.get();
        }

        return eventIds;
    }

}
