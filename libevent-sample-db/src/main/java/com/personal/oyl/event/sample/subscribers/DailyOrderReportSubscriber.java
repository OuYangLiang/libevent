package com.personal.oyl.event.sample.subscribers;

import com.personal.oyl.event.EventSerde;
import com.personal.oyl.event.sample.order.DailyOrderReport;
import com.personal.oyl.event.sample.order.Order;
import com.personal.oyl.event.sample.order.OrderRepos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.personal.oyl.event.EventSubscriber;
import com.personal.oyl.event.Event;
import com.personal.oyl.event.EventMapper;

import javax.annotation.Resource;

/**
 * @author OuYang Liang
 */
@Component("dailyOrderReportSubscriber")
public class DailyOrderReportSubscriber implements EventSubscriber {
    
    private static final Logger log = LoggerFactory.getLogger(DailyOrderReportSubscriber.class);
    
    @Resource
    private OrderRepos repos;
    
    @Resource
    private EventMapper eventMapper;

    @Resource
    private EventSerde eventSerde;
    
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @Override
    public void onEvent(Event e) {
        
        try {
            Order order = Order.fromJson(e.getContext());
            DailyOrderReport report = repos.selectDailyOrderReportByKey(new java.sql.Date(order.getOrderTime().getTime()));
            
            if (null == report) {
                report = new DailyOrderReport();
                report.setDay(new java.sql.Date(order.getOrderTime().getTime()));
                report.setOrderNum(1L);
                report.setOrderTotal(Long.valueOf(order.getOrderAmount()));
                
                repos.createDailyOrderReport(report);
            } else {
                report.setOrderNum(report.getOrderNum() + 1);
                report.setOrderTotal(report.getOrderTotal() + order.getOrderAmount());
                
                repos.updateDailyOrderReport(report);
            }
            
        } catch (DuplicateKeyException ex) {
            log.warn("Duplicated message " + eventSerde.toJson(e));
        }
        
    }

}
