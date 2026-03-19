package net.sabio.modirena;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ModirenaCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("modirena")
                    .then(CommandManager.literal("start")
                            .executes(ModirenaCommands::startGame))
            );
        });
    }
    private static int startGame(CommandContext<ServerCommandSource> context) {
        GameManager.getInstance().startGame();
        return 1;
    }
}
