package br.com.laboon.bukkit.command;

import br.com.laboon.core.command.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BukkitCommandFramework extends CommandFramework {

    private final JavaPlugin plugin;
    private final org.bukkit.command.CommandMap commandMap;

    public BukkitCommandFramework(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("O plugin não pode ser nulo.");
        }

        this.plugin = plugin;
        this.commandMap = findCommandMap();
    }

    @Override
    protected void registerMethod(CommandClass commandClass, Method method, Command annotation) {
        super.registerMethod(commandClass, method, annotation);

        registerBukkitCommand(commandClass, annotation);
    }

    private void registerBukkitCommand(CommandClass commandClass, Command annotation) {
        String name = annotation.name().trim().toLowerCase();

        BukkitCommand command = new BukkitCommand(name) {

            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                return executeCommand(commandClass, annotation, sender, label, args);
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                return completeCommand(commandClass, annotation, sender, alias, args);
            }
        };

        command.setDescription(annotation.description());

        command.setUsage(annotation.usage());

        command.setAliases(Arrays.asList(annotation.aliases()));

        commandMap.register(plugin.getName().toLowerCase(), command);

        plugin.getLogger().info("Comando registrado automaticamente: /" + name);
    }

    private boolean executeCommand(CommandClass commandClass, Command annotation, CommandSender sender, String label, String[] arguments) {
        BukkitCommandSender commandSender = new BukkitCommandSender(sender);

        String permission = annotation.permission();

        if (!commandSender.hasPermission(permission)) {
            commandSender.sendMessage("§cVocê não possui permissão para executar este comando.");
            return true;
        }

        if (!hasValidSubcommand(annotation, arguments)) {
            sendUsage(commandSender, annotation);
            return true;
        }

        BukkitCommandArgs commandArgs = new BukkitCommandArgs(sender, label, arguments);

        try {

            invoke(commandClass, commandArgs);

            return true;

        } catch (IllegalArgumentException exception) {

            commandSender.sendMessage("§cUso incorreto do comando.");

            return true;

        } catch (InvocationTargetException exception) {

            Throwable cause = exception.getCause();

            plugin.getLogger().severe("Erro ao executar o comando /" + label);

            if (cause != null) {
                cause.printStackTrace();
            } else {
                exception.printStackTrace();
            }

            commandSender.sendMessage("§cOcorreu um erro ao executar o comando.");

            return true;

        } catch (ReflectiveOperationException exception) {

            plugin.getLogger().severe("Não foi possível executar o comando /" + label);

            exception.printStackTrace();

            commandSender.sendMessage("§cOcorreu um erro ao executar o comando.");

            return true;
        }
    }

    private List<String> completeCommand(CommandClass commandClass, Command annotation, CommandSender sender, String alias, String[] arguments) {
        String permission = annotation.permission();

        if (permission != null && !permission.isBlank() && !sender.hasPermission(permission)) {

            return Collections.emptyList();
        }

        String[] subcommands = annotation.subcommands();

        if (arguments == null) {
            arguments = new String[0];
        }

        /*
         * Primeiro nível:
         *
         * /coinstest <TAB>
         *
         * Continua utilizando os subcommands
         * declarados na annotation.
         */
        if (arguments.length <= 1) {

            String input = arguments.length == 0 ? "" : arguments[0].toLowerCase();

            List<String> suggestions = new ArrayList<>();

            for (String subcommand : subcommands) {

                if (subcommand == null || subcommand.isBlank()) {
                    continue;
                }

                if (subcommand.toLowerCase().startsWith(input)) {

                    suggestions.add(subcommand);
                }
            }

            return suggestions;
        }

        /*
         * A partir daqui o comando pode fornecer
         * seu próprio Completer.
         */
        Completer completer = commandClass.getCompleter();

        if (completer == null) {
            return Collections.emptyList();
        }

        BukkitCommandArgs commandArgs = new BukkitCommandArgs(sender, alias, arguments);

        try {

            List<String> suggestions = completer.complete(commandArgs);

            if (suggestions == null) {
                return Collections.emptyList();
            }

            return suggestions;

        } catch (Exception exception) {

            plugin.getLogger().warning("Erro ao completar o comando /" + alias);

            exception.printStackTrace();

            return Collections.emptyList();
        }
    }

    private boolean hasValidSubcommand(Command annotation, String[] arguments) {
        String[] subcommands = annotation.subcommands();

        if (subcommands.length == 0) {
            return true;
        }

        if (arguments == null || arguments.length == 0) {
            return false;
        }

        String provided = arguments[0].trim();

        if (provided.isEmpty()) {
            return false;
        }

        for (String subcommand : subcommands) {

            if (subcommand == null) {
                continue;
            }

            if (subcommand.equalsIgnoreCase(provided)) {
                return true;
            }
        }

        return false;
    }

    private void sendUsage(BukkitCommandSender sender, Command annotation) {
        String usage = annotation.usage();

        if (usage == null || usage.isBlank()) {

            sender.sendMessage("§cUso incorreto do comando.");

            return;
        }

        sender.sendMessage("§eUso: " + usage);
    }

    private void invoke(CommandClass commandClass, BukkitCommandArgs args) throws ReflectiveOperationException {

        Method method = findCommandMethod(commandClass);

        method.setAccessible(true);

        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length == 0) {
            method.invoke(commandClass);
            return;
        }

        if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(BukkitCommandArgs.class)) {

            method.invoke(commandClass, args);

            return;
        }

        if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(CommandArgs.class)) {

            method.invoke(commandClass, args);

            return;
        }

        throw new IllegalArgumentException("Assinatura inválida para o comando: " + method.getName());
    }

    private Method findCommandMethod(CommandClass commandClass) {
        for (Method method : commandClass.getClass().getDeclaredMethods()) {

            if (method.isAnnotationPresent(Command.class)) {
                return method;
            }
        }

        throw new IllegalStateException("Nenhum método @Command encontrado em: " + commandClass.getClass().getName());
    }

    private org.bukkit.command.CommandMap findCommandMap() {
        try {

            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            field.setAccessible(true);

            return (org.bukkit.command.CommandMap) field.get(Bukkit.getServer());

        } catch (ReflectiveOperationException exception) {

            throw new IllegalStateException("Não foi possível acessar o CommandMap do Bukkit.", exception);
        }
    }
}