package fku.org.example.fku.features.yposoverlay;

import fku.org.example.fku.config.MovementConfig;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class YPosOverlay {
    private static Entity cachedTarget = null;
    private static long lastUpdateTick = -1L;

    public static void toggle() {
        MovementConfig config = MovementConfig.getInstance();
        config.setYPosOverlayEnabled(!config.yPosOverlayEnabled);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(("YPos \u663e\u793a: " + (config.yPosOverlayEnabled ? "\u00a7a\u5f00\u542f" : "\u00a7c\u5173\u95ed"))), true);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        int color;
        String text;
        double playerY;
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
        }
        if (!MovementConfig.getInstance().yPosOverlayEnabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.level.getGameTime() != lastUpdateTick && (lastUpdateTick = mc.level.getGameTime()) % 5L == 0L) {
            cachedTarget = YPosOverlay.findTargetEntity(mc);
        }
        if (cachedTarget == null) {
            return;
        }
        double targetY = cachedTarget.getY();
        if (Math.abs(targetY - (playerY = mc.player.getY())) < 0.5) {
            text = "Y: \u00a7a" + String.format("%.1f", targetY);
            color = 65280;
        } else {
            text = "Y: " + String.format("%.1f", targetY);
            color = 0xFFFFFF;
        }
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int textX = centerX - mc.font.width(text) / 2;
        int textY = centerY + 15;
        event.getGuiGraphics().drawString(mc.font, text, textX, textY, color);
    }

    private static Entity findTargetEntity(Minecraft mc) {
        Entity camera = mc.getCameraEntity();
        if (camera == null) {
            return null;
        }
        Vec3 eyePos = camera.getEyePosition(1.0f);
        Vec3 lookVec = camera.getViewVector(1.0f);
        Vec3 endPoint = eyePos.add(lookVec.scale(256.0));
        AABB searchArea = camera.getBoundingBox().inflate(256.0);
        List entities = mc.level.getEntities(camera, searchArea, e -> e != camera && e.isAlive() && !e.isSpectator());
        Entity best = null;
        double bestDist = 65536.0;
        for (Object entityObj : entities) {
            Entity entity = (Entity)entityObj;
            double distSq;
            AABB hitbox = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional hit = hitbox.clip(eyePos, endPoint);
            if (!hit.isPresent() || !((distSq = eyePos.distanceToSqr((Vec3)hit.get())) < bestDist)) continue;
            bestDist = distSq;
            best = entity;
        }
        return best;
    }
}

