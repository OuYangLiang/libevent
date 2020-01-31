package com.personal.oyl.event.jupiter;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author OuYang Liang
 */
public class TestInstance {
    private static final Logger log = LoggerFactory.getLogger(TestInstance.class);


    public void go() throws IOException, InterruptedException, KeeperException {
        TestZkInstance zkInstance = new TestZkInstance();
        log.info("Start to connect to zookeeper ......");
        zkInstance.initConnection(null);

        try {
            log.info("Create root znode ...");
            zkInstance.createRoot(JupiterConfiguration.instance().getNameSpace());
        } catch (KeeperException e) {
            if (!e.code().equals(KeeperException.Code.NODEEXISTS)) {
                throw e;
            }
        }
        log.info("Root znode created successfully ...");


        log.info("Start to lock master ....");
        if (zkInstance.lock("testId", JupiterConfiguration.instance().getMasterNode())) {
            log.info("Master locked successfully ....");
            log.info("To work as master ............");
        }
    }



    public static void main(String[] args)  {
        try {
            new TestInstance().go();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


}
