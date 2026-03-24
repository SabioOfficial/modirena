package net.sabio.modirena;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
