package com.personal.oyl.event;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.personal.oyl.event.util.Configuration;
import com.personal.oyl.event.util.ZkUtil;


/**
 * @author:ouyangliang2
 */
public class Worker {
    
    private static final Logger log = LoggerFactory.getLogger(Worker.class);
    
    private ZooKeeper zk;
    
    private String clientId;
    
    private EventSubmitThreadUtil threadUtil;
    
    public Worker(EventMapper mapper) {
        threadUtil = new EventSubmitThreadUtil(mapper);
    }
    
    public void start() throws IOException, InterruptedException, KeeperException {
        clientId = UUID.randomUUID().toString();
        
        CountDownLatch latch = new CountDownLatch(1);
        
        zk = new ZooKeeper(Configuration.instance().getZkAddrs(), Configuration.instance().getSessionTimeout(),
                new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState().equals(KeeperState.Expired)) {
                    try {
                        Worker.this.close();
                        Worker.this.start();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    
                }
                
                if (event.getState().equals(KeeperState.SyncConnected)) {
                    latch.countDown();
                }
            }
            
        });
        
        latch.await();
        // do what it should do as a worker...
        
        this.createWorkNode(Configuration.instance().getWorkerNode() + Configuration.SEPARATOR + clientId);
        log.info("Id: " + clientId + " work node created...");
        
        String source = ZkUtil.getInstance().getContent(zk, Configuration.instance().getWorkerNode() + Configuration.SEPARATOR + clientId, workWatcher);
        this.handleSource(source);
    }
    
    private Watcher workWatcher = (event) -> {
        try {
            if (event.getType().equals(EventType.NodeDataChanged)) {
                String source = ZkUtil.getInstance().getContent(zk, event.getPath(), Worker.this.workWatcher);
                this.handleSource(source);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    };
    
    private void handleSource(String source) {
        if (null == source || source.trim().isEmpty()) {
            return;
        }
        
        synchronized (Worker.class) {
            threadUtil.stopAll();
            String[] parts = source.split(Configuration.GROUP_SEPARATOR);
            
            for (String part : parts) {
                threadUtil.startForN(Integer.valueOf(part.trim()));
            }
        }
    }
    
    private void createWorkNode(String znode) throws InterruptedException, KeeperException {
        try{
            zk.create(znode, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch(KeeperException e) {
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.createWorkNode(znode);
            } else {
                throw e;
            }
        }
    }
    
    public void close() {
        threadUtil.stopAll();
        if (null != zk) {
            try {
                zk.close();
                zk = null;
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}