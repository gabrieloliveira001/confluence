package org.confluence.mod.common.data.saved;

import com.google.common.collect.Streams;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import org.confluence.mod.common.gameevent.LunarEventsGameEvent;
import org.confluence.mod.common.worldgen.structure.DungeonStructure;
import org.confluence.terraentity.entity.boss.cultist.LunaticCultist;
import org.confluence.terraentity.entity.monster.CultistDevotee;
import org.confluence.terraentity.init.entity.TEBossEntities;
import org.confluence.terraentity.init.entity.TEMonsterEntities;

/// 击败石巨人后，玩家靠近地牢入口时生成邪教徒仪式（4个信徒+2个弓箭手），击杀全部信徒召唤拜月教邪教徒
public final class CultistRitual {
    private static final int CHECK_INTERVAL = 200;
    private static final int TRIGGER_RANGE = 80;

    public static void tick(ServerLevel level) {
        if (level.getGameTime() % CHECK_INTERVAL != 0) return;
        if (!KillBoard.INSTANCE.isDefeated(TEBossEntities.GOLEM.get()) || LunarEventsGameEvent.INSTANCE.started()) return;
        if (Streams.stream(level.getAllEntities()).anyMatch(entity -> entity instanceof CultistDevotee || entity instanceof LunaticCultist)) return;
        for (ServerPlayer player : level.players()) {
            boolean spawned = DungeonStructure.iterateDungeon(level, player.chunkPosition(), structureStart -> {
                BlockPos gate = DungeonStructure.findGatePos(structureStart);
                if (gate == null || player.blockPosition().distSqr(gate) > TRIGGER_RANGE * TRIGGER_RANGE) return false;
                spawn(level, gate);
                return true;
            });
            if (spawned) return;
        }
    }

    private static void spawn(ServerLevel level, BlockPos center) {
        for (int i = 0; i < 4; i++) {
            float angle = i * Mth.HALF_PI;
            CultistDevotee devotee = TEMonsterEntities.CULTIST_DEVOTEE.get().create(level);
            if (devotee == null) continue;
            devotee.setRitualCenter(center);
            devotee.moveTo(center.getX() + 0.5 + Mth.cos(angle) * 3, center.getY(), center.getZ() + 0.5 + Mth.sin(angle) * 3, angle * Mth.RAD_TO_DEG + 90, 0);
            level.addFreshEntity(devotee);
        }
        for (int side = -1; side <= 1; side += 2) {
            Mob archer = TEMonsterEntities.CULTIST_ARCHER.get().create(level);
            if (archer == null) continue;
            archer.moveTo(center.getX() + 0.5 + side * 6, center.getY(), center.getZ() + 0.5, 0, 0);
            archer.setPersistenceRequired();
            level.addFreshEntity(archer);
        }
    }
}
