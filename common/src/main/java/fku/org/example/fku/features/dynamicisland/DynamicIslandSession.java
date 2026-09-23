package fku.org.example.fku.features.dynamicisland;

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 灵动岛「会话/在线状态」提示驱动，包含两类：
 *   1) 客户端自身进/退游戏（连接 / 断开世界）—— 受配置项「进/退游戏提示」控制
 *   2) 多人世界其他玩家进/退游戏 —— 受配置项「多人玩家进/退游戏」控制
 *
 * 第 2 类在你刚进入世界后有一个「抑制窗口」，窗口内不提示其他玩家进游戏，
 * 避免一次性刷出服务器上已在线玩家的进游戏提示；仅显示你进入世界之后发生的
 * 新进 / 退出。
 *
 * 关于「加载世界误报已退出」的修复：
 *   游戏载入/切换世界时，Forge 在真正 LoggingIn（进世界）之前会先触发一次
 *   LoggingOut（旧连接/菜单态清理）。若退游戏提示在 LoggingOut 时立即弹出，
 *   就会先显示「已退出世界」、地图加载完再显示「已加入游戏」。
 *   因此退游戏改为「延迟判定」：LoggingOut 仅挂起；若短时间内发生 LoggingIn
 *   （世界切换/重载）则视为过渡、取消退出提示；只有客户端真正落到「断开连接
 *   屏幕 / 标题屏」这类终态时才推送「已退出世界」，交给渲染循环每帧冲刷。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DynamicIslandSession {

    /** 退游戏后允许灵动岛在断连屏幕继续显示退出提示的窗口期 */
    private static boolean sessionEndActive = false;
    private static long sessionEndUntil = 0L;

    /** 客户端刚进入世界后的「抑制窗口」起点：窗口内不提示其他玩家进游戏（避免刚进服刷屏） */
    private static long joinSuppressUntil = 0L;

    private static final long SUPPRESS_MS = 4000L;
    /** 退游戏延迟判定的兜底超时：超过此时长既无登录、又未识别为终态时，仍按断开处理（极长，仅作状态自愈） */
    private static final long SAFETY_MS = 20000L;

    /** 待决的退游戏：LoggingOut 已发生、但尚未决定是否真正推送 */
    private static boolean pendingLogout = false;
    private static long pendingLogoutAt = 0L;

    /** 供 HUD 判断是否处于「退游戏提示窗口期」，以放宽渲染条件 */
    public static boolean isSessionEndActive() {
        long now = System.currentTimeMillis();
        if (sessionEndActive && now > sessionEndUntil) {
            sessionEndActive = false;
        }
        return sessionEndActive;
    }

    /**
     * 每帧由渲染循环调用：冲刷「待决退游戏」的最终判定。
     * 在屏幕（含断连/标题屏）与游戏内都会被驱动，确保即使处于断开界面也能及时弹出提示。
     */
    public static void tick() {
        long now = System.currentTimeMillis();

        // 退游戏窗口期收尾
        if (sessionEndActive && now > sessionEndUntil) {
            sessionEndActive = false;
        }

        // 冲刷待决的退游戏
        if (pendingLogout) {
            boolean terminal = isTerminalDisconnectScreen();
            boolean expired = now - pendingLogoutAt > SAFETY_MS;
            if (terminal || expired) {
                pendingLogout = false;
                flushLogout(now);
            }
            // 否则：仍处于加载/过渡态，继续等待（登录事件会取消 pendingLogout）
        }
    }

    /** 判断客户端是否已落到「终态」屏幕：断开连接屏幕或标题/主菜单（加载过渡屏不算） */
    private static boolean isTerminalDisconnectScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            return false; // 游戏内或加载过渡中，未到终态
        }
        return mc.screen instanceof net.minecraft.client.gui.screens.DisconnectedScreen
                || mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen;
    }

    private static void flushLogout(long now) {
        DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
        if (!cfg.enabled || !cfg.showSessionNotify) {
            return;
        }
        NotificationCenter.push("session_leave", "灵动岛", NotificationType.SESSION, "已退出世界 · 下次见");
        sessionEndActive = true;
        sessionEndUntil = now + (long) cfg.notificationDuration + 1500L;
        Fku.LOGGER.info("[DynamicIsland] 玩家退出世界，推送退游戏通知");
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
        if (!cfg.enabled) {
            return;
        }
        // 刚发生过 LoggingOut（世界重载/切换） → 取消那个待推送的「退出」提示，避免误报
        if (pendingLogout) {
            pendingLogout = false;
        }
        // 进入新世界，结束上一轮的退游戏窗口
        sessionEndActive = false;
        joinSuppressUntil = System.currentTimeMillis() + SUPPRESS_MS;
        if (cfg.showSessionNotify) {
            NotificationCenter.push("session_join", "灵动岛", NotificationType.SESSION, "已加入世界 · 欢迎回来");
            Fku.LOGGER.info("[DynamicIsland] 玩家加入世界，推送进游戏通知");
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
        if (!cfg.enabled || !cfg.showSessionNotify) {
            return;
        }
        // 不直接弹「已退出世界」：先挂起，待 tick() 判定。若短时间内发生 LoggingIn（世界切换/
        // 重载）则视为过渡、不提示退出；只有真正落到断连/标题屏才推送。
        pendingLogout = true;
        pendingLogoutAt = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
        if (!cfg.enabled || !cfg.showPlayerJoinLeave) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player p = event.getEntity();
        if (p == null) {
            return;
        }
        // 跳过本机玩家自身（避免显示「你 进入游戏」）
        if (mc.player != null && p.getUUID().equals(mc.player.getUUID())) {
            return;
        }
        // 刚进入世界后的抑制窗口内不提示（避免刷出服务器上已在线玩家）
        if (System.currentTimeMillis() < joinSuppressUntil) {
            return;
        }
        String name = p.getName().getString();
        NotificationCenter.pushPlayer(name, NotificationType.PLAYER_JOIN, "进入游戏", getSkin(p));
        Fku.LOGGER.info("[DynamicIsland] 玩家 {} 进入游戏", name);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
        if (!cfg.enabled || !cfg.showPlayerJoinLeave) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player p = event.getEntity();
        if (p == null) {
            return;
        }
        if (mc.player != null && p.getUUID().equals(mc.player.getUUID())) {
            return;
        }
        if (System.currentTimeMillis() < joinSuppressUntil) {
            return;
        }
        String name = p.getName().getString();
        NotificationCenter.pushPlayer(name, NotificationType.PLAYER_LEAVE, "退出游戏", getSkin(p));
        Fku.LOGGER.info("[DynamicIsland] 玩家 {} 退出游戏", name);
    }

    /** 取玩家皮肤贴图（用于渲染头像），失败返回 null 回退到箭头图标 */
    private static ResourceLocation getSkin(Player p) {
        try {
            if (p instanceof AbstractClientPlayer acp) {
                return acp.getSkinTextureLocation();
            }
        } catch (Throwable var2) {
        }
        return null;
    }
}
