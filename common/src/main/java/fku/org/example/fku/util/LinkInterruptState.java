package fku.org.example.fku.util;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

/**
 * 链接中断功能的状态持有（普通工具类，非 Mixin，可被任意模块安全引用）。
 *
 * - active：触发「链接中断」时为 true，使 ConnectionMixin 的禁连超时（exceptionCaught 取消）临时失效，
 *   保证本次主动断开能真正落到连接上，不被禁连超时吞掉。断开处理完后由主线程复位。
 * - pendingServer：触发时缓存的 ServerData，供「重新连接」按钮使用（即便退出后 getCurrentServer 被清空也不影响）。
 * - pendingAddress：触发时从当前连接真实远端地址解析出的 ServerAddress（host:port），作为重连目标。
 *   用真实远端地址而非 server.ip，可避免 server.ip 缺端口 / 格式异常 / LAN 随机端口等情况导致重连 Connection refused。
 */
public final class LinkInterruptState {

    public static volatile boolean active = false;
    public static volatile ServerData pendingServer = null;
    public static volatile ServerAddress pendingAddress = null;

    private LinkInterruptState() {}
}
