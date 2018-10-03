package com.personal.oyl.event.util;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public final class ZkUtil {
    private static ZkUtil instance;
    
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
    
    public String getContent(ZooKeeper zk, String znode, Watcher watcher) throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        try{
            byte[] source = zk.getData(znode, watcher, stat);
            return null == source ? null : new String(source);
        } catch(KeeperException e){
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                return this.getContent(zk, znode, watcher);
            } else {
                throw e;
            }
        }
    }
    
    public List<String> getChildren(ZooKeeper zk, String znode, Watcher watcher) throws KeeperException, InterruptedException {
        try{
            if (null == watcher) {
                return zk.getChildren(znode, false);
            } else {
                return zk.getChildren(znode, watcher);
            }
        } catch(KeeperException e){
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                return this.getChildren(zk, znode, watcher);
            } else {
                throw e;
            }
        }
    }
    
    public void setContent(ZooKeeper zk, String znode, String content) throws KeeperException, InterruptedException {
        try{
            zk.setData(znode, content.getBytes(), -1);
        } catch(KeeperException e){
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.setContent(zk, znode, content);
            } else {
                throw e;
            }
        }
    }
    
    public void createWorkNode(ZooKeeper zk, String znode) throws InterruptedException, KeeperException {
        try{
            zk.create(znode, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch(KeeperException e) {
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.createWorkNode(zk, znode);
            } else {
                throw e;
            }
        }
    }
    
    public void createRoot(ZooKeeper zk, String znode) throws InterruptedException, KeeperException {
        try {
            zk.create(znode,  "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch(KeeperException e) {
            if (e.code().equals(KeeperException.Code.NODEEXISTS)) {
                return;
            } else if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.createRoot(zk, znode);
            } else {
                throw e;
            }
        }
    }
}
