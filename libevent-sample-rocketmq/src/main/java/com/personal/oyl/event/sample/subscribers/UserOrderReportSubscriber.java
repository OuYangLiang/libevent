package com.personal.oyl.event.sample.subscribers;

import com.personal.oyl.event.EventSerde;
import com.personal.oyl.event.sample.order.Order;
import com.personal.oyl.event.sample.order.OrderRepos;
import com.personal.oyl.event.sample.order.UserOrderReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.personal.oyl.event.EventSubscriber;
import com.personal.oyl.event.Event;

import javax.annotation.Resource;

/**
 * @author OuYang Liang
 */
@Component("userOrderReportSubscriber")
public class UserOrderReportSubscriber implements EventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(UserOrderReportSubscriber.class);
    
    @Resource
    private OrderRepos orderRepos;

    @Resource
    private EventSerde eventSerde;
    
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @Override
    public void onEvent(Event e) {
        Order order = Order.fromJson(e.getContext());
        while (true) {
            UserOrderReport report = orderRepos.selectUserOrderReportByKey(order.getUserId());

            if (null == report) {
                report = new UserOrderReport();
                report.setUserId(order.getUserId());
                report.setOrderNum(0L);
                report.setOrderTotal(0L);

                try {
                    orderRepos.createUserOrderReport(report);
                } catch (DuplicateKeyException ex) {
                    log.warn("Duplicated message " + eventSerde.toJson(e));
                }
            } else {
                report.setOrderNum(1L);
                report.setOrderTotal(Long.valueOf(order.getOrderAmount()));

                orderRepos.updateUserOrderReport(report);
                break;
            }
        }
    }
}
