package com.personal.oyl.event.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);
    private static Configuration instance;
    private static Properties p;
    
    private Configuration () {
        
    }
    
    public static Configuration instance() {
        if (null == instance) {
            synchronized (Configuration.class) {
                if (null == instance) {
                    instance = new Configuration();
                    try {
                        instance.load();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                        System.exit(1);
                    }
                }
            }
        }
        
        return instance;
    }
    
    private void load() throws IOException {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream("event.properties");
            p = new Properties();
            p.load(is);
        } finally {
            if (null != is) {
                is.close();
                is = null;
            }
        }
    }
    
    public static final String SEPARATOR = "/";
    public static final String GROUP_SEPARATOR = ":";
    
    public String getNameSpace() {
        return p.getProperty("event.zookeeper.namespace");
    }
    
    public String getMasterNode() {
        return this.getNameSpace() + p.getProperty("event.zookeeper.master.node");
    }
    
    public String getWorkerNode() {
        return this.getNameSpace() + p.getProperty("event.zookeeper.worker.root.node");
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
    
    public String getKafkaTopic() {
        return p.getProperty("event.kafka.broker.topic");
    }
    
    public int getKafkaPartitions() {
        return Integer.parseInt(p.getProperty("event.kafka.broker.topic.partitions"));
    }
    
    public String getKafkaConsumerGroup() {
        return p.getProperty("event.kafka.consumer.group");
    }
    
    public Set<Integer> getTables() {
        Set<Integer> rlt = new HashSet<>();
        for (int i = 0; i < this.getNumOfEventTables(); i++) {
            rlt.add(Integer.valueOf(i));
        }
        return rlt;
    }
    
}
