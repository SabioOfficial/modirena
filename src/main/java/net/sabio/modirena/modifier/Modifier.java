package net.sabio.modirena.modifier;

import net.minecraft.server.MinecraftServer;

public abstract class Modifier {
    private final String id;
    private final String displayName;
    public Modifier(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }
    public String getId() {
        return id;
    }
    public String getDisplayName() {
        return displayName;
    }
    public abstract void onActivate(MinecraftServer server);
    public abstract void onDeactivate(MinecraftServer server);
    @Override
    public String toString() {
        return displayName + " (" + id + ")";
    }
}
