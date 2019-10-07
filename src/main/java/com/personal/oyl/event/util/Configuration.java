package com.personal.oyl.event.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);
    private static Configuration instance;
    private static Properties p;
    private static final String uuid = UUID.randomUUID().toString();
    
    private Configuration () {
        
    }
    
    public static Configuration instance() {
        if (null == instance) {
            synchronized (Configuration.class) {
                if (null == instance) {
                    instance = new Configuration();
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
        try (InputStream is = Configuration.class.getClassLoader().getResourceAsStream("event.properties")) {
            p = new Properties();
            p.load(is);
        }
    }
    
    public static final String SEPARATOR = "/";
    public static final String GROUP_SEPARATOR = ":";
    
    public String uuid() {
        return Configuration.uuid;
    }
    
    public String getNameSpace() {
        return p.getProperty("event.zookeeper.namespace");
    }
    
    public String getMasterNode() {
        return this.getNameSpace() + SEPARATOR + p.getProperty("event.zookeeper.master.node");
    }
    
    public String getWorkerNode() {
        return this.getNameSpace() + SEPARATOR + p.getProperty("event.zookeeper.worker.root.node");
    }
    
    public String getWorkerNode(String clientId) {
        return this.getWorkerNode() + Configuration.SEPARATOR + clientId;
    }
    
    public String getZkAddrs() {
        return p.getProperty("event.zookeeper.address");
    }
    
    public int getSessionTimeout() {
        return Integer.parseInt(p.getProperty("event.zookeeper.session.timeout"));
    }
    
    public int getNumOfEventTables() {
        return Integer.parseInt(p.getProperty("event.number.event.tables"));
    }
    
    public String getKafkaAddrs() {
        return p.getProperty("event.kafka.broker.address");
    }
    
    public String getProduceTopic() {
        return p.getProperty("event.kafka.broker.produce.topic");
    }
    
    public int getProduceTopicPartitions() {
        return Integer.parseInt(p.getProperty("event.kafka.broker.produce.topic.partitions"));
    }
    
    public String[] getConsumeTopics() {
        String val = p.getProperty("event.kafka.broker.consume.topics");
        return val.split(",");
    }
    
    public String getKafkaConsumerGroup() {
        return p.getProperty("event.kafka.consumer.group");
    }
    
    public Set<Integer> getTables() {
        Set<Integer> rlt = new HashSet<>();
        for (int i = 0; i < this.getNumOfEventTables(); i++) {
            rlt.add(i);
        }
        return rlt;
    }
    
}
