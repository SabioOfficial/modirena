package net.sabio.modirena;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.sabio.modirena.modifier.Modifier;
import net.sabio.modirena.modifier.ModifierRegistry;

import java.util.*;

public class VoteManager {
    private static VoteManager instance;
    private final Map<UUID, String> votes = new HashMap<>();
    private final Map<UUID, Integer> lastSlot = new HashMap<>();
    private List<Modifier> currentOptions = new ArrayList<>();
    private VoteManager() {}
    public static VoteManager getInstance() {
        if (instance == null) instance = new VoteManager();
        return instance;
    }
    public void startVote(int optionCount) {
        votes.clear();
        lastSlot.clear();
        currentOptions = pickRandom(optionCount);
        Modirena.LOGGER.info("vote started with " + currentOptions);
    }
    public List<Modifier> getCurrentOptions() {
        return currentOptions;
    }
    public void castVote(UUID playerUuid, int optionIndex) {
        if (optionIndex < 0 || optionIndex >= currentOptions.size()) return;
        votes.put(playerUuid, currentOptions.get(optionIndex).getId());
        Modirena.LOGGER.info("vote casted for " + currentOptions.get(optionIndex).getDisplayName());
    }
    public Modifier tallyVotes() {
        if (currentOptions.isEmpty()) return null;
        Map<String, Integer> tally = new HashMap<>();
        for (Modifier option : currentOptions) tally.put(option.getId(), 0);
        for (String vote : votes.values()) tally.merge(vote, 1, Integer::sum);
        tally.forEach((id, count) -> Modirena.LOGGER.info(id + ": " + count));
        return tally.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> ModifierRegistry.get(entry.getKey()))
                .orElse(currentOptions.getFirst());
    }
    private List<Modifier> pickRandom(int count) {
        List<Modifier> options = new ArrayList<>(ModifierRegistry.getAll());
        Collections.shuffle(options);
        return options.subList(0, Math.min(count, options.size()));
    }
    private static final Item[] VOTE_ITEMS = {
            Items.RED_CONCRETE,
            Items.GREEN_CONCRETE,
            Items.BLUE_CONCRETE
    };
    public void giveVoteItemsTo(ServerPlayerEntity player) {
        player.getInventory().clear();
        for (int i = 0; i < currentOptions.size(); i++) {
            Modifier option = currentOptions.get(i);
            ItemStack stack = new ItemStack(VOTE_ITEMS[i]);
            stack.set(DataComponentTypes.ITEM_NAME, Text.literal(option.getDisplayName()).styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
            player.getInventory().setStack(i, stack);
        }
    }
    public void giveVoteItems(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!PlayerManager.getInstance().isInGame(player)) continue;
            giveVoteItemsTo(player);
        }
    }
    public void clearVoteItems(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.getInventory().clear();
        }
    }
    public Integer getLastSlot(UUID uuid) {
        return lastSlot.get(uuid);
    }
    public void setLastSlot(UUID uuid, int slot) {
        lastSlot.put(uuid, slot);
    }
    public void clearLastSlots() {
        lastSlot.clear();
    }
}
