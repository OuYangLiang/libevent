package com.personal.oyl.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import com.personal.oyl.event.util.AppContext;

public class EventSubmitter implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(EventSubmitter.class);
    
    private int tbNum;
    
    public EventSubmitter(int tbNum) {
        this.tbNum = tbNum;
    }
    
    @Override
    public void run() {
        EventMapper mapper = AppContext.getContext().getBean(EventMapper.class);
        Configuration cfg  = AppContext.getContext().getBean(Configuration.class);
        
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.getKafkaAddrs());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        
        try {
            List<Long> eventIds = new LinkedList<>();
            List<Future<RecordMetadata>> futures = new LinkedList<>();
            
            while (!Thread.currentThread().isInterrupted()) {
                Map<String, Object> param = new HashMap<>();
                param.put("limit", Integer.valueOf(100));
                param.put("tbNum", tbNum);
                
                List<Event> list = mapper.queryTopN(param);
                
                if (null == list || list.isEmpty()) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                
                for (Event event : list) {
                    ProducerRecord<String, String> record = new ProducerRecord<>(cfg.getKafkaTopic(),
                            event.getGroup() % cfg.getKafkaPartitions(), event.getEventTime().getTime(), null,
                            event.json(), null);
                    futures.add(producer.send(record));
                    eventIds.add(event.getId());
                }
                
                boolean failed = false;
                for(Future<RecordMetadata> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        log.error(e.getMessage(), e);
                        failed = true;
                    }
                }
                
                if (!failed) {
                    Map<String, Object> param2 = new HashMap<>();
                    param2.put("list", eventIds);
                    param2.put("tbNum", tbNum);
                    mapper.batchClean(param2);
                }
                
            }
        } finally {
            producer.close();
        }
        
    }
    
}