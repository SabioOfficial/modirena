package net.sabio.modirena.modifier.modifiers;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sabio.modirena.Modirena;
import net.sabio.modirena.modifier.Modifier;

public class LowGravity extends Modifier {
    public LowGravity() {
        super("low_gravity", "Low Gravity");
    }
    @Override
    public void onActivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, Integer.MAX_VALUE, 2, false, false));
        }
        Modirena.LOGGER.info("low gravity activated");
    }
    @Override
    public void onDeactivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.removeStatusEffect(StatusEffects.SLOW_FALLING);
            player.removeStatusEffect(StatusEffects.JUMP_BOOST);
        }
    }
}
