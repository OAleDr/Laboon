package br.com.laboon.core.friend;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountManager;
import br.com.laboon.core.redis.RedisManager;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class FriendManager {

    private final FriendRepository repository;
    private final AccountManager accountManager;

    public FriendManager(RedisManager redisManager, AccountManager accountManager) {

        if (redisManager == null) {
            throw new IllegalArgumentException("RedisManager não pode ser nulo.");
        }
        if (accountManager == null) {
            throw new IllegalArgumentException("AccountManager não pode ser nulo.");
        }

        this.repository = new FriendRepository(redisManager);

        this.accountManager = accountManager;
    }

    /*
     * =========================
     * AMIGOS
     * =========================
     */

    public Set<UUID> getFriends(UUID uniqueId) {
        return repository.getFriends(uniqueId);
    }

    public boolean isFriend(UUID first, UUID second) {
        return repository.isFriend(first, second);
    }

    /*
     * =========================
     * SOLICITAR
     * =========================
     */

    public void sendRequest(UUID sender, UUID receiver) {

        validatePlayers(sender, receiver);

        if (sender.equals(receiver)) {
            throw new IllegalStateException("Você não pode adicionar a si mesmo.");
        }

        if (isFriend(sender, receiver)) {
            throw new IllegalStateException("Vocês já são amigos.");
        }

        if (repository.hasRequest(receiver, sender)) {

            throw new IllegalStateException("Você já enviou uma solicitação para esse jogador.");
        }

        if (repository.hasRequest(sender, receiver)) {

            throw new IllegalStateException("Esse jogador já enviou uma solicitação para você.");
        }

        Account account = accountManager.get(receiver);

        if (account == null) {
            throw new IllegalStateException("Conta do jogador não encontrada.");
        }

        if (!account.getPreferences().isFriendRequests()) {

            throw new IllegalStateException("Esse jogador não aceita solicitações de amizade.");
        }

        FriendRequest request = new FriendRequest(sender, receiver, Instant.now());

        repository.saveRequest(request);
    }

    /*
     * =========================
     * ACEITAR
     * =========================
     */

    public void acceptRequest(UUID receiver, UUID sender) {

        FriendRequest request = repository.getRequest(receiver, sender);

        if (request == null) {
            throw new IllegalStateException("Solicitação de amizade não encontrada.");
        }

        if (isFriend(receiver, sender)) {

            repository.removeRequest(receiver, sender);

            return;
        }

        repository.addFriend(receiver, sender);

        repository.removeRequest(receiver, sender);
    }

    /*
     * =========================
     * NEGAR
     * =========================
     */

    public void denyRequest(UUID receiver, UUID sender) {

        if (!repository.hasRequest(receiver, sender)) {

            throw new IllegalStateException("Solicitação de amizade não encontrada.");
        }

        repository.removeRequest(receiver, sender);
    }

    /*
     * =========================
     * CANCELAR
     * =========================
     */

    public void cancelRequest(UUID sender, UUID receiver) {

        if (!repository.hasRequest(receiver, sender)) {

            throw new IllegalStateException("Solicitação de amizade não encontrada.");
        }

        repository.removeRequest(receiver, sender);
    }

    /*
     * =========================
     * REMOVER
     * =========================
     */

    public void removeFriend(UUID remover, UUID target) {

        if (!isFriend(remover, target)) {

            throw new IllegalStateException("Esse jogador não está na sua lista de amigos.");
        }

        repository.removeFriend(remover, target);
    }

    /*
     * =========================
     * PEDIDOS
     * =========================
     */

    public List<FriendRequest> getRequests(UUID uniqueId) {

        return repository.getRequests(uniqueId);
    }

    public List<FriendRequest> getOutgoingRequests(UUID uniqueId) {

        return repository.getOutgoingRequests(uniqueId);
    }

    /*
     * =========================
     * REPOSITORY
     * =========================
     */

    public FriendRepository getRepository() {
        return repository;
    }

    private void validatePlayers(UUID first, UUID second) {

        if (first == null || second == null) {

            throw new IllegalArgumentException("UUID não pode ser nulo.");
        }
    }
}