/*
 * File Name:Locker.java
 * Author:ouyangliang2
 * Date:2017年7月24日
 * Copyright (C) 2006-2017
 */
 
package com.personal.oyl.event;

import java.util.concurrent.Semaphore;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

/**
 * @author:ouyangliang2
 */
public class SimpleLock {
    private ZooKeeper zk;
    
    public SimpleLock(ZooKeeper zk) {
        this.zk = zk;
    }
    
    public boolean tryLock(String clientId, String resource) 
            throws KeeperException, InterruptedException {
        try{
            zk.create(resource, clientId.getBytes(), 
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
            zk.create(resource, clientId.getBytes(), 
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
            if (clientId.equals(new String(data))) {
                return true;
            }
            
            return false;
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
    
    public void release(String clientId, String resource) throws KeeperException, InterruptedException {
        try{
            zk.delete(resource, -1);
        } catch(KeeperException e){
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.checkRelease(clientId, resource);
            } else {
                throw e;
            }
        }
    }
    
    private void checkRelease(String clientId, String resource) throws KeeperException, InterruptedException {
        try {
            Stat stat = new Stat();
            byte[] data = zk.getData(resource, false, stat);
            if (clientId.equals(new String(data))) {
                this.release(clientId, resource);
            }
        } catch(KeeperException e){
            if (e.code().equals(KeeperException.Code.NONODE)) {
                return;
            } else if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.checkRelease(clientId, resource);
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
    
    private void listenLock(String resource) throws InterruptedException, KeeperException {
        Semaphore s = new Semaphore(0);
        
        try {
            Stat stat = zk.exists(resource, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType().equals(EventType.NodeDeleted)) {
                        s.release();
                    }
                }
            });
            
            if (null != stat) {
                s.acquire();
            }
            
        } catch (KeeperException e) {
            if (e.code().equals(KeeperException.Code.CONNECTIONLOSS)) {
                this.listenLock(resource);
                return;
            } else {
                throw e;
            }
        }
    }
    
}
