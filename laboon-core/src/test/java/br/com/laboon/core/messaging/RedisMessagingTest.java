package br.com.laboon.core.messaging;

import br.com.laboon.core.redis.RedisManager;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisMessagingTest {

    @Test
    void shouldPublishAndReceiveMessage() throws InterruptedException {

        RedisManager redisManager =
                new RedisManager("localhost", 6379);

        RedisPublisher publisher =
                new RedisPublisher(redisManager);

        RedisSubscriber subscriber =
                new RedisSubscriber(redisManager);

        CountDownLatch latch = new CountDownLatch(1);

        String channel = "laboon:test";
        String message = "Olá, Laboon!";

        subscriber.subscribe(channel, (receivedChannel, receivedMessage) -> {

            assertEquals(channel, receivedChannel);
            assertEquals(message, receivedMessage);

            latch.countDown();
        });

        Thread.sleep(500);

        publisher.publish(channel, message);

        boolean received =
                latch.await(5, TimeUnit.SECONDS);

        assertTrue(received);

        redisManager.close();
    }
}