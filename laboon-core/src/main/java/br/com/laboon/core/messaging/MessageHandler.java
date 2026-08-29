package br.com.laboon.core.messaging;

@FunctionalInterface
public interface MessageHandler {

    void handle(String channel, String message);
}