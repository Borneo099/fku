package fku.org.example.fku.features.fakeplayer; /* water */

import com.mojang.authlib.GameProfile;
import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * FakePlayerFeature — 假人核心逻辑
 *
 * ★ 职责：
 *   1. 在客户端生成一个可交互的假人（OtherClientPlayer）
 *   2. 拦截 AttackEntityEvent，对假人模拟伤害（不发送网络包）
 *   3. 自动补充图腾、触发生命恢复与粒子效果
 *   4. 显示伤害反馈与死亡/图腾触发提示
 *
 * ★ 矛盾定性：
 *   假人只存在于客户端，服务端无对应实体。
 *   攻击假人时必须取消 AttackEntityEvent 阻止发包，
 *   否则服务端收到无效实体 ID 的攻击包可能导致断线。
 *
 * ★ 参考来源：
 *   AdvancedFakePlayer.java / IMGFakePlayer.java (InvincibleMachineGun)
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FakePlayerFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;

    /** 当前生成的假人实例 */
    private static FakePlayerEntity fakePlayer;

    /** 玩家攻击是否被本功能处理（防止重复处理） */
    private static boolean handledByUs = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        FakePlayerConfig.getInstance();
        Fku.LOGGER.info("[FakePlayer] 假人功能已初始化");
    }

    // ===== 公开接口 =====

    /** 生成假人 */
    public static void spawn() {
        remove();
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        if (mc.player == null || mc.level == null) return;

        fakePlayer = new FakePlayerEntity(mc.player, cfg.name, cfg.health, cfg.copyInv);

        // ★ 使用 ClientLevel.addPlayer() 正确注册假人到世界（addFreshEntity 在 ClientLevel 上无效）
        if (mc.level instanceof net.minecraft.client.multiplayer.ClientLevel) {
            ((net.minecraft.client.multiplayer.ClientLevel) mc.level).addPlayer(fakePlayer.getId(), fakePlayer);
        }

        Fku.LOGGER.info("[FakePlayer] 已生成: {}", cfg.name);
    }

    /** 移除假人 */
    public static void remove() {
        if (fakePlayer != null) {
            fakePlayer.discard();
            fakePlayer = null;
        }
    }

    /** 当前是否有假人生存 */
    public static boolean hasFakePlayer() {
        return fakePlayer != null && fakePlayer.isAlive();
    }

    /** 获取当前假人实体 */
    public static FakePlayerEntity getFakePlayer() {
        return fakePlayer;
    }

    /** 切换功能 */
    public static void toggle() {
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        cfg.setEnabled(!cfg.enabled);
        if (!cfg.enabled) {
            remove();
        } else {
            spawn();
        }
    }

    // ===== 事件处理 =====

    /**
     * ★ 拦截攻击事件
     *
     * 当玩家攻击假人时，取消 AttackEntityEvent 阻止发包，
     * 本地模拟伤害计算、无敌计时、图腾触发和视觉反馈。
     */
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        if (!cfg.enabled || !cfg.simulateDamage) return;
        if (fakePlayer == null || !fakePlayer.isAlive()) return;
        if (event.getTarget() != fakePlayer) return;
        if (handledByThisTick()) return;

        // ★ 取消事件 → 阻止 ServerboundInteractPacket 发往服务端
        event.setCanceled(true);

        // ★ 计算伤害
        float damage = calculateAttackDamage(event.getEntity());

        // ★ 检查暴击
        boolean isCrit = mc.player != null
            && mc.player.fallDistance > 0.0F
            && !mc.player.onGround()
            && !mc.player.isInWater()
            && !mc.player.hasEffect(MobEffects.BLINDNESS)
            && !mc.player.isPassenger();

        if (isCrit) damage *= 1.5F;

        // ★ 应用伤害
        fakePlayer.applyDamage(damage);

        // ★ 视觉反馈
        LocalPlayer player = mc.player;
        if (player != null) {
            mc.level.playSound(player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(),
                SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (cfg.showDamage) {
            displayClientMessage("§c假人受到伤害: §f" + String.format("%.1f", damage)
                + " §7(剩余: §f" + String.format("%.1f", Math.max(0, fakePlayer.getHealth())) + "§7)");
        }

        markHandled();
    }

    /**
     * ★ TpAura 联动接口
     *
     * 当 TpAura 攻击假人时，不走服务端发包，直接在客户端模拟伤害。
     * 返回 true 表示已处理（目标是当前假人），false 表示非假人目标。
     */
    public static boolean handleTpAuraAttack(Entity target) {
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        if (!cfg.enabled || !cfg.simulateDamage) return false;
        if (fakePlayer == null || !fakePlayer.isAlive()) return false;
        if (target != fakePlayer) return false;

        // ★ 计算伤害（基于玩家的装备）
        float damage = calculateAttackDamage(mc.player);

        // ★ 检查暴击
        boolean isCrit = mc.player != null
            && mc.player.fallDistance > 0.0F
            && !mc.player.onGround()
            && !mc.player.isInWater()
            && !mc.player.hasEffect(MobEffects.BLINDNESS)
            && !mc.player.isPassenger();
        if (isCrit) damage *= 1.5F;

        // ★ 应用伤害
        fakePlayer.applyDamage(damage);

        // ★ 音效反馈（该方法由赛博教员实现）
        if (mc.level != null) {
            mc.level.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(),
                SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (cfg.showDamage) {
            displayClientMessage("§c假人受到伤害: §f" + String.format("%.1f", damage)
                + " §7(剩余: §f" + String.format("%.1f", Math.max(0, fakePlayer.getHealth())) + "§7)");
        }

        return true;
    }

    /**
     * ★ Tick 事件：假人定时更新 + 自动图腾
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        FakePlayerConfig cfg = FakePlayerConfig.getInstance();
        if (!cfg.enabled || fakePlayer == null || !fakePlayer.isAlive()) return;

        // ★ 自动补充图腾（双持）
        if (cfg.autoTotem) {
            if (fakePlayer.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            }
            if (fakePlayer.getMainHandItem().getItem() != Items.TOTEM_OF_UNDYING) {
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            }
        }

        // ★ 更新无敌计时
        fakePlayer.tickCombat();

        // ★ 自动重生
        if (!fakePlayer.isAlive() && cfg.respawn) {
            spawn();
        }
    }

    // ===== 内部逻辑 =====

    /**
     * 计算玩家攻击伤害
     *
     * 基于玩家主手武器的基础攻击伤害属性 + 附魔加成
     */
    private static float calculateAttackDamage(LivingEntity attacker) {
        if (!(attacker instanceof net.minecraft.world.entity.player.Player)) return 1.0F;
        net.minecraft.world.entity.player.Player player = (net.minecraft.world.entity.player.Player) attacker;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return 1.0F;

        // ★ 获取武器基础伤害属性
        double baseDamage = weapon.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND)
            .get(Attributes.ATTACK_DAMAGE).stream()
            .mapToDouble(m -> m.getAmount())
            .sum();
        if (baseDamage == 0) baseDamage = 1.0;

        // ★ 计算附魔加成（锋利、亡灵杀手、节肢杀手）
        int sharpness = 0, smite = 0, bane = 0;
        var enchantments = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(weapon);
        for (var entry : enchantments.entrySet()) {
            var ench = entry.getKey();
            int level = entry.getValue();
            var id = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getKey(ench);
            if (id == null) continue;
            String path = id.getPath();
            if ("sharpness".equals(path)) sharpness += level;
            else if ("smite".equals(path)) smite += level;
            else if ("bane_of_arthropods".equals(path)) bane += level;
        }

        float enchantBonus = sharpness * 1.25F + smite * 2.5F + bane * 2.5F;

        // ★ 力量效果加成
        float strengthBonus = 0;
        if (player.hasEffect(MobEffects.DAMAGE_BOOST)) {
            MobEffectInstance effect = player.getEffect(MobEffects.DAMAGE_BOOST);
            if (effect != null) strengthBonus = 3.0F * (effect.getAmplifier() + 1);
        }
        // 虚弱效果减益
        float weaknessPenalty = 0;
        if (player.hasEffect(MobEffects.WEAKNESS)) {
            MobEffectInstance effect = player.getEffect(MobEffects.WEAKNESS);
            if (effect != null) weaknessPenalty = 4.0F * (effect.getAmplifier() + 1);
        }

        return (float) (baseDamage + enchantBonus + strengthBonus - weaknessPenalty);
    }

    /**
     * 判断本 Tick 是否已处理过攻击（防重入）
     */
    private static boolean handledByThisTick() {
        if (handledByUs) return true;
        return false;
    }

    private static void markHandled() {
        handledByUs = true;
        // 下个 Tick 自动重置
        new Thread(() -> {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            handledByUs = false;
        }).start();
    }

    private static void displayClientMessage(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(msg), false);
        }
    }

    // ===== 假人实体类 =====

    /**
     * 假人实体 — 客户端 Only 的 AbstractClientPlayer 子类（替代 RemotePlayer）
     *
     * ★ 职责：
     *   1. 复制真实玩家的位置/旋转/背包/姿态
     *   2. 管理受击无敌计时
     *   3. 处理伤害应用与图腾触发
     */
    public static class FakePlayerEntity extends AbstractClientPlayer {

        /** 受击无敌计时器（Tick） */
        private int combatCooldown = 0;

        /** 是否在地面（构造时固定） */
        private final boolean ground;

        public FakePlayerEntity(net.minecraft.world.entity.player.Player player, String name, float health, boolean copyInv) {
            super(mc.level, new GameProfile(UUID.randomUUID(), name));
            // ★ 复制玩家状态（参考 IMGFakePlayer 原始实现）
            this.copyPosition(player);
            this.setYRot(player.getYRot());
            this.setXRot(player.getXRot());
            this.yBodyRot = player.yBodyRot;
            this.yHeadRot = player.yHeadRot;
            this.yHeadRotO = player.yHeadRotO;
            // 渲染插值位置（相当于 Fabric Yarn 的 lastRenderX/Y/Z）
            this.xo = player.xo;
            this.yo = player.yo;
            this.zo = player.zo;
            this.wasTouchingWater = player.isInWater();
            this.setShiftKeyDown(player.isShiftKeyDown());
            this.setPose(player.getPose());
            this.ground = player.onGround();
            this.setOnGround(this.ground);
            this.setBoundingBox(player.getBoundingBox());
            this.setHealth(health);

            // ★ 复制背包
            if (copyInv) {
                var playerInv = player.getInventory();
                var fakeInv = this.getInventory();
                for (int i = 0; i < playerInv.getContainerSize(); i++) {
                    fakeInv.setItem(i, playerInv.getItem(i).copy());
                }
            }

            // ★ 初始图腾
            if (FakePlayerConfig.getInstance().autoTotem) {
                this.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            }

            // ★ 吸收血量
            float absorption = player.getAbsorptionAmount();
            this.setAbsorptionAmount(absorption);
        }

        @Override
        public boolean onGround() {
            return ground;
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }

        /**
         * Tick 更新（由 ClientTickEvent 调用）
         */
        public void tickCombat() {
            if (combatCooldown > 0) combatCooldown--;
            if (hurtTime > 0) hurtTime--;
        }

        /**
         * 应用伤害
         *
         * ★ 检查无敌计时
         * ★ 扣血 + 图腾触发 + 死亡处理
         */
        public void applyDamage(float damage) {
            if (combatCooldown > 0) return;

            float oldHealth = getHealth();
            float newHealth = oldHealth - damage;

            // 设置无敌时间
            this.combatCooldown = FakePlayerConfig.getInstance().invulnerableTicks;
            this.hurtTime = 10;
            this.hurtDuration = 10;

            if (newHealth <= 0f) {
                // ★ 尝试触发图腾
                boolean totemPopped = tryPopTotem();
                if (!totemPopped) {
                    die();
                } else {
                    this.setHealth(1.0f);
                }
            } else {
                this.setHealth(newHealth);
            }
        }

        /**
         * 尝试触发不死图腾
         *
         * @return true 如果图腾被触发
         */
        private boolean tryPopTotem() {
            boolean hasTotem = getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING
                || getMainHandItem().getItem() == Items.TOTEM_OF_UNDYING;

            if (!hasTotem) return false;

            // ★ 移除图腾
            if (getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING) {
                setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            } else {
                setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }

            // ★ 触发效果
            this.setHealth(10.0f);
            this.setAbsorptionAmount(4.0f);
            this.removeAllEffects();
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));

            // ★ 重设无敌时间
            this.combatCooldown = FakePlayerConfig.getInstance().invulnerableTicks;
            this.hurtTime = 10;

            // ★ 粒子效果 + 音效
            if (mc.level != null) {
                for (int i = 0; i < 30; i++) {
                    double vx = (mc.level.random.nextDouble() - 0.5) * 0.5;
                    double vy = mc.level.random.nextDouble() * 0.5;
                    double vz = (mc.level.random.nextDouble() - 0.5) * 0.5;
                    mc.level.addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                        this.getX() + vx * 2, this.getY() + 1.0 + vy * 2, this.getZ() + vz * 2,
                        vx, vy + 0.5, vz);
                }
                mc.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            FakePlayerConfig cfg = FakePlayerConfig.getInstance();
            if (cfg.showDamage) {
                FakePlayerFeature.displayClientMessage("§6假人触发了不死图腾！");
            }

            return true;
        }

        /**
         * 假人死亡
         */
        private void die() {
            this.setHealth(0f);
            if (mc.level != null) {
                mc.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            this.remove(RemovalReason.KILLED);
            fakePlayer = null;

            FakePlayerFeature.displayClientMessage("§c假人已死亡。");
        }
    }
}