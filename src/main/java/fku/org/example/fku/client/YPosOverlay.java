package fku.org.example.fku.client;

import fku.org.example.fku.config.MovementConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class YPosOverlay {

    // 缓存目标实体，避免每帧遍历过多实体时卡顿（简单优化）
    private static Entity cachedTarget = null;
    private static long lastUpdateTick = -1;

    public static void toggle() {
        MovementConfig config = MovementConfig.getInstance();
        config.yPosOverlayEnabled = !config.yPosOverlayEnabled;
        MovementConfig.save();
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "YPos 显示: " + (config.yPosOverlayEnabled ? "§a开启" : "§c关闭")
                    ), true);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;
        if (!MovementConfig.getInstance().yPosOverlayEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 更新目标实体（每 5 tick 更新一次，减少性能压力）
        if (mc.level.getGameTime() != lastUpdateTick) {
            lastUpdateTick = mc.level.getGameTime();
            if (lastUpdateTick % 5 == 0) {
                cachedTarget = findTargetEntity(mc);
            }
        }

        if (cachedTarget == null) return;

        double targetY = cachedTarget.getY();
        double playerY = mc.player.getY();

        String text;
        int color;
        if (Math.abs(targetY - playerY) < 0.5) {
            text = "Y: §a" + String.format("%.1f", targetY);
            color = 0x00FF00; // 绿色
        } else {
            text = "Y: " + String.format("%.1f", targetY);
            color = 0xFFFFFF; // 白色
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int textX = centerX - mc.font.width(text) / 2;
        int textY = centerY + 15; // 准星下方 15 像素

        event.getGuiGraphics().drawString(mc.font, text, textX, textY, color);
    }

    /**
     * 射线检测 256 格内最近的实体
     */
    private static Entity findTargetEntity(Minecraft mc) {
        Entity camera = mc.getCameraEntity();
        if (camera == null) return null;

        Vec3 eyePos = camera.getEyePosition(1.0F);
        Vec3 lookVec = camera.getViewVector(1.0F);
        Vec3 endPoint = eyePos.add(lookVec.scale(256.0));

        // 扩大搜索区域，半径 256 格
        AABB searchArea = camera.getBoundingBox().inflate(256.0);
        List<Entity> entities = mc.level.getEntities(camera, searchArea,
                e -> e != camera && e.isAlive() && !e.isSpectator());

        Entity best = null;
        double bestDist = 256.0 * 256.0; // 平方距离

        for (Entity entity : entities) {
            // 扩大碰撞箱以考虑点击容差
            AABB hitbox = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> hit = hitbox.clip(eyePos, endPoint);
            if (hit.isPresent()) {
                double distSq = eyePos.distanceToSqr(hit.get());
                if (distSq < bestDist) {
                    bestDist = distSq;
                    best = entity;
                }
            }
        }
        return best;
    }
}