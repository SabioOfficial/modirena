package net.sabio.modirena;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sabio.modirena.modifier.Modifier;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private static GameManager instance;
    private GameState state = GameState.WAITING;
    private final RoundTimer timer = new RoundTimer();
    private final List<Modifier> activeModifiers = new ArrayList<>();
    private MinecraftServer server;
    private int currentRound = 0;
    private static final int ROUNDS_PER_GAME = 5;
    private GameManager() {}
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    public int getCurrentRound() {
        return currentRound;
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
        currentRound = 1;
        activeModifiers.clear();
        transitionToVoting();
    }
    private void transitionToVoting() {
        if (PlayerManager.getInstance().getPlayerCount() < 2) {
            setState(GameState.WAITING);
            currentRound = 0;
            activeModifiers.clear();
            return;
        }
        setState(GameState.VOTING);
        VoteManager.getInstance().startVote(3);
        server.execute(() -> VoteManager.getInstance().giveVoteItems(server));
        Modirena.LOGGER.info("voting phase started, 15 seconds to vote");
        timer.start(15, this::transitionToCombat);
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
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ArenaManager.getInstance().sendPlayerToLobby(player);
        }
        if (currentRound < ROUNDS_PER_GAME) {
            currentRound++;
            timer.start(10, this::transitionToVoting);
        } else {
            timer.start(10, this::gameFinished);
        }
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
    private void gameFinished() {
        currentRound = 0;
        activeModifiers.clear();
        setState(GameState.WAITING);
    }
}
