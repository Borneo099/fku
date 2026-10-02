package fku.org.example.fku.features.autoattack; /* water */

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.tpaura.TpAuraFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AutoAttack（自动攻击）
 *
 * 长按左键 + 准星瞄准实体时自动触发攻击（无需手动点），支持冷却控制、实体过滤、攻击间隔、反作弊限速。
 *
 * ★ 与原版长按攻击的关系：
 *   原版长按左键会「边挖方块边对实体攻击」。本功能通过 Forge 的
 *   InputEvent.InteractionKeyMappingTriggered（攻击键触发，含长按重复）接管：当准星命中合法实体、
 *   且满足各项开关时，取消原版这次攻击并改为由本功能按可控节奏发动，从而：
 *     1) 瞄准方块时不接管，原版挖掘照常（避免与挖掘冲突）；
 *     2) 攻击节奏由 cooldownThreshold / attackInterval / maxAttacksPerSecond 控制，避免无脑连点触发反作弊。
 *
 * 纯客户端，@OnlyIn(Dist.CLIENT)。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class AutoAttackFeature {

    private static final Minecraft mc = Minecraft.getInstance();

    // 节奏控制状态
    private static int ticksSinceAttack = 0;   // 距上次攻击的触发次数（≈tick）
    private static long lastAttackMs = 0;       // 上次攻击时间戳（用于每秒限速）
    private static Entity lockedTarget = null;   // 锁定的目标（lockTarget 开启时有效）
    /** 选中目标：准星第一次压在合法实体上即“选中”，长按期间持续维持（即使准星移开/瞄到方块也保留），
     *  用于禁止破坏方块。与 lockedTarget 解耦，专为“选中而非准星在目标”的语义服务。 */
    private static Entity selectedTarget = null;

    public static boolean isEnabled() { return AutoAttackConfig.getInstance().enabled; }

    public static void setEnabled(boolean v) {
        AutoAttackConfig c = AutoAttackConfig.getInstance();
        c.enabled = v;
        AutoAttackConfig.save();
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§6[自动攻击] §" + (v ? "a已启用" : "c已禁用")), false);
        }
    }

    // ════════ 攻击键接管 ════════

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered e) {
        if (!e.isAttack()) return;              // 只处理攻击键（左键）
        if (!isEnabled()) return;
        // 接管时取消原版这次攻击/挖掘；实际攻击由 onClientTick 每 tick 驱动（避免事件不重复触发导致只打一次）。
        // 已“选中”目标时也取消（事件层拦截，确保长按期间绝不会触发破坏方块/对方块的交互）。
        if (wantsTakeOver() || isAttackingWithTarget()) e.setCanceled(true);
    }

    /** 主循环：每 tick 轮询左键长按状态驱动自动攻击；左键松开时清除锁定（锁敌只在按住期间维持）。
     *  同时维护 selectedTarget：准星首次压在合法实体上即选中，长按期间持续，用于禁止破坏方块。
     *  采用与 EndlessAimbotFeature 一致的 ClientTickEvent 轮询范式，规避 InteractionKeyMappingTriggered
     *  在部分环境下长按不重复触发、导致只尝试一次并被间隔门控彻底挡掉的问题。 */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (mc.player == null || mc.level == null) { lockedTarget = null; selectedTarget = null; ticksSinceAttack = 0; return; }
        if (!mc.options.keyAttack.isDown()) { lockedTarget = null; selectedTarget = null; return; }   // 松开左键 → 清锁
        AutoAttackConfig cfg = AutoAttackConfig.getInstance();
        // 选中逻辑：准星压在合法实体上即“选中”；之后即使准星移开/瞄到方块也持续（长按期间维持）
        if (mc.hitResult instanceof EntityHitResult eh) {
            Entity te = eh.getEntity();
            if (entityFilter(te, cfg)) selectedTarget = te;
        }
        if (selectedTarget != null && (!selectedTarget.isAlive() || mc.player.distanceTo(selectedTarget) > cfg.maxRange)) {
            selectedTarget = null;
        }
        if (isEnabled() && wantsTakeOver()) tryAttack();
    }

    /** 是否应接管本次攻击：左键长按 + 合法实体目标 + 通过各项开关。
     *  锁定中优先于挖掘保护：即使准星移开/瞄到方块也继续攻击锁定目标；
     *  未锁定时瞄到方块则不接管 → 原版挖掘照常（与挖掘冲突的唯一解）。 */
    private static boolean wantsTakeOver() {
        if (mc.player == null || mc.level == null) return false;
        if (mc.screen != null) return false;
        AutoAttackConfig cfg = AutoAttackConfig.getInstance();
        if (!mc.options.keyAttack.isDown()) return false;
        if (cfg.disableOnSneak && mc.player.isShiftKeyDown()) return false;
        if (cfg.disableInLiquid && mc.player.isInWater()) return false;
        if (cfg.onlyWhenHoldingWeapon && !holdingWeapon()) return false;
        if (TpAuraFeature.isEnabled()) return false;   // 与 TpAura 互斥：TpAura 激活时本功能让行
        // 锁定中：即使准星移开/瞄到方块也继续接管（锁敌优先于挖掘保护）
        if (cfg.lockTarget && lockedTarget != null && lockedTarget.isAlive()
                && mc.player.distanceTo(lockedTarget) <= cfg.maxRange) {
            return findTarget(cfg) != null;
        }
        // 未锁定：瞄到方块则不接管 → 原版挖掘照常
        if (mc.hitResult instanceof BlockHitResult) return false;
        return findTarget(cfg) != null;
    }

    private static boolean holdingWeapon() {
        ItemStack s = mc.player.getMainHandItem();
        if (s.isEmpty()) return false;
        Item it = s.getItem();
        return it instanceof SwordItem || it instanceof AxeItem || it instanceof TridentItem;
    }

    /** 选取目标：lockTarget 开启时维持锁定（脱离准星/瞄方块也跟，直到死亡或松开左键），
     *  丢失锁定后再按 onlyWhenLookingAt 重新索敌（准星实体 / 范围内最近）。 */
    private static Entity findTarget(AutoAttackConfig cfg) {
        if (mc.level == null || mc.player == null) return null;
        if (!mc.options.keyAttack.isDown()) { lockedTarget = null; return null; }

        // 维持锁定：存活、在范围、仍通过过滤 → 继续跟随
        if (cfg.lockTarget && lockedTarget != null) {
            if (lockedTarget.isAlive() && mc.player.distanceTo(lockedTarget) <= cfg.maxRange
                    && entityFilter(lockedTarget, cfg)) {
                return lockedTarget;
            }
            lockedTarget = null; // 锁定失效，重新索敌
        }

        Entity acquired;
        if (cfg.onlyWhenLookingAt) {
            if (mc.hitResult instanceof EntityHitResult eh) {
                Entity e = eh.getEntity();
                acquired = entityFilter(e, cfg) ? e : null;
            } else {
                acquired = null;
            }
        } else {
            Entity best = null;
            double bestDist = Double.MAX_VALUE;
            for (Entity e : mc.level.entitiesForRendering()) {
                if (!entityFilter(e, cfg)) continue;
                double d = mc.player.distanceTo(e);
                if (d <= cfg.maxRange && d < bestDist) { bestDist = d; best = e; }
            }
            acquired = best;
        }
        lockedTarget = acquired; // 记录锁定（lockTarget 关闭时此值无效但无害）
        return acquired;
    }

    /** 实际发动一次攻击（含冷却/间隔/限速门控）；不满足节奏则仅累计计数 */
    private static void tryAttack() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        AutoAttackConfig cfg = AutoAttackConfig.getInstance();
        Entity t = findTarget(cfg);
        if (t == null) return;

        ticksSinceAttack++;
        float progress = mc.player.getAttackStrengthScale(0.5f);
        boolean cooldownReady = cfg.attackMode.equals("FAST")
                ? (progress >= 0.0f)
                : (progress >= (float) cfg.cooldownThreshold);

        long now = System.currentTimeMillis();
        long minMs = 1000L / Math.max(1, cfg.maxAttacksPerSecond);
        boolean rateOk = (now - lastAttackMs) >= minMs;

        int need = cfg.attackInterval + (cfg.randomInterval ? ThreadLocalRandom.current().nextInt(0, 2) : 0);
        boolean intervalOk = ticksSinceAttack >= need;

        if (cooldownReady && rateOk && intervalOk) {
            mc.gameMode.attack(mc.player, t);
            if (cfg.swingHand) mc.player.swing(InteractionHand.MAIN_HAND);
            ticksSinceAttack = 0;
            lastAttackMs = now;
        }
    }

    // ════════ 实体过滤（复用 TpAura entityFilter 思路） ════════

    private static boolean entityFilter(Entity entity, AutoAttackConfig cfg) {
        if (!(entity instanceof LivingEntity) || !entity.isAlive() || entity == mc.player) return false;
        if (entity instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        if (mc.player.distanceTo(entity) > cfg.maxRange) return false;

        // 实体类型过滤
        Set<String> types = cfg.getEntityTypeSet();
        if (!types.isEmpty()) {
            String key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).getPath().toLowerCase();
            if (!types.contains(key)) return false;
        }

        if (cfg.ignoreNamed && entity.hasCustomName()) return false;
        if (cfg.ignoreTamed && entity instanceof TamableAnimal ta && ta.isTame()) return false;
        if (cfg.ignoreInvisible && entity.isInvisible()) return false;

        // 玩家名单（Whitelist / Blacklist）
        if (entity instanceof Player p) {
            // ignoreFriends：项目暂无独立好友系统，字段保留为 no-op（默认 false 不影响）
            if (!cfg.listMode.equals("Off")) {
                List<String> list = cfg.getPlayerList();
                String name = p.getScoreboardName();
                boolean inList = list.contains(name);
                if (cfg.listMode.equals("Whitelist") && !inList) return false;
                if (cfg.listMode.equals("Blacklist") && inList) return false;
            }
        }
        return true;
    }

    /** 是否正在持左键攻击一个已“选中”的目标（selectedTarget 非空且存活且在范围内）：用于禁止破坏方块。
     *  条件：启用 + 左键按住 + 未因潜行/水中禁用 + 未与 TpAura 互斥 + 开关开启 + 存在选中目标。
     *  ★ selectedTarget 是“选中”概念：准星第一次压在合法实体上即选中，之后长按期间持续（不要求准星仍在目标上）。
     *  返回 true 时：事件层取消交互 + MixinMultiPlayerGameMode 取消 start/continueDestroyBlock：
     *    - 不再误破坏/选中方块；
     *    - 自动切换工具/武器因不再触发“对方块攻击”而不会乱切；
     *    - 武器攻击冷却能正常回满。
     *  没有选中目标时返回 false → 原版挖掘照常。 */
    public static boolean isAttackingWithTarget() {
        if (!isEnabled()) return false;
        if (mc.player == null || mc.level == null) return false;
        if (mc.screen != null) return false;
        if (!mc.options.keyAttack.isDown()) return false;
        AutoAttackConfig cfg = AutoAttackConfig.getInstance();
        if (cfg.disableOnSneak && mc.player.isShiftKeyDown()) return false;
        if (cfg.disableInLiquid && mc.player.isInWater()) return false;
        if (TpAuraFeature.isEnabled()) return false;
        if (!cfg.noBlockBreakWhileAttacking) return false;
        return selectedTarget != null && selectedTarget.isAlive()
                && mc.player.distanceTo(selectedTarget) <= cfg.maxRange;
    }

    /** 供 MixinGameRendererPick 读取当前选中目标（用于把方块命中改写为实体命中） */
    public static Entity getSelectedTarget() {
        return selectedTarget;
    }

    /** 清状态用（功能关闭时由组件调用，避免残留计数/锁定） */
    public static void resetState() {
        ticksSinceAttack = 0;
        lastAttackMs = 0;
        lockedTarget = null;
        selectedTarget = null;
    }
}
