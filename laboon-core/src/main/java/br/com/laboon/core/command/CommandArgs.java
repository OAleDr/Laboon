package br.com.laboon.core.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandArgs {

    private final String label;
    private final List<String> arguments;

    public CommandArgs(String label, String[] arguments) {
        this.label = label;

        this.arguments = arguments == null ? Collections.emptyList() : List.copyOf(Arrays.asList(arguments));
    }

    public String getLabel() {
        return label;
    }

    public int size() {
        return arguments.size();
    }

    public boolean isEmpty() {
        return arguments.isEmpty();
    }

    public String getSubcommand() {
        String subcommand = get(0);

        if (subcommand == null) {
            return null;
        }

        return subcommand.toLowerCase();
    }

    public String get(int index) {
        if (index < 0 || index >= arguments.size()) {
            return null;
        }

        return arguments.get(index);
    }

    public String getArgument(int index) {
        return get(index + 1);
    }

    public String getRequired(int index) {
        String value = get(index);

        if (value == null) {
            throw new IllegalArgumentException("Argumento obrigatório não informado: " + index);
        }

        return value;
    }

    public String getRequiredArgument(int index) {
        return getRequired(index + 1);
    }

    public String getOrDefault(int index, String defaultValue) {
        String value = get(index);

        return value == null ? defaultValue : value;
    }

    public String getArgumentOrDefault(int index, String defaultValue) {
        return getOrDefault(index + 1, defaultValue);
    }

    public int getInt(int index) {
        return Integer.parseInt(getRequired(index));
    }

    public int getArgumentInt(int index) {
        return Integer.parseInt(getRequiredArgument(index));
    }

    public long getLong(int index) {
        return Long.parseLong(getRequired(index));
    }

    public long getArgumentLong(int index) {
        return Long.parseLong(getRequiredArgument(index));
    }

    public double getDouble(int index) {
        return Double.parseDouble(getRequired(index));
    }

    public double getArgumentDouble(int index) {
        return Double.parseDouble(getRequiredArgument(index));
    }

    public boolean getBoolean(int index) {
        return Boolean.parseBoolean(getRequired(index));
    }

    public boolean getArgumentBoolean(int index) {
        return Boolean.parseBoolean(getRequiredArgument(index));
    }

    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "CommandArgs{" + "label='" + label + '\'' + ", arguments=" + arguments + '}';
    }
}