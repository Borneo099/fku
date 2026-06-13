package fku.org.example.fku.client;

import fku.org.example.fku.config.HealthTagConfig;
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
    
    // 用于平滑动画
    private static float displayedHealth = -1f;
    private static long lastUpdateTime = 0;

    public static void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!HealthTagManager.shouldDisplay()) return;

        LivingEntity entity = HealthTagManager.getTargetEntity();
        HealthTagConfig config = HealthTagConfig.getInstance();
        float alpha = HealthTagManager.getAlpha();
        boolean editing = HealthTagManager.isEditing();

        // If no target but editing, show a placeholder
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

        // --- Render Background ---
        int alphaInt = (int) (alpha * 255);
        int bgColor = new Color(15, 15, 15, alphaInt).getRGB();
        int borderColor = new Color(60, 60, 60, alphaInt).getRGB();
        if (editing) {
            borderColor = new Color(0, 120, 215, alphaInt).getRGB();
        }

        drawBetterRoundedRect(guiGraphics, config.x, config.y, config.x + WIDTH, config.y + HEIGHT, 6, bgColor, borderColor);

        // --- Render 3D Entity Model ---
        if (entity != null) {
            // 设置 Mixin 标志位，实现 180 度正面视角
            if (entity instanceof ILivingEntityGui guiEntity) {
                guiEntity.fku$setGuiRendering(true);
            }
            
            // 渲染 3D 模型
            int modelX = config.x + 25;
            int modelY = config.y + HEIGHT - 8;
            
            // 动态缩放逻辑：根据实体的宽度和高度自动调整缩放比例
            // 基础尺寸为 18，对于大型实体（如末影龙、巨人）进行缩小
            float entityHeight = entity.getBbHeight();
            float entityWidth = entity.getBbWidth();
            float maxDim = Math.max(entityHeight, entityWidth);
            
            // 目标：将实体约束在约 30x30 的区域内
            float size = 18.0F;
            if (maxDim > 2.0F) {
                size = 18.0F * (2.0F / maxDim);
            }
            
            // 针对极小型实体（如蜜蜂、鱼）进行适度放大
            if (maxDim < 0.5F && maxDim > 0) {
                size = 18.0F * (0.5F / maxDim);
            }

            // 限制最小尺寸，防止由于异常数据导致消失
            size = Math.max(size, 2.0F);
            
            // 1.20.1 中需要使用 Quaternionf 来控制旋转
            float xMouse = (float)(modelX - mouseX);
            float yMouse = (float)(modelY - 30 - mouseY);
            
            Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
            Quaternionf rotation = new Quaternionf().rotateX((float) Math.atan(yMouse / 40.0F) * 20.0F * ((float) Math.PI / 180.0F));
            pose.mul(rotation);
            
            // 备份并设置实体旋转，以确保 3D 模型渲染时朝向正确
            float oldYRot = entity.getYRot();
            float oldYRotO = entity.yRotO;
            float oldYBodyRot = entity.yBodyRot;
            float oldYBodyRotO = entity.yBodyRotO;
            float oldYHeadRot = entity.getYHeadRot();
            float oldYHeadRotO = entity.yHeadRotO;
            
            // 1.20.1 中，getYHeadRot 和相关字段在 GUI 渲染中需要特别处理
            entity.setYRot(180.0f);
            entity.yRotO = 180.0f;
            entity.yBodyRot = 180.0f;
            entity.yBodyRotO = 180.0f;
            entity.setYHeadRot(180.0f + xMouse * 0.2f);
            entity.yHeadRotO = 180.0f + xMouse * 0.2f;

            // 调用渲染
            try {
                InventoryScreen.renderEntityInInventory(guiGraphics, modelX, modelY, (int)size, pose, null, entity);
            } catch (Exception e) {
                // 防止某些特殊实体渲染崩溃导致 UI 消失
            }
            
            // 还原旋转
            entity.setYRot(oldYRot);
            entity.yRotO = oldYRotO;
            entity.yBodyRot = oldYBodyRot;
            entity.yBodyRotO = oldYBodyRotO;
            entity.setYHeadRot(oldYHeadRot);
            entity.yHeadRotO = oldYHeadRotO;
            
            // 还原 Mixin 标志位
            if (entity instanceof ILivingEntityGui guiEntity) {
                guiEntity.fku$setGuiRendering(false);
            }
        }

        // --- Render Content ---
        int contentX = config.x + 45;
        
        // Name (加一点阴影和偏移)
        guiGraphics.drawString(Minecraft.getInstance().font, name, contentX, config.y + 8, 0xFFFFFF | (alphaInt << 24));

        // --- Health Bar System ---
        // 1. 平滑动画逻辑
        if (displayedHealth < 0 || Math.abs(displayedHealth - health) > 50) {
            displayedHealth = health;
        } else {
            float diff = health - displayedHealth;
            float step = diff * 0.15f; // 平滑插值系数
            displayedHealth += step;
            if (Math.abs(diff) < 0.01f) displayedHealth = health;
        }

        float healthRatio = Math.max(0, Math.min(1, health / maxHealth));
        float animatedRatio = Math.max(0, Math.min(1, displayedHealth / maxHealth));
        
        int barX = contentX;
        int barY = config.y + 24;
        int barWidth = WIDTH - 55;
        int barHeight = 8;
        int barRadius = 4;

        // 2. 绘制血条背景（长方形）
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, new Color(20, 20, 20, alphaInt).getRGB());
        
        // 3. 绘制动画缓冲层（长方形）
        if (animatedRatio > healthRatio) {
            int bufferColor = new Color(180, 50, 50, (int)(alphaInt * 0.8f)).getRGB();
            guiGraphics.fill(barX, barY, barX + (int)(barWidth * animatedRatio), barY + barHeight, bufferColor);
        }

        // 4. 绘制实际血条（长方形）
        int hColor = getHealthColor(healthRatio, alphaInt);
        guiGraphics.fill(barX, barY, barX + (int)(barWidth * healthRatio), barY + barHeight, hColor);

        // 5. 增加血条光泽感（顶部的高光细线）
        int highlightColor = new Color(255, 255, 255, (int)(alphaInt * 0.3f)).getRGB();
        guiGraphics.fill(barX, barY, barX + (int)(barWidth * healthRatio), barY + 1, highlightColor);

        // 6. Health Text (增大字号，美化字体显示)
        String healthText = String.format("%.1f / %.1f", health, maxHealth);
        float textScale = 0.85f; // 增大字号从 0.7 -> 0.85
        float textWidth = Minecraft.getInstance().font.width(healthText) * textScale;
        guiGraphics.pose().pushPose();
        // 动态位置：文字放在血条中间稍微偏上的位置
        guiGraphics.pose().translate(barX + (barWidth - textWidth) / 2, barY - 2, 0);
        guiGraphics.pose().scale(textScale, textScale, textScale);
        // 文字外发光效果（简单的二次绘制偏移）
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
            // Green to Yellow
            float f = (ratio - 0.5f) * 2f;
            r = (int) (255 * (1f - f));
            g = 255;
            b = 0;
        } else {
            // Yellow to Red
            float f = ratio * 2f;
            r = 255;
            g = (int) (255 * f);
            b = 0;
        }
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    private static void drawBetterRoundedRect(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int radius, int color, int borderColor) {
        // Main Body
        guiGraphics.fill(x1 + radius, y1, x2 - radius, y2, color);
        guiGraphics.fill(x1, y1 + radius, x2, y2 - radius, color);
        
        // Corners
        fillCircleCorner(guiGraphics, x1 + radius, y1 + radius, radius, 180, color);
        fillCircleCorner(guiGraphics, x2 - radius, y1 + radius, radius, 270, color);
        fillCircleCorner(guiGraphics, x1 + radius, y2 - radius, radius, 90, color);
        fillCircleCorner(guiGraphics, x2 - radius, y2 - radius, radius, 0, color);

        // Border
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
