package br.com.laboon.core.messaging;

public final class MessageBus {

    private final RedisPublisher publisher;
    private final RedisSubscriber subscriber;

    public MessageBus(RedisPublisher publisher, RedisSubscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    public void publish(String channel, String message) {
        publisher.publish(channel, message);
    }

    public void subscribe(String channel, MessageHandler handler) {
        subscriber.subscribe(channel, handler);
    }
}