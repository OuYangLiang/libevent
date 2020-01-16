package com.personal.oyl.event.jupiter;

import org.apache.zookeeper.KeeperException;

public interface InstanceListener {
    void onChange() throws InterruptedException, KeeperException;
}
