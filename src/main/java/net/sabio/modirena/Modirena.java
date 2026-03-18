package net.sabio.modirena;

import net.fabricmc.api.ModInitializer;
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
    }
}
