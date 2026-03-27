package net.sabio.modirena.modifier.modifiers;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sabio.modirena.modifier.Modifier;

public class Invisibility extends Modifier {
    public Invisibility() {
        super("invisibility", "Invisibility");
    }
    @Override
    public void onActivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }
    }
    @Override
    public void onDeactivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.removeStatusEffect(StatusEffects.INVISIBILITY);
        }
    }
}
