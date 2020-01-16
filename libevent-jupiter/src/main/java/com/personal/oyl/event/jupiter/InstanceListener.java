package com.personal.oyl.event.jupiter;

import org.apache.zookeeper.KeeperException;

/**
 * @author OuYang Liang
 */
public interface InstanceListener {
    void onChange() throws InterruptedException, KeeperException;
}
