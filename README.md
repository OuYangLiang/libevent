### 简介

---

libevent是一个基于Kafka的一个分布式事件驱动实现，设计文档：[https://ouyblog.com/2018/08/基于Kafka实现事件驱动架构.html](https://ouyblog.com/2018/08/基于Kafka实现事件驱动架构.html)

使用libevent发布事件时，事件消息会先被存入数据库，之后异步发送到kafka，以此来解决**消息丢失**或**无效消息**的问题。为了使用libevent，需要提供一个`EventMapper`的实现来负责事件消息的持久化：

```java
public interface EventMapper {
    void insert(int tbNum, Event event);
    
    List<Event> queryTopN(int tbNum, int limit);
    
    void batchClean(int tbNum, List<String> eventIds);
}
```

在libevent中，应用扮演着三种角色：

#### 事件发布者

使用libevent提供的`EventPublisher`类来发布事件，如

```java
EventMapper mapper = ... // your implementation
EventPublisher publisher = new EventPublisher(mapper);
publisher.publish("eventType", new Date(), order.json(), order.getUserId().intValue() % Configuration.instance().getNumOfEventTables());
```

#### 消费搬运者

由于事件消息是先被存入到DB中，所以需要通过线程异步将消息从DB取出来发送到Kafka。这个工作由libevent提供的`Worker`类来完成，所以在应用启动时，需要创建一个`Worker`实例来负责这项工作，如：

```java
@Override
public void onApplicationEvent(ContextRefreshedEvent event) {
    EventMapper mapper = ... // your implementation
    Worker worker = new Worker(mapper);

    try {
        log.error("start worker...");
        worker.start();
    } catch (IOException | InterruptedException | KeeperException e) {
        log.error(e.getMessage(), e);
    }
}
```

libevent通过支持多个事件分表，来改善单表的性能瓶颈，在集群环境下可以为每个应用实例启动一个`Worker`实例来分摊事件表的处理。`Master`类的任务是通过Zookeeper监听应用实例的变化，动态的分配事件分表实现负载均衡，所以在应用启动时，也需要初始化一个`Master`实例，如：

```java
@Override
public void onApplicationEvent(ContextRefreshedEvent event) {
    Master master = new Master();
    
    try {
        log.error("start master...");
        master.start();
    } catch (IOException | InterruptedException | KeeperException e) {
        log.error(e.getMessage(), e);
    }
}
```

#### 事件消费者

`EventConsumer`是一个`Runnable`实现，它会消费kafka中的事件消息，并调用订阅者执行对应的处理。在应用启动时需要启一个线程来执行，如：

```java
@Override
public void onApplicationEvent(ContextRefreshedEvent event) {
    Thread kafkaConsumer = new Thread(new EventConsumer());
    kafkaConsumer.start();
}
```

为了实现事件订阅，可以为每类事件提供一个或多个订阅者。订阅者需要实现接口`BaseSubscriber`：

```java
public interface BaseSubscriber {
    void onEvent(Event e);
}
```

类似的，在应用启动时，需要将订阅者和事件类型的关系维护到`SubscriberConfig`中：

```java
@Override
public void onApplicationEvent(ContextRefreshedEvent event) {
    BaseSubscriber sub1 = ... // your implementation
    BaseSubscriber sub2 = ... // your implementation
    
    SubscriberConfig.instance().addSubscriber("eventType", sub1);
    SubscriberConfig.instance().addSubscriber("eventType", sub2);
    SubscriberConfig.instance().addSubscriber("anotherType", sub2);
}
```

最后，libevent需要的配置信息，由event.properties文件提供，你需要在classpath下提供该文件。

[https://github.com/OuYangLiang/libevent-sample](https://github.com/OuYangLiang/libevent-sample)是一个使用libevent的一个示例。