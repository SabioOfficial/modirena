package net.sabio.modirena;

public class GameManager {
    private static GameManager instance;
    private GameState state = GameState.WAITING;
    private final RoundTimer timer = new RoundTimer();
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
    public void setState(GameState state) {
        this.state = state;
        Modirena.LOGGER.info("state changed to " + state);
    }
    public void startGame() {
        if (state != GameState.WAITING) {
            Modirena.LOGGER.warning("startGame called twice but the state is " + state);
            return;
        }
        transitionToVoting();
    }
    private void transitionToVoting() {
        setState(GameState.VOTING);
        Modirena.LOGGER.info("voting phase started, 15 seconds to vote");
        timer.start(15, this::transitionToCombat);
    }
    private void transitionToCombat() {
        setState(GameState.COMBAT);
        Modirena.LOGGER.info("combat phase started. 90 seconds till end");
        timer.start(90, this::transitionToResults);
    }
    private void transitionToResults() {
        setState(GameState.RESULTS);
        Modirena.LOGGER.info("results phase started. 10 seconds to see results.");
        timer.start(10, this::transitionToVoting);
    }
}
