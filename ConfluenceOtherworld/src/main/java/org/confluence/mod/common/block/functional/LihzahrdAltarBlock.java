package org.confluence.mod.common.block.functional;

import com.google.common.collect.Streams;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.confluence.mod.common.init.item.ConsumableItems;
import org.confluence.terraentity.entity.boss.golem.Golem;
import org.confluence.terraentity.init.entity.TEBossEntities;
import org.confluence.terraentity.utils.TEUtils;

/// [丛林蜥蜴祭坛](https://terraria.wiki.gg/zh/wiki/%E4%B8%9B%E6%9E%97%E8%9C%A5%E8%9C%B4%E7%A5%AD%E5%9D%9B)
/// 使用丛林蜥蜴能量电池召唤石巨人，不可破坏
public class LihzahrdAltarBlock extends Block {
    public LihzahrdAltarBlock() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 6)
                .noLootTable()
                .pushReaction(PushReaction.BLOCK));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ConsumableItems.LIHZAHRD_POWER_CELL)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel serverLevel) {
            if (Streams.stream(serverLevel.getAllEntities()).anyMatch(entity -> entity instanceof Golem)) {
                player.displayClientMessage(Component.translatable("message.confluence.lihzahrd_altar.golem_exists"), true);
                return ItemInteractionResult.FAIL;
            }
            Golem golem = new Golem(TEBossEntities.GOLEM.get(), serverLevel);
            golem.setPos(pos.getBottomCenter().add(0, 1.5, 0));
            if (TEUtils.internalSpawnEntity(golem, serverLevel)) {
                serverLevel.addFreshEntityWithPassengers(golem);
                serverLevel.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 0.6F);
                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
