package com.personal.oyl.event.sample;

import com.personal.oyl.event.EventMapper;
import com.personal.oyl.event.EventReceiver;
import com.personal.oyl.event.EventSubscriber;
import com.personal.oyl.event.SubscriberConfig;
import com.personal.oyl.event.jupiter.EventTransportMgr;
import com.personal.oyl.event.jupiter.Instance;
import com.personal.oyl.event.jupiter.LibeventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author OuYang Liang
 */
@Component
public class AppListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(AppListener.class);

    @Resource
    private EventSubscriber dailyOrderReportSubscriber;

    @Resource
    private EventSubscriber userOrderReportSubscriber;

    @Resource
    private EventTransportMgr eventTransportMgr;

    @Resource
    private EventReceiver eventReceiver;

    @Resource
    private EventMapper eventMapper;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            log.info("App started ......");

            SubscriberConfig.instance.addSubscriber("o_c", dailyOrderReportSubscriber);
            SubscriberConfig.instance.addSubscriber("o_c", userOrderReportSubscriber);

            Instance instance = new Instance(eventTransportMgr, eventMapper, eventReceiver);
            try {
                instance.go();
            } catch (LibeventException e) {
                log.error(e.getMessage(), e);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(instance::shutdown));
        }
    }

}