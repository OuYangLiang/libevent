package com.personal.oyl.event;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer implements Runnable{
    
    @Autowired
    private SubscriberConfig config;
    
    @Autowired
    private Configuration cfg;
    
    private KafkaConsumer<String, String> consumer;

    @Override
    public void run() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.getKafkaAddrs());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, cfg.getKafkaConsumerGroup());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = new KafkaConsumer<>(props);
        
        try {
            consumer.subscribe(Arrays.asList(cfg.getKafkaTopic()));
            
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    Event event = Event.fromJson(record.value());
                    List<BaseSubscriber> subs = config.getSubscribers(event.getEventType());
                    for (BaseSubscriber sub : subs) {
                        sub.onEvent(event);
                    }
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown
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
