package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import static net.minecraft.core.Direction.DOWN;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DuplicatorManager {

    private static final DuplicatorManager INSTANCE = new DuplicatorManager();

    private Phase phase = Phase.IDLE;
    private int tickCounter = 0;
    private boolean eventsRegistered;
    private int consecutiveFails = 0;

    private enum Phase {
        IDLE, ARMING, HOLDING, DUPING, COOLDOWN
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

    private void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        DuplicatorConfig cfg = getConfig();
        if (!cfg.enableTrident) {
            phase = Phase.IDLE;
            return;
        }

        switch (phase) {
            case IDLE : tickCounter = 0; phase = Phase.ARMING; break;

            case ARMING : {
                int slot = findBestWeaponSlot(player);
                if (slot == -1) {
                    tickCounter++;
                    if (tickCounter >= 20) { phase = Phase.IDLE; tickCounter = 0; }
                    break;
                }
                if (slot != 0) {
                    mc.gameMode.handleInventoryMouseClick(
                            player.containerMenu.containerId,
                            36 + slot, 0, ClickType.SWAP, player);
                }
                mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                phase = Phase.HOLDING;
                tickCounter = 0;
                Fku.LOGGER.debug("[Duplicator] 开始蓄力 slot={}", slot);
            } break;

            case HOLDING : {
                tickCounter++;
                if (tickCounter >= cfg.holdDuration) {
                    phase = Phase.DUPING;
                    tickCounter = 0;
                }
            } break;

            case DUPING : {
                try {
                    mc.gameMode.handleInventoryMouseClick(
                            player.containerMenu.containerId,
                            3, 0, ClickType.SWAP, player);

                    if (cfg.dropTridents) {
                        mc.gameMode.handleInventoryMouseClick(
                                player.containerMenu.containerId,
                                44, 0, ClickType.THROW, player);
                    }

                    mc.getConnection().send(new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM,
                            BlockPos.ZERO, DOWN, 0));

                    consecutiveFails = 0;
                    Fku.LOGGER.debug("[Duplicator] 复制完成");
                } catch (Exception e) {
                    consecutiveFails++;
                    Fku.LOGGER.error("[Duplicator] 复制异常: {}", e.getMessage());
                    if (consecutiveFails >= 3) {
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(
                                        "§c[三叉戟复制] 连续失败3次"), true);
                        phase = Phase.IDLE;
                        break;
                    }
                }
                phase = Phase.COOLDOWN;
                tickCounter = 0;
            } break;

            case COOLDOWN : {
                tickCounter++;
                if (tickCounter >= cfg.dupeDelay) { phase = Phase.IDLE; tickCounter = 0; }
            } break;
        }
    }

    private int findBestWeaponSlot(LocalPlayer player) {
        int bestSlot = -1;
        int bestDurability = Integer.MAX_VALUE;
        boolean hasRiptideWarned = false;

        for (int i = 0; i < 9; i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof TridentItem)) continue;

            int riptide = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.RIPTIDE, stack);
            if (riptide > 0) {
                if (!hasRiptideWarned) {
                    hasRiptideWarned = true;
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "§e[三叉戟复制] 激流附魔跳过"), true);
                }
                continue;
            }
            int dur = stack.getMaxDamage() - stack.getDamageValue();
            if (dur < bestDurability) {
                bestDurability = dur;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private DuplicatorConfig getConfig() { return DuplicatorConfig.getInstance(); }

    public void reset() {
        phase = Phase.IDLE;
        tickCounter = 0;
        consecutiveFails = 0;
    }

    public boolean isRunning() { return phase != Phase.IDLE; }
}
