package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 无尽自瞄 — 移植自 Lexis EndlessAimbotHack
 * 全图最近实体自瞄+自动开火（通过反射调用 TaCZ API，无依赖时仅旋转不自动开火）
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class EndlessAimbotFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;
    private static Field taczDataField = null;
    private static Field taczAimingField = null;
    private static boolean taczReflectionFailed = false;
    private static LivingEntity currentTarget = null;
    private static boolean hasTarget = false;
    private static long lastShootTime = 0L;
    private static final long SHOOT_COOLDOWN = 40L; // 40ms = 25发/秒

    // 反射调用 shoot
    private static Class<?> clientPlayerGunOperatorClass;
    private static Method fromLocalPlayerMethod;
    private static Method shootMethod;
    private static Class<?> shootResultClass;
    private static Object shootResultSuccess;
    private static boolean shootReflectionFailed = false;

    // HeadOnlyLook 模拟
    private static float targetYaw = 0, targetPitch = 0;
    private static float currentYaw = 0, currentPitch = 0;
    private static boolean isLooking = false;

    private static void initShootReflection() {
        if (shootReflectionFailed) return;
        try {
            clientPlayerGunOperatorClass = Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
            fromLocalPlayerMethod = clientPlayerGunOperatorClass.getMethod("fromLocalPlayer", LocalPlayer.class);
            shootMethod = clientPlayerGunOperatorClass.getMethod("shoot");
            shootResultClass = Class.forName("com.tacz.guns.api.entity.ShootResult");
            for (Object constant : shootResultClass.getEnumConstants()) {
                if (constant.toString().equals("SUCCESS")) { shootResultSuccess = constant; break; }
            }
        } catch (Exception e) {
            shootReflectionFailed = true;
            Fku.LOGGER.warn("[EndlessAimbot] TaCZ 枪械模组未安装，自动开火功能不可用");
        }
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        initShootReflection();
        MinecraftForge.EVENT_BUS.register(EndlessAimbotFeature.class);
        Fku.LOGGER.info("[EndlessAimbotFeature] 无尽自瞄已初始化");
    }

    public static boolean isEnabled() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.endlessAimbotEnabled;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.endlessAimbotEnabled || mc.player == null || mc.level == null) return;

        if (cfg.endlessOnlyOnLeftClick && !mc.options.keyAttack.isDown()) {
            stopLooking(); clearTarget(); return;
        }
        if (cfg.endlessOnlyWhenAiming && !isPlayerAiming(mc.player)) {
            stopLooking(); clearTarget(); return;
        }

        if (currentTarget != null && !currentTarget.isAlive()) { clearTarget(); }

        LocalPlayer player = mc.player;
        List<LivingEntity> targets = getValidTargets();
        LivingEntity nearest = findNearestTarget(targets, player);
        if (nearest == null) { stopLooking(); clearTarget(); return; }

        currentTarget = nearest;
        hasTarget = true;
        Vec3 targetPos = getAimPoint(nearest, cfg.endlessBodyPart);
        Vec3 eye = player.getEyePosition(1);
        Vec3 dir = targetPos.subtract(eye);
        double distXZ = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        if (distXZ < 0.01) return;

        targetYaw = normalizeAngle((float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90);
        targetPitch = Mth.clamp((float) (-Math.toDegrees(Math.atan2(dir.y, distXZ))), -90, 90);

        if (!isLooking) {
            currentYaw = player.getYRot();
            currentPitch = player.getXRot();
            isLooking = true;
        }

        // ★ 动态旋转速度 — 距离目标越远旋转越快，接近时减速
        float yawDiff = normalizeAngle(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;
        float absDiff = Math.max(Math.abs(yawDiff), Math.abs(pitchDiff));
        // 基础速度 + 距离修正（远距离更快，近距离更精确）
        float speedMultiplier = 0.12f + (Math.min(absDiff, 60f) / 60f) * 0.08f;
        float maxRot = cfg.endlessRotationSpeed * speedMultiplier;
        // 当接近目标时直接吸附（降低分段感）
        if (absDiff < 1.0f) {
            currentYaw = targetYaw;
            currentPitch = targetPitch;
        } else {
            currentYaw = normalizeAngle(currentYaw + Mth.clamp(yawDiff, -maxRot, maxRot));
            currentPitch = Mth.clamp(currentPitch + Mth.clamp(pitchDiff, -maxRot, maxRot), -90, 90);
        }
        player.setYRot(currentYaw);
        player.setXRot(currentPitch);
        mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(currentYaw, currentPitch, player.onGround()));

        // ★ 自动开火 — 当目标已锁定且距离足够近时持续射击
        if (!shootReflectionFailed) {
            long now = System.currentTimeMillis();
            if (now - lastShootTime >= SHOOT_COOLDOWN) {
                try {
                    Object operator = fromLocalPlayerMethod.invoke(null, player);
                    if (operator != null) {
                        shootMethod.invoke(operator);
                        lastShootTime = now;
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private static void stopLooking() { isLooking = false; }
    private static void clearTarget() { currentTarget = null; hasTarget = false; }

    private static LivingEntity findNearestTarget(List<LivingEntity> targets, LocalPlayer player) {
        if (targets.isEmpty()) return null;
        TaCZConfig cfg = TaCZConfig.getInstance();
        Vec3 eyePos = player.getEyePosition(1);
        Vec3 lookVec = player.getLookAngle();
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity entity : targets) {
            if (!entity.isAlive()) continue;
            Vec3 targetPos = entity.getBoundingBox().getCenter();
            Vec3 toTarget = targetPos.subtract(eyePos);
            double dist = toTarget.length();
            if (dist < 0.01 || toTarget.dot(lookVec) < 0) continue;
            if (!cfg.endlessAllowThroughWalls && !hasLineOfSight(eyePos, targetPos)) continue;
            if (dist < bestDist) { bestDist = dist; best = entity; }
        }
        return best;
    }

    private static boolean hasLineOfSight(Vec3 from, Vec3 to) {
        if (mc.level == null) return true;
        return mc.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    private static List<LivingEntity> getValidTargets() {
        List<LivingEntity> targets = new ArrayList<>();
        if (mc.player == null || mc.level == null) return targets;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player || !living.isAlive()) continue;
            targets.add(living);
        }
        return targets;
    }

    private static Vec3 getAimPoint(LivingEntity e, String bodyPart) {
        Vec3 c = e.getBoundingBox().getCenter();
        double feetY = e.getY();
        double h = e.getBbHeight();
        double y;
        switch (bodyPart) {
            case "头": y = feetY + e.getEyeHeight(); break;
            case "腿": y = feetY + h * 0.28; break;
            case "脚": y = feetY + h * 0.08; break;
            case "自动": return getAutoAimPoint(e, c);
            default: y = feetY + h * 0.5; break;
        }
        return new Vec3(c.x, y, c.z);
    }

    private static Vec3 getAutoAimPoint(LivingEntity e, Vec3 c) {
        if (mc.player == null) return c;
        double feetY = e.getY();
        double h = e.getBbHeight();
        double[] candidates = {feetY + e.getEyeHeight(), feetY + h * 0.5, feetY + h * 0.28, feetY + h * 0.08};
        Vec3 eye = mc.player.getEyePosition(1);
        float curYaw = mc.player.getYRot();
        float curPitch = mc.player.getXRot();
        Vec3 best = c;
        double bestAngle = Double.MAX_VALUE;
        for (double y : candidates) {
            Vec3 p = new Vec3(c.x, y, c.z);
            Vec3 d = p.subtract(eye);
            double distXZ = Math.sqrt(d.x * d.x + d.z * d.z);
            if (distXZ < 1e-4) continue;
            float yaw = normalizeAngle((float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90));
            float pitch = (float) (-Math.toDegrees(Math.atan2(d.y, distXZ)));
            double angle = Math.abs(normalizeAngle(yaw - curYaw)) + Math.abs(pitch - curPitch);
            if (angle < bestAngle) { bestAngle = angle; best = p; }
        }
        return best;
    }

    private static float normalizeAngle(float angle) {
        angle %= 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }

    private static boolean isPlayerAiming(LocalPlayer player) {
        if (!taczReflectionFailed) {
            try {
                if (taczDataField == null) {
                    for (Field f : player.getClass().getDeclaredFields()) {
                        if (f.getType().getName().equals("com.tacz.guns.client.gameplay.LocalPlayerDataHolder")) {
                            f.setAccessible(true); taczDataField = f; break;
                        }
                    }
                    if (taczDataField == null) { taczReflectionFailed = true; return player.isUsingItem(); }
                }
                Object data = taczDataField.get(player);
                if (data == null) return false;
                if (taczAimingField == null) {
                    taczAimingField = data.getClass().getDeclaredField("clientIsAiming");
                    taczAimingField.setAccessible(true);
                }
                return (Boolean) taczAimingField.get(data);
            } catch (Exception e) { taczReflectionFailed = true; }
        }
        return player.isUsingItem();
    }
}