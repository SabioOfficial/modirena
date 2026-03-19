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
}
