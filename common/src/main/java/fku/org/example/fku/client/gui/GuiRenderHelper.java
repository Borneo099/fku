package fku.org.example.fku.client.gui;

import fku.org.example.fku.config.GuiStyleConfig;

import net.minecraft.client.gui.GuiGraphics;

/**
 * GUI渲染辅助类 — 经 Apple Design 原则优化
 * - 软阴影（渐变衰减，取代多层硬边缘）
 * - 改进的毛玻璃效果（更平滑的层叠）
 * - 发光/高光效果
 * - 材质感知绘制（边框高光）
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
     * 绘制软阴影 — Apple Design §12: translucent depth
     * 用多层渐变半透明矩形模拟软阴影
     * @param guiGraphics 图形上下文
     * @param x 左上角X坐标
     * @param y 左上角Y坐标
     * @param width 宽度
     * @param height 高度
     * @param alpha 整体透明度乘数 (0~1)
     */
    public static void drawSoftShadow(GuiGraphics guiGraphics, int x, int y, int width, int height, float alpha) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (!config.shadowEnabled || alpha <= 0) return;
        
        int shadowStrength = config.shadowStrength;
        int shadowSize = Math.max(4, shadowStrength / 8);
        int baseAlpha = (int) (shadowStrength * alpha * 0.6f);
        
        // 从内到外：渐变半透明层 (Apple: 更大表面 = 更深阴影)
        for (int i = 0; i < shadowSize; i++) {
            int layerAlpha = (int) (baseAlpha * (1 - i / (double) shadowSize) * 0.7f);
            if (layerAlpha <= 0) continue;
            int sc = (layerAlpha << 24) | 0x000000;
            
            int inset = i;
            // 底部阴影（比右侧稍大，模拟光源从左上）
            guiGraphics.fill(x + inset - 2, y + height + inset, x + width - inset + 2, y + height + inset + 2, sc);
            // 右侧阴影
            guiGraphics.fill(x + width + inset, y + inset + 2, x + width + inset + 2, y + height - inset, sc);
        }
    }
    
    /**
     * 绘制发光边框 — Apple Design §12: light-catching edge
     * 顶部精细高光，底部柔和暗边，模拟真实材质的受光效果
     */
    public static void drawGlowBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int radius, float alpha) {
        int baseAlpha = (int)(180 * alpha);
        if (baseAlpha <= 0) return;
        
        // 顶部高光（亮边 — 模拟光源）
        int topColor = (baseAlpha << 24) | 0xFFFFFF;
        guiGraphics.fill(x + radius, y, x + width - radius, y + 1, topColor);
        // 底部暗边
        int bottomColor = (baseAlpha << 24) | 0x000000;
        guiGraphics.fill(x + radius, y + height - 1, x + width - radius, y + height, bottomColor);
    }
    
    /**
     * 绘制接受 alpha 的面板背景
     */
    public static void drawPanelBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean isTitleBar, float alpha) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        // 绘制阴影
        drawSoftShadow(guiGraphics, x, y, width, height, alpha);
        
        // 绘制背景
        int adjustedAlpha = (int) (config.backgroundAlpha * alpha);
        adjustedAlpha = Math.max(0, Math.min(255, adjustedAlpha));
        int bgColor = isTitleBar ? 
            config.getPrimaryColorWithAlpha(adjustedAlpha) : 
            config.getBackgroundColorWithAlpha(adjustedAlpha);
        
        drawRoundedRect(guiGraphics, x, y, width, height, bgColor, config.cornerRadius);
        
        // 绘制材质边框（Apple §12: 光效边缘）
        int borderColor = isTitleBar ? 
            config.getPrimaryColorWithAlpha(Math.min(255, adjustedAlpha + 40)) :
            config.getBorderColorWithAlpha((int)(200 * alpha));
        drawRoundedOutline(guiGraphics, x, y, width, height, borderColor, config.cornerRadius, 1);
        
        // 顶部高光（Apple: 模拟材质受光）
        if (alpha > 0.5f) {
            int glowAlpha = (int)(80 * alpha);
            int topGlow = (glowAlpha << 24) | 0xFFFFFF;
            if (config.cornerRadius > 0) {
                guiGraphics.fill(x + config.cornerRadius, y, x + width - config.cornerRadius, y + 1, topGlow);
            }
        }
    }

    /**
     * 绘制完整的面板背景（兼容旧调用）
     */
    public static void drawPanelBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean isTitleBar) {
        drawPanelBackground(guiGraphics, x, y, width, height, isTitleBar, 1f);
    }
    
    /**
     * 绘制组件背景（兼容旧调用 — 5参数）
     */
    public static void drawComponentBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean enabled) {
        drawComponentBackground(guiGraphics, x, y, width, height, enabled, 1f);
    }
    
    /**
     * 绘制组件背景（支持 alpha） — Apple: 清洁、无装饰的交互元素
     */
    public static void drawComponentBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean enabled, float alpha) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        int adjAlpha = (int)(180 * alpha);
        
        int bgColor = enabled ? 
            (config.getEnabledColor() | (adjAlpha << 24)) : 
            config.getBackgroundColorWithAlpha(adjAlpha);
        
        int radius = Math.max(2, config.cornerRadius / 2);
        drawRoundedRect(guiGraphics, x, y, width, height, bgColor, radius);
        
        // 启用状态下的发光内边框
        if (enabled) {
            int borderColor = config.getEnabledColor() | (255 << 24);
            drawRoundedOutline(guiGraphics, x, y, width, height, borderColor, radius, 1);
        }
    }
}