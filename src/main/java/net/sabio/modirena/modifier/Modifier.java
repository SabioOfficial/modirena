package net.sabio.modirena.modifier;

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
    public abstract void onActivate();
    public abstract void onDeactivate();
    @Override
    public String toString() {
        return displayName + " (" + id + ")";
    }
}
