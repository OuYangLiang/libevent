package com.personal.oyl.event.jupiter;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author OuYang Liang
 */
public class Instance {
    private static final Logger log = LoggerFactory.getLogger(Instance.class);

    private EventTransportMgr eventTransportMgr;

    private ZkInstance zkInstance;

    public Instance(EventTransportMgr eventTransportMgr) {
        this.eventTransportMgr = eventTransportMgr;
        zkInstance = new ZkInstance();
    }

    EventTransportMgr getEventTransportMgr() {
        return eventTransportMgr;
    }

    public void go() throws LibeventException {

        String instanceId = JupiterConfiguration.instance().uuid();
        log.info("Instance with id [" + instanceId + "] ready to start ......");


        try {
            zkInstance.initConnection(this);
        } catch (InterruptedException | IOException e) {
            throw new LibeventException(e);
        }

        this.ensureExist(JupiterConfiguration.instance().getNameSpace());
        this.ensureExist(JupiterConfiguration.instance().getWorkerNode());

        String assignment = null;
        try {
            zkInstance.createWorkNode(JupiterConfiguration.instance().getWorkerNode(instanceId));
            log.info("Worker znode created successfully ......");

            assignment = zkInstance.getContent(JupiterConfiguration.instance().getWorkerNode(instanceId), workWatcher);
        } catch (KeeperException | InterruptedException e) {
            if (!(e instanceof KeeperException.NodeExistsException)) {
                throw new LibeventException(e);
            }
        }

        if (null != assignment && !assignment.trim().isEmpty()) {
            this.eventTransportMgr.assign(assignment.trim());
        }

        new Thread(() -> {
            try {
                // blocking operation
                log.info("Start to lock master ....");
                if (zkInstance.lock(instanceId, JupiterConfiguration.instance().getMasterNode())) {
                    log.info("Now it is the master server...");
                    log.info("perform the first check of the assignment, invoke method onChange()...");
                    InstanceListener instanceListener = new InstanceListener(zkInstance);
                    instanceListener.onChange();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }).start();
    }

    public void shutdown() {
        this.eventTransportMgr.stopAll();
    }

    void pause() {
        this.eventTransportMgr.stopAll();
    }

    void resume() {
        this.eventTransportMgr.restartAll();
    }

    private Watcher workWatcher = (event) -> {
        try {
            if (event.getType().equals(Watcher.Event.EventType.NodeDataChanged)) {
                String assignment = zkInstance.getContent(event.getPath(), this.workWatcher);
                this.eventTransportMgr.assign(assignment.trim());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    };

    private void ensureExist(String znode) throws LibeventException {
        try {
            zkInstance.createRoot(znode);
        } catch (KeeperException | InterruptedException e) {
            if (!(e instanceof KeeperException.NodeExistsException)) {
                throw new LibeventException(e);
            }
        }
    }

}
