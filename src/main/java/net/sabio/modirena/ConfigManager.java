package net.sabio.modirena;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Path CONFIG_PATH = Path.of("config/modirena.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static JsonObject config = new JsonObject();
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                config = GSON.fromJson(reader, JsonObject.class);
                Modirena.LOGGER.info("config loaded");
            } catch (IOException exception) {
                Modirena.LOGGER.warning("failed to load config " + exception.getMessage());
            }
        } else {
            config = new JsonObject();
        }
    }
    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            Modirena.LOGGER.warning("failed to save config " + exception.getMessage());
        }
    }
    public static boolean isSetUp() {
        return config.has("setup") && config.get("setup").getAsBoolean();
    }
    public static void markSetUp() {
        config.addProperty("setup", true);
        save();
    }
}
