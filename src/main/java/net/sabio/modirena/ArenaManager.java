package net.sabio.modirena;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;
import net.sabio.modirena.modifier.Modifier;

import java.util.*;

public class ArenaManager {
    private static ArenaManager instance;
    private ArenaManager() {}
    public static ArenaManager getInstance() {
        if (instance == null) instance = new ArenaManager();
        return instance;
    }
    public void sendPlayersToArena(MinecraftServer server) {
        List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
        List<int[]> spawnPoints = getArenaSpawnPoints(players.size());
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++) {
            ServerPlayerEntity player = players.get(i);
            int[] position = spawnPoints.get(i % spawnPoints.size());
            resetPlayer(player);
            giveGear(player);
            PlayerManager.getInstance().setState(player, PlayerState.ALIVE);
            player.teleport(server.getOverworld(), position[0], position[1], position[2], Set.of(), 0f, 0f, false);
            player.changeGameMode(GameMode.SURVIVAL);
        }
        server.execute(() -> {
            for (Modifier modifier : GameManager.getInstance().getActiveModifiers()) {
                modifier.onActivate(server);
            }
        });
    }
    public void sendPlayerToLobby(ServerPlayerEntity player) {
        player.teleport(Objects.requireNonNull(player.getEntityWorld().getServer()).getOverworld(), 10, 65, 9, Set.of(), 0f, 0f, false);
        player.changeGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.clearStatusEffects();
    }
    private void resetPlayer(ServerPlayerEntity player) {
        player.setHealth(player.getMaxHealth());
        HungerManager hunger = player.getHungerManager();
        hunger.setFoodLevel(20);
        hunger.setSaturationLevel(20f);
        player.clearStatusEffects();
        player.getInventory().clear();
    }
    private void giveGear(ServerPlayerEntity player) {
        player.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        player.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        player.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
        player.equipStack(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
        player.getInventory().setStack(0, new ItemStack(Items.IRON_SWORD));
        player.getInventory().setStack(1, new ItemStack(Items.IRON_AXE));
        player.getInventory().setStack(2, new ItemStack(Items.COOKED_CHICKEN, 16));
    }
    private List<int[]> getArenaSpawnPoints(int playerCount) {
        int centerX = 241;
        int centerY = 65;
        int centerZ = 41;
        int radius = 28;
        int totalPoints = Math.max(playerCount, 3);
        List<int[]> points = new ArrayList<>();
        for (int i = 0; i < totalPoints; i++) {
            double angle = 2 * Math.PI * i / totalPoints;
            int x = centerX + (int) (radius * Math.cos(angle));
            int z = centerZ + (int) (radius * Math.sin(angle));
            points.add(new int[]{x, centerY, z});
        }
        return points;
    }
    public void resetArena(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        world.getEntitiesByType(EntityType.ITEM, entity -> {
            Box arenaBox = new Box(
                    StructureLoader.ARENA_ORIGIN.getX(), StructureLoader.ARENA_ORIGIN.getY() - 5,
                    StructureLoader.ARENA_ORIGIN.getZ(),
                    StructureLoader.ARENA_ORIGIN.getX() + 150, StructureLoader.ARENA_ORIGIN.getY() + 50,
                    StructureLoader.ARENA_ORIGIN.getZ() + 150
            );
            return entity.getBoundingBox().intersects(arenaBox);
        }).forEach(Entity::discard);
        StructureLoader.place(server, world, "modirena_arena", StructureLoader.ARENA_ORIGIN);
    }
}
