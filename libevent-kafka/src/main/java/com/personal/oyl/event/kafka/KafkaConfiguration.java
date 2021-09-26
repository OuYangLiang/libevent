package com.personal.oyl.event.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author OuYang Liang
 */
public final class KafkaConfiguration {
    private static final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);
    private static volatile KafkaConfiguration instance;
    private static Properties p;

    private KafkaConfiguration () {

    }

    public static KafkaConfiguration instance() {
        if (null == instance) {
            synchronized (KafkaConfiguration.class) {
                if (null == instance) {
                    instance = new KafkaConfiguration();
                    try {
                        load();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                        System.exit(1);
                    }
                }
            }
        }

        return instance;
    }

    private static void load() throws IOException {
        try (InputStream is = KafkaConfiguration.class.getClassLoader().getResourceAsStream("libevent-kafka.properties")) {
            p = new Properties();
            p.load(is);
        }
    }

    public String getKafkaAddrs() {
        return p.getProperty("event.kafka.broker.address", "localhost:9092");
    }

    public String getProduceTopic() {
        return p.getProperty("event.kafka.broker.produce.topic", "event_topic");
    }

    public int getProduceTopicPartitions() {
        return Integer.parseInt(p.getProperty("event.kafka.broker.produce.topic.partitions", "4"));
    }

    public String[] getConsumeTopics() {
        String val = p.getProperty("event.kafka.broker.consume.topics", "event_topic");
        return val.split(",");
    }

    public String getKafkaConsumerGroup() {
        return p.getProperty("event.kafka.consumer.group", "EventDrivenConsumer");
    }

}
