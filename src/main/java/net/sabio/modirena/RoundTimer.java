package net.sabio.modirena;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class RoundTimer {
    private int ticksRemaining = 0;
    private boolean running = false;
    private Runnable onComplete;
    public RoundTimer() {
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }
    private void tick(MinecraftServer server) {
        if (!running) return;
        ticksRemaining--;
        if (ticksRemaining <= 0) {
            running = false;
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
    public void start(int seconds, Runnable onComplete) {
        this.ticksRemaining = seconds * 20;
        this.onComplete = onComplete;
        this.running = true;
        Modirena.LOGGER.info("round timer started " + seconds);
    }
    public void stop() {
        running = false;
    }
    public int getSecondsRemaining() {
        return ticksRemaining / 20;
    }
    public boolean isRunning() {
        return running;
    }
}
