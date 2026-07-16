package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
//? if neoforge {
import net.neoforged.neoforge.client.event.ClientTickEvent;
//? }

/**
 * 三叉戟复制管理器 — 移植自 lexis TridentDupeHack
 * <p>
 * 新增：覆盖屏幕、Grim绕过、受伤自停、自动清理背包
 */
public class DuplicatorManager {

    private static final DuplicatorManager INSTANCE = new DuplicatorManager();

    private Phase phase = Phase.IDLE;
    private int tickCounter = 0;
    private int consecutiveFails = 0;
    private boolean stopping = false;
    private float lastHealth = -1;
    private int dropScanIndex = 0;
    private boolean eventsRegistered = false;

    private enum Phase { IDLE, ARMING, HOLDING, DUPING, COOLDOWN, DROPPING }

    private DuplicatorManager() {}

    public static DuplicatorManager getInstance() { return INSTANCE; }

    public static void registerEventHandlers() {
        if (!INSTANCE.eventsRegistered) {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            INSTANCE.eventsRegistered = true;
        }
    }

    public void start(Minecraft mc) {
        if (stopping) return;
        reset();
        if (mc.player != null) lastHealth = mc.player.getHealth();
        mc.setScreen(new DupeScreen());
    }

    public void stop() {
        if (stopping) return;
        stopping = true;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof DupeScreen) mc.setScreen(null);
        releaseUseKey(mc);
        reset();
        stopping = false;
    }

    public boolean isRunning() { return phase != Phase.IDLE; }

    @SubscribeEvent
    //? if neoforge {
        public void onClientTick(ClientTickEvent.Pre event) {
    //? } else {
        public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
    //? }
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof DupeScreen)) return;
        tick(mc);
    }

    @SubscribeEvent
    public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        stopping = true;
        reset();
        stopping = false;
    }

    private void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.getConnection() == null) return;
        var cfg = DuplicatorConfig.getInstance();

        // 受伤检测
        if (cfg.autoCloseOnDamage) {
            float hp = player.getHealth();
            if (lastHealth > 0 && hp < lastHealth) {
                player.displayClientMessage(Component.literal("§e[三叉戟复制] §f检测到受伤，自动关闭"), false);
                stop();
                return;
            }
            lastHealth = hp;
        }

        switch (phase) {
            case IDLE -> { tickCounter = 0; phase = Phase.ARMING; }
            case ARMING -> {
                int slot = findBestWeaponSlot(player);
                if (slot == -1) {
                    tickCounter++;
                    if (tickCounter >= 20) { phase = Phase.IDLE; tickCounter = 0; }
                    return;
                }
                if (slot != 0) {
                    mc.gameMode.handleInventoryMouseClick(
                            player.containerMenu.containerId, 36 + slot, 0, ClickType.SWAP, player);
                }
                // 按下使用键并发送包
                mc.options.keyUse.setDown(true);
                mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0));

                if (cfg.bypassGrim) {
                    mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                            player.getX(), player.getY(), player.getZ(), player.onGround()));
                    mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                            player.getYRot() + 0.1f, player.getXRot(), player.onGround()));
                }
                phase = Phase.HOLDING;
                tickCounter = 0;
            }
            case HOLDING -> {
                // 持续按住使用键
                mc.options.keyUse.setDown(true);
                tickCounter++;
                if (tickCounter >= cfg.holdDuration) {
                    releaseUseKey(mc);
                    phase = Phase.DUPING;
                    tickCounter = 0;
                }
            }
            case DUPING -> {
                try {
                    if (cfg.bypassGrim) {
                        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                                player.getX(), player.getY(), player.getZ(), player.onGround()));
                    }
                    // SWAP 合成格 slot 3 ↔ 热栏[0]
                    mc.gameMode.handleInventoryMouseClick(
                            player.containerMenu.containerId, 3, 0, ClickType.SWAP, player);
                    if (cfg.dropTridents) {
                        mc.gameMode.handleInventoryMouseClick(
                                player.containerMenu.containerId, 44, 0, ClickType.THROW, player);
                    }
                    mc.getConnection().send(new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                            BlockPos.ZERO, Direction.DOWN, 0));
                    consecutiveFails = 0;
                } catch (Exception e) {
                    consecutiveFails++;
                    if (consecutiveFails >= 3) {
                        player.displayClientMessage(Component.literal(
                                "§c[三叉戟复制] 连续失败3次，服务器可能已修复此漏洞"), false);
                        stop();
                        return;
                    }
                }
                phase = Phase.COOLDOWN;
                tickCounter = 0;
            }
            case COOLDOWN -> {
                tickCounter++;
                if (tickCounter >= cfg.dupeDelay) {
                    if (cfg.autoCleanInventory && countExtraTridents(player) >= 3) {
                        phase = Phase.DROPPING;
                        tickCounter = 0;
                        dropScanIndex = -1;
                    } else {
                        phase = Phase.IDLE;
                        tickCounter = 0;
                    }
                }
            }
            case DROPPING -> {
                if (dropScanIndex == -1) {
                    dropScanIndex = 0;
                } else if (dropScanIndex == -2) {
                    phase = Phase.IDLE;
                    tickCounter = 0;
                } else if (cfg.bypassGrim) {
                    int slot = findNextTridentSlot(player, dropScanIndex);
                    if (slot == -1) {
                        dropScanIndex = -2;
                    } else {
                        int containerSlot = slot < 9 ? 36 + slot : slot;
                        mc.gameMode.handleInventoryMouseClick(
                                player.containerMenu.containerId, containerSlot, 1, ClickType.THROW, player);
                        dropScanIndex = slot + 1;
                    }
                } else {
                    for (int i = 1; i < 36; i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (!stack.isEmpty() && stack.getItem() instanceof TridentItem) {
                            int containerSlot = i < 9 ? 36 + i : i;
                            mc.gameMode.handleInventoryMouseClick(
                                    player.containerMenu.containerId, containerSlot, 1, ClickType.THROW, player);
                        }
                    }
                    dropScanIndex = -2;
                }
            }
        }
    }

    private void releaseUseKey(Minecraft mc) {
        mc.options.keyUse.setDown(false);
    }

    private int findBestWeaponSlot(LocalPlayer player) {
        int bestSlot = -1;
        int bestDurability = Integer.MAX_VALUE;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof TridentItem)) continue;
            int riptide = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.RIPTIDE, stack);
            if (riptide > 0) continue;
            int dur = stack.getMaxDamage() - stack.getDamageValue();
            if (dur < bestDurability) { bestDurability = dur; bestSlot = i; }
        }
        return bestSlot;
    }

    private int countExtraTridents(LocalPlayer player) {
        int count = 0;
        for (int i = 1; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof TridentItem) count++;
        }
        return count;
    }

    private int findNextTridentSlot(LocalPlayer player, int start) {
        for (int i = Math.max(1, start); i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof TridentItem) return i;
        }
        return -1;
    }

    public void reset() {
        releaseUseKey(Minecraft.getInstance());
        phase = Phase.IDLE;
        tickCounter = 0;
        consecutiveFails = 0;
        lastHealth = -1;
        dropScanIndex = 0;
    }

    // ────────── 覆盖屏幕 ──────────

    public static class DupeScreen extends Screen {
        protected DupeScreen() {
            super(Component.literal("三叉戟复制"));
        }

        @Override
        public void render(GuiGraphics g, int mx, int my, float pt) {
            if (minecraft != null && font != null) {
                renderBackground(g);
                g.drawString(font, "§6正在自动快速复制三叉戟中...", width / 2, height / 2 - 20, 0xFFFFFF);
                g.drawString(font, "§7按 Esc 关闭功能并退出", width / 2, height / 2 + 5, 0xAAAAAA);
                super.render(g, mx, my, pt);
            }
        }

        @Override
        public void onClose() {
            if (!DuplicatorManager.getInstance().stopping) {
                DuplicatorManager.getInstance().stop();
            }
        }

        @Override
        public boolean isPauseScreen() { return false; }
    }
}
