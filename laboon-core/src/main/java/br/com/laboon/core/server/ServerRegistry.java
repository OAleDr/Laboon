package br.com.laboon.core.server;

import br.com.laboon.core.redis.RedisManager;

import java.util.List;

public final class ServerRegistry {

    private final ServerRepository repository;

    public ServerRegistry(RedisManager redisManager) {
        this.repository = new ServerRepository(redisManager);
    }

    public void register(ServerInfo server) {
        repository.save(server);
    }

    public ServerInfo find(String name) {
        return repository.findByName(name);
    }

    public List<ServerInfo> findByType(ServerType type) {
        return repository.findByType(type);
    }

    public ServerInfo findAvailable(ServerType type) {
        return repository.findAvailable(type);
    }

    public List<ServerInfo> findAll() {
        return repository.findAll();
    }

    public void unregister(String name) {
        repository.delete(name);
    }
}