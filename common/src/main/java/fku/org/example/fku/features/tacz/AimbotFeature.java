package fku.org.example.fku.features.tacz; /* water */

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 子弹自瞄 — 移植自 Lexis AimbotHack
 * FOV 圆形瞄准辅助，渲染屏幕圆圈
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class AimbotFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;
    private static Field taczDataField = null;
    private static Field taczAimingField = null;
    private static boolean taczReflectionFailed = false;
    private static LivingEntity currentTarget = null;
    private static boolean hasTarget = false;
    private static long lastUpdateTime = 0L;
    // 自定义实体 id 解析缓存（配置变化或内容变化时刷新）
    private static long customEntitiesCacheKey = 0L;
    private static Set<String> customEntityIds = new HashSet<>();

    public static void init() {
        if (initialized) return;
        initialized = true;
        MinecraftForge.EVENT_BUS.register(AimbotFeature.class);
        Fku.LOGGER.info("[AimbotFeature] 子弹自瞄已初始化");
    }

    public static boolean isEnabled() { 
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.aimbotEnabled; 
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.aimbotEnabled || mc.player == null || mc.level == null) return;

        // ★ 排除弓箭：仅手持枪械（TaCZ / SuperbWarfare）时生效
        if (!isHoldingGunWeapon()) {
            if (hasTarget) { hasTarget = false; currentTarget = null; }
            return;
        }

        if (cfg.aimbotOnlyWhenAiming && !isPlayerAiming(mc.player)) {
            if (hasTarget) { hasTarget = false; currentTarget = null; }
            return;
        }

        LocalPlayer player = mc.player;
        List<LivingEntity> targets = getValidTargets();
        LivingEntity best = findBestTarget(targets, player);
        if (best == null) {
            if (hasTarget) { hasTarget = false; currentTarget = null; }
            return;
        }

        Vec3 targetPos = getAimPoint(best, cfg.aimbotBodyPart);
        Vec3 eye = player.getEyePosition(1);
        Vec3 dir = targetPos.subtract(eye);
        double distXZ = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        if (distXZ < 0.01) return;

        float targetYaw = normalizeAngle((float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90);
        float targetPitch = Mth.clamp((float) (-Math.toDegrees(Math.atan2(dir.y, distXZ))), -90, 90);
        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();
        float yawDiff = normalizeAngle(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        long now = System.currentTimeMillis();
        float deltaSec = (float) (now - lastUpdateTime) / 1000f;
        lastUpdateTime = now;
        if (deltaSec <= 0 || deltaSec > 1) deltaSec = 0.05f;

        float maxRot = cfg.aimbotRotationSpeed * deltaSec;
        float stepYaw = Mth.clamp(yawDiff, -maxRot, maxRot);
        float stepPitch = Mth.clamp(pitchDiff, -maxRot, maxRot);
        float newYaw = normalizeAngle(currentYaw + stepYaw);
        float newPitch = Mth.clamp(currentPitch + stepPitch, -90, 90);

        player.setYRot(newYaw);
        player.setXRot(newPitch);
        mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(newYaw, newPitch, player.onGround()));
        hasTarget = true;
        currentTarget = best;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent event) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.aimbotEnabled || mc.player == null) return;
        // 仅手持枪械（TaCZ / SuperbWarfare）时显示自瞄圈
        if (!isHoldingGunWeapon()) return;
        // 【修复】使用 GUI 缩放坐标而非屏幕物理像素
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int cx = screenW / 2;
        int cy = screenH / 2;
        int drawColor = hasTarget ? cfg.aimbotLockColor : cfg.aimbotCircleColor;
        float r = (float) (drawColor >> 16 & 255) / 255f;
        float g = (float) (drawColor >> 8 & 255) / 255f;
        float b = (float) (drawColor & 255) / 255f;
        float a = (float) (drawColor >> 24 & 255) / 255f;
        float fovR = (float) (cfg.aimbotFovColor >> 16 & 255) / 255f;
        float fovG = (float) (cfg.aimbotFovColor >> 8 & 255) / 255f;
        float fovB = (float) (cfg.aimbotFovColor & 255) / 255f;
        float fovA = (float) (cfg.aimbotFovColor >> 24 & 255) / 255f;

        PoseStack poseStack = event.getGuiGraphics().pose();
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        int segments = 64;

        // 填充圆
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, cx, cy, 0).color(fovR, fovG, fovB, fovA).endVertex();
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 2 * i / segments;
            float x = cx + (float) (cfg.aimbotCircleSize * Math.cos(angle));
            float y = cy + (float) (cfg.aimbotCircleSize * Math.sin(angle));
            buffer.vertex(matrix, x, y, 0).color(fovR, fovG, fovB, fovA).endVertex();
        }
        tesselator.end();

        // 圆环边框
        RenderSystem.lineWidth(2f);
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 2 * i / segments;
            float x = cx + (float) (cfg.aimbotCircleSize * Math.cos(angle));
            float y = cy + (float) (cfg.aimbotCircleSize * Math.sin(angle));
            buffer.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(1f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // ── 辅助方法 ──

    private static LivingEntity findBestTarget(List<LivingEntity> targets, LocalPlayer player) {
        if (targets.isEmpty()) return null;
        Vec3 eyePos = player.getEyePosition(1);
        Vec3 lookVec = player.getLookAngle();
        float lookYaw = player.getYRot();
        float lookPitch = player.getXRot();
        TaCZConfig cfg = TaCZConfig.getInstance();

        double fov = mc.options.fov().get();
        double halfH = mc.getWindow().getScreenHeight() / 2.0;
        double halfFovRad = Math.toRadians(fov / 2.0);
        double projScale = halfH / Math.tan(halfFovRad);
        double circleAngleRad = Math.atan(cfg.aimbotCircleSize / projScale);

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity entity : targets) {
            if (!entity.isAlive()) continue;
            Vec3 targetPos = entity.getBoundingBox().getCenter();
            Vec3 toTarget = targetPos.subtract(eyePos);
            double dist = toTarget.length();
            if (dist < 0.01 || toTarget.dot(lookVec) < 0) continue;
            if (!cfg.aimbotAllowThroughWalls && !hasLineOfSight(eyePos, targetPos)) continue;

            double totalAngle = Math.acos(Mth.clamp(lookVec.dot(toTarget.normalize()), -1, 1));
            if (totalAngle > circleAngleRad * 1.5) continue;

            double distXZ = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            double targetYaw = Math.toDegrees(Math.atan2(toTarget.z, toTarget.x)) - 90;
            double targetPitch = -Math.toDegrees(Math.atan2(toTarget.y, distXZ));
            double yawDiff = Math.toRadians(normalizeAngle((float) (targetYaw - lookYaw)));
            double pitchDiff = Math.toRadians(targetPitch - lookPitch);
            yawDiff = Mth.clamp(yawDiff, -1.55, 1.55);
            pitchDiff = Mth.clamp(pitchDiff, -1.55, 1.55);
            double screenX = Math.tan(yawDiff) * projScale;
            double screenY = Math.tan(pitchDiff) * projScale;
            double screenDist = Math.sqrt(screenX * screenX + screenY * screenY);
            if (screenDist <= cfg.aimbotCircleSize && screenDist < bestDist) {
                bestDist = screenDist;
                best = entity;
            }
        }
        return best;
    }

    private static boolean hasLineOfSight(Vec3 from, Vec3 to) {
        if (mc.level == null) return true;
        HitResult result = mc.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        return result.getType() == HitResult.Type.MISS;
    }

    private static List<LivingEntity> getValidTargets() {
        List<LivingEntity> targets = new ArrayList<>();
        if (mc.player == null || mc.level == null) return targets;
        TaCZConfig cfg = TaCZConfig.getInstance();
        String mode = cfg.aimbotTargetMode;
        boolean isCustom = "自定义".equals(mode);
        if (isCustom) refreshCustomEntities();
        // 遍历所有世界中的实体
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player || !living.isAlive()) continue;
            if (!matchTarget(living, mode, isCustom)) continue;
            targets.add(living);
        }
        return targets;
    }

    /** 根据对象选择器过滤目标 */
    private static boolean matchTarget(LivingEntity e, String mode, boolean isCustom) {
        if ("仅玩家".equals(mode)) {
            return e instanceof Player;
        }
        if (isCustom) {
            if (customEntityIds.isEmpty()) return false;
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
            return id != null && customEntityIds.contains(id.toString());
        }
        // 全部实体：任意活着的 LivingEntity
        return true;
    }

    /** 解析自定义实体 id（逗号分隔），配置变化时刷新缓存 */
    private static void refreshCustomEntities() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        String raw = cfg.aimbotCustomEntities == null ? "" : cfg.aimbotCustomEntities;
        long key = raw.hashCode() * 31L + cfg.aimbotTargetMode.hashCode();
        if (key == customEntitiesCacheKey) return;
        customEntitiesCacheKey = key;
        Set<String> set = new HashSet<>();
        for (String s : raw.split(",")) {
            String t = s.trim().toLowerCase(java.util.Locale.ROOT);
            if (!t.isEmpty()) set.add(t);
        }
        customEntityIds = set;
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
        // SuperbWarfare 枪械：检测右键是否按下（SBW 右键开镜，但会取消 vanilla 事件，所以 isUsingItem 不可靠）
        if (isHoldingSBW()) {
            long window = mc.getWindow().getWindow();
            return org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        }

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

    /** 检测玩家是否手持 SuperbWarfare 枪械 */
    private static boolean isHoldingSBW() {
        if (mc.player == null) return false;
        try {
            Class<?> gunItemClass = Class.forName("com.atsuishio.superbwarfare.item.gun.GunItem");
            return gunItemClass.isInstance(mc.player.getMainHandItem().getItem());
        } catch (Exception e) { return false; }
    }

    /** 检测玩家是否手持 TaCZ 或 SuperbWarfare 枪械 */
    private static boolean isHoldingGunWeapon() {
        if (mc.player == null) return false;
        // TaCZ 检测
        try {
            Class<?> iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
            java.lang.reflect.Method getIGunOrNull = iGunClass.getMethod("getIGunOrNull", net.minecraft.world.item.ItemStack.class);
            Object gun = getIGunOrNull.invoke(null, mc.player.getMainHandItem());
            if (gun != null) return true;
        } catch (Exception ignored) {}
        // SuperbWarfare 检测
        try {
            Class<?> gunItemClass = Class.forName("com.atsuishio.superbwarfare.item.gun.GunItem");
            if (gunItemClass.isInstance(mc.player.getMainHandItem().getItem())) return true;
        } catch (Exception ignored) {}
        return false;
    }
}