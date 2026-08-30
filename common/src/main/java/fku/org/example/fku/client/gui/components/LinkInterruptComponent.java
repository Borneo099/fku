package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.util.LinkInterruptState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;

/**
 * 链接中断组件（工具菜单）
 *
 * 左键点击 → 主动断开当前服务器连接，弹出「连接中断」界面（DisconnectedScreen）。
 * 该界面由 DisconnectedScreenMixin 注入「重新连接」按钮，可一键重连。
 *
 * ★ 绕过禁连超时：断开前将 LinkInterruptState.active 置 true，使 ConnectionMixin 的
 *   exceptionCaught 取消逻辑临时失效，保证本次主动断开不会被禁连超时吞掉；
 *   断开处理在下一个客户端 tick 复位（mc.tell）。
 */
public class LinkInterruptComponent extends GuiComponent {

    public LinkInterruptComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "链接中断");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        boolean enabled = Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().player != null;
        GuiRenderHelper.drawComponentBackground(guiGraphics, x, y, width, height, enabled);
        String displayStr = "链接中断";
        int textColor = enabled ? config.getTextColor() : 0xAAAAAA;
        guiGraphics.drawString(Minecraft.getInstance().font, displayStr, x + 5, y + (height - 8) / 2 - 4, textColor);
        guiGraphics.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                trigger();
                return true;
            } else if (button == 1) {
                // 右键：提示信息
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) mc.player.displayClientMessage(
                    Component.literal("§7[链接中断] 左键断开当前连接并弹出连接中断界面，可在界面点「重新连接」重连"), false);
                return true;
            }
        }
        return false;
    }

    private void trigger() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.player == null) {
            if (mc.player != null) mc.player.displayClientMessage(
                Component.literal("§c[链接中断] 当前未连接到服务器，无法中断"), false);
            return;
        }
        ServerData server = mc.getCurrentServer();

        // 用当前连接「真实远端地址」作为重连目标（最可靠）：
        // 直接读取已建立 socket 的 host:port，避免 server.ip 缺端口 / 格式异常 / LAN 随机端口
        // 等情况导致重连 Connection refused。解析失败时退回 server.ip。
        ServerAddress addr = null;
        try {
            java.net.SocketAddress remote = mc.getConnection().getConnection().getRemoteAddress();
            if (remote instanceof java.net.InetSocketAddress isa) {
                addr = ServerAddress.parseString(isa.getHostString() + ":" + isa.getPort());
            }
        } catch (Exception ignored) {
            // 读取远端地址失败，下面用 server.ip 兜底
        }
        if (addr == null && server != null && server.ip != null && !server.ip.isEmpty()) {
            try {
                addr = ServerAddress.parseString(server.ip);
            } catch (Exception ignored) {
                // 地址无法解析则不缓存
            }
        }

        LinkInterruptState.pendingServer = server;
        LinkInterruptState.pendingAddress = addr;
        LinkInterruptState.active = true; // 绕过禁连超时
        mc.getConnection().getConnection().disconnect(Component.literal("§c[FKU] 链接中断"));
        // 断开处理（通道关闭/异常）在下一个客户端 tick 内完成，复位标记，恢复禁连超时原有行为
        mc.tell(() -> LinkInterruptState.active = false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}
