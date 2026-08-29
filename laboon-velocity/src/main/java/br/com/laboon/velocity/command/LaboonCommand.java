package br.com.laboon.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

public final class LaboonCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {

        invocation
                .source()
                .sendMessage(
                        Component.text(
                                "§aLaboon está funcionando!"
                        )
                );
    }
}