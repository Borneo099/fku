package fku.org.example.fku.features.entitycontrol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 实体控制配置界面
 *
 * 前提：需先骑上目标实体，开启后自由驱动坐骑移动/飞行。
 */
public class EntityControlConfigScreen extends Screen {
    private final Screen parent;
    private EditBox hSpeedField, vSpeedField, akDistField, akIntField;
    private Button enabledBtn, flightBtn, lockYawBtn, antiKickBtn;

    public EntityControlConfigScreen() {
        super(Component.literal("实体控制配置"));
        this.parent = Minecraft.getInstance().screen;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int w = 200;
        EntityControlConfig cfg = EntityControlConfig.getInstance();

        int y = 40;
        enabledBtn = Button.builder(Component.literal(btnText("启用", cfg.enabled)),
                b -> { cfg.setEnabled(!cfg.enabled); b.setMessage(Component.literal(btnText("启用", cfg.enabled))); })
                .bounds(cx - w / 2, y, w, 20).build();
        this.addRenderableWidget(enabledBtn);
        y += 26;

        flightBtn = Button.builder(Component.literal(btnText("飞行模式", cfg.flightMode)),
                b -> { cfg.setFlightMode(!cfg.flightMode); b.setMessage(Component.literal(btnText("飞行模式", cfg.flightMode))); })
                .bounds(cx - w / 2, y, w, 20).build();
        this.addRenderableWidget(flightBtn);
        y += 26;

        lockYawBtn = Button.builder(Component.literal(btnText("锁定坐骑朝向", cfg.lockYaw)),
                b -> { cfg.setLockYaw(!cfg.lockYaw); b.setMessage(Component.literal(btnText("锁定坐骑朝向", cfg.lockYaw))); })
                .bounds(cx - w / 2, y, w, 20).build();
        this.addRenderableWidget(lockYawBtn);
        y += 26;

        antiKickBtn = Button.builder(Component.literal(btnText("反踢出", cfg.antiKick)),
                b -> { cfg.setAntiKick(!cfg.antiKick); b.setMessage(Component.literal(btnText("反踢出", cfg.antiKick))); })
                .bounds(cx - w / 2, y, w, 20).build();
        this.addRenderableWidget(antiKickBtn);
        y += 30;

        hSpeedField = new EditBox(Minecraft.getInstance().font, cx - w / 2, y, w, 20, Component.literal(""));
        hSpeedField.setResponder(s -> parseDouble(s, cfg::setHorizontalSpeed, cfg.horizontalSpeed));
        this.addRenderableWidget(hSpeedField);
        hSpeedField.setValue(String.valueOf(cfg.horizontalSpeed));
        this.addRenderableWidget(Button.builder(Component.literal("水平速度 = " + cfg.horizontalSpeed),
                b -> {}).bounds(cx - w / 2 + w + 8, y, 90, 20).build());
        y += 26;

        vSpeedField = new EditBox(Minecraft.getInstance().font, cx - w / 2, y, w, 20, Component.literal(""));
        vSpeedField.setResponder(s -> parseDouble(s, cfg::setVerticalSpeed, cfg.verticalSpeed));
        this.addRenderableWidget(vSpeedField);
        vSpeedField.setValue(String.valueOf(cfg.verticalSpeed));
        this.addRenderableWidget(Button.builder(Component.literal("上升速度 = " + cfg.verticalSpeed),
                b -> {}).bounds(cx - w / 2 + w + 8, y, 90, 20).build());
        y += 26;

        akDistField = new EditBox(Minecraft.getInstance().font, cx - w / 2, y, w, 20, Component.literal(""));
        akDistField.setResponder(s -> parseDouble(s, cfg::setAntiKickDistance, cfg.antiKickDistance));
        this.addRenderableWidget(akDistField);
        akDistField.setValue(String.valueOf(cfg.antiKickDistance));
        this.addRenderableWidget(Button.builder(Component.literal("反踢距离 = " + cfg.antiKickDistance),
                b -> {}).bounds(cx - w / 2 + w + 8, y, 90, 20).build());
        y += 26;

        akIntField = new EditBox(Minecraft.getInstance().font, cx - w / 2, y, w, 20, Component.literal(""));
        akIntField.setResponder(s -> parseInt(s, cfg::setAntiKickInterval, cfg.antiKickInterval));
        this.addRenderableWidget(akIntField);
        akIntField.setValue(String.valueOf(cfg.antiKickInterval));
        this.addRenderableWidget(Button.builder(Component.literal("反踢间隔 = " + cfg.antiKickInterval),
                b -> {}).bounds(cx - w / 2 + w + 8, y, 90, 20).build());
        y += 32;

        this.addRenderableWidget(Button.builder(Component.literal("返回"),
                b -> Minecraft.getInstance().setScreen(parent)).bounds(cx - w / 2, y, w, 20).build());
    }

    private String btnText(String label, boolean on) { return label + ": " + (on ? "ON" : "OFF"); }

    private void parseDouble(String s, java.util.function.Consumer<Double> setter, double fallback) {
        try { setter.accept(Double.parseDouble(s)); }
        catch (NumberFormatException ignored) { setter.accept(fallback); }
    }

    private void parseInt(String s, java.util.function.Consumer<Integer> setter, int fallback) {
        try { setter.accept(Integer.parseInt(s)); }
        catch (NumberFormatException ignored) { setter.accept(fallback); }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        g.drawCenteredString(Minecraft.getInstance().font, "实体控制配置（需骑乘实体后生效）", this.width / 2, 16, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }
}
