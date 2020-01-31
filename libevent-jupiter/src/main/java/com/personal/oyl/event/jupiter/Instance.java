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

    public void go() throws InterruptedException, IOException, KeeperException {

        String instanceId = JupiterConfiguration.instance().uuid();
        log.info("Instance with id [" + instanceId + "] ready to start ......");


        zkInstance.initConnection(this);

        try {
            zkInstance.createRoot(JupiterConfiguration.instance().getNameSpace());
        } catch (KeeperException e) {
            if (!e.code().equals(KeeperException.Code.NODEEXISTS)) {
                throw e;
            }
        }

        try {
            zkInstance.createRoot(JupiterConfiguration.instance().getWorkerNode());
        } catch (KeeperException e) {
            if (!e.code().equals(KeeperException.Code.NODEEXISTS)) {
                throw e;
            }
        }

        zkInstance.createWorkNode(JupiterConfiguration.instance().getWorkerNode(instanceId));
        log.info("Worker znode created successfully ......");

        String assignment = zkInstance.getContent(JupiterConfiguration.instance().getWorkerNode(instanceId), workWatcher);
        if (null != assignment && !assignment.trim().isEmpty()) {
            this.eventTransportMgr.assign(assignment.trim());
        }

        // blocking operation
        log.info("Start to lock master ....");
        if (zkInstance.lock(instanceId, JupiterConfiguration.instance().getMasterNode())) {
            log.info("Now it is the master server...");
            log.info("perform the first check of the assignment, invoke method onChange()...");
            InstanceListener instanceListener = new InstanceListener(zkInstance);
            instanceListener.onChange();
        }
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
}
