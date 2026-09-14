package br.com.laboon.bukkit.command.commands;

import br.com.laboon.bukkit.command.BukkitCommandArgs;
import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.account.AccountType;
import br.com.laboon.core.command.Command;
import br.com.laboon.core.command.CommandClass;
import br.com.laboon.core.redis.RedisManager;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPooled;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class LinkCommand implements CommandClass {

    private static final String CODE_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final int CODE_LENGTH = 6;

    /**
     * Tempo de validade do código:
     * 10 minutos.
     */
    private static final int CODE_TTL_SECONDS = 600;

    /**
     * Código de vínculo.
     *
     * laboon:link:code:A7K92P
     *      ↓
     * UUID do jogador
     */
    private static final String CODE_KEY_PREFIX =
            "laboon:link:code:";

    /**
     * Código atualmente ativo para o jogador.
     *
     * laboon:link:player:<uuid>
     *      ↓
     * A7K92P
     */
    private static final String PLAYER_KEY_PREFIX =
            "laboon:link:player:";

    private final AccountManager accountManager;
    private final RedisManager redisManager;

    public LinkCommand(
            AccountManager accountManager,
            RedisManager redisManager
    ) {
        this.accountManager = accountManager;
        this.redisManager = redisManager;
    }

    @Command(
            name = "vincular",
            aliases = {"link"},
            description = "Gera um código para vincular sua conta ao Laboon Web.",
            usage = "/vincular"
    )
    public void execute(BukkitCommandArgs args) {

        /*
         * Apenas jogadores podem utilizar.
         */
        if (!args.isPlayer()) {

            args.getSender().sendMessage(
                    "§cApenas jogadores podem utilizar este comando."
            );

            return;
        }

        Player player = args.getSender().getPlayer();

        UUID uniqueId = player.getUniqueId();

        /*
         * Busca a conta pelo UUID.
         */
        Account account = accountManager.get(uniqueId);

        if (account == null) {

            player.sendMessage(
                    "§cSua conta ainda não está disponível."
            );

            player.sendMessage(
                    "§7Tente novamente em alguns segundos."
            );

            return;
        }

        /*
         * Conta ORIGINAL não precisa de vínculo.
         */
        if (account.getType() == AccountType.ORIGINAL) {

            player.sendMessage("");
            player.sendMessage("§6§lLABOON");
            player.sendMessage("");

            player.sendMessage(
                    "§aSua conta Minecraft é ORIGINAL."
            );

            player.sendMessage(
                    "§7Você não precisa utilizar o §f/vincular§7."
            );

            player.sendMessage("");

            return;
        }

        JedisPooled redis = redisManager.getJedis();

        String playerKey =
                PLAYER_KEY_PREFIX + uniqueId;

        /*
         * =========================================================
         * VERIFICA SE O JOGADOR JÁ POSSUI UM CÓDIGO
         * =========================================================
         */

        String existingCode = redis.get(playerKey);

        if (existingCode != null && !existingCode.isBlank()) {

            String existingCodeKey =
                    CODE_KEY_PREFIX + existingCode;

            /*
             * Confirma se o código ainda existe.
             *
             * Isso evita mostrar um código que já expirou.
             */
            if (redis.exists(existingCodeKey)) {

                player.sendMessage("");
                player.sendMessage("§6§lLABOON");
                player.sendMessage("");

                player.sendMessage(
                        "§eVocê já possui um código de vínculo ativo:"
                );

                player.sendMessage("");

                player.sendMessage(
                        "§f§l        " + existingCode
                );

                player.sendMessage("");

                player.sendMessage(
                        "§7Use este código no Laboon Web."
                );

                player.sendMessage(
                        "§7Não é necessário gerar outro código."
                );

                player.sendMessage("");

                return;
            }

            /*
             * A referência do jogador ficou para trás,
             * mas o código já expirou.
             *
             * Limpamos antes de gerar um novo.
             */
            redis.del(playerKey);
        }

        /*
         * =========================================================
         * GERA NOVO CÓDIGO
         * =========================================================
         */

        String code = generateUniqueCode(redis);

        String codeKey =
                CODE_KEY_PREFIX + code;

        /*
         * O código aponta para o UUID.
         *
         * O nickname NÃO é utilizado como identificação.
         */
        redis.setex(
                codeKey,
                CODE_TTL_SECONDS,
                uniqueId.toString()
        );

        /*
         * Guarda o código ativo do jogador.
         */
        redis.setex(
                playerKey,
                CODE_TTL_SECONDS,
                code
        );

        /*
         * =========================================================
         * MOSTRA O CÓDIGO
         * =========================================================
         */

        player.sendMessage("");
        player.sendMessage("§6§lLABOON");
        player.sendMessage("");

        player.sendMessage(
                "§fConta: §e" + account.getName()
        );

        player.sendMessage("");

        player.sendMessage(
                "§a§lCódigo de vínculo:"
        );

        player.sendMessage("");

        player.sendMessage(
                "§f§l        " + code
        );

        player.sendMessage("");

        player.sendMessage(
                "§7Este código é válido por §f10 minutos§7."
        );

        player.sendMessage(
                "§7Use-o no Laboon Web para vincular sua conta."
        );

        player.sendMessage("");
    }

    /**
     * Gera um código que ainda não existe no Redis.
     */
    private String generateUniqueCode(JedisPooled redis) {

        for (int attempt = 0; attempt < 20; attempt++) {

            String code = generateCode();

            String codeKey =
                    CODE_KEY_PREFIX + code;

            /*
             * Evita colisão com outro código ativo.
             */
            if (!redis.exists(codeKey)) {
                return code;
            }
        }

        throw new IllegalStateException(
                "Não foi possível gerar um código de vínculo único."
        );
    }

    /**
     * Gera um código de 6 caracteres.
     *
     * Removemos caracteres facilmente confundidos,
     * como I, O, 0 e 1.
     */
    private String generateCode() {

        StringBuilder code =
                new StringBuilder(CODE_LENGTH);

        for (int index = 0; index < CODE_LENGTH; index++) {

            int position =
                    ThreadLocalRandom.current()
                            .nextInt(CODE_CHARACTERS.length());

            code.append(
                    CODE_CHARACTERS.charAt(position)
            );
        }

        return code.toString();
    }
}