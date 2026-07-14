package fku.org.example.fku.features.fakeplayer; /* water */

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 假人（FakePlayer）配置界面
 *
 * ★ 职责：
 *   提供假人功能的参数调节界面。
 *   - 假人名称修改（EditBox）
 *   - 初始血量滑块
 *   - 功能开关按钮（模拟伤害、自动图腾、背包复制、显示伤害、自动重生）
 *   - 无敌时间滑块
 *
 * ★ 参考来源：
 *   PearlPhaseConfigScreen / AntiLagScreen 的界面布局风格
 *   AdvancedFakePlayer + IMGFakePlayer 的配置项设计
 */
public class FakePlayerConfigScreen extends Screen {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;
    private FakePlayerConfig cfg;

    private EditBox nameField;
    private Button simulateDamageBtn;
    private Button autoTotemBtn;
    private Button copyInvBtn;
    private Button showDamageBtn;
    private Button respawnBtn;

    // 行偏移常量
    private static final int ROW_SPAWN = 35;
    private static final int ROW_HEALTH = 55;
    private static final int ROW_NAME = 75;
    private static final int ROW_DAMAGE = 95;
    private static final int ROW_TOTEM = 115;
    private static final int ROW_COPYINV = 135;
    private static final int ROW_SHOWDAMAGE = 155;
    private static final int ROW_RESPAWN = 175;

    public FakePlayerConfigScreen() {
        super(Component.literal("假人配置"));
        this.cfg = FakePlayerConfig.getInstance();
    }

    @Override
    protected void init() {
        int cx = (this.width - WIDTH) / 2;
        int cy = (this.height - HEIGHT) / 2;

        Minecraft mc = Minecraft.getInstance();

        // ★ 生成假人按钮
        this.addRenderableWidget(
            Button.builder(
                Component.literal(cfg.enabled ? "§c移除假人" : "§a生成假人"),
                btn -> {
                    if (cfg.enabled) {
                        FakePlayerFeature.remove();
                        cfg.setEnabled(false);
                        btn.setMessage(Component.literal("§a生成假人"));
                    } else {
                        FakePlayerFeature.spawn();
                        cfg.setEnabled(true);
                        btn.setMessage(Component.literal("§c移除假人"));
                    }
                }
            ).bounds(cx + 10, cy + ROW_SPAWN - 25, 120, 20).build()
        );

        // ★ 初始血量
        this.addRenderableWidget(
            Button.builder(
                Component.literal("初始血量: " + cfg.health),
                btn -> {
                    int v = cfg.health;
                    v = (v >= 36) ? 1 : v + 2;
                    cfg.setHealth(v);
                    btn.setMessage(Component.literal("初始血量: " + cfg.health));
                }
            ).bounds(cx + 160, cy + ROW_SPAWN - 25, 120, 20).build()
        );

        // ★ 假人名称输入框
        this.nameField = new EditBox(mc.font, cx + 10, cy + ROW_NAME - 25, 200, 16,
            Component.literal("假人名称"));
        this.nameField.setValue(cfg.name);
        this.nameField.setMaxLength(16);
        this.nameField.setResponder(s -> {
            if (!s.isEmpty()) cfg.setName(s);
        });
        this.addWidget(this.nameField);

        // ★ 模拟伤害开关
        this.simulateDamageBtn = this.addRenderableWidget(
            buildToggleButton(cx + 10, cy + ROW_DAMAGE - 25, cfg.simulateDamage, "模拟伤害", btn -> {
                cfg.setSimulateDamage(!cfg.simulateDamage);
                btn.setMessage(Component.literal(
                    (cfg.simulateDamage ? "§a✔ 开启" : "§c✘ 关闭") + "  模拟伤害"));
            })
        );

        // ★ 自动图腾开关
        this.autoTotemBtn = this.addRenderableWidget(
            buildToggleButton(cx + 160, cy + ROW_DAMAGE - 25, cfg.autoTotem, "自动图腾", btn -> {
                cfg.setAutoTotem(!cfg.autoTotem);
                btn.setMessage(Component.literal(
                    (cfg.autoTotem ? "§a✔ 开启" : "§c✘ 关闭") + "  自动图腾"));
            })
        );

        // ★ 复制背包
        this.copyInvBtn = this.addRenderableWidget(
            buildToggleButton(cx + 10, cy + ROW_COPYINV - 25, cfg.copyInv, "复制背包", btn -> {
                cfg.setCopyInv(!cfg.copyInv);
                btn.setMessage(Component.literal(
                    (cfg.copyInv ? "§a✔ 开启" : "§c✘ 关闭") + "  复制背包"));
            })
        );

        // ★ 显示伤害
        this.showDamageBtn = this.addRenderableWidget(
            buildToggleButton(cx + 160, cy + ROW_COPYINV - 25, cfg.showDamage, "显示伤害", btn -> {
                cfg.setShowDamage(!cfg.showDamage);
                btn.setMessage(Component.literal(
                    (cfg.showDamage ? "§a✔ 开启" : "§c✘ 关闭") + "  显示伤害"));
            })
        );

        // ★ 自动重生
        this.respawnBtn = this.addRenderableWidget(
            buildToggleButton(cx + 10, cy + ROW_RESPAWN - 25, cfg.respawn, "自动重生", btn -> {
                cfg.setRespawn(!cfg.respawn);
                btn.setMessage(Component.literal(
                    (cfg.respawn ? "§a✔ 开启" : "§c✘ 关闭") + "  自动重生"));
            })
        );

        // ★ 无敌时间
        this.addRenderableWidget(
            Button.builder(
                Component.literal("无敌时间: " + cfg.invulnerableTicks + " tick"),
                btn -> {
                    int v = cfg.invulnerableTicks;
                    v = (v >= 20) ? 0 : v + 2;
                    if (v > 20) v = 20;
                    cfg.setInvulnerableTicks(v);
                    btn.setMessage(Component.literal("无敌时间: " + cfg.invulnerableTicks + " tick"));
                }
            ).bounds(cx + 160, cy + ROW_RESPAWN - 25, 120, 20).build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int cx = (this.width - WIDTH) / 2;
        int cy = (this.height - HEIGHT) / 2;

        // ★ 标题
        guiGraphics.drawString(Minecraft.getInstance().font, "§l§n假人配置", cx + 10, cy + 5, 0xFFFFFF);

        // ★ 状态提示
        String status = FakePlayerFeature.hasFakePlayer()
            ? "§a● 假人生存中"
            : "§c● 假人未生成";
        guiGraphics.drawString(Minecraft.getInstance().font, status, cx + 160, cy + 5, 0xFFFFFF);

        // ★ 标签
        guiGraphics.drawString(Minecraft.getInstance().font, "假人名称:", cx + 10, cy + ROW_NAME - 40, 0x888888);
        guiGraphics.drawString(Minecraft.getInstance().font, "初始血量 (2~36):", cx + 160, cy + ROW_NAME - 40, 0x888888);
        guiGraphics.drawString(Minecraft.getInstance().font, "无敌时间 (0~20 tick):", cx + 10, cy + ROW_TOTEM - 25, 0x888888);

        // ★ 渲染输入框
        this.nameField.render(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * 构建开关按钮（带颜色状态）
     */
    private Button buildToggleButton(int x, int y, boolean enabled, String label, Button.OnPress onPress) {
        String display = (enabled ? "§a✔ 开启" : "§c✘ 关闭") + "  " + label;
        return Button.builder(Component.literal(display), onPress)
            .bounds(x, y, 120, 20).build();
    }
}