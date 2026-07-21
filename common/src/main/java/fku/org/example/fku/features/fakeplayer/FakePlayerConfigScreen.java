package fku.org.example.fku.features.fakeplayer;

import fku.org.example.fku.features.fakeplayer.FakePlayerConfig;
import fku.org.example.fku.features.fakeplayer.FakePlayerFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FakePlayerConfigScreen
extends Screen {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;
    private FakePlayerConfig cfg = FakePlayerConfig.getInstance();
    private EditBox nameField;
    private Button simulateDamageBtn;
    private Button autoTotemBtn;
    private Button copyInvBtn;
    private Button showDamageBtn;
    private Button respawnBtn;
    private static final int ROW_SPAWN = 35;
    private static final int ROW_HEALTH = 55;
    private static final int ROW_NAME = 75;
    private static final int ROW_DAMAGE = 95;
    private static final int ROW_TOTEM = 115;
    private static final int ROW_COPYINV = 135;
    private static final int ROW_SHOWDAMAGE = 155;
    private static final int ROW_RESPAWN = 175;

    public FakePlayerConfigScreen() {
        super(Component.literal((String)"\u5047\u4eba\u914d\u7f6e"));
    }

    protected void init() {
        int cx = (this.width - 300) / 2;
        int cy = (this.height - 200) / 2;
        Minecraft mc = Minecraft.getInstance();
        this.addRenderableWidget(Button.builder(Component.literal((String)(this.cfg.enabled ? "\u00a7c\u79fb\u9664\u5047\u4eba" : "\u00a7a\u751f\u6210\u5047\u4eba")), btn -> {
            if (this.cfg.enabled) {
                FakePlayerFeature.remove();
                this.cfg.setEnabled(false);
                btn.setMessage(Component.literal((String)"\u00a7a\u751f\u6210\u5047\u4eba"));
            } else {
                FakePlayerFeature.spawn();
                this.cfg.setEnabled(true);
                btn.setMessage(Component.literal((String)"\u00a7c\u79fb\u9664\u5047\u4eba"));
            }
        }).bounds(cx + 10, cy + 35 - 25, 120, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u521d\u59cb\u8840\u91cf: " + this.cfg.health)), btn -> {
            int v = this.cfg.health;
            v = v >= 36 ? 1 : v + 2;
            this.cfg.setHealth(v);
            btn.setMessage(Component.literal((String)("\u521d\u59cb\u8840\u91cf: " + this.cfg.health)));
        }).bounds(cx + 160, cy + 35 - 25, 120, 20).build());
        this.nameField = new EditBox(mc.font, cx + 10, cy + 75 - 25, 200, 16, Component.literal((String)"\u5047\u4eba\u540d\u79f0"));
        this.nameField.m_94144_(this.cfg.name);
        this.nameField.m_94199_(16);
        this.nameField.m_94151_(s -> {
            if (!s.isEmpty()) {
                this.cfg.setName((String)s);
            }
        });
        this.m_7787_(this.nameField);
        this.simulateDamageBtn = (Button)this.addRenderableWidget(this.buildToggleButton(cx + 10, cy + 95 - 25, this.cfg.simulateDamage, "\u6a21\u62df\u4f24\u5bb3", btn -> {
            this.cfg.setSimulateDamage(!this.cfg.simulateDamage);
            btn.setMessage(Component.literal((String)((this.cfg.simulateDamage ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed") + "  \u6a21\u62df\u4f24\u5bb3")));
        }));
        this.autoTotemBtn = (Button)this.addRenderableWidget(this.buildToggleButton(cx + 160, cy + 95 - 25, this.cfg.autoTotem, "\u81ea\u52a8\u56fe\u817e", btn -> {
            this.cfg.setAutoTotem(!this.cfg.autoTotem);
            btn.setMessage(Component.literal((String)((this.cfg.autoTotem ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed") + "  \u81ea\u52a8\u56fe\u817e")));
        }));
        this.copyInvBtn = (Button)this.addRenderableWidget(this.buildToggleButton(cx + 10, cy + 135 - 25, this.cfg.copyInv, "\u590d\u5236\u80cc\u5305", btn -> {
            this.cfg.setCopyInv(!this.cfg.copyInv);
            btn.setMessage(Component.literal((String)((this.cfg.copyInv ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed") + "  \u590d\u5236\u80cc\u5305")));
        }));
        this.showDamageBtn = (Button)this.addRenderableWidget(this.buildToggleButton(cx + 160, cy + 135 - 25, this.cfg.showDamage, "\u663e\u793a\u4f24\u5bb3", btn -> {
            this.cfg.setShowDamage(!this.cfg.showDamage);
            btn.setMessage(Component.literal((String)((this.cfg.showDamage ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed") + "  \u663e\u793a\u4f24\u5bb3")));
        }));
        this.respawnBtn = (Button)this.addRenderableWidget(this.buildToggleButton(cx + 10, cy + 175 - 25, this.cfg.respawn, "\u81ea\u52a8\u91cd\u751f", btn -> {
            this.cfg.setRespawn(!this.cfg.respawn);
            btn.setMessage(Component.literal((String)((this.cfg.respawn ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed") + "  \u81ea\u52a8\u91cd\u751f")));
        }));
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u65e0\u654c\u65f6\u95f4: " + this.cfg.invulnerableTicks + " tick")), btn -> {
            int v = this.cfg.invulnerableTicks;
            int n = v = v >= 20 ? 0 : v + 2;
            if (v > 20) {
                v = 20;
            }
            this.cfg.setInvulnerableTicks(v);
            btn.setMessage(Component.literal((String)("\u65e0\u654c\u65f6\u95f4: " + this.cfg.invulnerableTicks + " tick")));
        }).bounds(cx + 160, cy + 175 - 25, 120, 20).build());
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.fillGradient(guiGraphics);
        int cx = (this.width - 300) / 2;
        int cy = (this.height - 200) / 2;
        guiGraphics.drawString(Minecraft.getInstance().font, "\u00a7l\u00a7n\u5047\u4eba\u914d\u7f6e", cx + 10, cy + 5, 0xFFFFFF);
        String status = FakePlayerFeature.hasFakePlayer() ? "\u00a7a\u25cf \u5047\u4eba\u751f\u5b58\u4e2d" : "\u00a7c\u25cf \u5047\u4eba\u672a\u751f\u6210";
        guiGraphics.drawString(Minecraft.getInstance().font, status, cx + 160, cy + 5, 0xFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, "\u5047\u4eba\u540d\u79f0:", cx + 10, cy + 75 - 40, 0x888888);
        guiGraphics.drawString(Minecraft.getInstance().font, "\u521d\u59cb\u8840\u91cf (2~36):", cx + 160, cy + 75 - 40, 0x888888);
        guiGraphics.drawString(Minecraft.getInstance().font, "\u65e0\u654c\u65f6\u95f4 (0~20 tick):", cx + 10, cy + 115 - 25, 0x888888);
        this.nameField.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private Button buildToggleButton(int x, int y, boolean enabled, String label, Button.OnPress onPress) {
        String display = (enabled ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed") + "  " + label;
        return Button.builder(Component.literal((String)display), (Button.OnPress)onPress).bounds(x, y, 120, 20).build();
    }
}

