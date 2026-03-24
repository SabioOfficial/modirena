package net.sabio.modirena;

import net.sabio.modirena.modifier.Modifier;
import net.sabio.modirena.modifier.ModifierRegistry;

import java.util.*;

public class VoteManager {
    private static VoteManager instance;
    private final Map<UUID, String> votes = new HashMap<>();
    private List<Modifier> currentOptions = new ArrayList<>();
    private VoteManager() {}
    public static VoteManager getInstance() {
        if (instance == null) instance = new VoteManager();
        return instance;
    }
    public void startVote(int optionCount) {
        votes.clear();
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
}
