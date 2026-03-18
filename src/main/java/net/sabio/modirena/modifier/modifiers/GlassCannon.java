package net.sabio.modirena.modifier.modifiers;

import net.sabio.modirena.Modirena;
import net.sabio.modirena.modifier.Modifier;

public class GlassCannon extends Modifier {
    public GlassCannon() {
        super("glass_cannon", "Glass Cannon");
    }
    @Override
    public void onActivate() {
        Modirena.LOGGER.info("glass cannon activated");
    }
    @Override
    public void onDeactivate() {
        Modirena.LOGGER.info("glass cannon deactivated");
    }
}
