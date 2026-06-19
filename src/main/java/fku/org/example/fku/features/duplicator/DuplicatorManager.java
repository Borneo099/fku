package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import static net.minecraft.core.Direction.DOWN;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 三叉戟/箭矢复制工具 — 状态机管理器
 *
 * 核心原理（SPIGOT-5608 + Killetx/TridentDupe）：
 *
 * 【三叉戟复制】
 *   1. 主动使用物品（useItem）开始蓄力
 *   2. 蓄力后 SWAP 合成格 ↔ 热栏[0]，将三叉戟移出主手
 *   3. 服务端处理后物品实例变更，旧实例无法追踪
 *   4. RELEASE_USE_ITEM → 服务端找不到实例 → 三叉戟不被消耗
 *
 * 【箭矢复制】
 *   1. 弓开始蓄力，箭矢在背包中
 *   2. PICKUP 箭矢 → 合成格 → 取回 → 还原（4次点击刷新实例）
 *   3. RELEASE → 服务端找不到原箭矢实例 → 箭矢不被消耗
 *
 * 参考资料：
 *   - Killletx/TridentDupe: https://github.com/Killetx/TridentDupe
 *   - SPIGOT-5608: setStorageContents → 物品实例变更机制
 */
public class DuplicatorManager {

    private static final DuplicatorManager INSTANCE = new DuplicatorManager();

    private Phase phase = Phase.IDLE;
    private int tickCounter = 0;
    private boolean eventsRegistered;
    private int consecutiveFails = 0;

    private enum Phase {
        IDLE,      // 空闲
        ARMING,    // 准备蓄力（找物品 → SWAP到slot 0 → useItem）
        HOLDING,   // 等待蓄力
        DUPING,    // 执行复制（三叉戟 SWAP / 箭矢 PICKUP + RELEASE）
        COOLDOWN   // 冷却后下一轮
    }

    private DuplicatorManager() {}

    public static DuplicatorManager getInstance() { return INSTANCE; }

    public static void registerEventHandlers() {
        if (!INSTANCE.eventsRegistered) {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            INSTANCE.eventsRegistered = true;
            Fku.LOGGER.info("[Duplicator] 事件处理器已注册");
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        tick();
    }

    @SubscribeEvent
    public void onPlayerDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        reset();
    }

    // ====================================================================
    //  tick — 主状态机
    // ====================================================================
    private void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        DuplicatorConfig cfg = getConfig();
        if (!cfg.enableTrident && !cfg.enableArrow) {
            phase = Phase.IDLE;
            return;
        }

        switch (phase) {
            case IDLE -> { tickCounter = 0; phase = Phase.ARMING; }

            case ARMING -> {
                int slot = findBestWeaponSlot(player, cfg);
                if (slot == -1) {
                    tickCounter++;
                    if (tickCounter >= 20) { phase = Phase.IDLE; tickCounter = 0; }
                    break;
                }
                // 不在热栏0则 SWAP 过去
                if (slot != 0) {
                    mc.gameMode.handleInventoryMouseClick(
                            player.containerMenu.containerId,
                            36 + slot, 0, ClickType.SWAP, player);
                }
                mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                phase = Phase.HOLDING;
                tickCounter = 0;
                Fku.LOGGER.debug("[Duplicator] 开始蓄力 slot={}", slot);
            }

            case HOLDING -> {
                tickCounter++;
                if (tickCounter >= cfg.holdDuration) {
                    phase = Phase.DUPING;
                    tickCounter = 0;
                }
            }

            case DUPING -> {
                try {
                    ItemStack usingItem = player.getUseItem();
                    boolean isTrident = usingItem.getItem() instanceof TridentItem;
                    boolean isBow     = usingItem.getItem() instanceof BowItem;

                    if (isTrident) {
                        // ── 三叉戟：SWAP 移出主手 ──
                        mc.gameMode.handleInventoryMouseClick(
                                player.containerMenu.containerId,
                                3, 0, ClickType.SWAP, player);
                        if (cfg.dropTridents) {
                            mc.gameMode.handleInventoryMouseClick(
                                    player.containerMenu.containerId,
                                    44, 0, ClickType.THROW, player);
                        }
                    } else if (isBow) {
                        // ── 弓/箭矢：PICKUP 刷新背包中的箭矢实例 ──
                        refreshArrowInstance(mc, player);
                    }

                    // RELEASE_USE_ITEM — 服务端找不到原实例 → 不消耗
                    mc.getConnection().send(new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                            BlockPos.ZERO, DOWN, 0));

                    consecutiveFails = 0;
                    Fku.LOGGER.debug("[Duplicator] 复制完成 (trident={}, bow={})", isTrident, isBow);
                } catch (Exception e) {
                    consecutiveFails++;
                    Fku.LOGGER.error("[Duplicator] 复制异常: {}", e.getMessage());
                    if (consecutiveFails >= 3) {
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(
                                        "§c[复制工具] 连续失败3次，服务器可能已修复此漏洞"), true);
                        phase = Phase.IDLE;
                        break;
                    }
                }
                phase = Phase.COOLDOWN;
                tickCounter = 0;
            }

            case COOLDOWN -> {
                tickCounter++;
                if (tickCounter >= cfg.dupeDelay) { phase = Phase.IDLE; tickCounter = 0; }
            }
        }
    }

    // ====================================================================
    //  refreshArrowInstance — PICKUP 箭矢 → 合成格 → 回原位（刷新实例）
    //
    //  背包布局（InventoryMenu）：
    //    热栏 (0-8)  → 容器 slot 36-44
    //    存储 (9-35) → 容器 slot 9-35
    //  合成格 slot 1：任意非冲突的合成槽
    //
    //  操作序列（4次 PICKUP）：
    //    PICKUP 箭矢slot       → 光标持有箭矢
    //    PICKUP 合成格slot 1   → 放入合成格
    //    PICKUP 合成格slot 1   → 从合成格取回
    //    PICKUP 原slot         → 还原原位
    // ====================================================================
    private void refreshArrowInstance(Minecraft mc, LocalPlayer player) {
        for (int invIdx = 0; invIdx < 36; invIdx++) {
            ItemStack stack = player.getInventory().getItem(invIdx);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ArrowItem)) continue;

            int containerSlot = invIdx < 9 ? 36 + invIdx : invIdx;
            int containerId = player.containerMenu.containerId;

            mc.gameMode.handleInventoryMouseClick(containerId, containerSlot, 0, ClickType.PICKUP, player);
            mc.gameMode.handleInventoryMouseClick(containerId, 1,             0, ClickType.PICKUP, player);
            mc.gameMode.handleInventoryMouseClick(containerId, 1,             0, ClickType.PICKUP, player);
            mc.gameMode.handleInventoryMouseClick(containerId, containerSlot, 0, ClickType.PICKUP, player);

            Fku.LOGGER.debug("[Duplicator] 箭矢实例已刷新 (invIdx={}, item={})",
                    invIdx, stack.getItem().getDescriptionId());
            return; // 只处理第一组箭矢
        }
        Fku.LOGGER.warn("[Duplicator] 未找到箭矢，跳过刷新");
    }

    // ====================================================================
    //  findBestWeaponSlot — 热栏中找三叉戟/弓，优先高耐久
    // ====================================================================
    private int findBestWeaponSlot(LocalPlayer player, DuplicatorConfig cfg) {
        int bestSlot = -1;
        int bestDurability = Integer.MAX_VALUE;
        boolean hasRiptideWarned = false;

        for (int i = 0; i < 9; i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            boolean isTrident = cfg.enableTrident && stack.getItem() instanceof TridentItem;
            boolean isBow     = cfg.enableArrow     && stack.getItem() instanceof BowItem;

            if (isTrident) {
                int riptide = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.RIPTIDE, stack);
                if (riptide > 0) {
                    if (!hasRiptideWarned) {
                        hasRiptideWarned = true;
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(
                                        "§e[复制工具] 激流附魔三叉戟跳过（不适用此复制方式）"), true);
                    }
                    continue;
                }
                int dur = stack.getMaxDamage() - stack.getDamageValue();
                if (dur < bestDurability) {
                    bestDurability = dur;
                    bestSlot = i;
                }
            } else if (isBow && bestSlot == -1) {
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private DuplicatorConfig getConfig() { return DuplicatorConfig.getInstance(); }

    private void reset() {
        phase = Phase.IDLE;
        tickCounter = 0;
        consecutiveFails = 0;
    }
}