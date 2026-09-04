package br.com.laboon.velocity.command;

import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.command.*;
import br.com.laboon.core.profile.ProfileManager;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public final class VelocityCommandFramework extends CommandFramework {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final VelocityCommandProvider provider;
    private final ProfileManager profileManager;

    public VelocityCommandFramework(ProxyServer proxyServer, Logger logger, VelocityCommandProvider provider, ProfileManager profileManager) {

        if (proxyServer == null) {
            throw new IllegalArgumentException("ProxyServer não pode ser nulo.");
        }

        if (logger == null) {
            throw new IllegalArgumentException("Logger não pode ser nulo.");
        }

        if (provider == null) {
            throw new IllegalArgumentException("VelocityCommandProvider não pode ser nulo.");
        }

        if (profileManager == null) {
            throw new IllegalArgumentException("ProfileManager não pode ser nulo.");
        }

        this.proxyServer = proxyServer;
        this.logger = logger;
        this.provider = provider;
        this.profileManager = profileManager;
    }

    @Override
    protected void registerMethod(CommandClass commandClass, Method method, Command annotation) {

        super.registerMethod(commandClass, method, annotation);

        registerVelocityCommand(commandClass, method, annotation);
    }

    private void registerVelocityCommand(CommandClass commandClass, Method method, Command annotation) {

        String name = annotation.name().trim().toLowerCase();

        if (name.isEmpty()) {

            throw new IllegalArgumentException("O nome do comando não pode ser vazio.");
        }

        String[] aliases = annotation.aliases();

        CommandManager commandManager = proxyServer.getCommandManager();

        var metaBuilder = commandManager.metaBuilder(name);

        if (aliases != null && aliases.length > 0) {

            metaBuilder.aliases(aliases);
        }

        commandManager.register(metaBuilder.build(), new RegisteredVelocityCommand(commandClass, method, annotation));

        logger.info("Comando registrado no Velocity: /{}", name);
    }

    private boolean hasGroupPermission(VelocityCommandSender sender, Group requiredGroup) {

        if (requiredGroup == null || requiredGroup == Group.DEFAULT) {

            return true;
        }

        /*
         * O console não possui um PlayerProfile.
         * Portanto, comandos administrativos podem
         * ser executados pelo console.
         */
        if (!sender.isPlayer()) {
            return true;
        }

        Player player = sender.requirePlayer();

        var profile = profileManager.get(player.getUniqueId());

        if (profile == null) {

            sender.sendMessage("§cSeu perfil ainda não foi carregado.");

            return false;
        }

        /*
         * PlayerProfile#getGroup() considera:
         *
         * - grupo permanente;
         * - grupos temporários;
         * - grupo temporário de maior poder.
         */
        return profile.hasGroupPermission(requiredGroup);
    }

    private final class RegisteredVelocityCommand implements SimpleCommand {

        private final CommandClass commandClass;
        private final Method method;
        private final Command annotation;

        private RegisteredVelocityCommand(CommandClass commandClass, Method method, Command annotation) {

            this.commandClass = commandClass;
            this.method = method;
            this.annotation = annotation;
        }

        @Override
        public void execute(Invocation invocation) {

            VelocityCommandSender sender = new VelocityCommandSender(invocation.source());

            /*
             * Verifica a permissão antes
             * de executar o comando.
             */
            if (!hasGroupPermission(sender, annotation.group())) {

                sender.sendMessage("§cVocê não possui permissão para executar este comando.");

                return;
            }

            VelocityCommandArgs args = new VelocityCommandArgs(invocation.source(), invocation.alias(), invocation.arguments());

            try {

                method.setAccessible(true);

                method.invoke(commandClass, args);

            } catch (InvocationTargetException exception) {

                Throwable cause = exception.getCause();

                if (cause instanceof IllegalArgumentException) {

                    sender.sendMessage("§cUso incorreto do comando.");

                    return;
                }

                logger.error("Erro ao executar o comando /{}", invocation.alias(), cause);

                sender.sendMessage("§cOcorreu um erro ao executar o comando.");

            } catch (Throwable exception) {

                logger.error("Erro ao executar o comando /{}", invocation.alias(), exception);

                sender.sendMessage("§cOcorreu um erro ao executar o comando.");
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {

            VelocityCommandSender sender = new VelocityCommandSender(invocation.source());

            /*
             * Não fornece sugestões para
             * quem não possui permissão.
             */
            if (!hasGroupPermission(sender, annotation.group())) {

                return Collections.emptyList();
            }

            VelocityCommandArgs args = new VelocityCommandArgs(invocation.source(), invocation.alias(), invocation.arguments());

            try {

                Completer completer = commandClass.getCompleter();

                if (completer == null) {
                    return Collections.emptyList();
                }

                CommandArgs commandArgs = args;

                List<String> suggestions = completer.complete(commandArgs);

                if (suggestions == null) {
                    return Collections.emptyList();
                }

                return suggestions;

            } catch (Throwable exception) {

                logger.error("Erro no tab completion do comando /{}", invocation.alias(), exception);

                return Collections.emptyList();
            }
        }
    }
}