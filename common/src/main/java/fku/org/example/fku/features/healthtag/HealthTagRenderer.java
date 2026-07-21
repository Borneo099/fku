package fku.org.example.fku.features.healthtag;

import fku.org.example.fku.api.ILivingEntityGui;
import fku.org.example.fku.features.healthtag.HealthTagConfig;
import fku.org.example.fku.features.healthtag.HealthTagManager;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class HealthTagRenderer {
    private static final int WIDTH = 180;
    private static final int HEIGHT = 45;
    private static boolean dragging = false;
    private static int dragOffsetX;
    private static int dragOffsetY;
    private static float displayedHealth;
    private static long lastUpdateTime;

    public static void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!HealthTagManager.shouldDisplay()) {
            return;
        }
        LivingEntity entity = HealthTagManager.getTargetEntity();
        HealthTagConfig config = HealthTagConfig.getInstance();
        float alpha = HealthTagManager.getAlpha();
        boolean editing = HealthTagManager.isEditing();
        String name = "\u672a\u68c0\u6d4b\u5230\u5b9e\u4f53";
        float health = 20.0f;
        float maxHealth = 20.0f;
        if (entity != null) {
            name = entity instanceof Player ? entity.m_7755_().getString() : entity.m_6095_().m_20676_().getString();
            health = entity.m_21223_();
            maxHealth = entity.m_21233_();
        } else if (!editing) {
            return;
        }
        int alphaInt = (alpha * 255.0f);
        int bgColor = new Color(15, 15, 15, alphaInt).getRGB();
        int borderColor = new Color(60, 60, 60, alphaInt).getRGB();
        if (editing) {
            borderColor = new Color(0, 120, 215, alphaInt).getRGB();
        }
        HealthTagRenderer.drawBetterRoundedRect(guiGraphics, config.x, config.y, config.x + 180, config.y + 45, 6, bgColor, borderColor);
        if (entity != null) {
            if (entity instanceof ILivingEntityGui) {
                ILivingEntityGui guiEntity = (ILivingEntityGui)entity;
                guiEntity.fku$setGuiRendering(true);
            }
            int modelX = config.x + 25;
            int modelY = config.y + 45 - 8;
            float entityHeight = entity.getBbHeight();
            float entityWidth = entity.getBbWidth();
            float maxDim = Math.max(entityHeight, entityWidth);
            float size = 18.0f;
            if (maxDim > 2.0f) {
                size = 18.0f * (2.0f / maxDim);
            }
            if (maxDim < 0.5f && maxDim > 0.0f) {
                size = 18.0f * (0.5f / maxDim);
            }
            size = Math.max(size, 2.0f);
            float xMouse = modelX - mouseX;
            float yMouse = modelY - 30 - mouseY;
            Quaternionf pose = new Quaternionf().rotateZ(Math.PI);
            Quaternionf rotation = new Quaternionf().rotateX(Math.atan(yMouse / 40.0f) * 20.0f * (Math.PI / 180));
            pose.mul((Quaternionfc)rotation);
            float oldYRot = entity.m_146908_();
            float oldYRotO = entity.f_19859_;
            float oldYBodyRot = entity.f_20883_;
            float oldYBodyRotO = entity.f_20884_;
            float oldYHeadRot = entity.m_6080_();
            float oldYHeadRotO = entity.f_20886_;
            entity.m_146922_(180.0f);
            entity.f_19859_ = 180.0f;
            entity.f_20883_ = 180.0f;
            entity.f_20884_ = 180.0f;
            entity.m_5616_(180.0f + xMouse * 0.2f);
            entity.f_20886_ = 180.0f + xMouse * 0.2f;
            try {
                InventoryScreen.m_280432_((GuiGraphics)guiGraphics, modelX, modelY, (size), (Quaternionf)pose, null, (LivingEntity) entity);
            }
            catch (Exception exception) {
                // ignored
            }
            entity.m_146922_(oldYRot);
            entity.f_19859_ = oldYRotO;
            entity.f_20883_ = oldYBodyRot;
            entity.f_20884_ = oldYBodyRotO;
            entity.m_5616_(oldYHeadRot);
            entity.f_20886_ = oldYHeadRotO;
            if (entity instanceof ILivingEntityGui) {
                ILivingEntityGui guiEntity = (ILivingEntityGui)entity;
                guiEntity.fku$setGuiRendering(false);
            }
        }
        int contentX = config.x + 45;
        guiGraphics.drawString(Minecraft.getInstance().font, name, contentX, config.y + 8, 0xFFFFFF | alphaInt << 24);
        if (displayedHealth < 0.0f || Math.abs(displayedHealth - health) > 50.0f) {
            displayedHealth = health;
        } else {
            float diff = health - displayedHealth;
            float step = diff * 0.15f;
            displayedHealth += step;
            if (Math.abs(diff) < 0.01f) {
                displayedHealth = health;
            }
        }
        float healthRatio = Math.max(0.0f, Math.min(1.0f, health / maxHealth));
        float animatedRatio = Math.max(0.0f, Math.min(1.0f, displayedHealth / maxHealth));
        int barX = contentX;
        int barY = config.y + 24;
        int barWidth = 125;
        int barHeight = 8;
        guiGraphics.m_280509_(barX, barY, barX + barWidth, barY + barHeight, new Color(20, 20, 20, alphaInt).getRGB());
        if (animatedRatio > healthRatio) {
            int bufferColor = new Color(180, 50, 50, (alphaInt * 0.8f)).getRGB();
            guiGraphics.m_280509_(barX, barY, barX + (barWidth * animatedRatio), barY + barHeight, bufferColor);
        }
        int hColor = HealthTagRenderer.getHealthColor(healthRatio, alphaInt);
        guiGraphics.m_280509_(barX, barY, barX + (barWidth * healthRatio), barY + barHeight, hColor);
        int highlightColor = new Color(255, 255, 255, (alphaInt * 0.3f)).getRGB();
        guiGraphics.m_280509_(barX, barY, barX + (barWidth * healthRatio), barY + 1, highlightColor);
        String healthText = String.format("%.1f / %.1f", health), maxHealth));
        float textScale = 0.85f;
        float textWidth = Minecraft.getInstance().font.m_92895_(healthText) * textScale;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().m_252880_(barX + (barWidth - textWidth) / 2.0f, (barY - 2), 0.0f);
        guiGraphics.pose().m_85841_(textScale, textScale, textScale);
        guiGraphics.drawString(Minecraft.getInstance().font, healthText, 1, 1, alphaInt / 2 << 24, false);
        guiGraphics.drawString(Minecraft.getInstance().font, healthText, 0, 0, 0xFFFFFF | alphaInt << 24, false);
        guiGraphics.pose().popPose();
        if (editing && entity == null) {
            guiGraphics.m_280137_(Minecraft.getInstance().font, "\u62d6\u52a8\u6211 (3D \u6a21\u578b\u533a\u57df)", config.x + 90, config.y + 22 - 4, 0x55FFFFFF);
        }
    }

    private static int getHealthColor(float ratio, int alpha) {
        int b;
        int g;
        int r;
        if (ratio > 0.5) {
            float f = (ratio - 0.5f) * 2.0f;
            r = (255.0f * (1.0f - f));
            g = 255;
            b = 0;
        } else {
            float f = ratio * 2.0f;
            r = 255;
            g = (255.0f * f);
            b = 0;
        }
        return alpha << 24 | r << 16 | g << 8 | b;
    }

    private static void drawBetterRoundedRect(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, int color, int borderColor) {
        guiGraphics.m_280509_(x1 + radius, y1, x2 - radius, y2, color);
        guiGraphics.m_280509_(x1, y1 + radius, x2, y2 - radius, color);
        HealthTagRenderer.fillCircleCorner(guiGraphics, x1 + radius, y1 + radius, radius, 180, color);
        HealthTagRenderer.fillCircleCorner(guiGraphics, x2 - radius, y1 + radius, radius, 270, color);
        HealthTagRenderer.fillCircleCorner(guiGraphics, x1 + radius, y2 - radius, radius, 90, color);
        HealthTagRenderer.fillCircleCorner(guiGraphics, x2 - radius, y2 - radius, radius, 0, color);
        guiGraphics.m_280509_(x1 + radius, y1, x2 - radius, y1 + 1, borderColor);
        guiGraphics.m_280509_(x1 + radius, y2 - 1, x2 - radius, y2, borderColor);
        guiGraphics.m_280509_(x1, y1 + radius, x1 + 1, y2 - radius, borderColor);
        guiGraphics.m_280509_(x2 - 1, y1 + radius, x2, y2 - radius, borderColor);
    }

    private static void fillCircleCorner(GuiGraphics guiGraphics, int x, int y, int radius, int startAngle, int color) {
        for (int i = 0; i < radius; ++i) {
            for (int j = 0; j < radius; ++j) {
                if (i * i + j * j > radius * radius) continue;
                int dx = startAngle == 180 || startAngle == 90 ? -i : i;
                int dy = startAngle == 180 || startAngle == 270 ? -j : j;
                guiGraphics.m_280509_(x + dx, y + dy, x + dx + 1, y + dy + 1, color);
            }
        }
    }

    public static boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (!HealthTagManager.shouldDisplay()) {
            return false;
        }
        HealthTagConfig config = HealthTagConfig.getInstance();
        if (mouseX >= config.x && mouseX <= (config.x + 180) && mouseY >= config.y && mouseY <= (config.y + 45) && button == 0) {
            dragging = true;
            dragOffsetX = mouseX - config.x;
            dragOffsetY = mouseY - config.y;
            return true;
        }
        return false;
    }

    public static void onMouseDragged(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            HealthTagConfig config = HealthTagConfig.getInstance();
            config.x = mouseX - dragOffsetX;
            config.y = mouseY - dragOffsetY;
        }
    }

    public static void onMouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            HealthTagConfig.save();
        }
    }

    static {
        displayedHealth = -1.0f;
        lastUpdateTime = 0L;
    }
}

