package br.com.laboon.core.command;

public interface CommandClass {

    default Completer getCompleter() {
        return null;
    }
}