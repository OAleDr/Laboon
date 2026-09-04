package br.com.laboon.core.account.group;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.messaging.MessageBus;

import java.time.Instant;
import java.util.Map;

public final class GroupUpdatePublisher {

    public static final String CHANNEL = "laboon:group:update";

    private final MessageBus messageBus;

    public GroupUpdatePublisher(MessageBus messageBus) {

        if (messageBus == null) {
            throw new IllegalArgumentException("MessageBus não pode ser nulo.");
        }

        this.messageBus = messageBus;
    }

    public void publish(Account account) {

        if (account == null) {
            throw new IllegalArgumentException("Account não pode ser nulo.");
        }

        messageBus.publish(CHANNEL, serialize(account));
    }

    private String serialize(Account account) {

        StringBuilder message = new StringBuilder();

        /*
         * UUID
         */
        message.append(account.getUniqueId());

        /*
         * Grupo permanente
         */
        message.append("|").append(account.getGroup().name());

        /*
         * Tag atual
         */
        message.append("|").append(account.getTag() == null ? "" : account.getTag());

        /*
         * Grupos temporários
         */
        for (Map.Entry<Group, Instant> entry : account.getTemporaryGroups().entrySet()) {

            Group group = entry.getKey();

            Instant expiresAt = entry.getValue();

            if (group == null || expiresAt == null) {

                continue;
            }

            /*
             * Não envia grupo já expirado.
             */
            if (!expiresAt.isAfter(Instant.now())) {

                continue;
            }

            message.append("|").append(group.name()).append(":").append(expiresAt);
        }

        return message.toString();
    }
}