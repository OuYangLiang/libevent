package com.personal.oyl.event.sample;

import com.personal.oyl.event.EventSerde;
import com.personal.oyl.event.EventSubscriber;
import com.personal.oyl.event.SubscriberConfig;
import com.personal.oyl.event.jupiter.EventTransportMgr;
import com.personal.oyl.event.jupiter.Instance;
import com.personal.oyl.event.kafka.KafkaEventConsumer;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

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
    private EventSerde eventSerde;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            log.info("App started ......");

            SubscriberConfig.instance().addSubscriber("o_c", dailyOrderReportSubscriber);
            SubscriberConfig.instance().addSubscriber("o_c", userOrderReportSubscriber);

            KafkaEventConsumer kafkaEventConsumer = new KafkaEventConsumer(eventSerde);
            Thread consumeThread = new Thread(kafkaEventConsumer);
            consumeThread.start();

            Instance instance = new Instance(eventTransportMgr);
            try {
                instance.go();
            } catch (IOException | KeeperException | InterruptedException e) {
                log.error(e.getMessage(), e);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() ->  {
                instance.shutdown();
                kafkaEventConsumer.wake();
                consumeThread.interrupt();
            }));
        }
    }

}