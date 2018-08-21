package com.personal.oyl.event;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.personal.oyl.event.util.ZkUtil;

@Component
public class Master {
    
    private static final Logger log = LoggerFactory.getLogger(Master.class);
    
    private ZooKeeper zk;
    @Autowired
    private Configuration cfg;
    private SimpleLock lock;
    
    private Watcher masterWatcher = (event) -> {
        if (event.getType().equals(EventType.NodeChildrenChanged)) {
            try {
                Master.this.onWorkerChange();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    };
    
    public void start() throws IOException, InterruptedException, KeeperException {
        String uuid = UUID.randomUUID().toString();
        
        CountDownLatch latch = new CountDownLatch(1);
        
        zk = new ZooKeeper(cfg.getZkAddrs(), cfg.getSessionTimeout(), new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                if (event.getState().equals(KeeperState.Expired)) {
                    try {
                        Master.this.close();
                        Master.this.start();
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
        
        lock = new SimpleLock(zk);
        lock.lock(uuid, cfg.getMasterNode());
        log.error("Now it is the master server...");
        // do what it should do as a master...
        
        ZkUtil.getInstance().getChildren(zk, cfg.getWorkerNode(), masterWatcher);
        log.error("ready for listening to workers...");
        
        log.error("perform the first check of the assignment, invoke method onWorkerChange()...");
        this.onWorkerChange();
    }
    
    private void close() {
        if (null != zk) {
            try {
                zk.close();
                zk = null;
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    private synchronized void onWorkerChange() throws KeeperException, InterruptedException {
        List<String> workerList = ZkUtil.getInstance().getChildren(zk, cfg.getWorkerNode(), masterWatcher);
        List<Holder> holders = new LinkedList<>();
        Set<Integer> assigned = new HashSet<>();
        for (String worker : workerList) {
            Holder holder = new Holder();
            String content = ZkUtil.getInstance().getContent(zk, cfg.getWorkerNode() + Configuration.SEPARATOR + worker, null);
            holder.setNode(worker);
            holder.setAssigned(content);
            holders.add(holder);
            assigned.addAll(holder.getAssigned());
        }
        
        cfg.getTables().forEach((t) -> {
            if (!assigned.contains(t)) {
                Collections.sort(holders, (o1, o2) -> o1.assigned.size() - o2.assigned.size());
                holders.get(0).addAssigned(t);
            }
        });
        
        for (Holder holder : holders) {
            if (holder.affected) {
                try{
                    ZkUtil.getInstance().setContent(zk, cfg.getWorkerNode() + Configuration.SEPARATOR + holder.getNode(), holder.assignedString());
                } catch(KeeperException e){
                    if (e instanceof KeeperException.NoNodeException) {
                        // 可能发生NONODE异常，这不是问题。
                        // NONODE意味着某个Worker下线了，Master会收到通知，并重新进行分配。
                        return;
                    }
                    throw e;
                }
            }
        }
    }
    
    private static class Holder {
        private String node;
        private List<Integer> assigned = new LinkedList<>();
        private boolean affected = false;

        public String getNode() {
            return node;
        }

        public void setNode(String node) {
            this.node = node;
        }

        public List<Integer> getAssigned() {
            return assigned;
        }

        public void addAssigned(Integer i) {
            this.getAssigned().add(i);
            affected = true;
        }

        public void setAssigned(String nodeContent) {
            if (null == nodeContent || nodeContent.trim().isEmpty()) {
                return;
            }
            
            String[] parts = nodeContent.trim().split(Configuration.GROUP_SEPARATOR);
            for (String part : parts) {
                assigned.add(Integer.valueOf(part.trim()));
            }
        }
        
        public String assignedString() {
            StringBuilder sb = new StringBuilder();
            int size = assigned.size();
            
            for (int i = 0; i < size; i++) {
                sb.append(this.getAssigned().get(i));
                if (i < (size - 1)) {
                    sb.append(Configuration.GROUP_SEPARATOR);
                }
            }
            return sb.toString();
        }
    }
}
