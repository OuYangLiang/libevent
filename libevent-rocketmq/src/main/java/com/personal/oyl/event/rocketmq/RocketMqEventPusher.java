package com.personal.oyl.event.rocketmq;

import com.personal.oyl.event.Event;
import com.personal.oyl.event.EventPusher;
import com.personal.oyl.event.EventSerde;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author OuYang Liang
 */
public class RocketMqEventPusher implements EventPusher {

    private static final Logger log = LoggerFactory.getLogger(RocketMqEventPusher.class);

    private EventSerde eventSerde;
    private DefaultMQProducer producer;

    public RocketMqEventPusher(EventSerde eventSerde) {
        this.eventSerde = eventSerde;

        this.producer = new DefaultMQProducer(RocketMqConfiguration.instance().getProduceGroup());
        this.producer.setNamesrvAddr(RocketMqConfiguration.instance().getNamesrvAddr());
        this.producer.setInstanceName(RocketMqConfiguration.instance().getInstanceName());
        try {
            this.producer.start();
        } catch (MQClientException e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    @Override
    public List<String> push(int tbNum, List<Event> events) {
        final int partition = tbNum % RocketMqConfiguration.instance().getProduceTopicPartitions();
        String topic  = RocketMqConfiguration.instance().getProduceTopic();
        String tag    = RocketMqConfiguration.instance().getProduceTag();

        List<String> eventIds = new LinkedList<>();

        for (Event event : events) {

            Message msg = null;
            try {
                msg = new Message(topic, tag, eventSerde.toJson(event).getBytes(RemotingHelper.DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            SendResult sendResult;
            try {
                sendResult = producer.send(msg, (mqs, m, arg) -> mqs.get((int)arg) ,partition);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }

            if (null != sendResult && sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
                eventIds.add(event.getEventId());
            } else {
                break;
            }

            eventIds.add(event.getEventId());
        }

        return eventIds;
    }
}
