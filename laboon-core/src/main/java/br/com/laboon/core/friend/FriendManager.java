package br.com.laboon.core.friend;

import br.com.laboon.core.redis.RedisManager;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class FriendManager {

    private final FriendRepository repository;

    public FriendManager(RedisManager redisManager) {

        if (redisManager == null) {
            throw new IllegalArgumentException("RedisManager não pode ser nulo.");
        }

        this.repository = new FriendRepository(redisManager);
    }

    /*
     * =========================
     * AMIGOS
     * =========================
     */

    public Set<UUID> getFriends(UUID uniqueId) {

        if (uniqueId == null) {
            return Set.of();
        }

        return repository.getFriends(uniqueId);
    }

    public List<Friend> getFriendDetails(UUID uniqueId) {

        if (uniqueId == null) {
            return List.of();
        }

        return repository.getFriendDetails(uniqueId);
    }

    public Friend getFriend(UUID owner, UUID friendId) {

        if (owner == null || friendId == null) {
            return null;
        }

        return repository.getFriend(owner, friendId);
    }

    public boolean isFriend(UUID first, UUID second) {

        return repository.isFriend(first, second);
    }

    public void addFriend(UUID first, UUID second) {

        if (first == null || second == null) {
            return;
        }

        if (first.equals(second)) {
            return;
        }

        repository.addFriend(first, second);
    }

    public void removeFriend(UUID first, UUID second) {

        if (first == null || second == null) {
            return;
        }

        repository.removeFriend(first, second);
    }

    /*
     * =========================
     * SOLICITAÇÕES
     * =========================
     */

    public boolean sendRequest(UUID sender, UUID receiver) {

        if (sender == null || receiver == null) {
            return false;
        }

        if (sender.equals(receiver)) {
            return false;
        }

        if (isFriend(sender, receiver)) {
            return false;
        }

        if (repository.hasRequest(receiver, sender)) {
            return false;
        }

        if (repository.hasRequest(sender, receiver)) {
            return false;
        }

        FriendRequest request = new FriendRequest(sender, receiver, Instant.now());

        repository.saveRequest(request);

        return true;
    }

    public boolean hasRequest(UUID receiver, UUID sender) {

        return repository.hasRequest(receiver, sender);
    }

    public List<FriendRequest> getRequests(UUID receiver) {

        return repository.getRequests(receiver);
    }

    public List<FriendRequest> getOutgoingRequests(UUID sender) {

        return repository.getOutgoingRequests(sender);
    }

    public boolean acceptRequest(UUID receiver, UUID sender) {

        if (receiver == null || sender == null) {
            return false;
        }

        FriendRequest request = repository.getRequest(receiver, sender);

        if (request == null) {
            return false;
        }

        if (isFriend(receiver, sender)) {

            repository.removeRequest(receiver, sender);

            return false;
        }

        repository.addFriend(receiver, sender);

        repository.removeRequest(receiver, sender);

        return true;
    }

    public boolean denyRequest(UUID receiver, UUID sender) {

        if (receiver == null || sender == null) {
            return false;
        }

        if (!repository.hasRequest(receiver, sender)) {
            return false;
        }

        repository.removeRequest(receiver, sender);

        return true;
    }

    public boolean cancelRequest(UUID sender, UUID receiver) {

        if (sender == null || receiver == null) {
            return false;
        }

        if (!repository.hasRequest(receiver, sender)) {
            return false;
        }

        repository.removeRequest(receiver, sender);

        return true;
    }

    public FriendRepository getRepository() {
        return repository;
    }
}