package com.personal.oyl.event.jupiter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author OuYang Liang
 */
public class ZkInstance {

    protected static final Logger log = LoggerFactory.getLogger(ZkInstance.class);

    protected ZooKeeper zk;
    protected volatile boolean expired = false;
    protected volatile boolean firstTimeToZk = true;

    protected Semaphore s = null;

    public void initConnection(Instance instance) throws InterruptedException, IOException {
        CountDownLatch latch = new CountDownLatch(1);
        zk = new ZooKeeper(JupiterConfiguration.instance().getZkAddrs(), JupiterConfiguration.instance().getSessionTimeout(),
                (event) -> {
                    if (event.getState().equals(Watcher.Event.KeeperState.Expired)) {
                        log.warn("Zookeeper session expired ...");
                        expired = true;
                        if (null != s) {
                            s.release();
                        }

                        instance.shutdown();

                        if (null != zk) {
                            try {
                                zk.close();
                                zk = null;
                            } catch (InterruptedException e) {
                                log.error(e.getMessage(), e);
                            }
                        }

                        try {
                            Instance newInst = new Instance(instance.getEventTransportMgr(), instance.getEventMapper(), instance.getEventReceiver());
                            newInst.go();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    } else if (event.getState().equals(Watcher.Event.KeeperState.Disconnected)) {
                        log.warn("Disconnected from zookeeper ...");
                        log.warn("Pause instance ...");
                        instance.pause();
                    } else if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                        if (firstTimeToZk) {
                            latch.countDown();
                            firstTimeToZk = false;
                            log.info("Connection to zookeeper created successfully ...");
                        } else {
                            log.warn("Resume instance ...");
                            instance.resume();
                        }
                    }
                });

        latch.await();
    }


    public String getContent(String znode, Watcher watcher) throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        try{
            byte[] source = zk.getData(znode, watcher, stat);
            return null == source ? null : new String(source, StandardCharsets.UTF_8);
        } catch(KeeperException e){
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                return this.getContent(znode, watcher);
            } else {
                throw e;
            }
        }
    }
    
    public List<String> getChildren(String znode, Watcher watcher) throws KeeperException, InterruptedException {
        try{
            if (null == watcher) {
                return zk.getChildren(znode, false);
            } else {
                return zk.getChildren(znode, watcher);
            }
        } catch(KeeperException e){
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                return this.getChildren(znode, watcher);
            } else {
                throw e;
            }
        }
    }
    
    public void setContent(String znode, String content) throws KeeperException, InterruptedException {
        try{
            zk.setData(znode, content.getBytes(StandardCharsets.UTF_8), -1);
        } catch(KeeperException e){
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.setContent(znode, content);
            } else {
                throw e;
            }
        }
    }
    
    public void createWorkNode(String znode) throws InterruptedException, KeeperException {
        try{
            zk.create(znode, "".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch(KeeperException e) {
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.createWorkNode(znode);
            } else {
                throw e;
            }
        }
    }
    
    public void createRoot(String znode) throws InterruptedException, KeeperException {
        try {
            zk.create(znode,  "".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch(KeeperException e) {
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.createRoot(znode);
            } else {
                throw e;
            }
        }
    }

    public boolean lock(String clientId, String resource) throws KeeperException, InterruptedException {
        while (true) {
            if (this.tryLock(clientId, resource)) {
                return true;
            }

            // blocking operation, return false if session expired.
            if (!listenLock(resource)) {
                return false;
            }
        }
    }

    private boolean tryLock(String clientId, String resource)
            throws KeeperException, InterruptedException {
        try{
            zk.create(resource, clientId.getBytes(StandardCharsets.UTF_8),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            return true;
        } catch(KeeperException e) {
            if (e.code().equals(KeeperException.Code.NODEEXISTS)) {
                return false;
            } else if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                return this.tryLockWhenConnectionLoss(clientId, resource);
            } else {
                throw e;
            }
        }
    }

    private boolean tryLockWhenConnectionLoss(String clientId, String resource)
            throws KeeperException, InterruptedException {

        try{
            zk.create(resource, clientId.getBytes(StandardCharsets.UTF_8),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

            return true;
        } catch(KeeperException e) {
            if (e.code().equals(KeeperException.Code.NODEEXISTS)) {
                return this.checkNode(clientId, resource);
            } else if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                return this.tryLockWhenConnectionLoss(clientId, resource);
            } else {
                throw e;
            }
        }
    }

    private boolean checkNode(String clientId, String resource) throws KeeperException, InterruptedException {
        try {
            Stat stat = new Stat();
            byte[] data = zk.getData(resource, false, stat);
            return clientId.equals(new String(data, StandardCharsets.UTF_8));
        } catch(KeeperException e){
            if (e.code().equals(KeeperException.Code.NONODE)) {
                return this.tryLock(clientId, resource);
            } else if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                return this.checkNode(clientId, resource);
            } else {
                throw e;
            }
        }
    }

    private boolean listenLock(String resource) throws InterruptedException, KeeperException {
        s = new Semaphore(0);

        try {
            Stat stat = zk.exists(resource, (event) -> {
                if (event.getType().equals(Watcher.Event.EventType.NodeDeleted)) {
                    s.release();
                }
            });

            if (null != stat) {
                s.acquire();
            }

            return !expired;
        } catch (KeeperException e) {
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                return this.listenLock(resource);
            } else {
                throw e;
            }
        }
    }
}
