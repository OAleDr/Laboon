package br.com.laboon.velocity.messaging;

import br.com.laboon.core.messaging.Channels;
import br.com.laboon.core.messaging.MessageBus;
import br.com.laboon.core.server.ServerInfo;
import br.com.laboon.core.server.ServerInfoSerializer;
import br.com.laboon.velocity.server.ServerRegistrationService;

public final class VelocityMessageService {

    private final MessageBus messageBus;
    private final ServerRegistrationService serverRegistrationService;

    public VelocityMessageService(
            MessageBus messageBus,
            ServerRegistrationService serverRegistrationService
    ) {
        this.messageBus = messageBus;
        this.serverRegistrationService =
                serverRegistrationService;
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
                Channels.SERVER_INFO,
                (channel, message) -> {

                    try {

                        ServerInfo server =
                                ServerInfoSerializer.deserialize(
                                        message
                                );

                        if (server == null) {

                            System.out.println(
                                    "[Laboon] ServerInfo inválido: "
                                            + message
                            );

                            return;
                        }


                        serverRegistrationService.register(
                                server
                        );

                    } catch (Exception e) {

                        System.out.println(
                                "[Laboon] Erro ao processar ServerInfo: "
                                        + message
                        );

                        e.printStackTrace();
                    }
                }
        );
    }
}