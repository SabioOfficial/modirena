package net.sabio.modirena;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.sabio.modirena.modifier.Modifier;

import java.util.*;
import java.util.stream.Collectors;

public class GameManager {
    private static GameManager instance;
    private GameState state = GameState.WAITING;
    private final RoundTimer timer = new RoundTimer();
    private final List<Modifier> activeModifiers = new ArrayList<>();
    private MinecraftServer server;
    private int currentRound = 0;
    private static final int ROUNDS_PER_GAME = 5;
    private final Map<UUID, Integer> wins = new HashMap<>();

    private GameManager() {
    }

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
        recordRoundWinner();
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
        List<ServerPlayerEntity> allPlayers = server.getPlayerManager().getPlayerList();
        List<ServerPlayerEntity> sorted = allPlayers.stream()
                .sorted((a, b) -> wins.getOrDefault(b.getUuid(), 0) - wins.getOrDefault(a.getUuid(), 0))
                .toList();
        String[] prefix = {"#1", "#2", "#3"};
        String modifierNames = activeModifiers.isEmpty() ? "None" : activeModifiers.stream().map(Modifier::getDisplayName)
                .collect(Collectors.joining(", "));
        for (ServerPlayerEntity player : allPlayers) {
            player.sendMessage(Text.literal("§8§m                    "), false);
            player.sendMessage(Text.literal("§e§lGAME RESULTS"), false);
            player.sendMessage(Text.literal("§8§m                    "), false);
            for (int i = 0; i < sorted.size(); i++) {
                String medal = i < prefix.length ? prefix[i] : "§f  ";
                int w = wins.getOrDefault(sorted.get(i).getUuid(), 0);
                player.sendMessage(Text.literal(medal + " §f" + sorted.get(i).getName().getString() + " §7- " + w + " win" + (w == 1 ? "" : "s")), false);
            }
            player.sendMessage(Text.literal("§8§m                    "), false);
            player.sendMessage(Text.literal("§7Modifiers this game: §b" + modifierNames), false);
            player.sendMessage(Text.literal("The next game will begin momentarily"), false);
            player.sendMessage(Text.literal("§8§m                    "), false);
        }
        currentRound = 0;
        wins.clear();
        activeModifiers.clear();
        setState(GameState.WAITING);
        if (PlayerManager.getInstance().getPlayerCount() >= 2) {
            timer.start(20, this::startGameFromTimer);
        }
    }
    private void recordRoundWinner() {
        List<ServerPlayerEntity> alive = server.getPlayerManager().getPlayerList().stream()
                .filter(player -> PlayerManager.getInstance().getState(player) == PlayerState.ALIVE)
                .toList();
        if (alive.size() == 1) {
            UUID winner = alive.getFirst().getUuid();
            wins.merge(winner, 1, Integer::sum);
        }
    }
    private void startGameFromTimer() {
        if (PlayerManager.getInstance().getPlayerCount() >= 2) {
            currentRound = 1;
            transitionToVoting();
        }
    }
}