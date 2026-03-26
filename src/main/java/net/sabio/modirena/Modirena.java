package net.sabio.modirena;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.rule.GameRules;
import net.sabio.modirena.modifier.Modifier;
import net.sabio.modirena.modifier.ModifierRegistry;

import java.util.logging.Logger;

public class Modirena implements ModInitializer {
    public static final Logger LOGGER = Logger.getLogger(ModirenaConstants.MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("modirena is running successfully");
        GameManager.getInstance();
        LOGGER.info("gamemanager ready. state " + GameManager.getInstance().getState());
        ModifierRegistry.registerAll();
        LOGGER.info("registered modifiers " + ModifierRegistry.getAll().size());
        ModirenaCommands.register();
        LOGGER.info("registered commands");
        PlayerManager.getInstance();
        LOGGER.info("playermanager ready.");
        PlayerBlockBreakEvents.BEFORE.register((world, player, position, state, entity) -> false);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            GameManager.getInstance().setServer(server);
            ConfigManager.load();
            if (!ConfigManager.isSetUp()) {
                LOGGER.info("first boot detected, so im placing structures now");
                StructureLoader.placeAll(server);
                ConfigManager.markSetUp();
            } else {
                LOGGER.info("structures have already been placed");
            }
            ServerWorld overworld = server.getOverworld();
            overworld.getGameRules().setValue(GameRules.ADVANCE_TIME, false, server);
            overworld.setTimeOfDay(6000);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            ServerWorld overworld = server.getOverworld();
            player.teleport(overworld, 10.0, 65.0, 9.0, java.util.Set.of(), 0.0f, 0.0f, false);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (GameManager.getInstance().getState() != GameState.WAITING) {
                for (Modifier modifier : GameManager.getInstance().getActiveModifiers()) {
                    modifier.onDeactivate(server);
                }
                GameManager.getInstance().getActiveModifiers().clear();
                GameManager.getInstance().setState(GameState.WAITING);
                GameManager.getInstance().getTimer().stop();
            }
        });
    }
}
