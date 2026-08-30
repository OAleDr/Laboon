package br.com.laboon.velocity.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public final class ClickableMessage {

    private final Component component;

    private ClickableMessage(
            Component component
    ) {
        this.component = component;
    }

    public static ClickableMessage text(
            String text
    ) {

        return new ClickableMessage(
                Component.text(text)
        );
    }

    public ClickableMessage clickCommand(
            String command
    ) {

        return new ClickableMessage(
                component.clickEvent(
                        ClickEvent.runCommand(command)
                )
        );
    }

    public ClickableMessage suggestCommand(
            String command
    ) {

        return new ClickableMessage(
                component.clickEvent(
                        ClickEvent.suggestCommand(command)
                )
        );
    }

    public ClickableMessage hover(
            String text
    ) {

        return new ClickableMessage(
                component.hoverEvent(
                        HoverEvent.showText(
                                Component.text(text)
                        )
                )
        );
    }

    public Component build() {

        return component;
    }
}