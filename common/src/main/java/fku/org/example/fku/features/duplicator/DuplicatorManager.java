package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.duplicator.DuplicatorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
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

    private DuplicatorManager() {
    }

    public static DuplicatorManager getInstance() {
        return INSTANCE;
    }

    public static void registerEventHandlers() {
        if (!DuplicatorManager.INSTANCE.eventsRegistered) {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            DuplicatorManager.INSTANCE.eventsRegistered = true;
            Fku.LOGGER.info("[Duplicator] \u4e8b\u4ef6\u5904\u7406\u5668\u5df2\u6ce8\u518c");
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        this.tick();
    }

    @SubscribeEvent
    public void onPlayerDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        this.reset();
    }

    /*
     * Unable to fully structure code
     */
    private void tick() {
        mc = Minecraft.getInstance();
        player = mc.player;
        if (player == null || mc.f_91073_ == null) {
            return;
        }
        cfg = this.getConfig();
        if (!cfg.enableTrident) {
            this.phase = Phase.IDLE;
            return;
        }
        switch (1.$SwitchMap$fku$org$example$fku$features$duplicator$DuplicatorManager$Phase[this.phase.ordinal()]) {
            case 1: {
                this.tickCounter = 0;
                this.phase = Phase.ARMING;
                break;
            }
            case 2: {
                slot = this.findBestWeaponSlot(player);
                if (slot == -1) {
                    ++this.tickCounter;
                    if (this.tickCounter < 20) break;
                    this.phase = Phase.IDLE;
                    this.tickCounter = 0;
                    break;
                }
                if (slot != 0) {
                    mc.f_91072_.m_171799_(player.f_36096_.f_38840_, 36 + slot, 0, ClickType.SWAP, (Player)player);
                }
                mc.f_91072_.m_233721_((Player)player, InteractionHand.MAIN_HAND);
                this.phase = Phase.HOLDING;
                this.tickCounter = 0;
                Fku.LOGGER.debug("[Duplicator] \u5f00\u59cb\u84c4\u529b slot={}", slot);
                break;
            }
            case 3: {
                ++this.tickCounter;
                if (this.tickCounter < cfg.holdDuration) break;
                this.phase = Phase.DUPING;
                this.tickCounter = 0;
                break;
            }
            case 4: {
                try {
                    mc.f_91072_.m_171799_(player.f_36096_.f_38840_, 3, 0, ClickType.SWAP, (Player)player);
                    if (cfg.dropTridents) {
                        mc.f_91072_.m_171799_(player.f_36096_.f_38840_, 44, 0, ClickType.THROW, (Player)player);
                    }
                    mc.m_91403_().m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.f_121853_, Direction.DOWN, 0));
                    this.consecutiveFails = 0;
                    Fku.LOGGER.debug("[Duplicator] \u590d\u5236\u5b8c\u6210");
                }
                catch (Exception e) {
                    ++this.consecutiveFails;
                    Fku.LOGGER.error("[Duplicator] \u590d\u5236\u5f02\u5e38: {}", e.getMessage());
                    if (this.consecutiveFails < 3) ** GOTO lbl52
                    player.m_5661_(Component.literal((String)"\u00a7c[\u4e09\u53c9\u621f\u590d\u5236] \u8fde\u7eed\u5931\u8d253\u6b21"), true);
                    this.phase = Phase.IDLE;
                    break;
                }
lbl52:
                // 2 sources

                this.phase = Phase.COOLDOWN;
                this.tickCounter = 0;
                break;
            }
            case 5: {
                ++this.tickCounter;
                if (this.tickCounter < cfg.dupeDelay) break;
                this.phase = Phase.IDLE;
                this.tickCounter = 0;
            }
        }
    }

    private int findBestWeaponSlot(LocalPlayer player) {
        int bestSlot = -1;
        int bestDurability = Integer.MAX_VALUE;
        boolean hasRiptideWarned = false;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.m_150109_().m_8020_(i);
            if (stack.m_41619_() || !(stack.m_41720_() instanceof TridentItem)) continue;
            int riptide = EnchantmentHelper.getTagEnchantmentLevel((Enchantment)Enchantments.f_44957_, (ItemStack)stack);
            if (riptide > 0) {
                if (hasRiptideWarned) continue;
                hasRiptideWarned = true;
                player.m_5661_(Component.literal((String)"\u00a7e[\u4e09\u53c9\u621f\u590d\u5236] \u6fc0\u6d41\u9644\u9b54\u8df3\u8fc7"), true);
                continue;
            }
            int dur = stack.m_41776_() - stack.m_41773_();
            if (dur >= bestDurability) continue;
            bestDurability = dur;
            bestSlot = i;
        }
        return bestSlot;
    }

    private DuplicatorConfig getConfig() {
        return DuplicatorConfig.getInstance();
    }

    public void reset() {
        this.phase = Phase.IDLE;
        this.tickCounter = 0;
        this.consecutiveFails = 0;
    }

    public boolean isRunning() {
        return this.phase != Phase.IDLE;
    }

    private static enum Phase {
        IDLE,
        ARMING,
        HOLDING,
        DUPING,
        COOLDOWN;

    }
}

