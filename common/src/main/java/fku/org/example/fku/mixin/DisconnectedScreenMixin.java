package fku.org.example.fku.mixin;

import fku.org.example.fku.util.LinkInterruptState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 给「连接中断」界面注入「重新连接」按钮。
 *
 * 按钮固定贴在界面底部「返回主菜单」按钮下方（原按钮最低位于 height-30），点击后使用
 * 触发链接中断时缓存的 ServerData（LinkInterruptState.pendingServer，优先）或当前
 * ServerData（mc.getCurrentServer）重连。
 *
 * 由于 Screen 的 width/height/addRenderableWidget 等均为继承成员、在本 Mixin 环境下无法直接
 * @Shadow，故改用 Minecraft 窗口尺寸定位，并通过反射把按钮加入 renderables/children（保证可渲染、可点击）。
 */
@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin {

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void onInit(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        ServerData server = LinkInterruptState.pendingServer != null
                ? LinkInterruptState.pendingServer
                : mc.getCurrentServer();
        if (server == null) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        int btnW = 200, btnH = 20;
        int x = (w - btnW) / 2;
        // 原「返回主菜单」按钮最低位于 h-30（高度20），重连按钮放在其下方 5px 处，任意文案长度都不重叠
        int reconnectY = h - 55;

        Button reconnect = Button.builder(Component.literal("§a重新连接"), b -> {
            ServerData s = LinkInterruptState.pendingServer;
            ServerAddress a = LinkInterruptState.pendingAddress;
            LinkInterruptState.pendingServer = null;
            LinkInterruptState.pendingAddress = null;
            // 优先用触发时缓存的「真实远端地址」，解析不到再退回 server.ip
            ServerAddress target = a;
            if (target == null && s != null && s.ip != null && !s.ip.isEmpty()) {
                try {
                    target = ServerAddress.parseString(s.ip);
                } catch (Exception ignored) {
                    target = null;
                }
            }
            if (target != null && s != null) {
                net.minecraft.client.gui.screens.ConnectScreen.startConnecting(
                        (Screen) null, mc, target, s, false);
            } else {
                // 兜底：回服务器列表
                mc.setScreen(new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(
                        new net.minecraft.client.gui.screens.TitleScreen()));
            }
        }).bounds(x, reconnectY, btnW, btnH).build();

        DisconnectedScreen screen = (DisconnectedScreen) (Object) this;
        addWidget(screen, reconnect);
    }

    private static void addWidget(Screen screen, Button button) {
        try {
            java.lang.reflect.Field renderables = Screen.class.getDeclaredField("renderables");
            renderables.setAccessible(true);
            ((List<Object>) renderables.get(screen)).add(button);
            java.lang.reflect.Field children = Screen.class.getDeclaredField("children");
            children.setAccessible(true);
            ((List<Object>) children.get(screen)).add(button);
        } catch (Exception ignored) {
            // 反射失败时不添加按钮，不影响原有断开界面
        }
    }
}
