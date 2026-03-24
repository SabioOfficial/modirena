package net.sabio.modirena;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager {
    private static PlayerManager instance;
    private final Map<UUID, PlayerState> players = new HashMap<>();
    private PlayerManager() {}
    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }
    public void addPlayer(ServerPlayerEntity player) {
        players.put(player.getUuid(), PlayerState.LOBBY);
        Modirena.LOGGER.info(player.getName().getString() + " added to modirena");
    }
    public void removePlayer(ServerPlayerEntity player) {
        players.remove(player.getUuid());
        Modirena.LOGGER.info(player.getName().getString() + " removed from modirena");
    }
    public void setState(ServerPlayerEntity player, PlayerState state) {
        if (!players.containsKey(player.getUuid())) return;
        players.put(player.getUuid(), state);
        Modirena.LOGGER.info(player.getName().getString() + " state changed to " + state);
    }
    public PlayerState getState(ServerPlayerEntity player) {
        return players.getOrDefault(player.getUuid(), null);
    }
    public boolean isInGame(ServerPlayerEntity player) {
        return players.containsKey(player.getUuid());
    }
    public Collection<ServerPlayerEntity> getAlivePlayers(MinecraftServer server) {
        return players.entrySet().stream()
                .map(uuid -> server.getPlayerManager().getPlayer(uuid.toString()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    public int getPlayerCount() {
        return players.size();
    }
    public void clear() {
        players.clear();
    }
}
