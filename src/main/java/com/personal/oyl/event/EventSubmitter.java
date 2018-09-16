package com.personal.oyl.event;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.personal.oyl.event.util.Configuration;

public class EventSubmitter implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(EventSubmitter.class);
    
    private int tbNum;
    private EventMapper mapper;
    
    public EventSubmitter(int tbNum, EventMapper mapper) {
        this.tbNum = tbNum;
        this.mapper = mapper;
    }
    
    @Override
    public void run() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.instance().getKafkaAddrs());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        
        try {
            List<String> eventIds = new LinkedList<>();
            List<Future<RecordMetadata>> futures = new LinkedList<>();
            
            while (!Thread.currentThread().isInterrupted()) {
                List<Event> list = mapper.queryTopN(tbNum, 100);
                
                if (null == list || list.isEmpty()) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                
                for (Event event : list) {
                    ProducerRecord<String, String> record = new ProducerRecord<>(
                            Configuration.instance().getProduceTopic(),
                            event.getGroup() % Configuration.instance().getProduceTopicPartitions(),
                            event.getEventTime().getTime(), null, event.json(), null);
                    futures.add(producer.send(record));
                    eventIds.add(event.getEventId());
                }
                
                boolean failed = false;
                for(Future<RecordMetadata> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        failed = true;
                        break;
                    } catch (ExecutionException e) {
                        log.error(e.getMessage(), e);
                        failed = true;
                        break;
                    }
                }
                
                if (!failed) {
                    mapper.batchClean(tbNum, eventIds);
                }
                
            }
        } finally {
            producer.close();
        }
        
    }
    
}