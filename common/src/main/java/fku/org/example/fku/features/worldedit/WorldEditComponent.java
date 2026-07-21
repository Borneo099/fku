package fku.org.example.fku.features.worldedit;

import fku.org.example.fku.client.gui.components.ConfigButtonComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class WorldEditComponent
extends ConfigButtonComponent {
    @Override
    protected String getFeatureName() {
        return "\u521b\u4e16\u795e";
    }

    public WorldEditComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u521b\u4e16\u795e", () -> WorldEditComponent.showHelp());
    }

    private static void showHelp() {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a76=== WorldEdit Lite \u5feb\u901f\u5e2e\u52a9 ==="), false);
        Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7e//wand \u00a77- \u83b7\u53d6\u9009\u533a\u5de5\u5177"), false);
        Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7e//set \u00a77/ //replace \u00a77- \u586b\u5145/\u66ff\u6362"), false);
        Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7e//sphere \u00a77/ //cyl \u00a77/ //pyramid \u00a77- \u5f62\u72b6"), false);
        Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7e//copy \u00a77/ //paste \u00a77- \u590d\u5236\u7c98\u8d34"), false);
        Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7e//undo \u00a77/ //redo \u00a77- \u64a4\u9500\u91cd\u505a"), false);
        Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a7e\u8f93\u5165 \u00a77//help \u00a7e\u67e5\u770b\u5168\u90e8\u547d\u4ee4"), false);
    }
}

