package net.sabio.modirena;

import net.fabricmc.api.ModInitializer;

import java.util.logging.Logger;

public class Modirena implements ModInitializer {
    public static final Logger LOGGER = Logger.getLogger(ModirenaConstants.MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("modirena is running successfully");
    }
}
