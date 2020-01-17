package com.personal.oyl.event.jupiter;

import java.io.IOException;
import java.nio.charset.Charset;
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
public final class ZkUtil {

    private static final Logger log = LoggerFactory.getLogger(ZkUtil.class);

    private static volatile ZkUtil instance;

    private ZkUtil () {

    }

    public static ZkUtil getInstance() {
        if (null == instance) {
            synchronized (ZkUtil.class) {
                if (null == instance) {
                    instance = new ZkUtil();
                }
            }
        }

        return instance;
    }

    private ZooKeeper zk;

    public void initConnection(Instance instance) throws InterruptedException, IOException {
        CountDownLatch latch = new CountDownLatch(1);
        zk = new ZooKeeper(JupiterConfiguration.instance().getZkAddrs(), JupiterConfiguration.instance().getSessionTimeout(),
                (event) -> {
                    if (event.getState().equals(Watcher.Event.KeeperState.Expired)) {
                        instance.stopAll();

                        if (null != zk) {
                            try {
                                zk.close();
                                zk = null;
                            } catch (InterruptedException e) {
                                log.error(e.getMessage(), e);
                            }
                        }

                        try {
                            instance.go();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                        latch.countDown();
                    }
                });

        latch.await();
    }


    public String getContent(String znode, Watcher watcher) throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        try{
            byte[] source = zk.getData(znode, watcher, stat);
            return null == source ? null : new String(source, Charset.forName("utf-8"));
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
            zk.setData(znode, content.getBytes(Charset.forName("utf-8")), -1);
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
            zk.create(znode, "".getBytes(Charset.forName("utf-8")), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
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
            zk.create(znode,  "".getBytes(Charset.forName("utf-8")), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch(KeeperException e) {
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.createRoot(znode);
            } else {
                throw e;
            }
        }
    }

    public void lock(String clientId, String resource) throws KeeperException, InterruptedException {
        while (true) {
            if (this.tryLock(clientId, resource)) {
                return;
            }

            this.listenLock(resource);
        }
    }

    private boolean tryLock(String clientId, String resource)
            throws KeeperException, InterruptedException {
        try{
            zk.create(resource, clientId.getBytes(Charset.forName("utf-8")),
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
            zk.create(resource, clientId.getBytes(Charset.forName("utf-8")),
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
            return clientId.equals(new String(data, Charset.forName("utf-8")));
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

    private void listenLock(String resource) throws InterruptedException, KeeperException {
        Semaphore s = new Semaphore(0);

        try {
            Stat stat = zk.exists(resource, (event) -> {
                if (event.getType().equals(Watcher.Event.EventType.NodeDeleted)) {
                    s.release();
                }
            });

            if (null != stat) {
                s.acquire();
            }

        } catch (KeeperException e) {
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.listenLock(resource);
            } else {
                throw e;
            }
        }
    }
}
