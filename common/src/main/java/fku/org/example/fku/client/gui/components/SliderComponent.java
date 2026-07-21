package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SliderComponent {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int minValue;
    protected int maxValue;
    protected int currentValue;
    protected String label;
    protected boolean dragging = false;
    protected OnValueChangedListener listener;
    protected float handlePos;
    protected float handleVel = 0.0f;
    protected float handleTarget = 0.0f;

    public SliderComponent(int x, int y, int width, int height, int minValue, int maxValue, int currentValue, String label, OnValueChangedListener listener) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = currentValue;
        this.label = label;
        this.listener = listener;
        this.handlePos = this.handleTarget = (currentValue - minValue) / (maxValue - minValue);
    }

    protected void updateHandle() {
        this.handleTarget = (this.currentValue - this.minValue) / (this.maxValue - this.minValue);
        float dt = Math.min(0.05f, 0.05f);
        float stiffness = 12.0f;
        float damping = 2.0f * Math.sqrt(stiffness);
        float disp = this.handlePos - this.handleTarget;
        float force = -stiffness * disp;
        float dampForce = -damping * this.handleVel;
        float accel = force + dampForce;
        this.handleVel += accel * dt;
        this.handlePos += this.handleVel * dt;
        if (Math.abs(disp) < 0.005f && Math.abs(this.handleVel) < 0.01f) {
            this.handlePos = this.handleTarget;
            this.handleVel = 0.0f;
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int handleX;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        this.updateHandle();
        guiGraphics.drawString(Minecraft.getInstance().font, this.label + ": " + this.currentValue, this.x, this.y - 12, config.getTextColor());
        GuiRenderHelper.drawRoundedRect(guiGraphics, this.x, this.y, this.width, 4, config.getBackgroundColorWithAlpha(200), 2);
        int fillWidth = (this.width * this.handlePos);
        if (fillWidth > 0) {
            GuiRenderHelper.drawRoundedRect(guiGraphics, this.x, this.y, fillWidth, 4, config.getPrimaryColorWithAlpha(200), 2);
        }
        if ((handleX = this.x + fillWidth - 3) < this.x) {
            handleX = this.x;
        }
        int handleY = this.y - 3;
        int handleSize = 10;
        GuiRenderHelper.drawRoundedRect(guiGraphics, handleX, handleY, handleSize, handleSize, -1, handleSize / 2);
        GuiRenderHelper.drawRoundedOutline(guiGraphics, handleX - 1, handleY - 1, handleSize + 2, handleSize + 2, config.getPrimaryColorWithAlpha(100), handleSize / 2 + 1, 1);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height)) {
            this.dragging = true;
            this.updateValue(mouseX);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.dragging && button == 0) {
            this.updateValue(mouseX);
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging) {
            this.dragging = false;
        }
    }

    private void updateValue(double mouseX) {
        float percentage = ((mouseX - this.x) / this.width);
        percentage = Math.max(0.0f, Math.min(1.0f, percentage));
        this.currentValue = this.minValue + (percentage * (this.maxValue - this.minValue));
        if (this.listener != null) {
            this.listener.onValueChanged(this.currentValue);
        }
    }

    public void setValue(int value) {
        this.currentValue = Math.max(this.minValue, Math.min(this.maxValue, value));
    }

    public int getValue() {
        return this.currentValue;
    }

    public static interface OnValueChangedListener {
        public void onValueChanged(int var1);
    }
}

