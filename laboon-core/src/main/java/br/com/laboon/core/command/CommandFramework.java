package br.com.laboon.core.command;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommandFramework {

    private final Map<String, RegisteredCommand> commands = new ConcurrentHashMap<>();

    public void register(CommandClass commandClass) {

        if (commandClass == null) {
            throw new IllegalArgumentException("A classe de comando não pode ser nula.");
        }

        Class<?> type = commandClass.getClass();

        for (Method method : type.getDeclaredMethods()) {

            Command annotation = method.getAnnotation(Command.class);

            if (annotation == null) {
                continue;
            }

            registerMethod(commandClass, method, annotation);
        }
    }

    protected void registerMethod(CommandClass commandClass, Method method, Command annotation) {

        String name = normalize(annotation.name());

        if (name.isEmpty()) {
            throw new IllegalArgumentException("O nome do comando não pode ser vazio.");
        }

        RegisteredCommand command = new RegisteredCommand(commandClass, method, annotation);

        RegisteredCommand previous = commands.putIfAbsent(name, command);

        if (previous != null) {
            throw new IllegalStateException("O comando já está registrado: " + name);
        }

        for (String alias : annotation.aliases()) {

            String normalizedAlias = normalize(alias);

            if (normalizedAlias.isEmpty()) {
                continue;
            }

            commands.putIfAbsent(normalizedAlias, command);
        }
    }

    public RegisteredCommand get(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        return commands.get(normalize(name));
    }

    public boolean contains(String name) {

        return get(name) != null;
    }

    public int size() {

        return commands.size();
    }

    protected Map<String, RegisteredCommand> getCommands() {

        return commands;
    }

    private String normalize(String value) {

        return value.trim().toLowerCase();
    }

    public record RegisteredCommand(CommandClass commandClass, Method method, Command annotation) {
    }
}