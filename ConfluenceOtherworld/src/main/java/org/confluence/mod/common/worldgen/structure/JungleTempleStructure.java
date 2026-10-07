package org.confluence.mod.common.worldgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.confluence.lib.common.worldgen.structure.GridPiece;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.init.ModStructures;
import org.confluence.mod.common.init.block.DecorativeBlocks;
import org.confluence.mod.common.init.block.FunctionalBlocks;
import org.confluence.mod.common.init.block.PotBlocks;

import java.util.*;

import static org.confluence.lib.util.StructureUtils.getHeight;

/// [丛林神庙](https://terraria.wiki.gg/zh/wiki/%E4%B8%9B%E6%9E%97%E7%A5%9E%E5%BA%99)
/// 埋在丛林地下的丛林蜥蜴砖迷宫，入口为需要神庙钥匙的门，最深处是放有丛林蜥蜴祭坛的高大房间
public class JungleTempleStructure extends Structure {
    public static final MapCodec<JungleTempleStructure> CODEC = simpleCodec(JungleTempleStructure::new);
    public static final ResourceKey<ConfiguredFeature<?, ?>> TEMPLE_CHEST = Confluence.asResourceKey(Registries.CONFIGURED_FEATURE, "jungle_temple_chest");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TEMPLE_ALTAR_CHEST = Confluence.asResourceKey(Registries.CONFIGURED_FEATURE, "jungle_temple_altar_chest");

    private static final int AIR = 0, BRICKS = 1, TILES = 2, POLISHED = 3, COLUMN = 4, DOOR_LOWER = 5, DOOR_UPPER = 6, ALTAR = 7, POT = 8, LIGHT = 9;
    /// 迷宫格子数，每格3格宽的走廊加1格墙
    private static final int CELLS_X = 16, CELLS_Z = 10;
    private static final int WIDTH = CELLS_X * 4 + 5, DEPTH = CELLS_Z * 4 + 5, HEIGHT = 17;
    private static final int CORRIDOR_HEIGHT = 4, ROOM_HEIGHT = 12;
    /// 祭坛房间占据的格子范围
    private static final int ROOM_X = 5, ROOM_Z = 5, ROOM_W = 6, ROOM_D = 4;

    public JungleTempleStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos startChunk = context.chunkPos();
        int x = startChunk.getMiddleBlockX();
        int z = startChunk.getMiddleBlockZ();
        if (x * x + z * z <= 600 * 600) {
            return Optional.empty();
        }
        int surface = getHeight(x, z, context);
        int bottom = context.heightAccessor().getMinBuildHeight() + 12;
        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
            WorldgenRandom random = context.random();
            int y = Math.max(bottom, surface - 45 - random.nextInt(15));
            BlockPos origin = new BlockPos(x - WIDTH / 2, y, z - DEPTH / 2);
            Object2IntMap<BlockPos> blockMap = new Object2IntOpenHashMap<>();
            Map<BlockPos, ResourceLocation> featureMap = new HashMap<>();
            generate(origin, random, blockMap, featureMap);
            GridPiece.addPieces(blockMap, Lists.newArrayList(
                    Blocks.AIR.defaultBlockState(),
                    DecorativeBlocks.LIHZAHRD_BRICKS.FULL.get().defaultBlockState(),
                    DecorativeBlocks.LIHZAHRD_TILES.get().defaultBlockState(),
                    DecorativeBlocks.POLISHED_LIHZAHRD.get().defaultBlockState(),
                    DecorativeBlocks.LIHZAHRD_COLUMN.get().defaultBlockState(),
                    DecorativeBlocks.LIHZAHRD_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER),
                    DecorativeBlocks.LIHZAHRD_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
                    FunctionalBlocks.LIHZAHRD_ALTAR.get().defaultBlockState(),
                    PotBlocks.LIHZAHRD_POT.get().defaultBlockState(),
                    Blocks.SHROOMLIGHT.defaultBlockState()
            ), featureMap, builder);
        });
    }

    private static int cellMin(int cell) {
        return 3 + cell * 4;
    }

    private static boolean isRoomCell(int cx, int cz) {
        return cx >= ROOM_X && cx < ROOM_X + ROOM_W && cz >= ROOM_Z && cz < ROOM_Z + ROOM_D;
    }

    static void generate(BlockPos origin, RandomSource random, Object2IntMap<BlockPos> blockMap, Map<BlockPos, ResourceLocation> featureMap) {
        // 外壳：整块实心砖
        for (int dx = 0; dx < WIDTH; dx++) {
            for (int dz = 0; dz < DEPTH; dz++) {
                for (int dy = 0; dy < HEIGHT; dy++) {
                    blockMap.put(origin.offset(dx, dy, dz), BRICKS);
                }
            }
        }

        // 迷宫：从入口格开始的深度优先回溯
        boolean[][] visited = new boolean[CELLS_X][CELLS_Z];
        for (int cx = 0; cx < CELLS_X; cx++) {
            for (int cz = 0; cz < CELLS_Z; cz++) {
                visited[cx][cz] = isRoomCell(cx, cz);
            }
        }
        int entranceZ = CELLS_Z / 2;
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, entranceZ});
        visited[0][entranceZ] = true;
        carveCell(origin, 0, entranceZ, blockMap);
        List<int[]> deadEnds = new ArrayList<>();
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!stack.isEmpty()) {
            int[] cell = stack.peek();
            List<int[]> options = new ArrayList<>(4);
            for (int[] d : dirs) {
                int nx = cell[0] + d[0], nz = cell[1] + d[1];
                if (nx >= 0 && nz >= 0 && nx < CELLS_X && nz < CELLS_Z && !visited[nx][nz]) {
                    options.add(new int[]{nx, nz});
                }
            }
            if (options.isEmpty()) {
                int[] popped = stack.pop();
                if (countOpenings(origin, popped, blockMap) == 1) {
                    deadEnds.add(popped);
                }
                continue;
            }
            int[] next = options.get(random.nextInt(options.size()));
            visited[next[0]][next[1]] = true;
            carveCell(origin, next[0], next[1], blockMap);
            carveBetween(origin, cell, next, blockMap);
            stack.push(next);
        }

        // 祭坛房间
        int roomX0 = cellMin(ROOM_X), roomZ0 = cellMin(ROOM_Z);
        int roomX1 = cellMin(ROOM_X + ROOM_W - 1) + 2, roomZ1 = cellMin(ROOM_Z + ROOM_D - 1) + 2;
        for (int dx = roomX0; dx <= roomX1; dx++) {
            for (int dz = roomZ0; dz <= roomZ1; dz++) {
                blockMap.put(origin.offset(dx, 1, dz), POLISHED);
                for (int dy = 2; dy < 2 + ROOM_HEIGHT; dy++) {
                    blockMap.put(origin.offset(dx, dy, dz), AIR);
                }
            }
        }
        // 房间与迷宫之间开一个入口（房间南侧中间）
        int doorCellX = ROOM_X + ROOM_W / 2;
        for (int dx = cellMin(doorCellX); dx < cellMin(doorCellX) + 3; dx++) {
            for (int dy = 2; dy < 2 + CORRIDOR_HEIGHT; dy++) {
                blockMap.put(origin.offset(dx, dy, roomZ0 - 1), AIR);
            }
        }
        // 立柱与照明
        for (int[] corner : new int[][]{{roomX0 + 1, roomZ0 + 1}, {roomX1 - 1, roomZ0 + 1}, {roomX0 + 1, roomZ1 - 1}, {roomX1 - 1, roomZ1 - 1}}) {
            for (int dy = 2; dy < 2 + ROOM_HEIGHT; dy++) {
                blockMap.put(origin.offset(corner[0], dy, corner[1]), COLUMN);
            }
            blockMap.put(origin.offset(corner[0], 2 + ROOM_HEIGHT, corner[1]), LIGHT);
        }
        int centerX = (roomX0 + roomX1) / 2, centerZ = (roomZ0 + roomZ1) / 2 + 2;
        blockMap.put(origin.offset(centerX, 2, centerZ), ALTAR);
        blockMap.put(origin.offset(centerX, 2 + ROOM_HEIGHT, centerZ), LIGHT);
        featureMap.put(origin.offset(roomX1 - 2, 2, roomZ1), TEMPLE_ALTAR_CHEST.location());
        for (int i = 0; i < 6; i++) {
            int px = roomX0 + 2 + random.nextInt(roomX1 - roomX0 - 3);
            int pz = roomZ0 + random.nextInt(2);
            BlockPos potPos = origin.offset(px, 2, pz);
            if (blockMap.getInt(potPos) == AIR) blockMap.put(potPos, POT);
        }

        // 入口：西墙上的神庙门，外侧留出空间
        int doorZ = cellMin(entranceZ) + 1;
        for (int dx = 0; dx < 3; dx++) {
            blockMap.put(origin.offset(dx, 2, doorZ), AIR);
            blockMap.put(origin.offset(dx, 3, doorZ), AIR);
        }
        blockMap.put(origin.offset(1, 2, doorZ), DOOR_LOWER);
        blockMap.put(origin.offset(1, 3, doorZ), DOOR_UPPER);
        for (int dx = -3; dx < 0; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 2; dy <= 4; dy++) {
                    blockMap.put(origin.offset(dx, dy, doorZ + dz), AIR);
                }
            }
        }

        // 死胡同放箱子，走廊零散放罐子和灯
        Util.shuffle(deadEnds, random);
        int chests = 0;
        for (int[] cell : deadEnds) {
            if (chests >= 8 || (cell[0] == 0 && cell[1] == entranceZ)) continue;
            featureMap.put(origin.offset(cellMin(cell[0]) + 1, 2, cellMin(cell[1]) + 1), TEMPLE_CHEST.location());
            ++chests;
        }
        for (int cx = 0; cx < CELLS_X; cx++) {
            for (int cz = 0; cz < CELLS_Z; cz++) {
                if (isRoomCell(cx, cz)) continue;
                BlockPos cellPos = origin.offset(cellMin(cx) + 1, 2, cellMin(cz) + 1);
                if (featureMap.containsKey(cellPos)) continue;
                if (random.nextInt(5) == 0) {
                    BlockPos potPos = cellPos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                    if (blockMap.getInt(potPos) == AIR) blockMap.put(potPos, POT);
                }
                if ((cx + cz) % 3 == 0) {
                    blockMap.put(cellPos.atY(origin.getY() + 2 + CORRIDOR_HEIGHT), LIGHT);
                }
            }
        }
    }

    private static void carveCell(BlockPos origin, int cx, int cz, Object2IntMap<BlockPos> blockMap) {
        for (int dx = cellMin(cx); dx < cellMin(cx) + 3; dx++) {
            for (int dz = cellMin(cz); dz < cellMin(cz) + 3; dz++) {
                blockMap.put(origin.offset(dx, 1, dz), TILES);
                for (int dy = 2; dy < 2 + CORRIDOR_HEIGHT; dy++) {
                    blockMap.put(origin.offset(dx, dy, dz), AIR);
                }
            }
        }
    }

    private static void carveBetween(BlockPos origin, int[] a, int[] b, Object2IntMap<BlockPos> blockMap) {
        int wallX = a[0] == b[0] ? -1 : cellMin(Math.max(a[0], b[0])) - 1;
        int wallZ = a[1] == b[1] ? -1 : cellMin(Math.max(a[1], b[1])) - 1;
        for (int i = 0; i < 3; i++) {
            int dx = wallX >= 0 ? wallX : cellMin(a[0]) + i;
            int dz = wallZ >= 0 ? wallZ : cellMin(a[1]) + i;
            blockMap.put(origin.offset(dx, 1, dz), TILES);
            for (int dy = 2; dy < 2 + CORRIDOR_HEIGHT; dy++) {
                blockMap.put(origin.offset(dx, dy, dz), AIR);
            }
        }
    }

    private static int countOpenings(BlockPos origin, int[] cell, Object2IntMap<BlockPos> blockMap) {
        int openings = 0;
        int mid = 1;
        int x0 = cellMin(cell[0]), z0 = cellMin(cell[1]);
        if (blockMap.getInt(origin.offset(x0 - 1, 2, z0 + mid)) == AIR) openings++;
        if (blockMap.getInt(origin.offset(x0 + 3, 2, z0 + mid)) == AIR) openings++;
        if (blockMap.getInt(origin.offset(x0 + mid, 2, z0 - 1)) == AIR) openings++;
        if (blockMap.getInt(origin.offset(x0 + mid, 2, z0 + 3)) == AIR) openings++;
        return openings;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.JUNGLE_TEMPLE.get();
    }
}
