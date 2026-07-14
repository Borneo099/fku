package fku.org.example.fku.features.antilag; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AntiLag（防拉回）核心逻辑类
 *
 * ★ 职责：
 *   1. 由 MixinClientPacketListener 调用 onPlayerPositionPacket() 拦截服务端拉回
 *   2. 在 ClientTickEvent 中执行 VClip 自动脱困 + 速率限制重置
 *   3. 管理假位置包的路径拆分发送
 *
 * ★ 设计思想（抓主要矛盾——服务端拉回拦截）：
 *   - Mixin 注入 handlePlayerPosition HEAD + cancel → 在位置被应用前拦截
 *   - 路径拆分（1.16模式）：从服务端位置到玩家位置的插值路径，骗过反作弊
 *   - 限制每秒发包数，防止被踢
 *
 * ★ 参考：
 *   AntiLag.java (Meteor Client) 的完整包拦截 + 假包发送逻辑移植
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AntiLagFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;

    /** 每秒包计数（线程安全） */
    private static final AtomicInteger movePacketCounter = new AtomicInteger(0);

    /**
     * 初始化
     */
    public static void init() {
        if (initialized) return;
        initialized = true;
        AntiLagConfig.getInstance(); // 加载配置
        Fku.LOGGER.info("[AntiLag] 功能已初始化");
    }

    // ════════════════════════════════════════════════════════
    // ★ Mixin 回调：由 MixinClientPacketListener.handlePlayerPosition HEAD 注入
    // ════════════════════════════════════════════════════════

    /**
     * 服务端位置同步包拦截处理
     * 在 ClientPacketListener.handlePlayerPosition() 的 Mixin HEAD 注入中调用
     *
     * @param packet  服务端发来的 PlayerPositionLook 包
     * @param ci      Mixin 回调控制，ci.cancel() 阻止位置被应用
     */
    public static void onPlayerPositionPacket(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        AntiLagConfig cfg = AntiLagConfig.getInstance();
        if (!cfg.enabled) return;

        LocalPlayer player = mc.player;
        if (player == null || player.connection == null) return;

        // ★ 服务端目标位置 vs 玩家当前位置
        Vec3 serverPos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
        Vec3 playerPos = player.position();
        double dist = playerPos.distanceTo(serverPos);

        // 超出范围 → 放行
        if (dist > cfg.range) return;

        // ★ 取消原位置更新（防止玩家被拉回）
        ci.cancel();

        // ★ 发送确认包（告知服务端"我已收到同步"）
        player.connection.send(new ServerboundAcceptTeleportationPacket(packet.getId()));

        // ★ 非反拉回模式：发送假位置包欺骗服务端
        if (!cfg.back) {
            // 虚空保护
            if (!cfg.allowIntoVoid && serverPos.y < player.level().getMinBuildHeight()) return;

            // 检查速率限制
            if (movePacketCounter.get() > cfg.limitPerSecond) {
                if (cfg.printWhenTooManyPacket) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§7[AntiLag] 达到限速上限，跳过假包发送"),
                            true);
                }
                return;
            }

            if ("MC1_16".equals(cfg.serverVersionMode)) {
                // ★ 路径拆分模式：从服务端位置到玩家位置逐步发送小步进包
                int steps = Math.max(1, (int) Math.ceil(dist / cfg.moveDistance));
                for (int i = 1; i <= steps; i++) {
                    double t = (double) i / steps;
                    double nx = serverPos.x + (playerPos.x - serverPos.x) * t;
                    double ny = serverPos.y + (playerPos.y - serverPos.y) * t;
                    double nz = serverPos.z + (playerPos.z - serverPos.z) * t;
                    sendMovePacket(nx, ny, nz, player.onGround());
                }
            } else {
                // ★ 直接模式：发送当前位置一个包
                sendMovePacket(playerPos.x, playerPos.y, playerPos.z, player.onGround());
            }
        }

        // ★ 此时由于 ci.cancel()，玩家位置仍为原始 clientPos，无需 setPos
        Fku.LOGGER.debug("[AntiLag] 拦截拉回: dist={}, server={}, client={}", dist, serverPos, playerPos);
    }

    // ════════════════════════════════════════════════════════
    // ★ Tick 事件：VClip 自动脱困 + 速率限制重置
    // ════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        AntiLagConfig cfg = AntiLagConfig.getInstance();
        if (!cfg.enabled) return;
        if (mc.player == null || mc.level == null) return;

        LocalPlayer player = mc.player;

        // ★ 每秒重置包计数器
        long now = System.currentTimeMillis();
        if (now - cfg.lastResetTime >= 1000) {
            movePacketCounter.set(0);
            cfg.lastResetTime = now;
            cfg.rateLimited = false;
        }

        // ★ VClip 自动脱困：水平碰撞 + 正在移动 + 非反拉回模式
        if (!cfg.back) {
            boolean isMoving = player.zza != 0 || player.xxa != 0;
            if (isMoving && player.horizontalCollision) {
                double dy = 0;
                if ("OnlyUp".equals(cfg.searchVclipMode) || "Both".equals(cfg.searchVclipMode)) {
                    dy = cfg.searchFindStep;
                } else if ("Down".equals(cfg.searchVclipMode) || "Both".equals(cfg.searchVclipMode)) {
                    dy = -cfg.searchFindStep;
                }
                if (dy != 0) {
                    player.setPos(player.getX(), player.getY() + dy, player.getZ());
                    Fku.LOGGER.debug("[AntiLag] VClip 自动脱困: dy={}", dy);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // ★ 工具方法
    // ════════════════════════════════════════════════════════

    /**
     * 发送假位置包（带速率计数）
     */
    private static void sendMovePacket(double x, double y, double z, boolean onGround) {
        LocalPlayer player = mc.player;
        if (player == null || player.connection == null) return;

        // 速率限制检查
        if (movePacketCounter.incrementAndGet() > AntiLagConfig.getInstance().limitPerSecond) {
            AntiLagConfig.getInstance().rateLimited = true;
            return;
        }

        player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                x, y, z,
                player.getYRot(),
                player.getXRot(),
                onGround));
    }

    /** 获取当前秒发包数（用于GUI显示） */
    public static int getCurrentPacketCount() {
        return movePacketCounter.get();
    }
}