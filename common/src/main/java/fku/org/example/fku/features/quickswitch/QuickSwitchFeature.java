package fku.org.example.fku.features.quickswitch;

import com.google.common.collect.Multimap;
import fku.org.example.fku.features.quickswitch.QuickSwitchConfig;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public class QuickSwitchFeature {
    private static final Minecraft mc = Minecraft.getInstance();
    private static SwitchState state = SwitchState.IDLE;
    private static int originalSlot = -1;
    private static List<Integer> switchQueue = new CopyOnWriteArrayList<Integer>();
    private static int switchStepIndex = 0;
    private static long actionTime = 0L;
    private static final int STEP_INTERVAL_MS = 10;

    public static void init() {
        QuickSwitchConfig.load();
    }

    public static boolean isEnabled() {
        return QuickSwitchConfig.getInstance().enabled;
    }

    public static void setEnabled(boolean v) {
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        if (v && "OFF".equals(cfg.mode)) {
            cfg.mode = "SMART";
            QuickSwitchConfig.save();
        }
        cfg.enabled = v;
        QuickSwitchConfig.save();
        if (QuickSwitchFeature.mc.player != null) {
            QuickSwitchFeature.mc.player.m_5661_(Component.literal((String)(v ? "\u00a7b[QuickSwitch] \u00a7a\u5df2\u542f\u7528" : "\u00a7b[QuickSwitch] \u00a7c\u5df2\u7981\u7528")), false);
        }
        if (!v) {
            QuickSwitchFeature.forceReset();
        }
    }

    public static void toggle() {
        QuickSwitchFeature.setEnabled(!QuickSwitchFeature.isEnabled());
    }

    public static boolean isIdle() {
        return state == SwitchState.IDLE;
    }

    public static void onAttackPacket(Channel channel) {
        if (state != SwitchState.IDLE) {
            return;
        }
        if (!QuickSwitchFeature.canHandle()) {
            return;
        }
        if (QuickSwitchFeature.mc.player == null || QuickSwitchFeature.mc.f_91073_ == null) {
            return;
        }
        if (channel == null || !channel.isOpen()) {
            return;
        }
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        int curSlot = QuickSwitchFeature.mc.player.m_150109_().f_35977_;
        switch (cfg.mode) {
            case "SMART": {
                int firstSlot;
                List<Integer> sequence = QuickSwitchFeature.buildSmartSequence();
                if (sequence.isEmpty()) {
                    return;
                }
                if (sequence.size() == 1 && sequence.get(0) == curSlot) {
                    return;
                }
                originalSlot = curSlot;
                switchQueue.clear();
                switchQueue.addAll(sequence);
                switchStepIndex = 0;
                QuickSwitchFeature.mc.player.m_150109_().f_35977_ = firstSlot = switchQueue.get(0).intValue();
                channel.writeAndFlush(new ServerboundSetCarriedItemPacket(firstSlot));
                switchStepIndex = 1;
                state = SwitchState.MULTI_SWITCH;
                actionTime = System.currentTimeMillis();
                if (!cfg.visualFeedback) break;
                QuickSwitchFeature.mc.player.m_5661_(Component.literal((String)("\u00a7b[QuickSwitch] \u00a7f\u667a\u80fd\u79d2\u5207 \u2192 \u5e8f\u5217 " + sequence + " (\u5171" + sequence.size() + "\u6b65)")), true);
                break;
            }
            case "CUSTOM": {
                int firstSlot;
                List<Integer> sequence = QuickSwitchFeature.buildCustomSequence(cfg);
                if (sequence.isEmpty()) {
                    return;
                }
                originalSlot = curSlot;
                switchQueue.clear();
                switchQueue.addAll(sequence);
                switchStepIndex = 0;
                QuickSwitchFeature.mc.player.m_150109_().f_35977_ = firstSlot = switchQueue.get(0).intValue();
                channel.writeAndFlush(new ServerboundSetCarriedItemPacket(firstSlot));
                switchStepIndex = 1;
                state = SwitchState.MULTI_SWITCH;
                actionTime = System.currentTimeMillis();
                if (!cfg.visualFeedback) break;
                QuickSwitchFeature.mc.player.m_5661_(Component.literal((String)("\u00a7b[QuickSwitch] \u00a7f\u81ea\u5b9a\u4e49\u5207 \u2192 \u5e8f\u5217 " + sequence + " (\u5171" + sequence.size() + "\u6b65)")), true);
            }
        }
    }

    public static void tick() {
        if (state == SwitchState.IDLE) {
            return;
        }
        if (QuickSwitchFeature.mc.player == null) {
            QuickSwitchFeature.forceReset();
            return;
        }
        long now = System.currentTimeMillis();
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        switch (state) {
            case MULTI_SWITCH: {
                if (now < actionTime + 10L) break;
                if (switchStepIndex < switchQueue.size()) {
                    int nextSlot;
                    QuickSwitchFeature.mc.player.m_150109_().f_35977_ = nextSlot = switchQueue.get(switchStepIndex).intValue();
                    if (QuickSwitchFeature.mc.player.f_108617_ != null) {
                        QuickSwitchFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(nextSlot));
                    }
                    ++switchStepIndex;
                    actionTime = now;
                    break;
                }
                state = SwitchState.WAITING_ATTACK;
                actionTime = now + cfg.rttDelay;
                break;
            }
            case WAITING_ATTACK: {
                if (now < actionTime) break;
                state = SwitchState.SWITCHING_BACK;
                actionTime = now + cfg.rttDelay;
                break;
            }
            case SWITCHING_BACK: {
                if (now < actionTime) break;
                QuickSwitchFeature.doRestore();
                state = SwitchState.IDLE;
            }
        }
    }

    private static List<Integer> buildSmartSequence() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int enchSlot = QuickSwitchFeature.findBestEnchantedWeaponSlot();
        if (enchSlot < 0) {
            return result;
        }
        int curSlot = QuickSwitchFeature.mc.player.m_150109_().f_35977_;
        if (enchSlot == curSlot) {
            return result;
        }
        result.add(enchSlot);
        return result;
    }

    private static List<Integer> buildCustomSequence(QuickSwitchConfig cfg) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if (QuickSwitchFeature.mc.player == null) {
            return result;
        }
        Inventory inv = QuickSwitchFeature.mc.player.m_150109_();
        String[] itemIds = cfg.customItems.split(",");
        if (itemIds.length == 0) {
            return result;
        }
        block0: for (String rawId : itemIds) {
            String targetId = rawId.trim().toLowerCase();
            if (targetId.isEmpty()) continue;
            boolean found = false;
            for (int slot = 0; slot < 9; ++slot) {
                String regName;
                ItemStack stack = inv.m_8020_(slot);
                if (stack.m_41619_() || !(regName = ForgeRegistries.ITEMS.getKey(stack.m_41720_()).toString().toLowerCase()).equals(targetId) && !regName.endsWith(":" + targetId)) continue;
                if (result.isEmpty() || (Integer)result.get(result.size() - 1) != slot) {
                    result.add(slot);
                }
                found = true;
                continue block0;
            }
        }
        return result;
    }

    private static void doRestore() {
        if (originalSlot < 0 || originalSlot > 8) {
            return;
        }
        if (QuickSwitchFeature.mc.player == null || QuickSwitchFeature.mc.player.f_108617_ == null) {
            return;
        }
        int slot = originalSlot;
        switchQueue.clear();
        switchStepIndex = 0;
        originalSlot = -1;
        QuickSwitchFeature.mc.player.m_150109_().f_35977_ = slot;
        QuickSwitchFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(slot));
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        if (cfg.visualFeedback) {
            QuickSwitchFeature.mc.player.m_5661_(Component.literal((String)("\u00a7b[QuickSwitch] \u00a7f\u5df2\u6062\u590d\u539f\u69fd\u4f4d " + slot)), true);
        }
    }

    public static void forceReset() {
        if (state != SwitchState.IDLE) {
            if (originalSlot >= 0 && QuickSwitchFeature.mc.player != null && QuickSwitchFeature.mc.player.f_108617_ != null) {
                QuickSwitchFeature.mc.player.m_150109_().f_35977_ = originalSlot;
                QuickSwitchFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(originalSlot));
            }
            switchQueue.clear();
            switchStepIndex = 0;
            originalSlot = -1;
            state = SwitchState.IDLE;
        }
    }

    private static boolean canHandle() {
        if (!QuickSwitchFeature.isEnabled()) {
            return false;
        }
        if (QuickSwitchFeature.mc.player == null) {
            return false;
        }
        QuickSwitchConfig cfg = QuickSwitchConfig.getInstance();
        return cfg.enabled && cfg.isActiveMode();
    }

    private static int findBestEnchantedWeaponSlot() {
        if (QuickSwitchFeature.mc.player == null) {
            return -1;
        }
        Inventory inv = QuickSwitchFeature.mc.player.m_150109_();
        int bestSlot = -1;
        double bestScore = 0.0;
        for (int i = 0; i < 9; ++i) {
            double score;
            ItemStack stack = inv.m_8020_(i);
            if (stack.m_41619_() || !((score = QuickSwitchFeature.calculateWeaponScore(stack)) > bestScore)) continue;
            bestScore = score;
            bestSlot = i;
        }
        return bestSlot;
    }

    private static int findEmptyHandOrNoDurabilitySlot() {
        if (QuickSwitchFeature.mc.player == null) {
            return -1;
        }
        Inventory inv = QuickSwitchFeature.mc.player.m_150109_();
        for (int i = 0; i < 9; ++i) {
            if (!inv.m_8020_(i).m_41619_()) continue;
            return i;
        }
        int bestSlot = -1;
        int bestScore = Integer.MIN_VALUE;
        for (int i = 0; i < 9; ++i) {
            Item item;
            int score;
            ItemStack stack = inv.m_8020_(i);
            if (stack.m_41619_() || (score = QuickSwitchFeature.getNoDurabilityScore(item = stack.m_41720_())) <= bestScore) continue;
            bestScore = score;
            bestSlot = i;
        }
        return bestSlot;
    }

    private static int getNoDurabilityScore(Item item) {
        if (item instanceof BlockItem) {
            return 100;
        }
        if (item.m_41472_()) {
            return 90;
        }
        if (item instanceof ArrowItem) {
            return 85;
        }
        if (item instanceof PotionItem) {
            return 80;
        }
        if (item instanceof SnowballItem) {
            return 80;
        }
        if (item instanceof EggItem) {
            return 80;
        }
        if (item instanceof EnderpearlItem) {
            return 80;
        }
        if (item instanceof FireChargeItem) {
            return 80;
        }
        if (item instanceof FishingRodItem) {
            return 70;
        }
        if (item instanceof ShieldItem) {
            return 70;
        }
        if (item instanceof FlintAndSteelItem) {
            return 65;
        }
        if (item instanceof ShearsItem) {
            return 60;
        }
        if (item instanceof LeadItem) {
            return 60;
        }
        if (item instanceof BrushItem) {
            return 60;
        }
        if (item instanceof HorseArmorItem) {
            return 50;
        }
        if (item instanceof SwordItem) {
            return 10;
        }
        if (item instanceof AxeItem) {
            return 10;
        }
        if (item instanceof PickaxeItem) {
            return 10;
        }
        if (item instanceof ShovelItem) {
            return 10;
        }
        if (item instanceof HoeItem) {
            return 10;
        }
        if (item instanceof TridentItem) {
            return 10;
        }
        if (item instanceof DiggerItem) {
            return 10;
        }
        if (item instanceof BowItem) {
            return 15;
        }
        if (item instanceof CrossbowItem) {
            return 15;
        }
        if (item instanceof ArmorItem) {
            return 20;
        }
        return 30;
    }

    private static double calculateWeaponScore(ItemStack stack) {
        if (stack.m_41619_()) {
            return 0.0;
        }
        double score = QuickSwitchFeature.calculateEnchantmentDamageScore(stack);
        if (score <= 0.0) {
            return 0.0;
        }
        Item item = stack.m_41720_();
        if (item instanceof AxeItem) {
            score += 20.0;
        } else if (item instanceof TridentItem) {
            score += 15.0;
        }
        return score;
    }

    private static double getBaseAttackDamage(ItemStack stack) {
        if (stack.m_41619_()) {
            return 0.0;
        }
        Multimap modifiers = stack.m_41638_(EquipmentSlot.MAINHAND);
        if (modifiers == null || modifiers.isEmpty()) {
            Item item = stack.m_41720_();
            if (item instanceof SwordItem) {
                SwordItem s = (SwordItem)item;
                return s.m_43299_();
            }
            if (item instanceof AxeItem) {
                return 7.0;
            }
            if (item instanceof TridentItem) {
                return 7.0;
            }
            if (item instanceof PickaxeItem) {
                return 3.0;
            }
            if (item instanceof ShovelItem) {
                return 2.5;
            }
            if (item instanceof HoeItem) {
                return 1.0;
            }
            return 1.0;
        }
        double total = 0.0;
        for (Map.Entry entry : modifiers.entries()) {
            if (entry.getKey() == null || !((Attribute)entry.getKey()).equals(Attributes.f_22281_)) continue;
            total += ((AttributeModifier)entry.getValue()).m_22218_();
        }
        return total > 0.0 ? total : 1.0;
    }

    private static double calculateEnchantmentDamageScore(ItemStack stack) {
        if (stack.m_41619_()) {
            return 0.0;
        }
        Map enchantments = EnchantmentHelper.m_44831_((ItemStack)stack);
        if (enchantments == null || enchantments.isEmpty()) {
            return 0.0;
        }
        double totalScore = 0.0;
        for (Map.Entry entry : enchantments.entrySet()) {
            Enchantment ench = (Enchantment)entry.getKey();
            int level = (Integer)entry.getValue();
            if (ench == null || level <= 0) continue;
            double factor = QuickSwitchFeature.getEnchantmentDamageFactor(ench, stack);
            totalScore += level * factor;
        }
        return totalScore;
    }

    private static double getEnchantmentDamageFactor(Enchantment ench, ItemStack stack) {
        Item item;
        ResourceLocation regName = ForgeRegistries.ENCHANTMENTS.getKey(ench);
        if (regName != null) {
            String path = regName.m_135815_().toLowerCase();
            if (path.contains("sharp")) {
                return 0.5;
            }
            if (path.contains("smite") || path.contains("undead")) {
                return 1.0;
            }
            if (path.contains("bane") || path.contains("arthropod")) {
                return 1.0;
            }
            if (path.contains("fire")) {
                return 0.8;
            }
            if (path.contains("power") && !path.contains("resolution")) {
                return 0.5;
            }
            if (path.contains("impaling")) {
                return 0.8;
            }
            if (path.contains("knockback") || path.contains("punch")) {
                return 0.3;
            }
            if (path.contains("sweep")) {
                return 0.4;
            }
            if (path.contains("damage") || path.contains("attack")) {
                return 0.4;
            }
        }
        if ((item = stack.m_41720_()) instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem || item instanceof PickaxeItem || item instanceof ShovelItem) {
            return 0.3;
        }
        return 0.2;
    }

    public static enum SwitchState {
        IDLE,
        MULTI_SWITCH,
        WAITING_ATTACK,
        SWITCHING_BACK;

    }
}

