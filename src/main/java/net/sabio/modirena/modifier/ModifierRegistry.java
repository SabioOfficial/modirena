package net.sabio.modirena.modifier;

import net.sabio.modirena.modifier.modifiers.GlassCannon;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModifierRegistry {
    private static final Map<String, Modifier> MODIFIERS = new LinkedHashMap<>();
    public static void registerAll() {
        register(new GlassCannon());
    }
    private static void register(Modifier modifier) {
        MODIFIERS.put(modifier.getId(), modifier);
    }
    public static Modifier get(String id) {
        return MODIFIERS.get(id);
    }
    public static Collection<Modifier> getAll() {
        return MODIFIERS.values();
    }
}
