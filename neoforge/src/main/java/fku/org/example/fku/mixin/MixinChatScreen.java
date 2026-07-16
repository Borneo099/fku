package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.worldedit.CommandRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinChatScreen — 在 ChatScreen 层拦截 // 开头的 WorldEdit 命令
 *
 * 为什么要在这层拦截：
 *   Minecraft 将 //set 等以 / 开头的消息路由到 sendCommand() 而非 sendChat()，
 *   ClientChatEvent 不触发；同时 Forge 的 ClientCommandHandler 会尝试解析
 *   // 开头的命令，失败后仍然发到服务器。在 ChatScreen.handleChatInput 拦截
 *   是最早的可控时机，确保 // 命令被准确处理而不发送到服务器。
 */
@OnlyIn(Dist.CLIENT)
@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    /**
     * 在 handleChatInput HEAD 注入
     * 如果消息以 // 开头，交给 CommandRegistry 处理并阻止原方法
     */
    @Inject(
            method = "handleChatInput",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fku$onHandleChatInput(String text, boolean addToHistory, CallbackInfo ci) {
        if (text != null && text.startsWith("//")) {
            Minecraft mc = Minecraft.getInstance();

            // 在聊天栏回显命令
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        Component.literal("§7[WorldEdit] §f" + text), false);
            }

            // 执行命令
            CommandRegistry.getInstance().execute(text);

            // 阻止原方法执行（不发送到服务器，不关闭聊天栏）
            // 注意：1.21.8 的 ChatScreen.handleChatInput(String, boolean) 为 void 方法，
            // 因此回调类型必须用 CallbackInfo（不能用 CallbackInfoReturnable）。
            ci.cancel();
        }
    }
}
