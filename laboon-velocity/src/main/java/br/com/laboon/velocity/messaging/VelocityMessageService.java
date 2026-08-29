package br.com.laboon.velocity.messaging;

import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.messaging.Channels;

public final class VelocityMessageService {

    private final MessageBus messageBus;

    public VelocityMessageService(
            MessageBus messageBus
    ) {
        this.messageBus = messageBus;
    }

    public void publishPlayerMessage(
            String message
    ) {

        messageBus.publish(
                Channels.PLAYER,
                message
        );
    }

    public void publishServerMessage(
            String message
    ) {

        messageBus.publish(
                Channels.SERVER,
                message
        );
    }

    public void listen() {

        messageBus.subscribe(
                Channels.SERVER,
                (channel, message) -> {

                    System.out.println(
                            "[Laboon] Server message: "
                                    + message
                    );
                }
        );
    }
}