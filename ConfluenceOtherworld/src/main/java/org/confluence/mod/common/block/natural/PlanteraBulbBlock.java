package org.confluence.mod.common.block.natural;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.confluence.lib.util.LibUtils;
import org.confluence.mod.common.data.saved.KillBoard;
import org.confluence.mod.common.init.block.NatureBlocks;
import org.confluence.mod.util.OverworldUtils;
import org.confluence.terraentity.entity.boss.plantera.Plantera;
import org.confluence.terraentity.init.entity.TEBossEntities;
import org.confluence.terraentity.utils.TEUtils;

/// [世纪之花灯泡](https://terraria.wiki.gg/zh/wiki/%E4%B8%96%E7%BA%AA%E4%B9%8B%E8%8A%B1%E7%81%AF%E6%B3%A1)
/// 新三王全部击败后在地下丛林草上生长，破坏后召唤世纪之花
public class PlanteraBulbBlock extends Block {
    public static final int CLASSIC_CHANCE = 1500;
    public static final int RANGE = 24;
    protected static final VoxelShape SHAPE = box(1, 0, 1, 15, 15, 15);

    public PlanteraBulbBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_PINK)
                .strength(1.5F, 6.0F)
                .sound(SoundType.SPORE_BLOSSOM)
                .lightLevel(state -> 7)
                .noOcclusion()
                .pushReaction(PushReaction.BLOCK));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(NatureBlocks.JUNGLE_GRASS_BLOCK);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) level.scheduleTick(pos, this, 1);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            summonPlantera(serverLevel, pos);
        }
    }

    public static void summonPlantera(ServerLevel level, BlockPos pos) {
        if (Streams.stream(level.getAllEntities()).anyMatch(entity -> entity instanceof Plantera)) return;
        Plantera plantera = new Plantera(TEBossEntities.PLANTERA.get(), level);
        plantera.setPos(pos.getCenter());
        if (TEUtils.internalSpawnEntity(plantera, level)) {
            level.addFreshEntityWithPassengers(plantera);
            level.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.0F, 1.6F);
        }
    }

    /// 在地下丛林草上方随机生长，范围内只能存在一个
    public static boolean canGrow(ServerLevel level, BlockState state, BlockPos pos, RandomSource random) {
        boolean isExpert = LibUtils.isAtLeastExpert(level, pos);
        if (state.canBeReplaced() &&
                pos.getY() < OverworldUtils.getSurfaceY() &&
                random.nextInt(isExpert ? CLASSIC_CHANCE : CLASSIC_CHANCE * 5 / 4) == 0 &&
                KillBoard.INSTANCE.isAllMechBossesDefeated() &&
                level.getBlockState(pos.above()).canBeReplaced()
        ) {
            Long2ObjectMap<ChunkAccess> map = new Long2ObjectArrayMap<>();
            for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-RANGE, -RANGE, -RANGE), pos.offset(RANGE, RANGE, RANGE))) {
                ChunkAccess access = map.computeIfAbsent(ChunkPos.asLong(blockPos), l -> LibUtils.getChunkIfLoaded(level, ChunkPos.getX(l), ChunkPos.getZ(l)));
                if (access != null && access.getBlockState(blockPos).is(NatureBlocks.PLANTERA_BULB)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
