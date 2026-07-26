package fku.org.example.fku.features.tacz; /* water */

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 子弹透视 — 渲染 TaCZ 子弹弹道轨迹线
 * 使用 BufferBuilder + Tesselator 直接渲染
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class BulletTracersFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;
    private static Class<?> kineticBulletClass = null;

    static {
        try {
            kineticBulletClass = Class.forName("com.tacz.guns.entity.EntityKineticBullet", false, BulletTracersFeature.class.getClassLoader());
        } catch (Throwable ignored) {}
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        MinecraftForge.EVENT_BUS.register(BulletTracersFeature.class);
        Fku.LOGGER.info("[BulletTracersFeature] 子弹透视已初始化");
    }

    public static boolean isEnabled() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.bulletTracersEnabled;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!isEnabled() || mc.player == null || mc.level == null) return;
        if (kineticBulletClass == null) return;

        Vec3 cam = event.getCamera().getPosition();
        double maxDistSq = (double) cfg.tracerMaxDistance * cfg.tracerMaxDistance;
        float r = (float) (cfg.tracerColor >> 16 & 255) / 255f;
        float g = (float) (cfg.tracerColor >> 8 & 255) / 255f;
        float b = (float) (cfg.tracerColor & 255) / 255f;
        float a = (float) (cfg.tracerColor >> 24 & 255) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(cfg.tracerLineWidth);
        RenderSystem.depthMask(false);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float partialTick = event.getPartialTick();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (kineticBulletClass.isInstance(entity) && !(mc.player.distanceToSqr(entity) > maxDistSq)) {
                // 当前位置
                double px = entity.xOld + (entity.getX() - entity.xOld) * partialTick;
                double py = entity.yOld + (entity.getY() - entity.yOld) * partialTick;
                double pz = entity.zOld + (entity.getZ() - entity.zOld) * partialTick;
                // 速度方向（轨迹预测）
                Vec3 delta = entity.getDeltaMovement();
                double speed = delta.length();
                if (speed > 0.01) {
                    Vec3 dir = delta.normalize();
                    // 上一帧位置（向后延伸 = 轨迹线）
                    double prevX = px - dir.x * 0.5;
                    double prevY = py - dir.y * 0.5;
                    double prevZ = pz - dir.z * 0.5;
                    // 从上一帧位置到当前位置画一条轨迹线
                    buffer.vertex(matrix, (float)prevX, (float)prevY, (float)prevZ).color(r, g, b, a * 0.3f).endVertex();
                    buffer.vertex(matrix, (float)px, (float)py, (float)pz).color(r, g, b, a).endVertex();
                    // 向前延伸（预测轨迹）
                    buffer.vertex(matrix, (float)px, (float)py, (float)pz).color(r, g, b, a).endVertex();
                    buffer.vertex(matrix, (float)(px + dir.x * 0.3), (float)(py + dir.y * 0.3), (float)(pz + dir.z * 0.3)).color(r, g, b, a * 0.2f).endVertex();
                }
            }
        }
        tesselator.end();
        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1f);
    }
}