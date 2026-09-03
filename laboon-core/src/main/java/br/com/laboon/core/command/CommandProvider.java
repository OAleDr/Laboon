package br.com.laboon.core.command;

@FunctionalInterface
public interface CommandProvider {

    CommandClass create(Class<? extends CommandClass> commandClass);
}