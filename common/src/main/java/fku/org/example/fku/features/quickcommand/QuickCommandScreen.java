package fku.org.example.fku.features.quickcommand;

import com.mojang.blaze3d.platform.InputConstants;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.quickcommand.QuickCommandConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class QuickCommandScreen
extends Screen {
    private static final int W = 410;
    private static final int ROW_H = 22;
    private static final int BASE_H = 90;
    private final QuickCommandConfig cfg;
    private final List<Row> rows = new ArrayList<Row>();
    private int scrollY = 0;
    private int listeningRow = -1;

    public QuickCommandScreen() {
        super(Component.literal((String)"\u5feb\u6377\u6307\u4ee4\u914d\u7f6e"));
        this.cfg = QuickCommandConfig.getInstance();
    }

    protected void init() {
        this.listeningRow = -1;
        this.resize();
    }

    private void resize() {
        this.rows.clear();
        this.clearWidgets();
        int cx = (this.width - 410) / 2;
        int y = (this.height - this.totalH()) / 2;
        int ly = y + 44;
        for (int i = 0; i < this.cfg.commands.size(); ++i) {
            QuickCommandConfig.CommandEntry cmd = this.cfg.commands.get(i);
            Row r = new Row();
            r.input = new EditBox(this.font, cx + 10, ly, 140, 16, Component.literal((String)""));
            r.input.m_94199_(Short.MAX_VALUE);
            r.input.m_94144_(cmd.enabled ? cmd.command : "\u00a77(\u5df2\u7981\u7528)");
            r.input.m_94186_(cmd.enabled);
            this.m_7787_(r.input);
            int fi = i;
            r.toggle = Button.builder(Component.literal((String)(cmd.enabled ? "\u00a7a\u2713" : "\u00a7c\u00d7")), b -> {
                cmd.enabled = !cmd.enabled;
                r.toggle.setMessage(Component.literal((String)(cmd.enabled ? "\u00a7a\u2713" : "\u00a7c\u00d7")));
                r.input.m_94186_(cmd.enabled);
                if (cmd.enabled) {
                    r.input.m_94144_(cmd.command);
                } else {
                    cmd.command = r.input.m_94155_();
                    r.input.m_94144_("\u00a77(\u5df2\u7981\u7528)");
                }
                QuickCommandScreen quickCommandScreen = this;
                quickCommandScreen.cfg.save();
            }).bounds(cx + 156, ly, 22, 16).build();
            this.addRenderableWidget(r.toggle);
            String hkText = QuickCommandScreen.hotkeyDisplay(cmd.hotkeyKey, cmd.hotkeyModifiers);
            r.bindBtn = Button.builder(Component.literal((String)("\u00a7e" + hkText)), b -> {
                this.listeningRow = fi;
                r.bindBtn.setMessage(Component.literal((String)"\u00a7a\u6309\u4e0b\u6309\u952e."));
            }).bounds(cx + 182, ly, 100, 16).build();
            this.addRenderableWidget(r.bindBtn);
            r.up = Button.builder(Component.literal((String)"\u25b2"), b -> this.move(fi, -1)).bounds(cx + 286, ly, 16, 16).build();
            this.addRenderableWidget(r.up);
            r.down = Button.builder(Component.literal((String)"\u25bc"), b -> this.move(fi, 1)).bounds(cx + 303, ly, 16, 16).build();
            this.addRenderableWidget(r.down);
            this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7c\u00d7"), b -> {
                this.cfg.commands.remove(fi);
                QuickCommandScreen quickCommandScreen = this;
                quickCommandScreen.cfg.save();
                this.resize();
            }).bounds(cx + 321, ly, 18, 16).build());
            this.rows.add(r);
            ly += 22;
        }
        int ly2 = ly;
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7a+ \u6dfb\u52a0"), b -> {
            this.cfg.commands.add(new QuickCommandConfig.CommandEntry());
            QuickCommandScreen quickCommandScreen = this;
            quickCommandScreen.cfg.save();
            this.resize();
        }).bounds(cx + 10, ly2, 70, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7c\u5220\u9664\u6700\u540e\u4e00\u884c"), b -> {
            if (!this.cfg.commands.isEmpty()) {
                this.cfg.commands.remove(this.cfg.commands.size() - 1);
                QuickCommandScreen quickCommandScreen = this;
                quickCommandScreen.cfg.save();
                this.resize();
            }
        }).bounds(cx + 85, ly2, 90, 18).build());
        int btnY = y + this.totalH() - 28;
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7a\u4fdd\u5b58\u5e76\u5173\u95ed"), b -> {
            this.saveAll();
            this.onClose();
        }).bounds(cx + 205 - 50, btnY, 100, 20).build());
    }

    private void saveAll() {
        for (int i = 0; i < this.cfg.commands.size() && i < this.rows.size(); ++i) {
            QuickCommandConfig.CommandEntry cmd = this.cfg.commands.get(i);
            String val = this.rows.get(i).input.m_94155_();
            if (!cmd.enabled || val.startsWith("\u00a77")) continue;
            cmd.command = val;
        }
        QuickCommandScreen quickCommandScreen = this;
        quickCommandScreen.cfg.save();
    }

    private void move(int idx, int dir) {
        int ni = idx + dir;
        if (ni < 0 || ni >= this.cfg.commands.size()) {
            return;
        }
        List<QuickCommandConfig.CommandEntry> list = this.cfg.commands;
        QuickCommandConfig.CommandEntry tmp = list.get(idx);
        list.set(idx, list.get(ni));
        list.set(ni, tmp);
        QuickCommandScreen quickCommandScreen = this;
        quickCommandScreen.cfg.save();
        this.resize();
    }

    private int totalH() {
        return 90 + this.cfg.commands.size() * 22;
    }

    private static String hotkeyDisplay(int key, int mods) {
        if (key < 0) {
            return "\u672a\u7ed1\u5b9a";
        }
        StringBuilder sb = new StringBuilder();
        if ((mods & 2) != 0) {
            sb.append("Ctrl+");
        }
        if ((mods & 1) != 0) {
            sb.append("Shift+");
        }
        if ((mods & 4) != 0) {
            sb.append("Alt+");
        }
        sb.append(InputConstants.m_84827_(key, 0).m_84875_().getString());
        return sb.toString();
    }

    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (this.listeningRow >= 0 && this.listeningRow < this.rows.size()) {
            QuickCommandConfig.CommandEntry cmd = this.cfg.commands.get(this.listeningRow);
            long win = Minecraft.getInstance().getWindow().m_85439_();
            boolean shift = GLFW.glfwGetKey(win, 340) == 1 || GLFW.glfwGetKey(win, 344) == 1;
            boolean ctrl = GLFW.glfwGetKey(win, 341) == 1 || GLFW.glfwGetKey(win, 345) == 1;
            boolean alt = GLFW.glfwGetKey(win, 342) == 1 || GLFW.glfwGetKey(win, 346) == 1;
            int mods = (shift ? 1 : 0) | (ctrl ? 2 : 0) | (alt ? 4 : 0);
            cmd.hotkeyKey = keyCode;
            cmd.hotkeyModifiers = mods;
            if (keyCode != 340 && keyCode != 344 && keyCode != 341 && keyCode != 345 && keyCode != 342 && keyCode != 346 && keyCode != 256) {
                QuickCommandScreen quickCommandScreen = this;
                quickCommandScreen.cfg.save();
                this.listeningRow = -1;
                this.resize();
                return true;
            }
            if (keyCode == 256) {
                this.listeningRow = -1;
                this.resize();
                return true;
            }
            return true;
        }
        for (Row r : this.rows) {
            if (!r.input.m_93696_() || !r.input.m_7933_(keyCode, scanCode, modifiers)) continue;
            return true;
        }
        if (keyCode == 256) {
            this.saveAll();
            this.onClose();
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        int cx = (this.width - 410) / 2;
        int y = (this.height - this.totalH()) / 2;
        int h = this.totalH();
        GuiRenderHelper.drawRoundedRect(g, cx - 2, y - 2, 414, h + 4, -1440603614, 8);
        GuiRenderHelper.drawRoundedOutline(g, cx - 2, y - 2, 414, h + 4, -11184811, 8, 1);
        g.drawString(this.font, "\u00a7l\u5feb\u6377\u6307\u4ee4\u914d\u7f6e \u2014 \u6bcf\u884c\u53ef\u7ed1\u5b9a\u7ec4\u5408\u952e\u70ed\u952e", cx + 10, y + 8, 0xFFFFFF);
        g.drawString(this.font, "\u00a77\u6307\u4ee4\u4ee5 / \u5f00\u5934  \u70b9\u51fb[\u7ed1\u5b9a\u70ed\u952e]\u540e\u6309\u7ec4\u5408\u952e  \u25b2\u25bc\u6392\u5e8f", cx + 10, y + 22, 0x888888);
        if (this.listeningRow >= 0) {
            g.drawString(this.font, "\u00a7a\u00a7l\u6309\u7ec4\u5408\u952e\u7ed1\u5b9a\u6b64\u884c. (ESC\u53d6\u6d88)", cx + 10, y + 36, 0xFFFF00);
        }
        for (Row r : this.rows) {
            r.input.render(g, mx, my, pt);
        }
        super.render(g, mx, my, pt);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class Row {
        EditBox input;
        Button toggle;
        Button bindBtn;
        Button up;
        Button down;

        private Row() {
        }
    }
}

