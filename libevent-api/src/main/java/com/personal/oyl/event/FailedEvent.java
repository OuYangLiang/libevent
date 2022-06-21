package com.personal.oyl.event;

public class FailedEvent {
    private Long id;
    private String subscriberId;
    private Event event;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String key() {
        return event.getEventType() + "_" + subscriberId + "_" + Long.toString(event.getRouteKey());
    }
}
