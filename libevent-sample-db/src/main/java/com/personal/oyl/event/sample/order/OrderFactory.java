package com.personal.oyl.event.sample.order;

import java.util.Date;
import java.util.Random;

import org.springframework.stereotype.Component;

/**
 * @author OuYang Liang
 */
@Component
public class OrderFactory {
    static Random r = new Random();
    
    public Order randomOrder() {
        Order order = new Order();
        order.setOrderAmount(r.nextInt(1000));
        order.setOrderTime(new Date());
        order.setUserId(Long.valueOf(r.nextInt(10)));
        
        return order;
    }
    
}
