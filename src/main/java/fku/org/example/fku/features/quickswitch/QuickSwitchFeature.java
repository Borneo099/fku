package fku.org.example.fku.features.quickswitch; /* water */

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * QuickSwitchFeature — 鬼手秒切核心逻辑（该方法由赛博教员实现）
 *
 * ★ 职责：
 *   在攻击前切换热栏到武器槽位，攻击后延迟恢复原槽位。
 *   实现"1tick内继承上一把武器基础攻击力+蓄力 + 下一把武器魔咒"的秒切效果。
 *
 * ★ 设计思想（照搬击退方向模式——实践论）：
 *   击退方向模式：MixinMultiPlayerGameMode.attack() HEAD → setPending → 
 *   MixinConnectionAttackInterceptor → sendRotation → letAttackThrough
 *   
 *   秒切模式：MixinMultiPlayerGameMode.attack() HEAD → switchToSlot(weapon) → 
 *   scheduleRestore → letAttackThrough → 定时恢复
 *
 *   关键差异：击退方向在 Connection.send() 层发旋转包（必须在攻击包前），
 *   秒切在 attack() 层直接发切换包（同样在攻击包前），恢复用定时器。
 *
 * ★ 秒切原理（用户反馈）：
 *   - 切换武器后1tick内继承上一把武器基础攻击力和蓄力进度
 *   - 拥有下一把武器的魔咒效果和特性（破盾、手长）
 *   - 所以切换必须在攻击前完成，服务端先收到切换包，再收到攻击包
 *
 * ★ 参考：
 *   - 击退方向 FakeRotationManager 的 pending + 定时恢复模式
 *   - leavesHack Aura.doAura() 流程：switchToSlot → attack → restore
 *
 * ★ v5 架构变更（2026-06-28）：
 *   彻底照搬击退方向模式。不再使用 channel.write() 绕过 Connection.send()，
 *   改为在 MixinMultiPlayerGameMode.attack() HEAD 直接用 connection.send() 发切换包，
 *   攻击包由 MultiPlayerGameMode.attack() 原逻辑发送，恢复用 ClientTick 定时器。
 */
@OnlyIn(Dist.CLIENT)
public class QuickSwitchFeature {

    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * 初始化：加载配置
     * 由 Fku.java commonSetup 调用
     */
    public static void init() {
        QuickSwitchConfig.load();
    }

    /** 功能开关 */
    private static boolean enabled = false;

    /** 恢复定时器：>0 表示需要恢复，每 Tick 减 1，到 0 时恢复原槽位 */
    private static int restoreTimer = 0;
    /** 需要恢复到的原槽位 */
    private static int restoreSlot = -1;
    /** 恢复前等待的 Tick 数（给服务端足够时间处理攻击包） */
    private static final int RESTORE_DELAY_TICKS = 2;
    /** 九切模式：当前轮换槽位索引（0-8），每次攻击后递增 */
    private static int nineSlotIndex = 0;

    // ════════ 公开接口 ════════

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean v) {
        if (v && "OFF".equals(QuickSwitchConfig.getInstance().mode)) {
            QuickSwitchConfig.getInstance().mode = "SILENT";
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
    }

    public static void toggle() { setEnabled(!enabled); }

    /**
     * 检查 QuickSwitch 是否启用且处于有效模式
     */
    public static boolean canHandle() {
        if (!enabled) return false;
        if (mc.player == null) return false;
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        return cfg.enabled && cfg.isActiveMode();
    }

    // ════════ 攻击前处理（由 MixinConnectionAttackInterceptor 调用） ════════

    /**
     * 攻击前通过 Channel 发送切换包（由 MixinConnectionAttackInterceptor 调用）
     *
     * ★ 使用 channel.writeAndFlush() 而不走 connection.send()，确保：
     *   1. 切换包在攻击包之前写入 netty 管道
     *   2. 不触发 MixinConnectionAttackInterceptor 的递归保护
     *   3. 捕获所有攻击（手动 + TpAura + 第三方 Aura）
     *
     * ★ 模式说明：
     *   - SILENT：仅切换到武器槽位，攻击后恢复
     *   - NINE_SLOT：每攻击一次切换一个槽位（0→1→...→8→0→...），轮换攻击
     *
     * @param channel netty channel（由 MixinConnectionAttackInterceptor 提供）
     */
    public static void handlePreAttackViaChannel(io.netty.channel.Channel channel) {
        if (!canHandle()) return;
        if (mc.player == null || mc.level == null) return;

        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        int originalSlot = mc.player.getInventory().selected;

        switch (cfg.mode) {
            case "NINE_SLOT" -> {
                // ★ 九切模式：轮换所有9个热栏槽位，每攻击一次切一个
                int slot = nineSlotIndex % 9;
                if (slot != originalSlot) {
                    mc.player.getInventory().selected = slot;
                    channel.writeAndFlush(new ServerboundSetCarriedItemPacket(slot));
                    scheduleRestore(originalSlot);
                    if (cfg.visualFeedback) {
                        mc.player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§b[QuickSwitch] §f九切 → 槽位 " + slot),
                            true
                        );
                    }
                }
                nineSlotIndex = (nineSlotIndex + 1) % 9; // 轮换到下一个
            }
            case "SILENT" -> {
                // ★ 静默模式：切换到武器槽位，攻击后恢复
                int weaponSlot = findPreferredWeaponSlot(cfg);
                if (weaponSlot >= 0 && weaponSlot != originalSlot) {
                    mc.player.getInventory().selected = weaponSlot;
                    channel.writeAndFlush(new ServerboundSetCarriedItemPacket(weaponSlot));
                    scheduleRestore(originalSlot);
                    if (cfg.visualFeedback) {
                        mc.player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§b[QuickSwitch] §f静默秒切 → 槽位 " + weaponSlot),
                            true
                        );
                    }
                }
            }
            case "CUSTOM" -> {
                // ★ 自定义模式：按 prioritySlots 顺序切换
                int[] prioritySlots = cfg.prioritySlots;
                if (prioritySlots.length > 0) {
                    int slot = prioritySlots[nineSlotIndex % prioritySlots.length];
                    if (slot >= 0 && slot <= 8 && slot != originalSlot) {
                        mc.player.getInventory().selected = slot;
                        channel.writeAndFlush(new ServerboundSetCarriedItemPacket(slot));
                        scheduleRestore(originalSlot);
                        if (cfg.visualFeedback) {
                            mc.player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("§b[QuickSwitch] §f自定义切 → 槽位 " + slot),
                                true
                            );
                        }
                    }
                    nineSlotIndex = (nineSlotIndex + 1) % prioritySlots.length;
                }
            }
        }
    }

    // ════════ Tick 定时器 ════════

    /**
     * 每 Tick 调用：恢复定时器逻辑
     * 由 KnockbackFeature.onClientTick() 驱动
     */
    public static void tick() {
        if (restoreTimer > 0) {
            restoreTimer--;
            if (restoreTimer == 0 && restoreSlot >= 0) {
                doRestore();
            }
        }
    }

    /** 切换槽位（内部使用，用于恢复时发包） */
    private static void switchToSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        mc.player.getInventory().selected = slot;
        mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }

    /** 调度恢复：设置定时器，延迟 RESTORE_DELAY_TICKS 后恢复原槽位 */
    private static void scheduleRestore(int slot) {
        restoreTimer = RESTORE_DELAY_TICKS;
        restoreSlot = slot;
    }

    /** 执行恢复：切换回原槽位 */
    private static void doRestore() {
        if (restoreSlot < 0 || restoreSlot > 8) return;
        if (mc.player == null) return;

        switchToSlot(restoreSlot);
        restoreSlot = -1;

        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        if (cfg.visualFeedback && mc.player != null) {
            mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§b[QuickSwitch] §f已恢复原槽位"),
                true
            );
        }
    }

    /** 查找最佳武器槽位 */
    private static int findPreferredWeaponSlot(QuickSwitchConfig cfg) {
        int weaponSlot = findFirstWeaponInHotbar();
        if (weaponSlot >= 0) return weaponSlot;
        return findFirstMatchingItemInHotbar(cfg);
    }

    /**
     * 按武器类型（Sword/Axe/Trident）查找热栏中第一个武器
     */
    private static int findFirstWeaponInHotbar() {
        Inventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (item instanceof SwordItem || item instanceof AxeItem
                        || item instanceof TridentItem) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 从热栏查找第一个匹配自定义物品列表的物品
     */
    private static int findFirstMatchingItemInHotbar(QuickSwitchConfig cfg) {
        Inventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && isItemInList(stack, cfg)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 判断物品是否在自定义列表中
     */
    private static boolean isItemInList(ItemStack stack, QuickSwitchConfig cfg) {
        if (stack.isEmpty()) return false;
        String regName = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString().toLowerCase();
        String[] items = cfg.customItems.toLowerCase().split(",");
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) continue;
            if (regName.equals(item) || regName.endsWith(":" + item)) {
                return true;
            }
        }
        return false;
    }
}