# projectreactor-demo
Project Reactor Demo

Publisher - Database, Kafka, Server

Subscriber - Publisher data consumer

Subscription - connector of Publisher and Subscriber

Processor - combines Publisher and Subscriber

```java
public interface Publisher<T> {

    public void subscribe(Subscriber<? super T> s);

}
```

```java
public interface Subscriber<T> {

    public void onSubscribe(Subscription s);

    public void onNext(T t);

    public void onError(Throwable t);

    public void onComplete();

}
```

```java
public interface Subscription {

    public void request(long n);

    public void cancel();

}
```

```java
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
}
```