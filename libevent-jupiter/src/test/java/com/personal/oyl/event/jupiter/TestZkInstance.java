package com.personal.oyl.event.jupiter;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author OuYang Liang
 * @since 2020-01-18
 */
public class TestZkInstance extends ZkInstance {
    @Override
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

                        if (null != zk) {
                            try {
                                zk.close();
                                zk = null;
                            } catch (InterruptedException e) {
                                log.error(e.getMessage(), e);
                            }
                        }

                        TestInstance test = new TestInstance();
                        try {
                            test.go();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    if (event.getState().equals(Watcher.Event.KeeperState.Disconnected)) {
                        log.warn("Disconnected from zookeeper...");
                        log.warn("Pause instance ...");
                    }

                    if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                        if (firstTimeToZk) {
                            latch.countDown();
                            firstTimeToZk = false;
                            log.info("Connection to zookeeper created successfully ...");
                        } else {
                            log.warn("Resume instance ...");
                        }
                    }
                });

        latch.await();
    }
}
