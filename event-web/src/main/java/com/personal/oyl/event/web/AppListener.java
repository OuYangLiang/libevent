package com.personal.oyl.event.web;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.personal.oyl.event.Master;
import com.personal.oyl.event.SubscriberConfig;
import com.personal.oyl.event.Worker;
import com.personal.oyl.event.web.subscribers.Sub1;

@Component
public class AppListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(AppListener.class);
    
    @Autowired
    private SubscriberConfig config;
    
    @Autowired
    private Worker worker;
    
    @Autowired
    private Master master;
    

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            config.addSubscriber("Event Type", new Sub1());
            
            try {
                log.error("start worker...");
                worker.start();
            } catch (IOException | InterruptedException | KeeperException e) {
                log.error(e.getMessage(), e);
            }
            
            try {
                log.error("start master...");
                master.start();
            } catch (IOException | InterruptedException | KeeperException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
