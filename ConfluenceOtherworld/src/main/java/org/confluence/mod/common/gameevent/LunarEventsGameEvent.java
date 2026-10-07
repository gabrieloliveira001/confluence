package org.confluence.mod.common.gameevent;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import org.confluence.lib.color.GlobalColors;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.init.item.MaterialItems;
import org.confluence.mod.util.ModUtils;
import org.confluence.mod.util.OverworldUtils;
import org.confluence.terraentity.entity.boss.moonlord.MoonLord;
import org.confluence.terraentity.entity.boss.pillar.CelestialPillar;
import org.confluence.terraentity.init.entity.TEBossEntities;
import org.confluence.terraentity.utils.TEUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/// [天界事件](https://terraria.wiki.gg/zh/wiki/%E5%A4%A9%E7%95%8C%E4%BA%8B%E4%BB%B6)
/// 击败拜月教邪教徒后在其死亡位置周围生成四根天界柱；四柱全部被摧毁一分钟后月亮领主降临
public enum LunarEventsGameEvent implements GameEvent {
    INSTANCE;
    public static final ResourceKey<LunarEventsGameEvent> KEY = GameEvent.createKey(Confluence.asResource("lunar_events"));
    public static final List<Supplier<? extends EntityType<CelestialPillar>>> PILLARS = List.of(
            TEBossEntities.SOLAR_PILLAR, TEBossEntities.VORTEX_PILLAR, TEBossEntities.NEBULA_PILLAR, TEBossEntities.STARDUST_PILLAR
    );
    public static final List<Supplier<? extends Item>> FRAGMENTS = List.of(
            MaterialItems.SOLAR_FRAGMENT, MaterialItems.VORTEX_FRAGMENT, MaterialItems.NEBULA_FRAGMENT, MaterialItems.STARDUST_FRAGMENT
    );
    private static final int ALL_DEFEATED = 0b1111;
    private static final int DOOM_DELAY = 60 * 20;
    private static final int PILLAR_DISTANCE = 160;

    private transient @Nullable MinecraftServer server;
    private transient boolean forceStart;
    private transient boolean forceEnd;
    private boolean started;
    private @Nullable BlockPos center;
    private int defeatedMask;
    private int doomTicks = -1;

    public static int pillarIndex(EntityType<?> type) {
        for (int i = 0; i < PILLARS.size(); i++) {
            if (PILLARS.get(i).get() == type) return i;
        }
        return -1;
    }

    /// 由拜月教邪教徒死亡时调用
    public void begin(BlockPos center) {
        if (started) return;
        this.center = center;
        this.forceStart = true;
    }

    public void onPillarDefeated(int index) {
        if (!started || index < 0) return;
        this.defeatedMask |= 1 << index;
        if (defeatedMask == ALL_DEFEATED && doomTicks < 0) {
            this.doomTicks = DOOM_DELAY;
            broadcast(Component.translatable("message.confluence.lunar_events.all_down"));
        }
    }

    @Override
    public void open(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void close(MinecraftServer server) {
        this.server = null;
    }

    @Override
    public void tick() {
        if (!started || server == null) return;
        if (doomTicks > 0 && --doomTicks == 0) {
            summonMoonLord();
        }
    }

    @Override
    public void countKilled(LivingEntity living) {
        if (!started || !(living.level() instanceof ServerLevel level)) return;
        for (CelestialPillar pillar : level.getEntitiesOfClass(CelestialPillar.class, living.getBoundingBox().inflate(CelestialPillar.ENEMY_RANGE))) {
            if (pillar.isEnemy(living.getType())) {
                pillar.reduceShield(1);
            }
        }
    }

    @Override
    public boolean canStart() {
        return forceStart;
    }

    @Override
    public boolean canEnd() {
        return forceEnd;
    }

    @Override
    public void onStart() {
        this.started = true;
        this.forceStart = false;
        this.defeatedMask = 0;
        this.doomTicks = -1;
        if (server == null) return;
        ServerLevel level = OverworldUtils.getLevel(server);
        BlockPos origin = center == null ? level.getSharedSpawnPos() : center;
        broadcast(Component.translatable("message.confluence.lunar_events.start"));
        float offset = level.random.nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < PILLARS.size(); i++) {
            float angle = offset + i * Mth.HALF_PI;
            int x = Mth.floor(origin.getX() + Mth.cos(angle) * PILLAR_DISTANCE);
            int z = Mth.floor(origin.getZ() + Mth.sin(angle) * PILLAR_DISTANCE);
            level.getChunk(x >> 4, z >> 4); // 确保区块已生成
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            CelestialPillar pillar = PILLARS.get(i).get().create(level);
            if (pillar == null) continue;
            pillar.moveTo(x + 0.5, y, z + 0.5, 0, 0);
            if (TEUtils.internalSpawnEntity(pillar, level)) {
                level.addFreshEntity(pillar);
                broadcast(Component.translatable("message.confluence.lunar_events.pillar", pillar.getDisplayName(), x, y, z));
                Confluence.LOGGER.info("Lunar events: {} spawned at [{}, {}, {}]", pillar.getType().getDescriptionId(), x, y, z);
            }
        }
    }

    @Override
    public void onEnd() {
        this.started = false;
        this.forceStart = false;
        this.forceEnd = false;
        this.center = null;
        this.doomTicks = -1;
    }

    private void summonMoonLord() {
        if (server == null) return;
        ServerLevel level = OverworldUtils.getLevel(server);
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            this.doomTicks = 20; // 等有玩家在主世界时再降临
            return;
        }
        ServerPlayer player = players.get(level.random.nextInt(players.size()));
        ModUtils.summonBoss(level, player.blockPosition(), new MoonLord(TEBossEntities.MOON_LORD.get(), level), false);
        broadcast(Component.translatable("message.confluence.lunar_events.doom"));
        this.forceEnd = true;
    }

    private void broadcast(Component component) {
        if (server == null) return;
        Component message = component.copy().withColor(GlobalColors.EVENT.get());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }

    @Override
    public boolean started() {
        return started;
    }

    @Override
    public boolean forceStart() {
        if (started) return false;
        this.forceStart = true;
        return true;
    }

    @Override
    public void forceEnd() {
        if (started) {
            this.forceEnd = true;
        }
    }

    @Override
    public void decode(CompoundTag tag) {
        this.started = tag.getBoolean("Started");
        this.defeatedMask = tag.getInt("Defeated");
        this.doomTicks = tag.contains("Doom") ? tag.getInt("Doom") : -1;
        this.center = NbtUtils.readBlockPos(tag, "Center").orElse(null);
    }

    @Override
    public void encode(CompoundTag tag) {
        tag.putBoolean("Started", started);
        tag.putInt("Defeated", defeatedMask);
        tag.putInt("Doom", doomTicks);
        if (center != null) {
            tag.put("Center", NbtUtils.writeBlockPos(center));
        }
    }

    @Override
    public ResourceKey<LunarEventsGameEvent> key() {
        return KEY;
    }
}
