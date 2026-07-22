package fku.org.example.fku.mixin;

import fku.org.example.fku.features.worldedit.CommandRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(value={ChatScreen.class})
public abstract class MixinChatScreen {
    @Inject(method={"handleChatInput"}, at={@At(value="HEAD")}, cancellable=true)
    private void fku$onHandleChatInput(String text, boolean addToHistory, CallbackInfoReturnable<Object> cir) {
        if (text != null && text.startsWith("//")) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal((String)("\u00a77[WorldEdit] \u00a7f" + text)), false);
            }
            CommandRegistry.getInstance().execute(text);
            cir.cancel();
        }
    }
}

