package com.personal.oyl.event.sample.order;

public class UserOrderReport {
    private Long userId;
    private Long orderNum;
    private Long orderTotal;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public Long getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(Long orderTotal) {
        this.orderTotal = orderTotal;
    }
    
}
