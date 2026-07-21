package fku.org.example.fku.features.fakeplayer;

import com.mojang.authlib.GameProfile;
import fku.org.example.fku.Fku;
import fku.org.example.fku.features.fakeplayer.FakePlayerConfig;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class FakePlayerFeature {
    private static boolean initialized = false;
    private static FakePlayerEntity fakePlayer;
    private static boolean handledByUs;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        FakePlayerConfig.getInstance();
        Fku.LOGGER.info("[FakePlayer] \u5047\u4eba\u529f\u80fd\u5df2\u521d\u59cb\u5316");
    }

    public static void spawn() {
        FakePlayerFeature.remove();
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        Minecraft mc = FakePlayerFeature.getMc();
        if (mc == null || mc.player == null || mc.f_91073_ == null) {
            return;
        }
        fakePlayer = new FakePlayerEntity((Player)mc.player, cfg.name, cfg.health, cfg.copyInv);
        if (mc.f_91073_ instanceof ClientLevel) {
            mc.f_91073_.m_104630_(fakePlayer.m_19879_(), (AbstractClientPlayer)fakePlayer);
        }
        Fku.LOGGER.info("[FakePlayer] \u5df2\u751f\u6210: {}", cfg.name);
    }

    public static void remove() {
        if (fakePlayer != null) {
            fakePlayer.m_146870_();
            fakePlayer = null;
        }
    }

    public static boolean hasFakePlayer() {
        return fakePlayer != null && fakePlayer.m_6084_();
    }

    public static FakePlayerEntity getFakePlayer() {
        return fakePlayer;
    }

    public static void toggle() {
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        cfg.setEnabled(!cfg.enabled);
        if (!cfg.enabled) {
            FakePlayerFeature.remove();
        } else {
            FakePlayerFeature.spawn();
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        LocalPlayer player;
        boolean isCrit;
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        if (!cfg.enabled || !cfg.simulateDamage) {
            return;
        }
        if (fakePlayer == null || !fakePlayer.m_6084_()) {
            return;
        }
        if (event.getTarget() != fakePlayer) {
            return;
        }
        if (FakePlayerFeature.handledByThisTick()) {
            return;
        }
        event.setCanceled(true);
        float damage = FakePlayerFeature.calculateAttackDamage((LivingEntity)event.getEntity());
        Minecraft mc = FakePlayerFeature.getMc();
        boolean bl = isCrit = mc != null && mc.player != null && mc.player.f_19789_ > 0.0f && !mc.player.m_20096_() && !mc.player.m_20069_() && !mc.player.m_21023_(MobEffects.f_19610_) && !mc.player.m_20159_();
        if (isCrit) {
            damage *= 1.5f;
        }
        fakePlayer.applyDamage(damage);
        LocalPlayer localPlayer = player = mc != null ? mc.player : null;
        if (player != null) {
            mc.f_91073_.m_6263_((Player)player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.f_12323_, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (cfg.showDamage) {
            FakePlayerFeature.displayClientMessage("\u00a7c\u5047\u4eba\u53d7\u5230\u4f24\u5bb3: \u00a7f" + String.format("%.1f", damage)) + " \u00a77(\u5269\u4f59: \u00a7f" + String.format("%.1f", Math.max(0.0f, fakePlayer.m_21223_()))) + "\u00a77)");
        }
        FakePlayerFeature.markHandled();
    }

    public static boolean handleTpAuraAttack(Entity target) {
        boolean isCrit;
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        Minecraft mc = FakePlayerFeature.getMc();
        if (mc == null || !cfg.enabled || !cfg.simulateDamage) {
            return false;
        }
        if (fakePlayer == null || !fakePlayer.m_6084_()) {
            return false;
        }
        if (target != fakePlayer) {
            return false;
        }
        float damage = FakePlayerFeature.calculateAttackDamage((LivingEntity)mc.player);
        boolean bl = isCrit = mc.player != null && mc.player.f_19789_ > 0.0f && !mc.player.m_20096_() && !mc.player.m_20069_() && !mc.player.m_21023_(MobEffects.f_19610_) && !mc.player.m_20159_();
        if (isCrit) {
            damage *= 1.5f;
        }
        fakePlayer.applyDamage(damage);
        if (mc.f_91073_ != null) {
            mc.f_91073_.m_6263_((Player)mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.f_12323_, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (cfg.showDamage) {
            FakePlayerFeature.displayClientMessage("\u00a7c\u5047\u4eba\u53d7\u5230\u4f24\u5bb3: \u00a7f" + String.format("%.1f", damage)) + " \u00a77(\u5269\u4f59: \u00a7f" + String.format("%.1f", Math.max(0.0f, fakePlayer.m_21223_()))) + "\u00a77)");
        }
        return true;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        if (!cfg.enabled || fakePlayer == null || !fakePlayer.m_6084_()) {
            return;
        }
        if (cfg.autoTotem) {
            if (fakePlayer.m_21206_().m_41720_() != Items.f_42747_) {
                fakePlayer.m_21008_(InteractionHand.OFF_HAND, new ItemStack((ItemLike)Items.f_42747_));
            }
            if (fakePlayer.m_21205_().m_41720_() != Items.f_42747_) {
                fakePlayer.m_21008_(InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.f_42747_));
            }
        }
        fakePlayer.tickCombat();
        if (!fakePlayer.m_6084_() && cfg.respawn) {
            FakePlayerFeature.spawn();
        }
    }

    private static float calculateAttackDamage(LivingEntity attacker) {
        MobEffectInstance effect;
        MobEffectInstance effect2;
        if (!(attacker instanceof Player)) {
            return 1.0f;
        }
        Player player = (Player)attacker;
        ItemStack weapon = player.m_21205_();
        if (weapon.m_41619_()) {
            return 1.0f;
        }
        double baseDamage = weapon.m_41638_(EquipmentSlot.MAINHAND).get(Attributes.f_22281_).stream().mapToDouble(m -> m.m_22218_()).sum();
        if (baseDamage == 0.0) {
            baseDamage = 1.0;
        }
        int sharpness = 0;
        int smite = 0;
        int bane = 0;
        Map enchantments = EnchantmentHelper.m_44831_((ItemStack)weapon);
        for (Map.Entry entry : enchantments.entrySet()) {
            Enchantment ench = (Enchantment)entry.getKey();
            int level = (Integer)entry.getValue();
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(ench);
            if (id == null) continue;
            String path = id.m_135815_();
            if ("sharpness".equals(path)) {
                sharpness += level;
                continue;
            }
            if ("smite".equals(path)) {
                smite += level;
                continue;
            }
            if (!"bane_of_arthropods".equals(path)) continue;
            bane += level;
        }
        float enchantBonus = sharpness * 1.25f + smite * 2.5f + bane * 2.5f;
        float strengthBonus = 0.0f;
        if (player.m_21023_(MobEffects.f_19600_) && (effect2 = player.m_21124_(MobEffects.f_19600_)) != null) {
            strengthBonus = 3.0f * (effect2.m_19564_() + 1);
        }
        float weaknessPenalty = 0.0f;
        if (player.m_21023_(MobEffects.f_19613_) && (effect = player.m_21124_(MobEffects.f_19613_)) != null) {
            weaknessPenalty = 4.0f * (effect.m_19564_() + 1);
        }
        return (baseDamage + enchantBonus + strengthBonus - weaknessPenalty);
    }

    private static boolean handledByThisTick() {
        return handledByUs;
    }

    private static void markHandled() {
        handledByUs = true;
        new Thread(() -> {
            try {
                Thread.sleep(50L);
            }
            catch (InterruptedException interruptedException) {
                // ignored
            }
            handledByUs = false;
        }).start();
    }

    private static void displayClientMessage(String msg) {
        Minecraft mc = FakePlayerFeature.getMc();
        if (mc != null && mc.player != null) {
            mc.player.m_5661_(Component.literal((String)msg), false);
        }
    }

    static {
        handledByUs = false;
    }

    public static class FakePlayerEntity
    extends AbstractClientPlayer {
        private int combatCooldown = 0;
        private final boolean ground;

        public FakePlayerEntity(Player player, String name, float health, boolean copyInv) {
            super(FakePlayerFeature.getMc().f_91073_, new GameProfile(UUID.randomUUID(), name));
            this.m_20359_((Entity)player);
            this.m_146922_(player.m_146908_());
            this.m_146926_(player.m_146909_());
            this.f_20883_ = player.f_20883_;
            this.f_20885_ = player.f_20885_;
            this.f_20886_ = player.f_20886_;
            this.xOld = player.xOld;
            this.yOld = player.yOld;
            this.zOld = player.zOld;
            this.f_19798_ = player.m_20069_();
            this.m_20260_(player.m_6144_());
            this.m_20124_(player.m_20089_());
            this.ground = player.m_20096_();
            this.m_6853_(this.ground);
            this.m_20011_(player.m_20191_());
            this.m_21153_(health);
            if (copyInv) {
                Inventory playerInv = player.m_150109_();
                Inventory fakeInv = this.m_150109_();
                for (int i = 0; i < playerInv.m_6643_(); ++i) {
                    fakeInv.m_6836_(i, playerInv.m_8020_(i).m_41777_());
                }
            }
            if (FakePlayerConfig.getInstance().autoTotem) {
                this.m_21008_(InteractionHand.OFF_HAND, new ItemStack((ItemLike)Items.f_42747_));
            }
            float absorption = player.m_6103_();
            this.m_7911_(absorption);
        }

        public boolean m_20096_() {
            return this.ground;
        }

        public boolean m_5833_() {
            return false;
        }

        public boolean m_7500_() {
            return false;
        }

        public void tickCombat() {
            if (this.combatCooldown > 0) {
                --this.combatCooldown;
            }
            if (this.f_20916_ > 0) {
                --this.f_20916_;
            }
        }

        public void applyDamage(float damage) {
            if (this.combatCooldown > 0) {
                return;
            }
            float oldHealth = this.m_21223_();
            float newHealth = oldHealth - damage;
            this.combatCooldown = FakePlayerConfig.getInstance().invulnerableTicks;
            this.f_20916_ = 10;
            this.f_20917_ = 10;
            if (newHealth <= 0.0f) {
                boolean totemPopped = this.tryPopTotem();
                if (!totemPopped) {
                    this.die();
                } else {
                    this.m_21153_(1.0f);
                }
            } else {
                this.m_21153_(newHealth);
            }
        }

        private boolean tryPopTotem() {
            boolean hasTotem;
            Minecraft mc = FakePlayerFeature.getMc();
            boolean bl = hasTotem = this.m_21206_().m_41720_() == Items.f_42747_ || this.m_21205_().m_41720_() == Items.f_42747_;
            if (!hasTotem) {
                return false;
            }
            if (this.m_21206_().m_41720_() == Items.f_42747_) {
                this.m_21008_(InteractionHand.OFF_HAND, ItemStack.f_41583_);
            } else {
                this.m_21008_(InteractionHand.MAIN_HAND, ItemStack.f_41583_);
            }
            this.m_21153_(10.0f);
            this.m_7911_(4.0f);
            this.m_21219_();
            this.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 900, 1));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19607_, 800, 0));
            this.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 100, 1));
            this.combatCooldown = FakePlayerConfig.getInstance().invulnerableTicks;
            this.f_20916_ = 10;
            if (mc != null && mc.f_91073_ != null) {
                for (int i = 0; i < 30; ++i) {
                    double vx = (mc.f_91073_.f_46441_.m_188500_() - 0.5) * 0.5;
                    double vy = mc.f_91073_.f_46441_.m_188500_() * 0.5;
                    double vz = (mc.f_91073_.f_46441_.m_188500_() - 0.5) * 0.5;
                    mc.f_91073_.m_7106_((ParticleOptions)ParticleTypes.f_123767_, this.getX() + vx * 2.0, this.getY() + 1.0 + vy * 2.0, this.getZ() + vz * 2.0, vx, vy + 0.5, vz);
                }
                mc.f_91073_.m_6263_(null, this.getX(), this.getY(), this.getZ(), SoundEvents.f_12513_, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            FakePlayerConfig cfg = FakePlayerConfig.getInstance();
            if (cfg.showDamage) {
                FakePlayerFeature.displayClientMessage("\u00a76\u5047\u4eba\u89e6\u53d1\u4e86\u4e0d\u6b7b\u56fe\u817e\uff01");
            }
            return true;
        }

        private void die() {
            this.m_21153_(0.0f);
            Minecraft mc = FakePlayerFeature.getMc();
            if (mc != null && mc.f_91073_ != null) {
                mc.f_91073_.m_6263_(null, this.getX(), this.getY(), this.getZ(), SoundEvents.f_12322_, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            this.m_142687_(Entity.RemovalReason.KILLED);
            fakePlayer = null;
            FakePlayerFeature.displayClientMessage("\u00a7c\u5047\u4eba\u5df2\u6b7b\u4ea1\u3002");
        }
    }
}

