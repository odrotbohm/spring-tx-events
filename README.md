# Spring transaction bound events

This library adds the necessary infrastructure to use Spring's `ApplicationEventPublisher` to publish events that are broadcasted on transaction commit. To use this, add the library to your classpath:

```java
<dependency>
    <groupId>org.springframework.labs</groupId>
    <artifactId>spring-tx-events</artifactId>
    <version>1.0.0.BUILD-SNAPSHOT</version>
</dependency>
```

In your Spring configuration, add a bean named `applicationEventMulticaster` like this:

```java
@Bean ApplicationEventMulticaster applicationEventMulticaster() {
  return new TransactionAwareApplicationEventMulticaster();
}
```

With that in place you can now create event classes that extend `TransactionBoundApplicationEvent` and use them as follows:

```java
class MyComponent

  private final ApplicationEventPublisher;

  @Inject
  public class MyComponent(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Transactional
  public void pay(Order order, CreditCard creditCard) {

    // process payment
    publisher.publishEvent(new OrderPaidEvent(order.getId()));
  }
}
```

Assuming `OrderPaidEvent` inherits from `TransactionBoundApplicationEvent`, the published event will *only* be multicasted in case the transaction succeeds.

