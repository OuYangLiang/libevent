package com.personal.oyl.event;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class Configuration {
    public static final String SEPARATOR = "/";
    public static final String GROUP_SEPARATOR = ":";
    
    private String namespace  = "/event-driven/";
    private String masterNode = "master";
    private String workerNode = "workers";
    
    private String zkAddrs = "localhost:2181";
    private int sessionTimeout = 30000;
    
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
        for (int i = 0; i <= 7; i++) {
            rlt.add(Integer.valueOf(i));
        }
        return rlt;
    }
}
