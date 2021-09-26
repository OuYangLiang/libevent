package com.personal.oyl.event.rocketmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author OuYang Liang
 */
public class RocketMqConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RocketMqConfiguration.class);
    private static volatile RocketMqConfiguration instance;
    private static Properties p;

    private RocketMqConfiguration() {

    }

    public static RocketMqConfiguration instance() {
        if (null == instance) {
            synchronized (RocketMqConfiguration.class) {
                if (null == instance) {
                    instance = new RocketMqConfiguration();
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
        try (InputStream is = RocketMqConfiguration.class.getClassLoader().getResourceAsStream("libevent-rocketmq.properties")) {
            p = new Properties();
            p.load(is);
        }
    }

    public String getNamesrvAddr() {
        return p.getProperty("event.rocketmq.nameserver.address", "localhost:9876");
    }

    public String getProduceGroup() {
        return p.getProperty("event.rocketmq.broker.produce.group", "EventDrivenProducer");
    }

    public String getProduceTopic() {
        return p.getProperty("event.rocketmq.broker.produce.topic", "event_topic");
    }

    public String getProduceTag() {
        return p.getProperty("event.rocketmq.broker.produce.topic.tag", "tag");
    }

    public int getProduceTopicPartitions() {
        return Integer.parseInt(p.getProperty("event.rocketmq.broker.produce.topic.partitions", "4"));
    }

    public Map<String, String> getConsumeTopic() {
        String str = p.getProperty("event.rocketmq.broker.consume.topic", "event_topic_1:tag,event_topic_2:tag");
        Map<String, String> rlt = new HashMap<>();
        String[] parts = str.trim().split(",");
        for (String part : parts) {
            String[] subPart = part.trim().split(":");
            rlt.put(subPart[0].trim(), subPart[1].trim());
        }
        return rlt;
    }

    public String getConsumerGroup() {
        return p.getProperty("event.rocketmq.consumer.group", "EventDrivenConsumer");
    }

    public int getNumOfThreads() {
        return Integer.parseInt(p.getProperty("event.rocketmq.consumer.parallelism", "4"));
    }

    public String getInstanceName() {
        return p.getProperty("event.rocketmq.instance", "DEFAULT");
    }

}
