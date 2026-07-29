package fku.org.example.fku.features.attackindicator; /* water */

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.attackindicator.AttackIndicatorConfig;
import fku.org.example.fku.features.attackindicator.AttackIndicatorRenderer;
import fku.org.example.fku.features.tpaura.TpAuraFeature;
import fku.org.example.fku.features.standattack.StandAttackFeature;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 攻击指示器核心功能 — 监听攻击事件、跟踪目标、触发特效渲染
 * 支持三种触发模式：ON_ATTACK（仅攻击触发）、ON_TPAURA_LOCK（仅TpAura锁定触发）、BOTH（两者都触发）
 * 该功能由赛博教员实现
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class AttackIndicatorFeature {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;
    private static Entity currentTarget = null;
    private static Entity previousTarget = null;
    private static int despawnCounter = 0;
    private static int ticksSinceLastAttack = 0;
    private static final int ATTACK_TIMEOUT_TICKS = 40;
    /** 是否由普通攻击触发 */
    private static boolean triggeredByAttack = false;
    private static final List<SwordWave> activeSwordWaves = new ArrayList<>();
    /** TpAura攻击计数器 — 上次记录的TpAura攻击次数，用于检测新攻击事件 */
    private static int lastTpAuraAttackCount = 0;
    /** StandAttack攻击计数器 — 上次记录的StandAttack攻击次数，用于检测新攻击事件 */
    private static int lastStandAttackCount = 0;

    /** 初始化攻击指示器 — 加载配置、注册事件 */
    public static void init() {
        if (initialized) return;
        initialized = true;
        AttackIndicatorConfig.load();
        MinecraftForge.EVENT_BUS.register(AttackIndicatorFeature.class);
        Fku.LOGGER.info("[AttackIndicator] 已初始化");
    }

    public static Entity getCurrentTarget() { return currentTarget; }
    public static boolean hasActiveTarget() { return currentTarget != null; }
    public static List<SwordWave> getActiveSwordWaves() { return activeSwordWaves; }

    /** 攻击事件触发 — 设置目标并生成剑波特效 */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        AttackIndicatorConfig cfg = AttackIndicatorConfig.getInstance();
        if (!cfg.enabled) return;
        if (event.getEntity() != mc.player) return;

        String mode = cfg.triggerMode;
        if ("ON_TPAURA_LOCK".equals(mode)) return; // 仅TpAura模式，跳过攻击触发

        triggeredByAttack = true;
        ticksSinceLastAttack = 0;
        setTarget(event.getTarget());

        if (cfg.enableSwordWave && event.getTarget() != null && mc.player != null) {
            Vec3 playerPos = mc.player.position().add(0.0, mc.player.getBbHeight() * 0.5, 0.0);
            Vec3 targetPos = event.getTarget().position().add(0.0, event.getTarget().getBbHeight() * 0.5, 0.0);
            activeSwordWaves.add(new SwordWave(playerPos, targetPos));
        }
    }

    /**
     * 客户端Tick事件 — 跟踪目标状态、检测TpAura攻击、清理过期特效
     *
     * ★ 设计思想（实践论）：
     *   普通攻击：onAttackEntity 设置 target + triggeredByAttack，超时40tick后清理
     *   TpAura攻击：通过 attackCounter 变化检测，设置 target
     *   BOTH/ON_TPAURA_LOCK模式：持续跟随 TpAura.currentTarget
     *   两者互不干扰，普通攻击超时仅在无TpAura目标时生效
     *
     * 该方法由赛博教员实现
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.player == null) return;

        AttackIndicatorConfig cfg = AttackIndicatorConfig.getInstance();
        if (!cfg.enabled) return;

        // 1. 普通攻击计时
        if (triggeredByAttack) {
            ++ticksSinceLastAttack;
        }

        // 2. TpAura攻击计数器检测
        TpAuraFeature tpAura = TpAuraFeature.getInstance();
        int currentTpAttacks = TpAuraFeature.attackCounter;
        if (currentTpAttacks != lastTpAuraAttackCount) {
            int diff = Math.min(currentTpAttacks - lastTpAuraAttackCount, 5);
            lastTpAuraAttackCount = currentTpAttacks;
            String mode = cfg.triggerMode;
            if (("BOTH".equals(mode) || "ON_TPAURA_LOCK".equals(mode)) && tpAura.currentTarget != null) {
                setTarget(tpAura.currentTarget);
                // 生成剑波
                if (cfg.enableSwordWave && mc.player != null) {
                    for (int i = 0; i < diff; i++) {
                        Vec3 playerPos = mc.player.position().add(0.0, mc.player.getBbHeight() * 0.5, 0.0);
                        Vec3 targetPos = tpAura.currentTarget.position().add(0.0, tpAura.currentTarget.getBbHeight() * 0.5, 0.0);
                        activeSwordWaves.add(new SwordWave(playerPos, targetPos));
                    }
                }
            }
        }

        // ★ 2.5 StandAttack攻击计数器检测
        StandAttackFeature standAttack = StandAttackFeature.getInstance();
        int currentStAttacks = StandAttackFeature.attackCounter;
        if (currentStAttacks != lastStandAttackCount) {
            int diff = Math.min(currentStAttacks - lastStandAttackCount, 5);
            lastStandAttackCount = currentStAttacks;
            String mode = cfg.triggerMode;
            if (("BOTH".equals(mode) || "ON_TPAURA_LOCK".equals(mode)) && standAttack.currentTarget != null) {
                setTarget(standAttack.currentTarget);
                // 生成剑波
                if (cfg.enableSwordWave && mc.player != null) {
                    for (int i = 0; i < diff; i++) {
                        Vec3 playerPos = mc.player.position().add(0.0, mc.player.getBbHeight() * 0.5, 0.0);
                        Vec3 targetPos = standAttack.currentTarget.position().add(0.0, standAttack.currentTarget.getBbHeight() * 0.5, 0.0);
                        activeSwordWaves.add(new SwordWave(playerPos, targetPos));
                    }
                }
            }
        }

        // 3. BOTH/ON_TPAURA_LOCK模式：持续跟随TpAura目标
        //    每次tick都将目标设为TpAura的当前目标，确保目标更新
        //    ★ 修复：添加 !triggeredByAttack 条件，防止TpAura覆盖普通攻击刚触发的目标
        if (!"ON_ATTACK".equals(cfg.triggerMode) && !triggeredByAttack && tpAura != null && tpAura.currentTarget != null) {
            setTarget(tpAura.currentTarget);
        }

        // ★ 3.5 持续跟随StandAttack目标
        if (!"ON_ATTACK".equals(cfg.triggerMode) && !triggeredByAttack && standAttack != null && standAttack.currentTarget != null) {
            setTarget(standAttack.currentTarget);
        }

        // 4. 普通攻击超时清理（仅当无TpAura/StandAttack目标时）
        //    如果TpAura或StandAttack有目标，则持续跟随，不清理
        if (triggeredByAttack && ticksSinceLastAttack > ATTACK_TIMEOUT_TICKS) {
            boolean hasTpAuraTarget = tpAura != null && tpAura.currentTarget != null
                && !"ON_ATTACK".equals(cfg.triggerMode);
            boolean hasStandAttackTarget = standAttack != null && standAttack.currentTarget != null
                && !"ON_ATTACK".equals(cfg.triggerMode);
            if (!hasTpAuraTarget && !hasStandAttackTarget) {
                clearTarget();
                triggeredByAttack = false;
                ticksSinceLastAttack = 0;
            }
        }

        // 5. 清理目标死亡
        if (currentTarget != null && !currentTarget.isAlive()) {
            clearTarget();
            triggeredByAttack = false;
            ticksSinceLastAttack = 0;
        }

        // 6. 清理过期剑波
        Iterator<SwordWave> it = activeSwordWaves.iterator();
        while (it.hasNext()) {
            if (it.next().isExpired()) it.remove();
        }
    }

    private static void setTarget(Entity target) {
        if (target == currentTarget) {
            despawnCounter = 0;
            return;
        }
        if (currentTarget != null && currentTarget != target) {
            previousTarget = currentTarget;
            despawnCounter = AttackIndicatorConfig.getInstance().despawnDelay;
        }
        currentTarget = target;
        despawnCounter = 0;
    }

    private static void clearTarget() {
        if (currentTarget != null) {
            previousTarget = currentTarget;
            despawnCounter = AttackIndicatorConfig.getInstance().despawnDelay;
        }
        currentTarget = null;
    }

    /** 世界渲染 — 绘制目标特效和连接特效 */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        AttackIndicatorConfig cfg = AttackIndicatorConfig.getInstance();
        if (!cfg.enabled || mc.player == null) return;

        // 目标特效
        if (currentTarget instanceof LivingEntity livingTarget) {
            AttackIndicatorRenderer.renderTargetEffects(event.getPoseStack(), livingTarget, cfg);
        }
        // 连接特效
        if (currentTarget != null) {
            AttackIndicatorRenderer.renderConnectionEffects(event.getPoseStack(), mc.player, currentTarget, cfg);
        }
        // 残留目标特效（过渡动画）
        if (previousTarget instanceof LivingEntity && despawnCounter > 0) {
            AttackIndicatorRenderer.renderTargetEffects(event.getPoseStack(), (LivingEntity) previousTarget, cfg);
            if (--despawnCounter <= 0) previousTarget = null;
        }
        // 剑波
        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.getPosition();
        for (SwordWave sw : activeSwordWaves) {
            Vec3 camStart = sw.startPos.subtract(cameraPos);
            Vec3 camEnd = sw.targetPos.subtract(cameraPos);
            AttackIndicatorRenderer.renderSwordWave(event.getPoseStack(), camStart, camEnd, sw.getProgress(), cfg);
        }
    }

    /** 剑波数据类 — 记录剑波起始位置、目标位置和生命周期 */
    public static class SwordWave {
        public Vec3 startPos;
        public Vec3 targetPos;
        public long spawnTime;
        public static final long DURATION_MS = 600L;

        public SwordWave(Vec3 start, Vec3 target) {
            this.startPos = start;
            this.targetPos = target;
            this.spawnTime = System.currentTimeMillis();
        }

        public float getProgress() {
            long elapsed = System.currentTimeMillis() - this.spawnTime;
            return Math.min(1.0f, (float)elapsed / (float)DURATION_MS);
        }

        public Vec3 getCurrentPos() {
            return startPos.lerp(targetPos, getProgress());
        }

        public boolean isExpired() {
            return getProgress() >= 1.0f;
        }
    }
}