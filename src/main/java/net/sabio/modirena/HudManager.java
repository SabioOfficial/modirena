package net.sabio.modirena;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.sabio.modirena.modifier.Modifier;

import java.util.List;
import java.util.stream.Collectors;

public class HudManager {
    public static Text buildActionBar(MinecraftServer server) {
        GameState state = GameManager.getInstance().getState();
        int seconds = GameManager.getInstance().getTimer().getSecondsRemaining();
        int round = GameManager.getInstance().getCurrentRound();
        int alive = (int) server.getPlayerManager().getPlayerList().stream()
                .filter(player -> PlayerManager.getInstance().getState(player) == PlayerState.ALIVE)
                .count();
        List<Modifier> active = GameManager.getInstance().getActiveModifiers();
        String modifiers = active.isEmpty() ? "None" : active.stream()
                .map(Modifier::getDisplayName)
                .collect(Collectors.joining(", "));
        return switch (state) {
            case WAITING -> Text.literal("Waiting for players...")
                    .formatted(Formatting.GRAY);
            case VOTING -> Text.literal("Round " + round + "/5")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal("  |  ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal("VOTE NOW").formatted(Formatting.GREEN))
                    .append(Text.literal("  |  ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(seconds + "s").formatted(Formatting.WHITE))
                    .append(Text.literal("  |  Active: " + modifiers).formatted(Formatting.AQUA));
            case COMBAT -> Text.literal("Round " + round + "/5")
                    .formatted(Formatting.RED)
                    .append(Text.literal("  |  ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(alive + " alive").formatted(Formatting.WHITE))
                    .append(Text.literal("  |  ").formatted(Formatting.DARK_GRAY))
                    .append(Text.literal(seconds + "s").formatted(Formatting.YELLOW))
                    .append(Text.literal("  |  " + modifiers).formatted(Formatting.AQUA));
            case RESULTS -> Text.literal("Intermission")
                    .formatted(Formatting.AQUA)
                    .append(Text.literal("  |  Next round in ").formatted(Formatting.WHITE))
                    .append(Text.literal(seconds + "s").formatted(Formatting.YELLOW));
        };
    }
}
