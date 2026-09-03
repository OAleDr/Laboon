package br.com.laboon.core.command;

import java.util.List;

@FunctionalInterface
public interface Completer {

    List<String> complete(CommandArgs args);
}