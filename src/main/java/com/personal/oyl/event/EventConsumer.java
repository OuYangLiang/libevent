package com.personal.oyl.event;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import com.personal.oyl.event.util.Configuration;

public class EventConsumer implements Runnable{
    
    private KafkaConsumer<String, String> consumer;

    @Override
    public void run() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.instance().getKafkaAddrs());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Configuration.instance().getKafkaConsumerGroup());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumer = new KafkaConsumer<>(props);
        
        try {
            consumer.subscribe(Arrays.asList(Configuration.instance().getConsumeTopics()));
            
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    Event event = Event.fromJson(record.value());
                    List<BaseSubscriber> subs = SubscriberConfig.instance().getSubscribers(event.getEventType());
                    for (BaseSubscriber sub : subs) {
                        sub.onEvent(event);
                    }
                }
                
                consumer.commitSync();
            }
            
        } catch (WakeupException e) {
            // ignore for shutdown
        } catch (Exception e) {
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
