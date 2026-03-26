package net.sabio.modirena;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldProperties;

import java.io.InputStream;

public class StructureLoader {
    public static final BlockPos LOBBY_ORIGIN = new BlockPos(0, 64, 0);
    public static final BlockPos ARENA_ORIGIN = new BlockPos(200, 64, 0);
    public static void placeAll(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        place(server, world, "modirena_lobby", LOBBY_ORIGIN);
        place(server, world, "modirena_arena", ARENA_ORIGIN);
        Modirena.LOGGER.info("structures placed successfully");
    }
    public static void place(MinecraftServer server, ServerWorld world, String name, BlockPos origin) {
        try {
            String path = "/data/modirena/structures/" + name + ".nbt";
            InputStream stream = StructureLoader.class.getResourceAsStream(path);
            if (stream == null) {
                Modirena.LOGGER.warning("structure file not found in jar: " + path);
                return;
            }
            NbtCompound nbt = net.minecraft.nbt.NbtIo.readCompressed(stream, NbtSizeTracker.ofUnlimitedBytes());
            StructureTemplate template = new StructureTemplate();
            RegistryEntryLookup<Block> blockLookup = server.getRegistryManager().getOrThrow(RegistryKeys.BLOCK);
            template.readNbt(blockLookup, nbt);
            clearArea(world, origin, template.getSize());
            StructurePlacementData placement = new StructurePlacementData();
            template.place(world, origin, origin, placement, world.random, 2);
            Modirena.LOGGER.info("placed structure " + name + " at " + origin);
        } catch (Exception exception) {
            Modirena.LOGGER.warning("failed to place structure " + name + " " + exception.getMessage());
        }
    }
    private static void clearArea(ServerWorld world, BlockPos origin, Vec3i size) {
        BlockPos.iterate(
                origin.getX() - 1, origin.getY() - 1, origin.getZ() - 1,
                origin.getX() + size.getX() + 1, origin.getY() + size.getY() + 1, origin.getZ() + size.getZ() + 1
        ).forEach(position -> world.setBlockState(position, Blocks.AIR.getDefaultState(), 3));
    }
}
