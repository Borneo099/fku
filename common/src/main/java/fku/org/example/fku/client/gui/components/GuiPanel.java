package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI面板基类 — 经 Apple Design 原则优化
 * - 弹簧物理动画（可中断、可重定向）
 * - 拖拽速度投射（松手惯性）
 * - 橡皮筋边界（软边界回弹）
 * - 即时反馈（不阻塞输入）
 */
public abstract class GuiPanel {
    protected String title;
    protected int x, y, width, height;
    protected boolean dragging = false;
    protected int dragOffsetX, dragOffsetY;
    protected boolean expanded = true;
    protected final List<GuiComponent> components = new ArrayList<>();
    protected final Minecraft mc = Minecraft.getInstance();
    
    // ——— 弹簧物理动画 ———
    /** 当前渲染高度（弹簧输出值） */
    protected float currentHeight;
    /** 弹簧速度 */
    protected float springVelocity = 0f;
    /** 弹簧目标值 */
    protected float springTarget;
    /** 上次帧时间 */
    protected long lastFrameTime = 0;
    /** 面板整体进入动画（0→1） */
    protected float entryProgress = 0f;
    protected float entryVelocity = 0f;
    protected boolean entryStarted = false;
    /** 面板索引（用于错峰启动） */
    protected int panelIndex = 0;
    /** 面板进入起始时间（错峰用） */
    protected long entryStartTime = 0;
    
    // ——— 拖拽速度投射 ———
    /** 速度历史（最后 5 帧位置/时间） */
    protected final float[] dragPositions = new float[5];
    protected final long[] dragTimes = new long[5];
    protected int dragHistoryIndex = 0;
    /** 松手后惯性动画 */
    protected boolean momentumActive = false;
    protected float momentumX = 0, momentumY = 0;
    protected float momentumVX = 0, momentumVY = 0;
    
    // 收放状态
    protected boolean collapsed = false;

    public GuiPanel(String title, int x, int y, int width, int height) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.springTarget = 20; // 初始只显示标题栏
        this.currentHeight = 20;
        this.lastFrameTime = System.currentTimeMillis();
        this.entryStartTime = System.currentTimeMillis();
        init();
    }

    /** 设置面板索引（影响错峰启动延迟） */
    public void setPanelIndex(int index) {
        this.panelIndex = index;
    }

    protected abstract void init();

    protected void addComponent(GuiComponent component) {
        this.components.add(component);
        updatePositions();
    }

    // ═══════════════════════════════════════════════
    //  弹簧物理 — Apple Design §3 Interruptibility
    // ═══════════════════════════════════════════════

    /**
     * 临界阻尼弹簧更新（Apple §4: damping=1.0, no overshoot）
     * @param target  目标值
     * @param current 当前值（弹簧位置）
     * @param vel     当前速度
     * @param dt      帧时间（秒）
     * @param stiffness 弹簧刚度 — 越高响应越快
     * @return [newPosition, newVelocity]
     */
    protected static float[] springDamp(float target, float current, float vel, float dt, float stiffness) {
        // 临界阻尼: damping = 2 * sqrt(stiffness)
        float damping = 2.0f * (float) Math.sqrt(stiffness);
        float displacement = current - target;
        float springForce = -stiffness * displacement;
        float dampingForce = -damping * vel;
        float accel = springForce + dampingForce;
        float newVel = vel + accel * dt;
        float newPos = current + newVel * dt;
        // 如果非常接近目标，直接归位防振荡
        if (Math.abs(displacement) < 0.5f && Math.abs(newVel) < 1f) {
            return new float[]{target, 0f};
        }
        return new float[]{newPos, newVel};
    }

    /**
     * 更新弹簧动画（含面板错峰启动）
     */
    protected void updateSpringAnimation() {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastFrameTime) / 1000f, 0.05f);
        lastFrameTime = now;

        if (!config.animationEnabled) {
            currentHeight = collapsed ? 20 : targetHeight();
            springVelocity = 0;
            entryProgress = 1f;
            return;
        }

        // — 面板进入动画：错峰启动（每索引延迟 60ms，面板逐次弹出） —
        long elapsedSinceOpen = now - entryStartTime;
        long panelDelay = panelIndex * 60L; // 每个面板延迟 60ms
        if (elapsedSinceOpen >= panelDelay) {
            if (!entryStarted) {
                entryStarted = true;
                entryProgress = 0f;
                entryVelocity = 0f;
            }
            if (entryProgress < 1f) {
                float[] result = springDamp(1f, entryProgress, entryVelocity, dt, 6f);
                entryProgress = result[0];
                entryVelocity = result[1];
            }
        }

        // — 高度弹簧（展开/收起） —
        springTarget = collapsed ? 20 : targetHeight();
        float[] hResult = springDamp(springTarget, currentHeight, springVelocity, dt, config.springStiffness);
        currentHeight = hResult[0];
        springVelocity = hResult[1];
    }

    /** 组件弹簧进度（用于子组件逐个进入） */
    protected float componentSpringDelay(int index) {
        float raw = entryProgress * 1.5f - index * 0.08f; // 每个组件延迟 0.08
        return Math.max(0, Math.min(1, raw));
    }

    // ═══════════════════════════════════════════════
    //  渲染
    // ═══════════════════════════════════════════════

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        
        // 更新弹簧动画（不阻塞输入）
        updateSpringAnimation();
        
        // 应用进入动画缩放
        float entryScale = 0.85f + 0.15f * entryProgress;
        float entryAlpha = Math.min(1f, entryProgress * 2f);
        int renderHeight = (int) currentHeight;
        if (renderHeight <= 0) return;
        
        // 绘制阴影（仅展开且有进入进度时）
        if (config.shadowEnabled && renderHeight > 20 && entryProgress > 0.1f) {
            GuiRenderHelper.drawSoftShadow(guiGraphics, x, y, width, renderHeight, entryAlpha);
        }
        
        // 标题栏带进入缩放
        int titleBarWidth = (int) (width * entryScale);
        int titleBarX = x + (width - titleBarWidth) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, titleBarX, y, titleBarWidth, 20, true, entryAlpha);
        
        // 标题文字
        int textAlpha = (int)(255 * entryAlpha);
        int textColor = (textAlpha << 24) | (config.getTextColor() & 0xFFFFFF);
        guiGraphics.drawString(mc.font, title, titleBarX + 5, y + 6, textColor);
        
        // 收放指示器
        String indicator = collapsed ? "+" : "−";
        int indColor = collapsed ? 0x888888 : config.getTextColor();
        indColor = (textAlpha << 24) | (indColor & 0xFFFFFF);
        guiGraphics.drawString(mc.font, indicator, x + width - 12, y + 6, indColor);
        
        // 内容区域（进入进度>30%时开始渐变显示）
        if (!collapsed && entryProgress > 0.3f) {
            int contentHeight = renderHeight - 20;
            if (contentHeight > 0) {
                float contentAlpha = Math.min(1f, (entryProgress - 0.3f) / 0.4f);
                GuiRenderHelper.drawPanelBackground(guiGraphics, x, y + 20, width, contentHeight, false, entryAlpha * contentAlpha);
            }
            
            // ★ 组件逐个弹出：每个组件有独立的延迟，依次淡入（Apple Design §8: 空间一致性）
            for (int i = 0; i < components.size(); i++) {
                GuiComponent component = components.get(i);
                if (!component.isVisible()) continue;
                float compAlpha = componentSpringDelay(i);
                if (compAlpha <= 0.01f) continue; // 透明度极低时跳过
                component.renderWithAlpha(guiGraphics, mouseX, mouseY, partialTick, compAlpha);
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  鼠标事件 — Apple Design §1 Response + §5 Velocity handoff
    // ═══════════════════════════════════════════════

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 无论动画是否完成都响应 — Apple §1: 即时反馈
        // 点击标题栏区域
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 20) {
            if (button == 0) {
                // 左键拖拽 — 记录抓取偏移（Apple §2: respect grab offset）
                dragging = true;
                dragOffsetX = (int) mouseX - x;
                dragOffsetY = (int) mouseY - y;
                // 重置速度历史
                dragHistoryIndex = 0;
                momentumActive = false;
                return true;
            } else if (button == 1) {
                // 右键收放 — 弹簧自动处理中断重定向（Apple §3）
                collapsed = !collapsed;
                // 可中断：保留当前速度，只是改变目标（§3）
                return true;
            }
        }
        
        // 组件点击（不依赖 animationProgress —— Apple §3: interruptibility）
        if (!collapsed) {
            for (GuiComponent component : components) {
                if (component.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            // 记录速度历史（Apple §5）
            dragPositions[dragHistoryIndex % 5] = (float) mouseX;
            dragTimes[dragHistoryIndex % 5] = System.nanoTime();
            dragHistoryIndex++;
            
            this.x = (int) mouseX - dragOffsetX;
            this.y = (int) mouseY - dragOffsetY;
            applyRubberBand();
            updatePositions();
            savePosition();
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            // 计算释放速度（Apple §5: Velocity handoff — 最后几帧的移动速度）
            if (dragHistoryIndex >= 3) {
                int idx = (dragHistoryIndex - 1) % 5;
                int prevIdx = (dragHistoryIndex - 3) % 5;
                float dx = dragPositions[idx] - dragPositions[prevIdx];
                long dt = dragTimes[idx] - dragTimes[prevIdx];
                if (dt > 0) {
                    float speed = dx / dt * 1e9f; // px/s
                    float absSpeed = Math.abs(speed);
                    if (absSpeed > 20f) {
                        // 投射惯性（Apple §6: Momentum projection）
                        momentumActive = true;
                        momentumVX = speed * 0.6f;
                        momentumVY = 0;
                    }
                }
            }
        }
    }

    /**
     * 更新惯性滑行（每次 render 调用）
     */
    protected void updateMomentum() {
        if (!momentumActive) return;
        
        this.x += (int) (momentumVX * 0.05f);
        applyRubberBand();
        updatePositions();
        savePosition();
        
        // 摩擦力衰减
        momentumVX *= 0.85f;
        momentumVY *= 0.85f;
        if (Math.abs(momentumVX) < 1f) {
            momentumActive = false;
            momentumVX = 0;
        }
    }

    /**
     * 橡皮筋边界 — Apple Design §9: Rubber-banding
     * 代替硬边界截断，带渐进阻力
     */
    protected void applyRubberBand() {
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        float margin = 20f;
        
        // 左边界
        if (x < -margin) {
            float overshoot = -(x + margin);
            x = (int) (-margin - rubberband(overshoot, sw, 0.55f));
        }
        // 上边界
        if (y < -margin) {
            float overshoot = -(y + margin);
            y = (int) (-margin - rubberband(overshoot, sh, 0.55f));
        }
        // 右边界
        if (x + width > sw + margin) {
            float overshoot = (x + width) - (sw + margin);
            x = (int) (sw + margin - width + rubberband(overshoot, sw, 0.55f));
        }
        // 下边界
        if (y + (int) currentHeight > sh + margin) {
            float overshoot = (y + (int) currentHeight) - (sh + margin);
            y = (int) (sh + margin - (int) currentHeight + rubberband(overshoot, sh, 0.55f));
        }
        
        // 当释放后且不在拖拽中，弹回可见区域
        if (!dragging && !momentumActive) {
            snapBackToBounds(sw, sh);
        }
    }

    /**
     * Apple 精确橡皮筋函数（WWDC 2018）
     */
    private static float rubberband(float overshoot, float dimension, float constant) {
        return (overshoot * dimension * constant) / (dimension + constant * Math.abs(overshoot));
    }

    /**
     * 弹回可见区域
     */
    private void snapBackToBounds(int sw, int sh) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > sw) x = sw - width;
        if (y + (int) currentHeight > sh) y = sh - (int) currentHeight;
    }

    // ═══════════════════════════════════════════════
    //  键盘事件
    // ═══════════════════════════════════════════════

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!collapsed) {
            for (GuiComponent component : components) {
                if (component.keyPressed(keyCode, scanCode, modifiers)) return true;
            }
        }
        return false;
    }

    // ═══════════════════════════════════════════════
    //  布局
    // ═══════════════════════════════════════════════

    protected void updatePositions() {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        int yOffset = 25;
        for (int i = 0; i < components.size(); i++) {
            components.get(i).updatePosition(this.x, this.y, yOffset);
            yOffset += config.componentHeight + config.componentSpacing;
        }
    }

    /** 计算展开时的完整高度 */
    protected float targetHeight() {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        int total = 25; // 标题栏 + 第一个组件间距
        for (GuiComponent c : components) {
            if (c.isVisible()) {
                total += config.componentHeight + config.componentSpacing;
            }
        }
        return Math.max(total, 30);
    }
    
    /** 获取当前渲染高度 */
    public int getCurrentHeight() {
        return (int) currentHeight;
    }
    
    /** 组件是否可见（用于碰撞检测） */
    public boolean isVisible() {
        return entryProgress > 0.1f;
    }
    
    /** 子类实现：保存位置到配置 */
    protected abstract void savePosition();
}