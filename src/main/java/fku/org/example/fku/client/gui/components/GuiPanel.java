package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI面板基类
 * 支持圆角、毛玻璃效果、动画、收放功能
 */
public abstract class GuiPanel {
    protected String title;
    protected int x, y, width, height;
    protected boolean dragging = false;
    protected int dragOffsetX, dragOffsetY;
    protected boolean expanded = true;
    protected final List<GuiComponent> components = new ArrayList<>();
    protected final Minecraft mc = Minecraft.getInstance();
    
    // 动画相关
    protected float animationProgress = 0f;
    protected float targetHeight;
    protected float currentHeight;
    protected long lastAnimationTime = 0;
    
    // 收放状态
    protected boolean collapsed = false;

    public GuiPanel(String title, int x, int y, int width, int height) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.targetHeight = height;
        this.currentHeight = 20; // 初始只显示标题栏
        init();
    }

    protected abstract void init();

    protected void addComponent(GuiComponent component) {
        this.components.add(component);
        updatePositions();
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        // 更新动画
        updateAnimation();
        
        // 计算实际渲染高度
        int renderHeight = (int) currentHeight;
        
        // 绘制阴影
        if (config.shadowEnabled && !collapsed) {
            GuiRenderHelper.drawShadow(guiGraphics, x, y, width, renderHeight);
        }
        
        // 绘制标题栏背景
        GuiRenderHelper.drawPanelBackground(guiGraphics, x, y, width, 20, true);
        
        // 绘制标题文字
        guiGraphics.drawString(mc.font, title, x + 5, y + 6, config.getTextColor());
        
        // 绘制收放指示器
        String indicator = collapsed ? "+" : "-";
        guiGraphics.drawString(mc.font, indicator, x + width - 12, y + 6, config.getTextColor());
        
        // 如果展开且动画完成，绘制内容区域
        if (!collapsed && animationProgress > 0.5f) {
            // 绘制内容区域背景
            int contentHeight = (int) (currentHeight - 20);
            if (contentHeight > 0) {
                GuiRenderHelper.drawPanelBackground(guiGraphics, x, y + 20, width, contentHeight, false);
            }
            
            // 绘制组件
            for (GuiComponent component : components) {
                if (component.isVisible()) {
                    component.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            }
        }
    }

    /**
     * 更新动画状态
     */
    protected void updateAnimation() {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        if (!config.animationEnabled) {
            currentHeight = collapsed ? 20 : targetHeight;
            animationProgress = collapsed ? 0f : 1f;
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastAnimationTime;
        lastAnimationTime = currentTime;
        
        float target = collapsed ? 20 : targetHeight;
        float speed = (targetHeight - 20) / (config.animationSpeed / 1000f) * (deltaTime / 1000f);
        
        if (currentHeight < target) {
            currentHeight += speed;
            if (currentHeight > target) currentHeight = target;
        } else if (currentHeight > target) {
            currentHeight -= speed;
            if (currentHeight < target) currentHeight = target;
        }
        
        // 计算动画进度
        animationProgress = (currentHeight - 20) / (targetHeight - 20);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 点击标题栏区域
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 20) {
            if (button == 0) {
                // 左键拖拽
                dragging = true;
                dragOffsetX = (int) mouseX - x;
                dragOffsetY = (int) mouseY - y;
                return true;
            } else if (button == 1) {
                // 右键收放
                collapsed = !collapsed;
                return true;
            }
        }
        
        // 如果展开，处理组件点击
        if (!collapsed && animationProgress > 0.5f) {
            for (GuiComponent component : components) {
                if (component.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            this.x = (int) mouseX - dragOffsetX;
            this.y = (int) mouseY - dragOffsetY;
            clampPosition();
            updatePositions();
            savePosition();
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!collapsed) {
            for (GuiComponent component : components) {
                if (component.keyPressed(keyCode, scanCode, modifiers)) return true;
            }
        }
        return false;
    }

    protected void updatePositions() {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        int yOffset = 25;
        for (int i = 0; i < components.size(); i++) {
            components.get(i).updatePosition(this.x, this.y, yOffset);
            yOffset += config.componentHeight + config.componentSpacing;
        }
        // 更新目标高度
        targetHeight = 20 + yOffset - 20;
    }

    protected void clampPosition() {
        if (this.x < 0) this.x = 0;
        if (this.y < 0) this.y = 0;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        if (this.x + this.width > screenWidth) this.x = screenWidth - this.width;
        if (this.y + (int) currentHeight > screenHeight) this.y = screenHeight - (int) currentHeight;
    }

    protected abstract void savePosition();
    
    /**
     * 获取当前渲染高度
     */
    public int getCurrentHeight() {
        return (int) currentHeight;
    }
}