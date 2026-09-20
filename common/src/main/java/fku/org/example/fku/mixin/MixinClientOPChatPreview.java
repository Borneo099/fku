package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.clientop.ClientOPFeature;
import fku.org.example.fku.features.clientop.ClientOPConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinClientOPChatPreview — 在聊天输入框上方/悬停/动作栏绘制“指令执行预览”
 *
 * 仅本地渲染文本，不拦截输入、不修改发包。预览明确标注“本地模拟, 非真实权限”，
 * 避免用户误以为已获得服务端权限。
 */
@OnlyIn(Dist.CLIENT)
@Mixin(ChatScreen.class)
public abstract class MixinClientOPChatPreview {

    @Inject(method = "render", at = @At("TAIL"))
    private void fku$drawPreview(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!ClientOPFeature.isEnabled()) return;
        ClientOPConfig cfg = ClientOPConfig.getInstance();
        if (!cfg.enablePreview) return;

        String cmd = ClientOPFeature.currentInput;
        if (cmd == null || cmd.isEmpty()) return;
        String preview = ClientOPFeature.buildPreview(cmd);
        if (preview == null || preview.isEmpty()) return;

        try {
            int color = cfg.highlightOpCommands ? (0xFF000000 | cfg.getHighlightColor()) : 0xFFFFFFFF;
            String text = "§7预览: " + preview;
            int textW = Minecraft.getInstance().font.width(text);

            switch (cfg.previewPosition) {
                case "TOOLTIP" -> {
                    int x = mouseX + 8;
                    int y = mouseY + 8;
                    drawBg(guiGraphics, x, y, textW);
                    guiGraphics.drawString(Minecraft.getInstance().font, text, x + 3, y + 2, color);
                }
                case "ACTION_BAR" -> {
                    int x = (guiGraphics.guiWidth() - textW) / 2;
                    int y = 12;
                    drawBg(guiGraphics, x, y, textW);
                    guiGraphics.drawString(Minecraft.getInstance().font, text, x + 3, y + 2, color);
                }
                default -> { // CHAT_ABOVE：绘制在聊天输入框上方（底部区域），抬高一行避免与原版错误提示重叠
                    int x = 4;
                    int y = guiGraphics.guiHeight() - 40;
                    drawBg(guiGraphics, x, y, textW);
                    guiGraphics.drawString(Minecraft.getInstance().font, text, x + 3, y + 2, color);
                }
            }
        } catch (Exception ignored) {}
    }

    private static void drawBg(GuiGraphics g, int x, int y, int textW) {
        g.fill(x, y, x + textW + 6, y + 12, 0xCC000000);
    }
}
