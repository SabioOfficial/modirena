package net.sabio.modirena.modifier.modifiers;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sabio.modirena.modifier.Modifier;

public class Giant extends Modifier {
    public Giant() {
        super("giant", "Giant");
    }
    @Override
    public void onActivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, Integer.MAX_VALUE, 1, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, Integer.MAX_VALUE, 1, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, Integer.MAX_VALUE, 3, false, false));
        }
    }
    @Override
    public void onDeactivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.removeStatusEffect(StatusEffects.STRENGTH);
            player.removeStatusEffect(StatusEffects.SLOWNESS);
            player.removeStatusEffect(StatusEffects.JUMP_BOOST);
        }
    }
}
