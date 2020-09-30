package com.personal.oyl.event.jupiter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author OuYang Liang
 */
public final class JupiterConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JupiterConfiguration.class);
    private static volatile JupiterConfiguration instance;
    private static Properties p;
    private static final String uuid = UUID.randomUUID().toString();
    
    private JupiterConfiguration() {
        
    }
    
    public static JupiterConfiguration instance() {
        if (null == instance) {
            synchronized (JupiterConfiguration.class) {
                if (null == instance) {
                    instance = new JupiterConfiguration();
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
        try (InputStream is = JupiterConfiguration.class.getClassLoader().getResourceAsStream("libevent-jupiter.properties")) {
            p = new Properties();
            p.load(is);
        }
    }
    
    public static final String SEPARATOR = "/";
    public static final String GROUP_SEPARATOR = ":";
    
    public String uuid() {
        return JupiterConfiguration.uuid;
    }
    
    public String getNameSpace() {
        return p.getProperty("event.zookeeper.namespace", "/root");
    }
    
    public String getMasterNode() {
        return this.getNameSpace() + SEPARATOR + p.getProperty("event.zookeeper.master.node", "master");
    }
    
    public String getWorkerNode() {
        return this.getNameSpace() + SEPARATOR + p.getProperty("event.zookeeper.worker.root.node", "workers");
    }
    
    public String getWorkerNode(String clientId) {
        return this.getWorkerNode() + SEPARATOR + clientId;
    }
    
    public String getZkAddrs() {
        return p.getProperty("event.zookeeper.address", "localhost:2181");
    }
    
    public int getSessionTimeout() {
        return Integer.parseInt(p.getProperty("event.zookeeper.session.timeout", "15000"));
    }
    
    public int getNumOfEventTables() {
        return Integer.parseInt(p.getProperty("event.number.event.tables", "8"));
    }
    
    public Set<Integer> getTables() {
        Set<Integer> rlt = new HashSet<>();
        for (int i = 0; i < this.getNumOfEventTables(); i++) {
            rlt.add(i);
        }
        return rlt;
    }
    
}
