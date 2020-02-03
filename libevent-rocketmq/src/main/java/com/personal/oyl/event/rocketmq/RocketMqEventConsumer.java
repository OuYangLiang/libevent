package com.personal.oyl.event.rocketmq;

import com.personal.oyl.event.*;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @author OuYang Liang
 */
public class RocketMqEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(RocketMqEventConsumer.class);
    private EventSerde eventSerde;
    private DefaultMQPushConsumer consumer;

    public RocketMqEventConsumer(EventSerde eventSerde) {
        this.eventSerde = eventSerde;
    }

    public void start() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMqConfiguration.instance().getConsumerGroup());

        consumer.setNamesrvAddr(RocketMqConfiguration.instance().getNamesrvAddr());
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setConsumeThreadMin(RocketMqConfiguration.instance().getNumOfThreads());
        consumer.setConsumeThreadMax(RocketMqConfiguration.instance().getNumOfThreads());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setInstanceName(RocketMqConfiguration.instance().getInstanceName());

        for (Map.Entry<String, String> entry : RocketMqConfiguration.instance().getConsumeTopic().entrySet()) {
            consumer.subscribe(entry.getKey(), entry.getValue());
        }
        consumer.registerMessageListener(
            (List<MessageExt> msgs, ConsumeOrderlyContext context) -> {
                for (MessageExt msg : msgs) {
                    String message = null;
                    try {
                        message = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        log.error(e.getMessage(), e);
                    }

                    Event event = null;
                    if (null != message) {
                        try {
                            event = eventSerde.fromJson(message);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    if (null != event) {
                        EventReceiver.instance().onEvent(event);
                    }
                }

                return ConsumeOrderlyStatus.SUCCESS;
            }
        );

        consumer.start();
    }

    public void shutdown() {
        if (null != consumer) {
            consumer.shutdown();
        }
    }

}
