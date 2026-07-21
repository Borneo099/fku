package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public abstract class GuiPanel {
    protected String title;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean dragging = false;
    protected int dragOffsetX;
    protected int dragOffsetY;
    protected boolean expanded = true;
    protected final List<GuiComponent> components = new ArrayList<GuiComponent>();
    protected final Minecraft mc = Minecraft.getInstance();
    protected float currentHeight;
    protected float springVelocity = 0.0f;
    protected float springTarget;
    protected long lastFrameTime = 0L;
    protected float entryProgress = 0.0f;
    protected float entryVelocity = 0.0f;
    protected boolean entryStarted = false;
    protected int panelIndex = 0;
    protected long entryStartTime = 0L;
    protected final float[] dragPositions = new float[5];
    protected final long[] dragTimes = new long[5];
    protected int dragHistoryIndex = 0;
    protected boolean momentumActive = false;
    protected float momentumX = 0.0f;
    protected float momentumY = 0.0f;
    protected float momentumVX = 0.0f;
    protected float momentumVY = 0.0f;
    protected boolean collapsed = false;

    public GuiPanel(String title, int x, int y, int width, int height) {
        this.title = title;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.springTarget = 20.0f;
        this.currentHeight = 20.0f;
        this.lastFrameTime = System.currentTimeMillis();
        this.entryStartTime = System.currentTimeMillis();
        this.init();
    }

    public void setPanelIndex(int index) {
        this.panelIndex = index;
    }

    protected abstract void init();

    protected void addComponent(GuiComponent component) {
        this.components.add(component);
        this.updatePositions();
    }

    protected static float[] springDamp(float target, float current, float vel, float dt, float stiffness) {
        float damping = 2.0f * Math.sqrt(stiffness);
        float displacement = current - target;
        float springForce = -stiffness * displacement;
        float dampingForce = -damping * vel;
        float accel = springForce + dampingForce;
        float newVel = vel + accel * dt;
        float newPos = current + newVel * dt;
        if (Math.abs(displacement) < 0.5f && Math.abs(newVel) < 1.0f) {
            return new float[]{target, 0.0f};
        }
        return new float[]{newPos, newVel};
    }

    protected void updateSpringAnimation() {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        long now = System.currentTimeMillis();
        float dt = Math.min((now - this.lastFrameTime) / 1000.0f, 0.05f);
        this.lastFrameTime = now;
        if (!config.animationEnabled) {
            this.currentHeight = this.collapsed ? 20.0f : this.targetHeight();
            this.springVelocity = 0.0f;
            this.entryProgress = 1.0f;
            return;
        }
        long elapsedSinceOpen = now - this.entryStartTime;
        long panelDelay = this.panelIndex * 60L;
        if (elapsedSinceOpen >= panelDelay) {
            if (!this.entryStarted) {
                this.entryStarted = true;
                this.entryProgress = 0.0f;
                this.entryVelocity = 0.0f;
            }
            if (this.entryProgress < 1.0f) {
                float[] result = GuiPanel.springDamp(1.0f, this.entryProgress, this.entryVelocity, dt, 6.0f);
                this.entryProgress = result[0];
                this.entryVelocity = result[1];
            }
        }
        this.springTarget = this.collapsed ? 20.0f : this.targetHeight();
        float[] hResult = GuiPanel.springDamp(this.springTarget, this.currentHeight, this.springVelocity, dt, config.springStiffness);
        this.currentHeight = hResult[0];
        this.springVelocity = hResult[1];
    }

    protected float componentSpringDelay(int index) {
        float raw = this.entryProgress * 1.5f - index * 0.08f;
        return Math.max(0.0f, Math.min(1.0f, raw));
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        this.updateSpringAnimation();
        float entryScale = 0.85f + 0.15f * this.entryProgress;
        float entryAlpha = Math.min(1.0f, this.entryProgress * 2.0f);
        int renderHeight = this.currentHeight;
        if (renderHeight <= 0) {
            return;
        }
        if (config.shadowEnabled && renderHeight > 20 && this.entryProgress > 0.1f) {
            GuiRenderHelper.drawSoftShadow(guiGraphics, this.x, this.y, this.width, renderHeight, entryAlpha);
        }
        int titleBarWidth = (this.width * entryScale);
        int titleBarX = this.x + (this.width - titleBarWidth) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, titleBarX, this.y, titleBarWidth, 20, true, entryAlpha);
        int textAlpha = (255.0f * entryAlpha);
        int textColor = textAlpha << 24 | config.getTextColor() & 0xFFFFFF;
        guiGraphics.drawString(this.mc.font, this.title, titleBarX + 5, this.y + 6, textColor);
        String indicator = this.collapsed ? "+" : "\u2212";
        int indColor = this.collapsed ? 0x888888 : config.getTextColor();
        indColor = textAlpha << 24 | indColor & 0xFFFFFF;
        guiGraphics.drawString(this.mc.font, indicator, this.x + this.width - 12, this.y + 6, indColor);
        if (!this.collapsed && this.entryProgress > 0.3f) {
            int contentHeight = renderHeight - 20;
            if (contentHeight > 0) {
                float contentAlpha = Math.min(1.0f, (this.entryProgress - 0.3f) / 0.4f);
                GuiRenderHelper.drawPanelBackground(guiGraphics, this.x, this.y + 20, this.width, contentHeight, false, entryAlpha * contentAlpha);
            }
            for (int i = 0; i < this.components.size(); ++i) {
                float compAlpha;
                GuiComponent component = this.components.get(i);
                if (!component.isVisible() || (compAlpha = this.componentSpringDelay(i)) <= 0.01f) continue;
                component.renderWithAlpha(guiGraphics, mouseX, mouseY, partialTick, compAlpha);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + 20)) {
            if (button == 0) {
                this.dragging = true;
                this.dragOffsetX = mouseX - this.x;
                this.dragOffsetY = mouseY - this.y;
                this.dragHistoryIndex = 0;
                this.momentumActive = false;
                return true;
            }
            if (button == 1) {
                this.collapsed = !this.collapsed;
                return true;
            }
        }
        if (!this.collapsed) {
            for (GuiComponent component : this.components) {
                if (!component.mouseClicked(mouseX, mouseY, button)) continue;
                return true;
            }
        }
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.dragging) {
            this.dragPositions[this.dragHistoryIndex % 5] = mouseX;
            this.dragTimes[this.dragHistoryIndex % 5] = System.nanoTime();
            ++this.dragHistoryIndex;
            this.x = mouseX - this.dragOffsetX;
            this.y = mouseY - this.dragOffsetY;
            this.applyRubberBand();
            this.updatePositions();
            this.savePosition();
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging) {
            this.dragging = false;
            if (this.dragHistoryIndex >= 3) {
                float speed;
                float absSpeed;
                int idx = (this.dragHistoryIndex - 1) % 5;
                int prevIdx = (this.dragHistoryIndex - 3) % 5;
                float dx = this.dragPositions[idx] - this.dragPositions[prevIdx];
                long dt = this.dragTimes[idx] - this.dragTimes[prevIdx];
                if (dt > 0L && (absSpeed = Math.abs(speed = dx / dt * 1.0E9f)) > 20.0f) {
                    this.momentumActive = true;
                    this.momentumVX = speed * 0.6f;
                    this.momentumVY = 0.0f;
                }
            }
        }
    }

    protected void updateMomentum() {
        if (!this.momentumActive) {
            return;
        }
        this.x += (this.momentumVX * 0.05f);
        this.applyRubberBand();
        this.updatePositions();
        this.savePosition();
        this.momentumVX *= 0.85f;
        this.momentumVY *= 0.85f;
        if (Math.abs(this.momentumVX) < 1.0f) {
            this.momentumActive = false;
            this.momentumVX = 0.0f;
        }
    }

    protected void applyRubberBand() {
        float overshoot;
        int sw = this.mc.getWindow().m_85445_();
        int sh = this.mc.getWindow().m_85446_();
        float margin = 20.0f;
        if (this.x < -margin) {
            overshoot = -(this.x + margin);
            this.x = (-margin - GuiPanel.rubberband(overshoot, sw, 0.55f));
        }
        if (this.y < -margin) {
            overshoot = -(this.y + margin);
            this.y = (-margin - GuiPanel.rubberband(overshoot, sh, 0.55f));
        }
        if ((this.x + this.width) > sw + margin) {
            overshoot = (this.x + this.width) - (sw + margin);
            this.x = (sw + margin - this.width + GuiPanel.rubberband(overshoot, sw, 0.55f));
        }
        if ((this.y + this.currentHeight) > sh + margin) {
            overshoot = (this.y + this.currentHeight) - (sh + margin);
            this.y = (sh + margin - (this.currentHeight) + GuiPanel.rubberband(overshoot, sh, 0.55f));
        }
        if (!this.dragging && !this.momentumActive) {
            this.snapBackToBounds(sw, sh);
        }
    }

    private static float rubberband(float overshoot, float dimension, float constant) {
        return overshoot * dimension * constant / (dimension + constant * Math.abs(overshoot));
    }

    private void snapBackToBounds(int sw, int sh) {
        if (this.x < 0) {
            this.x = 0;
        }
        if (this.y < 0) {
            this.y = 0;
        }
        if (this.x + this.width > sw) {
            this.x = sw - this.width;
        }
        if (this.y + this.currentHeight > sh) {
            this.y = sh - this.currentHeight;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.collapsed) {
            for (GuiComponent component : this.components) {
                if (!component.keyPressed(keyCode, scanCode, modifiers)) continue;
                return true;
            }
        }
        return false;
    }

    protected void updatePositions() {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        int yOffset = 25;
        for (int i = 0; i < this.components.size(); ++i) {
            this.components.get(i).updatePosition(this.x, this.y, yOffset);
            yOffset += config.componentHeight + config.componentSpacing;
        }
    }

    protected float targetHeight() {
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        int total = 25;
        for (GuiComponent c : this.components) {
            if (!c.isVisible()) continue;
            total += config.componentHeight + config.componentSpacing;
        }
        return Math.max(total, 30);
    }

    public int getCurrentHeight() {
        return this.currentHeight;
    }

    public boolean isVisible() {
        return this.entryProgress > 0.1f;
    }

    protected abstract void savePosition();
}

