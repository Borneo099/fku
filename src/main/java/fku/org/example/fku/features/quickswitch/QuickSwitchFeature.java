package fku.org.example.fku.features.quickswitch; /* water */

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * QuickSwitchFeature — 鬼手秒切（v7 — 多步序列状态机版）（该方法由赛博教员实现）
 *
 * ★ 状态机设计（v7 多步序列版）：
 *   ┌──────────────────────────────────────────────────────────────────────┐
 *   │  IDLE                                                                │
 *   │   │  (拦截攻击包, 构建切换序列)                                        │
 *   │   ▼                                                                  │
 *   │  MULTI_SWITCH ──(每tick发一个切换包)──▶ 全部发完 → 攻击包已发出        │
 *   │   │                                    (设置 actionTime=now+rttDelay)  │
 *   │   ▼                                                                  │
 *   │  WAITING_ATTACK ──(延迟到达)──▶ SWITCHING_BACK                       │
 *   │   │                        (设置 actionTime=now+rttDelay)            │
 *   │   ▼                                                                  │
 *   │   └──(延迟到达)── SWITCHING_BACK → 切回原槽位 → IDLE                  │
 *   └──────────────────────────────────────────────────────────────────────┘
 *
 * ★ v7 核心改动：
 *   - 支持多步切换序列（switchQueue），一次触发可连续切多个槽位
 *   - CUSTOM 模式：按 customItems 列表顺序全部秒切完 → 攻击 → 切回原槽位
 *   - SMART 模式：自动查找附魔评分最高的武器直接攻击，不切空手（避免空手伤害/耐久浪费）
 *
 * ★ 时序控制：
 *   所有切换包通过 channel.writeAndFlush() 在 HEAD 注入中紧贴攻击包前发出。
 *   切换序列在 MULTI_SWITCH 状态下逐 tick 发出（每 tick 一个）。
 *   攻击包由 MixinConnectionAttackInterceptor 在 onAttackPacket 返回后正常发送。
 *   切回包由 ClientTick 轮询驱动，延迟 rttDelay 毫秒后执行。
 */
@OnlyIn(Dist.CLIENT)
public class QuickSwitchFeature {

    private static final Minecraft mc = Minecraft.getInstance();

    // ════════════════════════════════════════════════════════════
    // ★ 状态机定义（v7 多步序列版）
    // ════════════════════════════════════════════════════════════

    /** 秒切状态 */
    public enum SwitchState {
        /** 空闲，无秒切活动 */
        IDLE,
        /**
         * 正在发送多步切换序列。
         * 每个 tick 从 switchQueue 取出一个槽位并发送切换包。
         * 全部发完后转入 WAITING_ATTACK。
         */
        MULTI_SWITCH,
        /**
         * 切换序列已全部发出，攻击包已到达/即将到达服务端。
         * 等待 rttDelay 毫秒后进入 SWITCHING_BACK。
         */
        WAITING_ATTACK,
        /**
         * 等待 rttDelay 毫秒后切回原槽位。
         * 切回完成后回到 IDLE。
         */
        SWITCHING_BACK
    }

    private static SwitchState state = SwitchState.IDLE;

    /** 原始槽位（用于切回） */
    private static int originalSlot = -1;

    /** 切换序列队列：存储待切换的槽位列表 */
    private static List<Integer> switchQueue = new CopyOnWriteArrayList<>();

    /** 当前切换步骤索引 */
    private static int switchStepIndex = 0;

    /** 动作时间戳（用于 RTT 延迟控制） */
    private static long actionTime = 0;

    /** 每个 MULTI_SWITCH 步骤之间的间隔（毫秒），默认 10ms */
    private static final int STEP_INTERVAL_MS = 10;

    // ════════════════════════════════════════════════════════════
    // ★ 功能开关
    // ════════════════════════════════════════════════════════════

    private static boolean enabled = false;

    public static void init() {
        QuickSwitchConfig.load();
        enabled = QuickSwitchConfig.getInstance().enabled;
    }

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean v) {
        if (v && "OFF".equals(QuickSwitchConfig.getInstance().mode)) {
            QuickSwitchConfig.getInstance().mode = "SMART";
            QuickSwitchConfig.save();
        }
        enabled = v;
        QuickSwitchConfig.getInstance().setEnabled(v);
        if (mc.player != null) {
            mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    v ? "§b[QuickSwitch] §a已启用" : "§b[QuickSwitch] §c已禁用"
                ),
                false
            );
        }
        if (!v) forceReset();
    }

    public static void toggle() { setEnabled(!enabled); }

    public static boolean isIdle() { return state == SwitchState.IDLE; }

    // ════════════════════════════════════════════════════════════
    // ★ 攻击包拦截（由 MixinConnectionAttackInterceptor HEAD 调用）
    // ════════════════════════════════════════════════════════════

    /**
     * 拦截到攻击包时调用 — v7 多步序列入口。
     *
     * 流程：
     *   1. 仅在 IDLE 状态下触发
     *   2. 保存原槽位
     *   3. 根据模式构建切换序列（switchQueue）
     *   4. 将序列第一个槽位的切换包写入 channel（紧贴攻击包之前）
     *   5. 设置 state = MULTI_SWITCH，后续步骤由 tick() 驱动
     *
     * @param channel netty channel
     */
    public static void onAttackPacket(io.netty.channel.Channel channel) {
        if (state != SwitchState.IDLE) return;
        if (!canHandle()) return;
        if (mc.player == null || mc.level == null) return;
        if (channel == null || !channel.isOpen()) return;

        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        int curSlot = mc.player.getInventory().selected;

        switch (cfg.mode) {
            case "SMART" -> {
                // ★ SMART 模式：附魔武器 → 空手/无耐久 → 攻击 → 切回
                List<Integer> sequence = buildSmartSequence();
                if (sequence.isEmpty()) return; // 无可用物品

                // 如果序列只有一个且等于当前槽位，无需切换
                if (sequence.size() == 1 && sequence.get(0) == curSlot) return;

                // 保存原槽位
                originalSlot = curSlot;

                // 构建切换队列
                switchQueue.clear();
                switchQueue.addAll(sequence);
                switchStepIndex = 0;

                // 发送序列中第一个切换包
                int firstSlot = switchQueue.get(0);
                mc.player.getInventory().selected = firstSlot;
                channel.writeAndFlush(new ServerboundSetCarriedItemPacket(firstSlot));
                switchStepIndex = 1;

                // 进入 MULTI_SWITCH 状态
                state = SwitchState.MULTI_SWITCH;
                actionTime = System.currentTimeMillis();

                if (cfg.visualFeedback) {
                    mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                            "§b[QuickSwitch] §f智能秒切 → 序列 " + sequence + " (共" + sequence.size() + "步)"
                        ),
                        true
                    );
                }
            }
            case "CUSTOM" -> {
                // ★ CUSTOM 模式：按顺序秒切完所有匹配物品 → 攻击 → 切回
                List<Integer> sequence = buildCustomSequence(cfg);
                if (sequence.isEmpty()) return; // 无匹配物品

                // 保存原槽位（CUSTOM 也需要切回了）
                originalSlot = curSlot;

                // 构建切换队列
                switchQueue.clear();
                switchQueue.addAll(sequence);
                switchStepIndex = 0;

                // 发送序列中第一个切换包
                int firstSlot = switchQueue.get(0);
                mc.player.getInventory().selected = firstSlot;
                channel.writeAndFlush(new ServerboundSetCarriedItemPacket(firstSlot));
                switchStepIndex = 1;

                // 进入 MULTI_SWITCH 状态
                state = SwitchState.MULTI_SWITCH;
                actionTime = System.currentTimeMillis();

                if (cfg.visualFeedback) {
                    mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                            "§b[QuickSwitch] §f自定义切 → 序列 " + sequence + " (共" + sequence.size() + "步)"
                        ),
                        true
                    );
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ Tick 轮询（由 KnockbackFeature.onClientTick END 调用）
    // ════════════════════════════════════════════════════════════

    /**
     * 每 ClientTick 调用一次，驱动状态机前进。
     *
     * MULTI_SWITCH → 逐步发完切换包 → WAITING_ATTACK
     * WAITING_ATTACK → 延迟到达 → SWITCHING_BACK
     * SWITCHING_BACK → 延迟到达 → IDLE（执行切回）
     */
    public static void tick() {
        if (state == SwitchState.IDLE) return;
        if (mc.player == null) {
            forceReset();
            return;
        }

        long now = System.currentTimeMillis();
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();

        switch (state) {
            case MULTI_SWITCH -> {
                // ★ 检查是否到了发下一步的时间
                if (now >= actionTime + STEP_INTERVAL_MS) {
                    if (switchStepIndex < switchQueue.size()) {
                        // 还有未发的切换包 → 发下一个
                        int nextSlot = switchQueue.get(switchStepIndex);
                        mc.player.getInventory().selected = nextSlot;
                        // MULTI_SWITCH 阶段的后续切换包用 connection.send()
                        if (mc.player.connection != null) {
                            mc.player.connection.send(new ServerboundSetCarriedItemPacket(nextSlot));
                        }
                        switchStepIndex++;
                        actionTime = now;
                    } else {
                        // ★ 全部切换包发完毕 → 进入等待攻击阶段
                        state = SwitchState.WAITING_ATTACK;
                        actionTime = now + cfg.rttDelay;
                    }
                }
            }
            case WAITING_ATTACK -> {
                if (now >= actionTime) {
                    // 攻击包预计已到达服务端 → 进入切回阶段
                    state = SwitchState.SWITCHING_BACK;
                    actionTime = now + cfg.rttDelay;
                }
            }
            case SWITCHING_BACK -> {
                if (now >= actionTime) {
                    // 延迟到达 → 切回原槽位
                    doRestore();
                    state = SwitchState.IDLE;
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // ★ 序列构建方法
    // ════════════════════════════════════════════════════════════

    /**
     * ★ SMART 模式：构建 [附魔武器槽位] 切换序列
     *
     * 逻辑：
     *   1. 在热栏中查找附魔增伤评分最高的武器/工具
     *   2. 如果找到且不是当前手持槽位，返回 [该槽位]
     *   3. 如果就是当前手持槽位或没有合适武器，返回空列表（不触发秒切）
     *
     * ★ v8 变更：不再切空手/无耐久物品，直接使用附魔武器攻击。
     *   实测显示切空手攻击不消耗耐久但产生空手伤害（≈0），
     *   且大部分服务器会记录耐久消耗，造成数据不一致。
     *
     * @return 槽位列表，空列表表示无可用物品或不需切换
     */
    private static List<Integer> buildSmartSequence() {
        List<Integer> result = new ArrayList<>();

        // 1. 查找最佳附魔武器
        int enchSlot = findBestEnchantedWeaponSlot();
        if (enchSlot < 0) return result; // 无附魔武器，不触发秒切

        int curSlot = mc.player.getInventory().selected;

        // 2. 如果附魔武器就是当前手持的，无需切换
        if (enchSlot == curSlot) return result;

        // 3. 仅返回附魔武器槽位（不再切空手）
        result.add(enchSlot);
        return result;
    }

    /**
     * ★ CUSTOM 模式：构建按 customItems 顺序的全量切换序列
     *
     * 逻辑：
     *   1. 解析 customItems 字符串为物品ID列表（逗号分隔）
     *   2. 按 ID 顺序遍历热栏，找到每个 ID 对应的槽位
     *   3. 找不到的跳过，找到的全部加入序列
     *   4. 返回完整序列
     *
     * @param cfg 配置实例
     * @return 槽位列表（有序），空列表表示无匹配物品
     */
    private static List<Integer> buildCustomSequence(QuickSwitchConfig cfg) {
        List<Integer> result = new ArrayList<>();
        if (mc.player == null) return result;

        Inventory inv = mc.player.getInventory();
        String[] itemIds = cfg.customItems.split(",");
        if (itemIds.length == 0) return result;

        for (String rawId : itemIds) {
            String targetId = rawId.trim().toLowerCase();
            if (targetId.isEmpty()) continue;

            // 在热栏中查找所有匹配该ID的物品（取第一个匹配的）
            boolean found = false;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = inv.getItem(slot);
                if (stack.isEmpty()) continue;

                String regName = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().toLowerCase();
                if (regName.equals(targetId) || regName.endsWith(":" + targetId)) {
                    // 避免重复添加同一槽位
                    if (result.isEmpty() || result.get(result.size() - 1) != slot) {
                        result.add(slot);
                    }
                    found = true;
                    break; // 每个ID只取第一个匹配
                }
            }
            // 找不到就跳过这个ID
        }

        return result;
    }

    // ════════════════════════════════════════════════════════════
    // ★ 辅助方法
    // ════════════════════════════════════════════════════════════

    /**
     * 使用 connection.send() 切回原槽位
     */
    private static void doRestore() {
        if (originalSlot < 0 || originalSlot > 8) return;
        if (mc.player == null || mc.player.connection == null) return;

        int slot = originalSlot;

        // 清理状态
        switchQueue.clear();
        switchStepIndex = 0;
        originalSlot = -1;

        mc.player.getInventory().selected = slot;
        mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));

        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        if (cfg.visualFeedback) {
            mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§b[QuickSwitch] §f已恢复原槽位 " + slot),
                true
            );
        }
    }

    /**
     * 强制重置状态机（模块禁用/玩家死亡时调用）
     */
    public static void forceReset() {
        if (state != SwitchState.IDLE) {
            // 如果有待恢复的槽位，立即切回
            if (originalSlot >= 0 && mc.player != null && mc.player.connection != null) {
                mc.player.getInventory().selected = originalSlot;
                mc.player.connection.send(new ServerboundSetCarriedItemPacket(originalSlot));
            }
            switchQueue.clear();
            switchStepIndex = 0;
            originalSlot = -1;
            state = SwitchState.IDLE;
        }
    }

    private static boolean canHandle() {
        if (!enabled) return false;
        if (mc.player == null) return false;
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        return cfg.enabled && cfg.isActiveMode();
    }

    // ════════════════════════════════════════════════════════════
    // ★ 武器选择与附魔增伤算法（保留原有算法不变）
    // ════════════════════════════════════════════════════════════

    /**
     * SMART 模式：查找热栏中附魔增伤评分最高的物品
     *
     * @return 槽位索引（0-8），无合适物品则返回 -1
     */
    private static int findBestEnchantedWeaponSlot() {
        if (mc.player == null) return -1;
        Inventory inv = mc.player.getInventory();
        int bestSlot = -1;
        double bestScore = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            double score = calculateWeaponScore(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    /**
     * ★ 新增：查找空手或无耐久消耗物品的槽位
     *
     * 用于 SMART 模式的第二步——切到不消耗耐久的物品来执行实际攻击。
     * 优先级：
     *   1. 完全空的槽位（最优——真正空手）
     *   2. 不消耗耐久的物品（方块、食物、药水、其他非工具类物品）
     *   3. 耐久值已满/接近满的工具（减少磨损影响）
     *
     * 排除当前手持的附魔武器槽位（避免切到自己）。
     *
     * @return 槽位索引（0-8），无合适物品则返回 -1
     */
    private static int findEmptyHandOrNoDurabilitySlot() {
        if (mc.player == null) return -1;
        Inventory inv = mc.player.getInventory();

        // 第一优先级：完全空的槽位
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).isEmpty()) {
                return i;
            }
        }

        // 第二优先级：不消耗耐久的物品
        int bestSlot = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();

            // 检测是否为不消耗耐久的物品类型
            int score = getNoDurabilityScore(item);

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    /**
     * 评估物品的"无耐久消耗"适合度（分数越高越适合用来替代攻击）
     *
     * 方块/食物/药水等完全不消耗耐久的物品得分最高
     * 工具/武器得分最低（因为它们会消耗耐久）
     */
    private static int getNoDurabilityScore(Item item) {
        // ★ 高分：完全不消耗耐久的物品
        if (item instanceof BlockItem) return 100;           // 方块
        if (item.isEdible()) return 90;                       // 食物（1.20.1 用 isEdible()）
        if (item instanceof ArrowItem) return 85;            // 箭矢
        if (item instanceof PotionItem) return 80;           // 药水
        if (item instanceof SnowballItem) return 80;         // 雪球/蛋等投掷物
        if (item instanceof EggItem) return 80;
        if (item instanceof EnderpearlItem) return 80;       // 末影珍珠
        if (item instanceof FireChargeItem) return 80;       // 火焰弹
        if (item instanceof FishingRodItem) return 70;       // 鱼竿（低耐耗）
        if (item instanceof ShieldItem) return 70;           // 盾牌
        if (item instanceof FlintAndSteelItem) return 65;    // 打火石
        if (item instanceof ShearsItem) return 60;           // 剪刀
        if (item instanceof LeadItem) return 60;             // 拴绳
        if (item instanceof BrushItem) return 60;            // 刷子
        if (item instanceof HorseArmorItem) return 50;       // 马铠

        // ★ 低分：会消耗耐久的工具和武器
        if (item instanceof SwordItem) return 10;
        if (item instanceof AxeItem) return 10;
        if (item instanceof PickaxeItem) return 10;
        if (item instanceof ShovelItem) return 10;
        if (item instanceof HoeItem) return 10;
        if (item instanceof TridentItem) return 10;
        // MaceItem 在 1.20.1 不存在（1.21+ 才有），用 DiggerItem 作为兜底
        if (item instanceof DiggerItem) return 10;           // 镐/铲/锄的基类
        if (item instanceof BowItem) return 15;
        if (item instanceof CrossbowItem) return 15;
        if (item instanceof ArmorItem) return 20;

        // ★ 默认中等分数（未知物品类型，保守估计）
        return 30;
    }

    /**
     * 计算一件物品的综合战斗评分
     *
     * ★ v8 纯附魔+特属性评分：
     *   - 去除基础攻击力，只计算附魔增伤
     *   - 特殊武器属性加分：斧头(破盾)+20，三叉戟(距离)+15
     *   - 无附魔物品评分 = 0（不会被秒切选中）
     *
     * 评分 = 附魔增伤总分 + 特殊属性加分
     */
    private static double calculateWeaponScore(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        double score = calculateEnchantmentDamageScore(stack);
        if (score <= 0) return 0; // 无附魔 → 不秒切

        // ★ 特殊武器属性加分
        Item item = stack.getItem();
        if (item instanceof AxeItem) score += 20;     // 斧头破盾
        else if (item instanceof TridentItem) score += 15; // 三叉戟距离

        return score;
    }

    /**
     * 获取物品的基础攻击力（无附魔时）
     */
    private static double getBaseAttackDamage(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        if (modifiers == null || modifiers.isEmpty()) {
            Item item = stack.getItem();
            if (item instanceof SwordItem s) return s.getDamage();
            if (item instanceof AxeItem) return 7.0;
            if (item instanceof TridentItem) return 7.0;
            if (item instanceof PickaxeItem) return 3.0;
            if (item instanceof ShovelItem) return 2.5;
            if (item instanceof HoeItem) return 1.0;
            return 1.0;
        }
        double total = 0;
        for (var entry : modifiers.entries()) {
            if (entry.getKey() != null && entry.getKey().equals(Attributes.ATTACK_DAMAGE)) {
                total += entry.getValue().getAmount();
            }
        }
        return total > 0 ? total : 1.0;
    }

    /**
     * ★ 核心算法：计算附魔增伤总分
     */
    private static double calculateEnchantmentDamageScore(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments == null || enchantments.isEmpty()) return 0;

        double totalScore = 0;

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();
            if (ench == null || level <= 0) continue;

            double factor = getEnchantmentDamageFactor(ench, stack);
            totalScore += level * factor;
        }

        return totalScore;
    }

    /**
     * 获取单个附魔的伤害增强因子
     */
    private static double getEnchantmentDamageFactor(Enchantment ench, ItemStack stack) {
        ResourceLocation regName = ForgeRegistries.ENCHANTMENTS.getKey(ench);
        if (regName != null) {
            String path = regName.getPath().toLowerCase();

            if (path.contains("sharp")) return 0.5;
            if (path.contains("smite") || path.contains("undead")) return 1.0;
            if (path.contains("bane") || path.contains("arthropod")) return 1.0;
            if (path.contains("fire")) return 0.8;
            if (path.contains("power") && !path.contains("resolution")) return 0.5;
            if (path.contains("impaling")) return 0.8;
            if (path.contains("knockback") || path.contains("punch")) return 0.3;
            if (path.contains("sweep")) return 0.4;
            if (path.contains("damage") || path.contains("attack")) return 0.4;
        }

        Item item = stack.getItem();
        if (item instanceof SwordItem || item instanceof AxeItem ||
            item instanceof TridentItem || item instanceof PickaxeItem ||
            item instanceof ShovelItem) {
            return 0.3;
        }

        return 0.2;
    }
}
