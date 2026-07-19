package fku.org.example.fku.features.killaura; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class KillAuraScreen extends Screen {
    private final KillAuraConfig cfg = KillAuraConfig.getInstance();
    private EditBox rangeBox, delayBox, whitelistBox;
    private static final int W = 140;
    private int bx, by0;

    public KillAuraScreen() { super(Component.literal("杀戮光环")); }

    @Override
    protected void init() {
        bx = width / 2 - W / 2;
        by0 = height / 2 - 100;
        int y = by0, sp = 20;

        // 开关
        addRenderableWidget(Button.builder(Component.literal(cfg.enabled ? "§a■ 开启" : "§c□ 关闭"), b -> {
            cfg.setEnabled(!cfg.enabled); b.setMessage(Component.literal(cfg.enabled ? "§a■ 开启" : "§c□ 关闭"));
        }).bounds(bx, y, W, 18).build());

        // 范围
        y += sp + 4;
        rangeBox = new EditBox(font, bx + 46, y, 40, 16, Component.literal(""));
        rangeBox.setValue(String.valueOf(cfg.range));
        addWidget(rangeBox);

        // 延迟
        y += sp;
        delayBox = new EditBox(font, bx + 71, y, 30, 16, Component.literal(""));
        delayBox.setValue(String.valueOf(cfg.delay));
        addWidget(delayBox);

        // 目标模式
        y += sp;
        addRenderableWidget(Button.builder(Component.literal(cfg.targetMode == 0 ? "§b[最近]" : "§b[最低血]"), b -> {
            cfg.setTargetMode(cfg.targetMode == 0 ? 1 : 0);
            b.setMessage(Component.literal(cfg.targetMode == 0 ? "§b[最近]" : "§b[最低血]"));
        }).bounds(bx + 46, y, 80, 16).build());

        // 5个开关
        y += sp + 2;
        mkToggle(y, "自动切剑", cfg.autoSwitch, v -> cfg.setAutoSwitch(v)); y += sp - 2;
        mkToggle(y, "自动旋转", cfg.autoRotate, v -> cfg.setAutoRotate(v)); y += sp - 2;
        mkToggle(y, "仅玩家", cfg.playersOnly, v -> cfg.setPlayersOnly(v)); y += sp - 2;
        mkToggle(y, "满冷却攻击", cfg.attackCooldown, v -> cfg.setAttackCooldown(v)); y += sp - 2;
        mkToggle(y, "多目标攻击", cfg.multiTarget, v -> cfg.setMultiTarget(v)); y += sp;

        // ★ 白名单 (minecraft:zombie,minecraft:skeleton 逗号分隔)
        whitelistBox = new EditBox(font, bx, y, W, 16, Component.literal(""));
        whitelistBox.setMaxLength(10000);
        whitelistBox.setValue(String.join(",", cfg.whitelist));
        addWidget(whitelistBox);
        y += sp + 4;

        // 完成
        addRenderableWidget(Button.builder(Component.literal("§a完成"), b -> { save(); onClose(); })
                .bounds(bx + 30, y, 80, 18).build());
    }

    private void mkToggle(int y, String label, boolean cur, java.util.function.Consumer<Boolean> cb) {
        final boolean[] state = {cur};
        addRenderableWidget(Button.builder(Component.literal((state[0] ? "§a" : "§7") + label), b -> {
            state[0] = !state[0]; cb.accept(state[0]);
            b.setMessage(Component.literal((state[0] ? "§a" : "§7") + label));
        }).bounds(bx, y, W, 16).build());
    }

    private void save() {
        try { cfg.setRange(Double.parseDouble(rangeBox.getValue())); } catch (Exception ignored) {}
        try { cfg.setDelay(Integer.parseInt(delayBox.getValue())); } catch (Exception ignored) {}
        // ★ 白名单保存
        cfg.whitelist.clear();
        for (String s : whitelistBox.getValue().split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) cfg.whitelist.add(t);
        }
        cfg.save();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        GuiRenderHelper.drawRoundedRect(g, bx - 10, by0 - 8, W + 20, 280, 0xAA222222, 8);
        g.drawString(font, "§l杀戮光环", bx, by0, 0xFFFFFF);
        g.drawString(font, "范围:", bx, by0 + 24, 0xFFFFFF);
        g.drawString(font, "延迟(刻):", bx, by0 + 44, 0xFFFFFF);
        g.drawString(font, "目标:", bx, by0 + 64, 0xFFFFFF);
        g.drawString(font, "§7白名单(逗号分隔):", bx, by0 + 194, 0xFFFFFF);
        rangeBox.render(g, mx, my, pt);
        delayBox.render(g, mx, my, pt);
        whitelistBox.render(g, mx, my, pt);
        super.render(g, mx, my, pt);
    }

    @Override public boolean isPauseScreen() { return false; }
}
