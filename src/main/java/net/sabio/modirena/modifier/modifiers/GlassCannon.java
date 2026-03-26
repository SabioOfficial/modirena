package net.sabio.modirena.modifier.modifiers;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sabio.modirena.Modirena;
import net.sabio.modirena.modifier.Modifier;

public class GlassCannon extends Modifier {
    private static final Identifier DAMAGE_ID = Identifier.of("modirena", "glass_cannon_damage");
    private static final Identifier HEALTH_ID = Identifier.of("modirena", "glass_cannon_health");
    public GlassCannon() {
        super("glass_cannon", "Glass Cannon");
    }
    @Override
    public void onActivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            var damageAttribute = player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
            if (damageAttribute != null) {
                damageAttribute.removeModifier(DAMAGE_ID);
                damageAttribute.addPersistentModifier(new EntityAttributeModifier(
                        DAMAGE_ID, 0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
            var healthAttribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.removeModifier(HEALTH_ID);
                healthAttribute.addPersistentModifier(new EntityAttributeModifier(
                        HEALTH_ID, -0.7, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }
    @Override
    public void onDeactivate(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            var damageAttribute = player.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
            if (damageAttribute != null) damageAttribute.removeModifier(DAMAGE_ID);
            var healthAttribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (healthAttribute != null) healthAttribute.removeModifier(HEALTH_ID);
        }
    }
}
