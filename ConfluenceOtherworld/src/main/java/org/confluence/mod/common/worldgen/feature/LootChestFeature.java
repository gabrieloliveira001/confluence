package org.confluence.mod.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.LootTable;
import org.confluence.mod.common.block.common.BaseChestBlock;

/// 在原点放置一个带战利品表的箱子（已解锁），朝向自动对准空气一侧
public class LootChestFeature extends Feature<LootChestFeature.Config> {
    public LootChestFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        BlockState state = config.chest().getState(context.random(), pos);
        if (state.hasProperty(BaseChestBlock.UNLOCKED)) {
            state = state.setValue(BaseChestBlock.UNLOCKED, true);
        }
        if (state.hasProperty(ChestBlock.FACING)) {
            state = StructurePiece.reorient(level, pos, state);
        }
        level.setBlock(pos, state, 2);
        RandomizableContainer.setBlockEntityLootTable(level, context.random(), pos, config.lootTable());
        return true;
    }

    public record Config(BlockStateProvider chest, ResourceKey<LootTable> lootTable) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockStateProvider.CODEC.fieldOf("chest").forGetter(Config::chest),
                ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(Config::lootTable)
        ).apply(instance, Config::new));
    }
}
