package fku.org.example.fku.features.healthtag; /* water */

import fku.org.example.fku.api.ILivingEntityGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import java.awt.Color;

public class HealthTagRenderer {
    private static final int WIDTH = 180;
    private static final int HEIGHT = 45;
    private static boolean dragging = false;
    private static int dragOffsetX, dragOffsetY;
    
    private static float displayedHealth = -1f;
    private static long lastUpdateTime = 0;

    public static void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!HealthTagManager.shouldDisplay()) return;

        LivingEntity entity = HealthTagManager.getTargetEntity();
        HealthTagConfig config = HealthTagConfig.getInstance();
        float alpha = HealthTagManager.getAlpha();
        boolean editing = HealthTagManager.isEditing();

        String name = "未检测到实体";
        float health = 20f;
        float maxHealth = 20f;

        if (entity != null) {
            name = entity instanceof Player ? entity.getName().getString() : entity.getType().getDescription().getString();
            health = entity.getHealth();
            maxHealth = entity.getMaxHealth();
        } else if (!editing) {
            return;
        }

        int alphaInt = (int) (alpha * 255);
        int bgColor = new Color(15, 15, 15, alphaInt).getRGB();
        int borderColor = new Color(60, 60, 60, alphaInt).getRGB();
        if (editing) borderColor = new Color(0, 120, 215, alphaInt).getRGB();

        drawBetterRoundedRect(guiGraphics, config.x, config.y, config.x + WIDTH, config.y + HEIGHT, 6, bgColor, borderColor);

        if (entity != null) {
            if (entity instanceof ILivingEntityGui guiEntity) guiEntity.fku$setGuiRendering(true);
            
            int modelX = config.x + 25;
            int modelY = config.y + HEIGHT - 8;
            float entityHeight = entity.getBbHeight();
            float entityWidth = entity.getBbWidth();
            float maxDim = Math.max(entityHeight, entityWidth);
            float size = 18.0F;
            if (maxDim > 2.0F) size = 18.0F * (2.0F / maxDim);
            if (maxDim < 0.5F && maxDim > 0) size = 18.0F * (0.5F / maxDim);
            size = Math.max(size, 2.0F);
            
            float xMouse = (float)(modelX - mouseX);
            float yMouse = (float)(modelY - 30 - mouseY);
            Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf rotation = new Quaternionf().rotateX((float) Math.atan(yMouse / 40.0F) * 20.0F * ((float) Math.PI / 180.0F));
            pose.mul(rotation);

            float oldYRot = entity.getYRot();
            float oldYRotO = entity.yRotO;
            float oldYBodyRot = entity.yBodyRot;
            float oldYBodyRotO = entity.yBodyRotO;
            float oldYHeadRot = entity.getYHeadRot();
            float oldYHeadRotO = entity.yHeadRotO;

            entity.setYRot(180.0f);
            entity.yRotO = 180.0f;
            entity.yBodyRot = 180.0f;
            entity.yBodyRotO = 180.0f;
            entity.setYHeadRot(180.0f + xMouse * 0.2f);
            entity.yHeadRotO = 180.0f + xMouse * 0.2f;

            try { InventoryScreen.renderEntityInInventory(guiGraphics, modelX, modelY, (int)size, pose, null, entity); } catch (Exception e) {}

            entity.setYRot(oldYRot);
            entity.yRotO = oldYRotO;
            entity.yBodyRot = oldYBodyRot;
            entity.yBodyRotO = oldYBodyRotO;
            entity.setYHeadRot(oldYHeadRot);
            entity.yHeadRotO = oldYHeadRotO;

            if (entity instanceof ILivingEntityGui guiEntity) guiEntity.fku$setGuiRendering(false);
        }

        int contentX = config.x + 45;
        guiGraphics.drawString(Minecraft.getInstance().font, name, contentX, config.y + 8, 0xFFFFFF | (alphaInt << 24));

        if (displayedHealth < 0 || Math.abs(displayedHealth - health) > 50) {
            displayedHealth = health;
        } else {
            float diff = health - displayedHealth;
            float step = diff * 0.15f;
            displayedHealth += step;
            if (Math.abs(diff) < 0.01f) displayedHealth = health;
        }

        float healthRatio = Math.max(0, Math.min(1, health / maxHealth));
        float animatedRatio = Math.max(0, Math.min(1, displayedHealth / maxHealth));
        int barX = contentX;
        int barY = config.y + 24;
        int barWidth = WIDTH - 55;
        int barHeight = 8;

        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, new Color(20, 20, 20, alphaInt).getRGB());
        if (animatedRatio > healthRatio) {
            int bufferColor = new Color(180, 50, 50, (int)(alphaInt * 0.8f)).getRGB();
            guiGraphics.fill(barX, barY, barX + (int)(barWidth * animatedRatio), barY + barHeight, bufferColor);
        }
        int hColor = getHealthColor(healthRatio, alphaInt);
        guiGraphics.fill(barX, barY, barX + (int)(barWidth * healthRatio), barY + barHeight, hColor);
        int highlightColor = new Color(255, 255, 255, (int)(alphaInt * 0.3f)).getRGB();
        guiGraphics.fill(barX, barY, barX + (int)(barWidth * healthRatio), barY + 1, highlightColor);

        String healthText = String.format("%.1f / %.1f", health, maxHealth);
        float textScale = 0.85f;
        float textWidth = Minecraft.getInstance().font.width(healthText) * textScale;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(barX + (barWidth - textWidth) / 2, barY - 2, 0);
        guiGraphics.pose().scale(textScale, textScale, textScale);
        guiGraphics.drawString(Minecraft.getInstance().font, healthText, 1, 1, (alphaInt / 2 << 24), false);
        guiGraphics.drawString(Minecraft.getInstance().font, healthText, 0, 0, 0xFFFFFF | (alphaInt << 24), false);
        guiGraphics.pose().popPose();

        if (editing && entity == null) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, "拖动我 (3D 模型区域)", config.x + WIDTH / 2, config.y + HEIGHT / 2 - 4, 0x55FFFFFF);
        }
    }

    private static int getHealthColor(float ratio, int alpha) {
        int r, g, b;
        if (ratio > 0.5) {
            float f = (ratio - 0.5f) * 2f;
            r = (int) (255 * (1f - f)); g = 255; b = 0;
        } else {
            float f = ratio * 2f;
            r = 255; g = (int) (255 * f); b = 0;
        }
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    private static void drawBetterRoundedRect(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, int color, int borderColor) {
        guiGraphics.fill(x1 + radius, y1, x2 - radius, y2, color);
        guiGraphics.fill(x1, y1 + radius, x2, y2 - radius, color);
        fillCircleCorner(guiGraphics, x1 + radius, y1 + radius, radius, 180, color);
        fillCircleCorner(guiGraphics, x2 - radius, y1 + radius, radius, 270, color);
        fillCircleCorner(guiGraphics, x1 + radius, y2 - radius, radius, 90, color);
        fillCircleCorner(guiGraphics, x2 - radius, y2 - radius, radius, 0, color);
        guiGraphics.fill(x1 + radius, y1, x2 - radius, y1 + 1, borderColor);
        guiGraphics.fill(x1 + radius, y2 - 1, x2 - radius, y2, borderColor);
        guiGraphics.fill(x1, y1 + radius, x1 + 1, y2 - radius, borderColor);
        guiGraphics.fill(x2 - 1, y1 + radius, x2, y2 - radius, borderColor);
    }

    private static void fillCircleCorner(GuiGraphics guiGraphics, int x, int y, int radius, int startAngle, int color) {
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    int dx = (startAngle == 180 || startAngle == 90) ? -i : i;
                    int dy = (startAngle == 180 || startAngle == 270) ? -j : j;
                    guiGraphics.fill(x + dx, y + dy, x + dx + 1, y + dy + 1, color);
                }
            }
        }
    }

    public static boolean onMouseClicked(double mouseX, double mouseY, int button) {
        if (!HealthTagManager.shouldDisplay()) return false;
        HealthTagConfig config = HealthTagConfig.getInstance();
        if (mouseX >= config.x && mouseX <= config.x + WIDTH && mouseY >= config.y && mouseY <= config.y + HEIGHT) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = (int) mouseX - config.x;
                dragOffsetY = (int) mouseY - config.y;
                return true;
            }
        }
        return false;
    }

    public static void onMouseDragged(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            HealthTagConfig config = HealthTagConfig.getInstance();
            config.x = (int) mouseX - dragOffsetX;
            config.y = (int) mouseY - dragOffsetY;
        }
    }

    public static void onMouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            HealthTagConfig.save();
        }
    }
}