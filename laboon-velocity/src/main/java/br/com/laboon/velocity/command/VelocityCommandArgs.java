package br.com.laboon.velocity.command;

import br.com.laboon.core.command.CommandArgs;
import com.velocitypowered.api.command.CommandSource;

public final class VelocityCommandArgs extends CommandArgs {

    private final CommandSource source;
    private final VelocityCommandSender sender;

    public VelocityCommandArgs(
            CommandSource source,
            String label,
            String[] arguments
    ) {
        super(label, arguments);

        if (source == null) {
            throw new IllegalArgumentException(
                    "CommandSource não pode ser nulo."
            );
        }

        this.source = source;
        this.sender = new VelocityCommandSender(source);
    }

    public CommandSource getSource() {
        return source;
    }

    public VelocityCommandSender getSender() {
        return sender;
    }

    public boolean isPlayer() {
        return sender.isPlayer();
    }
}