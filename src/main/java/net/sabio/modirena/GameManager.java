package net.sabio.modirena;

import net.minecraft.server.MinecraftServer;
import net.sabio.modirena.modifier.Modifier;

import java.util.ArrayList;
import java.util.List;

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
        activeModifiers.clear();
        transitionToVoting();
    }
    private void transitionToVoting() {
        setState(GameState.VOTING);
        VoteManager.getInstance().startVote(3);
        Modirena.LOGGER.info("voting phase started, 15 seconds to vote");
        timer.start(15, this::transitionToCombat);
    }
    private void transitionToCombat() {
        if (server == null) {
            setState(GameState.WAITING);
            return;
        }
        Modifier winner = VoteManager.getInstance().tallyVotes();
        if (winner != null) {
            activeModifiers.add(winner);
            winner.onActivate(server);
            Modirena.LOGGER.info(winner.getDisplayName() + " is the modifier for this round");
        }
        setState(GameState.COMBAT);
        Modirena.LOGGER.info("combat phase started. 90 seconds till end");
        timer.start(90, this::transitionToResults);
    }
    private void transitionToResults() {
        for (Modifier modifier : activeModifiers) {
            modifier.onDeactivate(server);
        }
        setState(GameState.RESULTS);
        Modirena.LOGGER.info("results phase started. 10 seconds to see results. active modifiers: " + activeModifiers);
        activeModifiers.clear();
        timer.start(10, this::transitionToVoting);
    }
}
