package net.sabio.modirena;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sabio.modirena.modifier.Modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameManager {
    private static GameManager instance;
    private GameState state = GameState.WAITING;
    private final RoundTimer timer = new RoundTimer();
    private final List<Modifier> activeModifiers = new ArrayList<>();
    private MinecraftServer server;
    private GameManager() {}
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    public GameState getState() {
        return state;
    }
    public RoundTimer getTimer() {
        return timer;
    }
    public MinecraftServer getServer() {
        return server;
    }
    public void setServer(MinecraftServer server) {
        this.server = server;
    }
    public List<Modifier> getActiveModifiers() {
        return activeModifiers;
    }
    public void setState(GameState state) {
        this.state = state;
        Modirena.LOGGER.info("state changed to " + state);
    }
    public void startGame() {
        if (state != GameState.WAITING) {
            Modirena.LOGGER.warning("startGame called twice but the state is " + state);
            return;
        }
        if (PlayerManager.getInstance().getPlayerCount() < 2) {
            Modirena.LOGGER.warning("not enough players to start game");
            return;
        }
        activeModifiers.clear();
        transitionToVoting();
    }
    private void transitionToVoting() {
        setState(GameState.VOTING);
        VoteManager.getInstance().startVote(3);
        Modirena.LOGGER.info("voting phase started, 15 seconds to vote");
        timer.start(15, this::transitionToCombat);
        server.execute(() -> VoteManager.getInstance().giveVoteItems(server));
    }
    private void transitionToCombat() {
        if (server == null) {
            setState(GameState.WAITING);
            return;
        }
        VoteManager.getInstance().clearVoteItems(server);
        Modifier winner = VoteManager.getInstance().tallyVotes();
        if (winner != null) {
            activeModifiers.add(winner);
            Modirena.LOGGER.info(winner.getDisplayName() + " is the modifier for this round");
        }
        setState(GameState.COMBAT);
        ArenaManager.getInstance().sendPlayersToArena(server);
        Modirena.LOGGER.info("combat phase started. 90 seconds till end");
        timer.start(90, this::transitionToResults);
    }
    private void transitionToResults() {
        PlayerManager.getInstance().clearAllDeathPositions();
        for (Modifier modifier : activeModifiers) {
            modifier.onDeactivate(server);
        }
        setState(GameState.RESULTS);
        ArenaManager.getInstance().resetArena(server);
        activeModifiers.clear();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ArenaManager.getInstance().sendPlayerToLobby(player);
        }
        timer.start(10, this::transitionToVoting);
    }
    public void checkRoundEnd() {
        if (state != GameState.COMBAT) return;
        List<ServerPlayerEntity> alive = server.getPlayerManager().getPlayerList().stream()
                .filter(player -> PlayerManager.getInstance().getState(player) == PlayerState.ALIVE)
                .toList();
        if (alive.size() <= 1) {
            timer.stop();
            transitionToResults();
        }
    }
}
