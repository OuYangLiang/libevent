package com.personal.oyl.event.kafka;

import java.util.Arrays;
import java.util.Properties;

import com.personal.oyl.event.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author OuYang Liang
 */
public class KafkaEventConsumer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);
    private final EventSerde eventSerde;
    private final EventReceiver eventReceiver;

    public KafkaEventConsumer(EventSerde eventSerde, EventReceiver eventReceiver) {
        this.eventSerde = eventSerde;
        this.eventReceiver = eventReceiver;
    }

    private KafkaConsumer<String, String> consumer;

    @Override
    public void run() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfiguration.instance().getKafkaAddrs());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfiguration.instance().getKafkaConsumerGroup());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumer = new KafkaConsumer<>(props);

        try {
            consumer.subscribe(Arrays.asList(KafkaConfiguration.instance().getConsumeTopics()));

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    Event event = null;
                    try {
                        event = eventSerde.fromJson(record.value());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    if (null != event) {
                        this.eventReceiver.onEvent(event);
                    }
                }

                consumer.commitSync();
            }

        } catch (WakeupException e) {
            log.error(e.getMessage(), e);
        } finally {
            consumer.close();
        }
    }

    public void wake() {
        if (null != consumer) {
            this.consumer.wakeup();
        }
    }
}