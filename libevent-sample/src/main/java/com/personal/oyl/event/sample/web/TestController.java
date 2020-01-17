package com.personal.oyl.event.sample.web;

import com.personal.oyl.event.sample.order.Order;
import com.personal.oyl.event.sample.order.OrderFactory;
import com.personal.oyl.event.sample.order.OrderRepos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author OuYang Liang
 * @since 2020-01-15
 */
@RestController
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @Resource
    private OrderRepos repos;

    @Resource
    private OrderFactory orderFactory;

    @RequestMapping("/test")
    public String test() {
        log.info("test accessed ...");
        return "hello world";
    }

    @RequestMapping("/newOrder")
    public Object newOrder() {
        Order order = orderFactory.randomOrder();
        repos.createOrder(order);
        return order;
    }

    @RequestMapping("/report")
    public Object report() {
        TestResult result = new TestResult();
        result.setItems(repos.selectAllUserReport());
        result.setDailyItems(repos.selectAllDailyReport());

        return result;
    }

}
