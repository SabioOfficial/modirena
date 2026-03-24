package net.sabio.modirena;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModirenaCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("modirena")
                    .then(CommandManager.literal("start")
                            .executes(ModirenaCommands::startGame))
                    .then(CommandManager.literal("join")
                            .executes(ModirenaCommands::joinGame))
                    .then(CommandManager.literal("leave")
                            .executes(ModirenaCommands::leaveGame))
            );
        });
    }
    private static int startGame(CommandContext<ServerCommandSource> context) {
        GameManager.getInstance().startGame();
        return 1;
    }
    private static int joinGame(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        PlayerManager.getInstance().addPlayer(player);
        return 1;
    }
    private static int leaveGame(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        PlayerManager.getInstance().removePlayer(player);
        return 1;
    }
}
