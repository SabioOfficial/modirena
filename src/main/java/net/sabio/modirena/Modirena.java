package net.sabio.modirena;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.rule.GameRules;
import net.sabio.modirena.modifier.Modifier;
import net.sabio.modirena.modifier.ModifierRegistry;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

public class Modirena implements ModInitializer {
    public static final Logger LOGGER = Logger.getLogger(ModirenaConstants.MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("modirena is running successfully");
        GameManager.getInstance();
        LOGGER.info("gamemanager ready. state " + GameManager.getInstance().getState());
        ModifierRegistry.registerAll();
        LOGGER.info("registered modifiers " + ModifierRegistry.getAll().size());
        ModirenaCommands.register();
        LOGGER.info("registered commands");
        PlayerManager.getInstance();
        LOGGER.info("playermanager ready.");
        PlayerBlockBreakEvents.BEFORE.register((world, player, position, state, entity) -> {
            if (GameManager.getInstance().getState() != GameState.COMBAT) return false;
            if (!PlayerManager.getInstance().isInGame((ServerPlayerEntity) player)) return false;
            boolean isAllowedBlock = state.isOf(Blocks.SHORT_GRASS) || state.isOf(Blocks.TALL_GRASS) || state.isOf(Blocks.DANDELION) || state.isOf(Blocks.POPPY);
            if (isAllowedBlock) {
                return true;
            }
            return false;
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            GameManager.getInstance().setServer(server);
            ConfigManager.load();
            if (!ConfigManager.isSetUp()) {
                LOGGER.info("first boot detected, so im placing structures now");
                StructureLoader.placeAll(server);
                ConfigManager.markSetUp();
            } else {
                LOGGER.info("structures have already been placed");
            }
            server.getOverworld().setSpawnPoint(WorldProperties.SpawnPoint.create(
                    server.getOverworld().getRegistryKey(), new BlockPos(10, 65, 9), 0.0f, 0.0f
            ));
            ServerWorld overworld = server.getOverworld();
            overworld.getGameRules().setValue(GameRules.ADVANCE_TIME, false, server);
            overworld.getGameRules().setValue(GameRules.ADVANCE_WEATHER, false, server);
            overworld.getGameRules().setValue(GameRules.RESPAWN_RADIUS, 0, server);
            overworld.setTimeOfDay(6000);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            ServerWorld overworld = server.getOverworld();
            PlayerManager.getInstance().addPlayer(player);
            player.getInventory().clear();
            player.clearStatusEffects();
            player.changeGameMode(GameMode.ADVENTURE);
            if (GameManager.getInstance().getState() == GameState.COMBAT) {
                PlayerManager.getInstance().setState(player, PlayerState.SPECTATING);
                player.changeGameMode(GameMode.SPECTATOR);
                server.execute(() -> {
                    List<ServerPlayerEntity> alive = server.getPlayerManager().getPlayerList().stream()
                            .filter(serverPlayer -> PlayerManager.getInstance().getState(serverPlayer) == PlayerState.ALIVE)
                            .toList();
                    if (!alive.isEmpty()) {
                        ServerPlayerEntity target = alive.get(new Random().nextInt(alive.size()));
                        player.teleport(
                                overworld, target.getX(), target.getY(), target.getZ(), Set.of(), target.getYaw(),
                                target.getPitch(), false
                        );
                    }
                });
            } else {
                player.teleport(overworld, 10.0, 65.0, 9.0, java.util.Set.of(), 0.0f, 0.0f, false);
                if (GameManager.getInstance().getState() == GameState.VOTING) {
                    server.execute(() -> VoteManager.getInstance().giveVoteItemsTo(player));
                }
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            PlayerState state = PlayerManager.getInstance().getState(player);
            PlayerManager.getInstance().removePlayer(player);
            if (state != PlayerState.ALIVE) return;
            if (GameManager.getInstance().getState() != GameState.COMBAT) return;
            for (Modifier modifier : GameManager.getInstance().getActiveModifiers()) {
                modifier.onDeactivate(server);
            }
            GameManager.getInstance().getActiveModifiers().clear();
            GameManager.getInstance().checkRoundEnd();
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity)) return true;
            return GameManager.getInstance().getState() == GameState.COMBAT;
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            if (GameManager.getInstance().getState() != GameState.COMBAT) return;
            if (!PlayerManager.getInstance().isInGame(player)) return;
            PlayerManager.getInstance().setDeathPosition(player, player.getX(), player.getY(), player.getZ());
            PlayerManager.getInstance().setState(player, PlayerState.SPECTATING);
            GameManager.getInstance().checkRoundEnd();
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!PlayerManager.getInstance().isInGame(newPlayer)) return;
            double[] deathPos = PlayerManager.getInstance().getDeathPosition(newPlayer.getUuid());
            if (deathPos != null) {
                PlayerManager.getInstance().clearDeathPosition(newPlayer.getUuid());
                newPlayer.changeGameMode(GameMode.SPECTATOR);
                newPlayer.teleport(
                        Objects.requireNonNull(newPlayer.getEntityWorld().getServer()).getSpawnWorld(),
                        deathPos[0], deathPos[1], deathPos[2], Set.of(), newPlayer.getYaw(), newPlayer.getPitch(), false
                );
            } else {
                ArenaManager.getInstance().sendPlayerToLobby(newPlayer);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (GameManager.getInstance().getState() != GameState.VOTING) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!PlayerManager.getInstance().isInGame(player)) continue;
                int slot = player.getInventory().getSelectedSlot();
                Integer last = VoteManager.getInstance().getLastSlot(player.getUuid());
                if (last != null && last == slot) continue;
                VoteManager.getInstance().setLastSlot(player.getUuid(), slot);
                if (slot < VoteManager.getInstance().getCurrentOptions().size()) {
                    VoteManager.getInstance().castVote(player.getUuid(), slot);
                }
            }
        });
    }
}
