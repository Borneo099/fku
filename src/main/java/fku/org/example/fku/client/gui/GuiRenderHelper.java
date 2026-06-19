package fku.org.example.fku.client.gui;

import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * GUI渲染辅助类
 * 提供圆角矩形、毛玻璃效果、阴影等渲染功能
 */
public class GuiRenderHelper {
    
    /**
     * 绘制圆角矩形
     * @param guiGraphics 图形上下文
     * @param x 左上角X坐标
     * @param y 左上角Y坐标
     * @param width 宽度
     * @param height 高度
     * @param color 颜色（含Alpha）
     * @param radius 圆角半径
     */
    public static void drawRoundedRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, int radius) {
        if (radius <= 0 || width <= 0 || height <= 0) {
            if (width > 0 && height > 0) {
                guiGraphics.fill(x, y, x + width, y + height, color);
            }
            return;
        }
        
        // 限制圆角半径不超过宽高的一半
        radius = Math.min(radius, Math.min(width / 2, height / 2));
        if (radius <= 0) {
            guiGraphics.fill(x, y, x + width, y + height, color);
            return;
        }
        
        // 绘制中心矩形
        guiGraphics.fill(x + radius, y, x + width - radius, y + height, color);
        guiGraphics.fill(x, y + radius, x + radius, y + height - radius, color);
        guiGraphics.fill(x + width - radius, y + radius, x + width, y + height - radius, color);
        
        // 绘制四个圆角（使用小矩形近似）
        // 避免整数除法导致 step=0 的死循环（半径<3时退化为填充）
        int step = Math.max(1, radius / 3);
        for (int i = 0; i <= radius; i += step) {
            // 左上角
            guiGraphics.fill(x + i, y + i, x + radius, y + radius, color);
            // 右上角
            guiGraphics.fill(x + width - radius, y + i, x + width - i, y + radius, color);
            // 左下角
            guiGraphics.fill(x + i, y + height - radius, x + radius, y + height - i, color);
            // 右下角
            guiGraphics.fill(x + width - radius, y + height - radius, x + width - i, y + height - i, color);
        }
    }
    
    /**
     * 绘制圆角边框
     * @param guiGraphics 图形上下文
     * @param x 左上角X坐标
     * @param y 左上角Y坐标
     * @param width 宽度
     * @param height 高度
     * @param color 边框颜色
     * @param radius 圆角半径
     * @param borderWidth 边框宽度
     */
    public static void drawRoundedOutline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, int radius, int borderWidth) {
        if (width <= 0 || height <= 0) return;
        if (radius <= 0) {
            guiGraphics.renderOutline(x, y, width, height, color);
            return;
        }
        
        radius = Math.min(radius, Math.min(width / 2, height / 2));
        if (radius <= 0) {
            guiGraphics.renderOutline(x, y, width, height, color);
            return;
        }
        
        // 绘制边框的四个边
        // 上边
        guiGraphics.fill(x + radius, y, x + width - radius, y + borderWidth, color);
        // 下边
        guiGraphics.fill(x + radius, y + height - borderWidth, x + width - radius, y + height, color);
        // 左边
        guiGraphics.fill(x, y + radius, x + borderWidth, y + height - radius, color);
        // 右边
        guiGraphics.fill(x + width - borderWidth, y + radius, x + width, y + height - radius, color);
        
        // 四个圆角边框
        for (int i = 0; i < borderWidth; i++) {
            int r = radius - i;
            if (r <= 0) break;
            
            // 左上角
            guiGraphics.fill(x + i, y + i, x + borderWidth, y + borderWidth, color);
            // 右上角
            guiGraphics.fill(x + width - borderWidth, y + i, x + width - i, y + borderWidth, color);
            // 左下角
            guiGraphics.fill(x + i, y + height - borderWidth, x + borderWidth, y + height - i, color);
            // 右下角
            guiGraphics.fill(x + width - borderWidth, y + height - borderWidth, x + width - i, y + height - i, color);
        }
    }
    
    /**
     * 绘制毛玻璃效果背景
     * 通过多层半透明矩形叠加实现
     * @param guiGraphics 图形上下文
     * @param x 左上角X坐标
     * @param y 左上角Y坐标
     * @param width 宽度
     * @param height 高度
     */
    public static void drawBlurBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        int blurStrength = config.blurStrength;
        int baseAlpha = config.backgroundAlpha;
        
        // 根据毛玻璃强度调整透明度
        int adjustedAlpha = (int) (baseAlpha * (1 - blurStrength / 100.0 * 0.3));
        
        // 绘制多层半透明背景模拟毛玻璃效果
        int layers = Math.max(1, blurStrength / 20);
        for (int i = 0; i < layers; i++) {
            int layerAlpha = adjustedAlpha + (i * 10);
            layerAlpha = Math.min(255, layerAlpha);
            int color = config.getBackgroundColorWithAlpha(layerAlpha);
            drawRoundedRect(guiGraphics, x + i, y + i, width - i * 2, height - i * 2, color, config.cornerRadius);
        }
    }
    
    /**
     * 绘制阴影效果
     * @param guiGraphics 图形上下文
     * @param x 左上角X坐标
     * @param y 左上角Y坐标
     * @param width 宽度
     * @param height 高度
     */
    public static void drawShadow(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (!config.shadowEnabled) return;
        
        int shadowStrength = config.shadowStrength;
        int shadowSize = Math.max(2, shadowStrength / 10);
        
        // 绘制多层阴影
        for (int i = 0; i < shadowSize; i++) {
            int shadowAlpha = (int) (shadowStrength * (1 - i / (double) shadowSize) * 0.5);
            shadowAlpha = Math.max(0, Math.min(100, shadowAlpha));
            int shadowColor = (shadowAlpha << 24) | 0x000000;
            
            // 底部阴影
            guiGraphics.fill(x + i, y + height, x + width - i, y + height + shadowSize - i, shadowColor);
            // 右侧阴影
            guiGraphics.fill(x + width, y + i, x + width + shadowSize - i, y + height - i, shadowColor);
        }
    }
    
    /**
     * 绘制完整的面板背景（包含阴影、毛玻璃、圆角）
     * @param guiGraphics 图形上下文
     * @param x 左上角X坐标
     * @param y 左上角Y坐标
     * @param width 宽度
     * @param height 高度
     * @param isTitleBar 是否是标题栏
     */
    public static void drawPanelBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean isTitleBar) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        // 绘制阴影
        drawShadow(guiGraphics, x, y, width, height);
        
        // 绘制背景
        int bgColor = isTitleBar ? 
            config.getPrimaryColorWithAlpha(config.backgroundAlpha) : 
            config.getBackgroundColorWithAlpha(config.backgroundAlpha);
        
        drawRoundedRect(guiGraphics, x, y, width, height, bgColor, config.cornerRadius);
        
        // 绘制边框
        int borderColor = config.getBorderColorWithAlpha(200);
        drawRoundedOutline(guiGraphics, x, y, width, height, borderColor, config.cornerRadius, 1);
    }
    
    /**
     * 绘制组件背景
     * @param guiGraphics 图形上下文
     * @param x 左上角X坐标
     * @param y 左上角Y坐标
     * @param width 宽度
     * @param height 高度
     * @param enabled 是否启用状态
     */
    public static void drawComponentBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean enabled) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        int bgColor = enabled ? 
            config.getEnabledColor() | (150 << 24) : 
            config.getBackgroundColorWithAlpha(180);
        
        drawRoundedRect(guiGraphics, x, y, width, height, bgColor, Math.max(2, config.cornerRadius / 2));
        
        // 绘制边框
        int borderColor = enabled ? 
            config.getEnabledColor() | (255 << 24) : 
            config.getBorderColorWithAlpha(200);
        drawRoundedOutline(guiGraphics, x, y, width, height, borderColor, Math.max(2, config.cornerRadius / 2), 1);
    }
}