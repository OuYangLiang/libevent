package com.personal.oyl.event.rocketmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

/**
 * @author OuYang Liang
 */
public class RocketMqConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RocketMqConfiguration.class);
    private static volatile RocketMqConfiguration instance;
    private static Properties p;
    private static final String uuid = UUID.randomUUID().toString();

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
        return p.getProperty("event.rocketmq.nameserver.address");
    }

    public String getProduceGroup() {
        return p.getProperty("event.rocketmq.broker.produce.group");
    }

    public String getProduceTopic() {
        return p.getProperty("event.rocketmq.broker.produce.topic");
    }

    public String getProduceTag() {
        return p.getProperty("event.rocketmq.broker.produce.topic.tag");
    }

    public int getProduceTopicPartitions() {
        return Integer.parseInt(p.getProperty("event.rocketmq.broker.produce.topic.partitions"));
    }

    public String getConsumeTopic() {
        return p.getProperty("event.rocketmq.broker.consume.topic");
    }

    public String getConsumeTag() {
        return p.getProperty("event.rocketmq.broker.consume.topic.tag");
    }

    public String getConsumerGroup() {
        return p.getProperty("event.rocketmq.consumer.group");
    }

    public int getNumOfThreads() {
        return Integer.parseInt(p.getProperty("event.rocketmq.consumer.parallelism"));
    }

    public String getInstanceName() {
        return p.getProperty("event.rocketmq.instance");
    }

}
