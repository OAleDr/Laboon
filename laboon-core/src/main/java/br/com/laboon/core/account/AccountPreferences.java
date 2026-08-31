package br.com.laboon.core.account;

import br.com.laboon.core.language.LanguageLocale;

public final class AccountPreferences {

    private LanguageLocale language;

    private boolean privateMessages;
    private boolean friendRequests;
    private boolean serverJoinMessages;

    public AccountPreferences() {

        this.language = LanguageLocale.ptBR();

        this.privateMessages = true;
        this.friendRequests = true;
        this.serverJoinMessages = true;
    }

    public AccountPreferences(LanguageLocale language, boolean privateMessages, boolean friendRequests, boolean serverJoinMessages) {

        this.language = language;

        this.privateMessages = privateMessages;

        this.friendRequests = friendRequests;

        this.serverJoinMessages = serverJoinMessages;
    }

    public LanguageLocale getLanguage() {
        return language;
    }

    public boolean isPrivateMessages() {
        return privateMessages;
    }

    public boolean isFriendRequests() {
        return friendRequests;
    }

    public boolean isServerJoinMessages() {
        return serverJoinMessages;
    }

    public void setLanguage(LanguageLocale language) {

        this.language = language;
    }

    public void setPrivateMessages(boolean privateMessages) {

        this.privateMessages = privateMessages;
    }

    public void setFriendRequests(boolean friendRequests) {

        this.friendRequests = friendRequests;
    }

    public void setServerJoinMessages(boolean serverJoinMessages) {

        this.serverJoinMessages = serverJoinMessages;
    }
}