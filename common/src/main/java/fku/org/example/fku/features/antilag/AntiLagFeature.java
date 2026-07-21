package fku.org.example.fku.features.antilag;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.antilag.AntiLagConfig;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
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

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class AntiLagFeature {
    private static boolean initialized = false;
    private static final AtomicInteger movePacketCounter = new AtomicInteger(0);

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        AntiLagConfig.getInstance();
        Fku.LOGGER.info("[AntiLag] \u529f\u80fd\u5df2\u521d\u59cb\u5316");
    }

    public static void onPlayerPositionPacket(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        AntiLagConfig cfg = AntiLagConfig.getInstance();
        if (!cfg.enabled) {
            return;
        }
        Minecraft mc = AntiLagFeature.getMc();
        if (mc == null) {
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null || player.f_108617_ == null) {
            return;
        }
        Vec3 serverPos = new Vec3(packet.m_132818_(), packet.m_132821_(), packet.m_132822_());
        Vec3 playerPos = player.position();
        double dist = playerPos.m_82554_(serverPos);
        if (dist > cfg.range) {
            return;
        }
        ci.cancel();
        player.f_108617_.m_104955_((Packet)new ServerboundAcceptTeleportationPacket(packet.m_132825_()));
        if (!cfg.back) {
            if (!cfg.allowIntoVoid && serverPos.y < player.m_9236_().m_141937_()) {
                return;
            }
            if (movePacketCounter.get() > cfg.limitPerSecond) {
                if (cfg.printWhenTooManyPacket) {
                    player.m_5661_(Component.literal((String)"\u00a77[AntiLag] \u8fbe\u5230\u9650\u901f\u4e0a\u9650\uff0c\u8df3\u8fc7\u5047\u5305\u53d1\u9001"), true);
                }
                return;
            }
            if ("MC1_16".equals(cfg.serverVersionMode)) {
                int steps = Math.max(1, Math.ceil(dist / cfg.moveDistance));
                for (int i = 1; i <= steps; ++i) {
                    double t = i / steps;
                    double nx = serverPos.x + (playerPos.x - serverPos.x) * t;
                    double ny = serverPos.y + (playerPos.y - serverPos.y) * t;
                    double nz = serverPos.z + (playerPos.z - serverPos.z) * t;
                    AntiLagFeature.sendMovePacket(nx, ny, nz, player.m_20096_());
                }
            } else {
                AntiLagFeature.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, player.m_20096_());
            }
        }
        Fku.LOGGER.debug("[AntiLag] \u62e6\u622a\u62c9\u56de: dist={}, server={}, client={}", new Object[]{dist, serverPos, playerPos});
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        AntiLagConfig cfg = AntiLagConfig.getInstance();
        if (!cfg.enabled) {
            return;
        }
        Minecraft mc = AntiLagFeature.getMc();
        if (mc == null || mc.player == null || mc.f_91073_ == null) {
            return;
        }
        LocalPlayer player = mc.player;
        long now = System.currentTimeMillis();
        if (now - cfg.lastResetTime >= 1000L) {
            movePacketCounter.set(0);
            cfg.lastResetTime = now;
            cfg.rateLimited = false;
        }
        if (!cfg.back) {
            boolean isMoving;
            boolean bl = isMoving = player.f_20902_ != 0.0f || player.f_20900_ != 0.0f;
            if (isMoving && player.f_19862_) {
                double dy = 0.0;
                if ("OnlyUp".equals(cfg.searchVclipMode) || "Both".equals(cfg.searchVclipMode)) {
                    dy = cfg.searchFindStep;
                } else if ("Down".equals(cfg.searchVclipMode) || "Both".equals(cfg.searchVclipMode)) {
                    dy = -cfg.searchFindStep;
                }
                if (dy != 0.0) {
                    player.m_6034_(player.getX(), player.getY() + dy, player.getZ());
                    Fku.LOGGER.debug("[AntiLag] VClip \u81ea\u52a8\u8131\u56f0: dy={}", dy);
                }
            }
        }
    }

    private static void sendMovePacket(double x, double y, double z, boolean onGround) {
        Minecraft mc = AntiLagFeature.getMc();
        if (mc == null) {
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null || player.f_108617_ == null) {
            return;
        }
        if (movePacketCounter.incrementAndGet() > AntiLagConfig.getInstance().limitPerSecond) {
            AntiLagConfig.getInstance().rateLimited = true;
            return;
        }
        player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.PosRot(x, y, z, player.m_146908_(), player.m_146909_(), onGround));
    }

    public static int getCurrentPacketCount() {
        return movePacketCounter.get();
    }
}

