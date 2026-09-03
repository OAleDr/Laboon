package br.com.laboon.core.command;

import java.util.Collection;

public final class CommandLoader {

    private CommandLoader() {
    }

    public static void load(CommandFramework framework, Collection<Class<? extends CommandClass>> commandClasses, CommandProvider provider) {

        if (framework == null) {
            throw new IllegalArgumentException("O framework de comandos não pode ser nulo.");
        }

        if (commandClasses == null) {
            throw new IllegalArgumentException("A lista de comandos não pode ser nula.");
        }

        if (provider == null) {
            throw new IllegalArgumentException("O provider de comandos não pode ser nulo.");
        }

        for (Class<? extends CommandClass> commandClass : commandClasses) {

            if (commandClass == null) {
                continue;
            }

            CommandClass command = provider.create(commandClass);

            if (command == null) {
                throw new IllegalStateException("O provider não conseguiu criar o comando: " + commandClass.getName());
            }

            framework.register(command);
        }
    }
}