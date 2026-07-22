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
        if (mc == null || mc.player == null || mc.level == null) {
            return;
        }
        fakePlayer = new FakePlayerEntity((Player)mc.player, cfg.name, cfg.health, cfg.copyInv);
        if (mc.level instanceof ClientLevel) {
            mc.level.addPlayer(fakePlayer.getId(), (AbstractClientPlayer)fakePlayer);
        }
        Fku.LOGGER.info("[FakePlayer] \u5df2\u751f\u6210: {}", cfg.name);
    }

    public static void remove() {
        if (fakePlayer != null) {
            fakePlayer.discard();
            fakePlayer = null;
        }
    }

    public static boolean hasFakePlayer() {
        return fakePlayer != null && fakePlayer.isAlive();
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
        if (fakePlayer == null || !fakePlayer.isAlive()) {
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
        boolean bl = isCrit = mc != null && mc.player != null && mc.player.fallDistance > 0.0f && !mc.player.onGround() && !mc.player.isInWater() && !mc.player.hasEffect(MobEffects.BLINDNESS) && !mc.player.isPassenger();
        if (isCrit) {
            damage *= 1.5f;
        }
        fakePlayer.applyDamage(damage);
        LocalPlayer localPlayer = player = mc != null ? mc.player : null;
        if (player != null) {
            mc.level.playSound((Player)player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (cfg.showDamage) {
            FakePlayerFeature.displayClientMessage("\u00a7c\u5047\u4eba\u53d7\u5230\u4f24\u5bb3: \u00a7f" + String.format("%.1f", damage) + " \u00a77(\u5269\u4f59: \u00a7f" + String.format("%.1f", Math.max(0.0f, fakePlayer.getHealth())) + "\u00a77)");
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
        if (fakePlayer == null || !fakePlayer.isAlive()) {
            return false;
        }
        if (target != fakePlayer) {
            return false;
        }
        float damage = FakePlayerFeature.calculateAttackDamage((LivingEntity)mc.player);
        boolean bl = isCrit = mc.player != null && mc.player.fallDistance > 0.0f && !mc.player.onGround() && !mc.player.isInWater() && !mc.player.hasEffect(MobEffects.BLINDNESS) && !mc.player.isPassenger();
        if (isCrit) {
            damage *= 1.5f;
        }
        fakePlayer.applyDamage(damage);
        if (mc.level != null) {
            mc.level.playSound((Player)mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (cfg.showDamage) {
            FakePlayerFeature.displayClientMessage("\u00a7c\u5047\u4eba\u53d7\u5230\u4f24\u5bb3: \u00a7f" + String.format("%.1f", damage) + " \u00a77(\u5269\u4f59: \u00a7f" + String.format("%.1f", Math.max(0.0f, fakePlayer.getHealth())) + "\u00a77)");
        }
        return true;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        if (!cfg.enabled || fakePlayer == null || !fakePlayer.isAlive()) {
            return;
        }
        if (cfg.autoTotem) {
            if (fakePlayer.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING));
            }
            if (fakePlayer.getMainHandItem().getItem() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING));
            }
        }
        fakePlayer.tickCombat();
        if (!fakePlayer.isAlive() && cfg.respawn) {
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
        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) {
            return 1.0f;
        }
        double baseDamage = weapon.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().mapToDouble(m -> m.getAmount()).sum();
        if (baseDamage == 0.0) {
            baseDamage = 1.0;
        }
        int sharpness = 0;
        int smite = 0;
        int bane = 0;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments((ItemStack)weapon);
        for (Map.Entry entry : enchantments.entrySet()) {
            Enchantment ench = (Enchantment)entry.getKey();
            int level = (Integer)entry.getValue();
            ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(ench);
            if (id == null) continue;
            String path = id.getPath();
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
        if (player.hasEffect(MobEffects.DAMAGE_BOOST) && (effect2 = player.getEffect(MobEffects.DAMAGE_BOOST)) != null) {
            strengthBonus = 3.0f * (effect2.getAmplifier() + 1);
        }
        float weaknessPenalty = 0.0f;
        if (player.hasEffect(MobEffects.WEAKNESS) && (effect = player.getEffect(MobEffects.WEAKNESS)) != null) {
            weaknessPenalty = 4.0f * (effect.getAmplifier() + 1);
        }
        return (float)(baseDamage + enchantBonus + strengthBonus - weaknessPenalty);
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
            mc.player.displayClientMessage(Component.literal((String)msg), false);
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
            super(FakePlayerFeature.getMc().level, new GameProfile(UUID.randomUUID(), name));
            this.copyPosition((Entity)player);
            this.setYRot(player.getYRot());
            this.setXRot(player.getXRot());
            this.yBodyRot = player.yBodyRot;
            this.yHeadRot = player.yHeadRot;
            this.yHeadRotO = player.yHeadRotO;
            this.xOld = player.xOld;
            this.yOld = player.yOld;
            this.zOld = player.zOld;
            this.wasTouchingWater = player.isInWater();
            this.setShiftKeyDown(player.isShiftKeyDown());
            this.setPose(player.getPose());
            this.ground = player.onGround();
            this.setOnGround(this.ground);
            this.setBoundingBox(player.getBoundingBox());
            this.setHealth(health);
            if (copyInv) {
                Inventory playerInv = player.getInventory();
                Inventory fakeInv = this.getInventory();
                for (int i = 0; i < playerInv.getContainerSize(); ++i) {
                    fakeInv.setItem(i, playerInv.getItem(i).copy());
                }
            }
            if (FakePlayerConfig.getInstance().autoTotem) {
                this.setItemInHand(InteractionHand.OFF_HAND, new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING));
            }
            float absorption = player.getAbsorptionAmount();
            this.setAbsorptionAmount(absorption);
        }

        public boolean onGround() {
            return this.ground;
        }

        public boolean isSpectator() {
            return false;
        }

        public boolean isCreative() {
            return false;
        }

        public void tickCombat() {
            if (this.combatCooldown > 0) {
                --this.combatCooldown;
            }
            if (this.hurtTime > 0) {
                --this.hurtTime;
            }
        }

        public void applyDamage(float damage) {
            if (this.combatCooldown > 0) {
                return;
            }
            float oldHealth = this.getHealth();
            float newHealth = oldHealth - damage;
            this.combatCooldown = FakePlayerConfig.getInstance().invulnerableTicks;
            this.hurtTime = 10;
            this.hurtDuration = 10;
            if (newHealth <= 0.0f) {
                boolean totemPopped = this.tryPopTotem();
                if (!totemPopped) {
                    this.die();
                } else {
                    this.setHealth(1.0f);
                }
            } else {
                this.setHealth(newHealth);
            }
        }

        private boolean tryPopTotem() {
            boolean hasTotem;
            Minecraft mc = FakePlayerFeature.getMc();
            boolean bl = hasTotem = this.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING || this.getMainHandItem().getItem() == Items.TOTEM_OF_UNDYING;
            if (!hasTotem) {
                return false;
            }
            if (this.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING) {
                this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            } else {
                this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            this.setHealth(10.0f);
            this.setAbsorptionAmount(4.0f);
            this.removeAllEffects();
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            this.combatCooldown = FakePlayerConfig.getInstance().invulnerableTicks;
            this.hurtTime = 10;
            if (mc != null && mc.level != null) {
                for (int i = 0; i < 30; ++i) {
                    double vx = (mc.level.random.nextDouble() - 0.5) * 0.5;
                    double vy = mc.level.random.nextDouble() * 0.5;
                    double vz = (mc.level.random.nextDouble() - 0.5) * 0.5;
                    mc.level.addParticle((ParticleOptions)ParticleTypes.TOTEM_OF_UNDYING, this.getX() + vx * 2.0, this.getY() + 1.0 + vy * 2.0, this.getZ() + vz * 2.0, vx, vy + 0.5, vz);
                }
                mc.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            FakePlayerConfig cfg = FakePlayerConfig.getInstance();
            if (cfg.showDamage) {
                FakePlayerFeature.displayClientMessage("\u00a76\u5047\u4eba\u89e6\u53d1\u4e86\u4e0d\u6b7b\u56fe\u817e\uff01");
            }
            return true;
        }

        private void die() {
            this.setHealth(0.0f);
            Minecraft mc = FakePlayerFeature.getMc();
            if (mc != null && mc.level != null) {
                mc.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_DEATH, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            this.remove(Entity.RemovalReason.KILLED);
            fakePlayer = null;
            FakePlayerFeature.displayClientMessage("\u00a7c\u5047\u4eba\u5df2\u6b7b\u4ea1\u3002");
        }
    }
}

