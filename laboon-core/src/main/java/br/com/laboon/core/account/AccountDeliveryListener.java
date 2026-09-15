package br.com.laboon.core.account;

import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.account.group.GroupUpdatePublisher;
import br.com.laboon.core.account.repository.AccountRepository;
import br.com.laboon.core.messaging.MessageBus;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.UUID;

public final class AccountDeliveryListener {

    public static final String CHANNEL = "laboon:minecraft:delivery";

    private final Logger logger;
    private final AccountRepository accountRepository;
    private final TemporaryGroupService temporaryGroupService;
    private final GroupUpdatePublisher groupUpdatePublisher;

    private final Gson gson = new Gson();

    public AccountDeliveryListener(Logger logger, AccountRepository accountRepository, TemporaryGroupService temporaryGroupService, GroupUpdatePublisher groupUpdatePublisher) {
        this.logger = logger;
        this.accountRepository = accountRepository;
        this.temporaryGroupService = temporaryGroupService;
        this.groupUpdatePublisher = groupUpdatePublisher;
    }

    public void register(MessageBus messageBus) {

        messageBus.subscribe(CHANNEL, this::handle);

        logger.info("Listener de entregas do Minecraft iniciado.");
    }

    private void handle(String channel, String message) {

        if (!CHANNEL.equals(channel)) {
            return;
        }

        logger.info("Nova entrega recebida do Redis.");

        logger.info("Payload: {}", message);

        AccountDeliveryPayload payload;

        try {

            payload = gson.fromJson(message, AccountDeliveryPayload.class);

        } catch (JsonSyntaxException exception) {

            logger.error("Payload de entrega inválido.", exception);

            return;
        }

        if (payload == null) {

            logger.error("Payload de entrega vazio.");

            return;
        }

        process(payload);
    }

    private void process(AccountDeliveryPayload payload) {

        logger.info("=================================");

        logger.info("PROCESSANDO ENTREGA MINECRAFT");

        logger.info("=================================");

        logger.info("Delivery: {}", payload.deliveryId);

        logger.info("Pedido: {}", payload.orderId);

        logger.info("Minecraft UUID: {}", payload.minecraftUuid);

        logger.info("Produto: {}", payload.productName);

        logger.info("Slug: {}", payload.productSlug);

        try {

            UUID uuid = UUID.fromString(payload.minecraftUuid);

            Account account = accountRepository.findById(uuid);

            if (account == null) {

                logger.warn("Conta Minecraft não encontrada para UUID {}.", uuid);

                return;
            }

            Group group = resolveGroup(payload.productSlug);

            if (group == null) {

                logger.warn("Não foi possível identificar o grupo para o produto {}.", payload.productSlug);

                return;
            }

            Duration duration = resolveDuration(payload);

            logger.info("Grupo identificado: {}", group.name());

            logger.info("Duração: {}", duration);

            temporaryGroupService.setTemporaryGroup(account, group, duration);

            groupUpdatePublisher.publish(account);

            logger.info("Grupo {} aplicado com sucesso para {}.", group.name(), uuid);

            logger.info("Atualização de grupo publicada.");

            logger.info("=================================");

        } catch (IllegalArgumentException exception) {

            logger.error("UUID inválido na entrega: {}", payload.minecraftUuid, exception);

        } catch (Exception exception) {

            logger.error("Erro ao processar entrega {}.", payload.deliveryId, exception);
        }
    }

    private Group resolveGroup(String productSlug) {

        if (productSlug == null) {
            return null;
        }

        return switch (productSlug.toLowerCase()) {

            case "legendplus", "legend+" -> Group.LEGENDPLUS;

            case "legend" -> Group.LEGEND;

            case "explorerplus", "explorer+" -> Group.EXPLORERPLUS;

            case "explorer" -> Group.EXPLORER;

            default -> null;
        };
    }

    private Duration resolveDuration(AccountDeliveryPayload payload) {

        /*
         * TEMPORÁRIO:
         *
         * A duração definitiva deverá vir
         * da configuração do produto.
         *
         * Por enquanto usamos 30 dias
         * para testar o fluxo completo.
         */

        return Duration.ofDays(30);
    }

    private static final class AccountDeliveryPayload {

        private String deliveryId;

        private String orderId;

        private String userId;

        private String minecraftUuid;

        private String productId;

        private String productSlug;

        private String productName;

        private Object[] benefits;
    }
}