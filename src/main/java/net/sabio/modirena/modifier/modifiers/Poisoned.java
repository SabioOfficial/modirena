package net.sabio.modirena.modifier.modifiers;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sabio.modirena.Modirena;
import net.sabio.modirena.modifier.Modifier;

public class Poisoned extends Modifier {
    public Poisoned() {
        super("poisoned", "Poisoned");
    }
    @Override
    public void onActivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, Integer.MAX_VALUE, 0, false, false));
        }
        Modirena.LOGGER.info("poisoned activated");
    }
    @Override
    public void onDeactivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.removeStatusEffect(StatusEffects.POISON);
        }
    }
}
