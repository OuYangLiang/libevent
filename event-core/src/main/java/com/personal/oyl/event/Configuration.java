package com.personal.oyl.event;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Configuration {
    public static final String SEPARATOR = "/";
    public static final String GROUP_SEPARATOR = ":";
    
    @Value("${event.zookeeper.namespace}")
    private String namespace;
    
    @Value("${event.zookeeper.master.node}")
    private String masterNode;
    
    @Value("${event.zookeeper.worker.root.node}")
    private String workerNode;
    
    @Value("${event.zookeeper.address}")
    private String zkAddrs;
    
    @Value("${event.zookeeper.session.timeout}")
    private int sessionTimeout;
    
    @Value("${event.number.event.tables}")
    private int numOfEventTables;
    
    @Value("${event.kafka.broker.address}")
    private String kafkaAddrs;
    
    @Value("${event.kafka.broker.topic}")
    private String kafkaTopic;
    
    @Value("${event.kafka.broker.topic.partitions}")
    private int kafkaPartitions;
    
    @Value("${event.kafka.consumer.group}")
    private String kafkaConsumerGroup;
    
    public String getKafkaConsumerGroup() {
        return kafkaConsumerGroup;
    }

    public void setKafkaConsumerGroup(String kafkaConsumerGroup) {
        this.kafkaConsumerGroup = kafkaConsumerGroup;
    }

    public int getKafkaPartitions() {
        return kafkaPartitions;
    }

    public void setKafkaPartitions(int kafkaPartitions) {
        this.kafkaPartitions = kafkaPartitions;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public String getKafkaAddrs() {
        return kafkaAddrs;
    }

    public void setKafkaAddrs(String kafkaAddrs) {
        this.kafkaAddrs = kafkaAddrs;
    }

    public String getZkAddrs() {
        return zkAddrs;
    }

    public void setZkAddrs(String zkAddrs) {
        this.zkAddrs = zkAddrs;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace.endsWith(SEPARATOR) ? namespace : namespace + SEPARATOR;
    }

    public String getMasterNode() {
        return this.getNamespace() + masterNode;
    }

    public void setMasterNode(String masterNode) {
        this.masterNode = masterNode;
    }

    public String getWorkerNode() {
        return this.getNamespace() + workerNode;
    }

    public void setWorkerNode(String workerNode) {
        this.workerNode = workerNode;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
    
    public Set<Integer> getTables() {
        Set<Integer> rlt = new HashSet<>();
        for (int i = 0; i < numOfEventTables; i++) {
            rlt.add(Integer.valueOf(i));
        }
        return rlt;
    }
}
