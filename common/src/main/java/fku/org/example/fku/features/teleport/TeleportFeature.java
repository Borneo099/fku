package fku.org.example.fku.features.teleport;

import fku.org.example.fku.features.teleport.TeleportConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TeleportFeature {
    private static final Minecraft mc = Minecraft.getInstance();

    public static void init() {
        TeleportConfig.getInstance();
    }

    public static void teleportToCrosshair() {
        Vec3 look;
        TeleportConfig cfg = TeleportConfig.getInstance();
        if (!cfg.enabled) {
            TeleportFeature.sendMsg("\u00a7c\u77ac\u79fb\u529f\u80fd\u672a\u5f00\u542f");
            return;
        }
        LocalPlayer p = TeleportFeature.mc.player;
        if (p == null || TeleportFeature.mc.f_91073_ == null) {
            return;
        }
        double max = cfg.maxDistance;
        Vec3 from = p.m_20299_(1.0f);
        BlockHitResult hit = TeleportFeature.mc.f_91073_.m_45547_(new ClipContext(from, from.add(look = p.getLookAngle().scale(max)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p));
        if (hit.m_6662_() == HitResult.Type.BLOCK) {
            BlockPos bp = hit.m_82425_();
            Vec3 tp = new Vec3(bp.m_123341_() + 0.5, bp.m_123342_() + 1.0, bp.m_123343_() + 0.5);
            if (TeleportFeature.mc.f_91073_.m_45756_((Entity)p, p.m_20191_().m_82383_(tp.subtract(p.position())))) {
                p.m_6034_(tp.x, tp.y, tp.z);
                p.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.PosRot(tp.x, tp.y, tp.z, p.m_146908_(), p.m_146909_(), false));
                TeleportFeature.sendMsg("\u00a7a\u77ac\u79fb\u5230 " + bp.m_123341_() + " " + bp.m_123342_() + " " + bp.m_123343_());
            } else {
                TeleportFeature.sendMsg("\u00a7c\u76ee\u6807\u4f4d\u7f6e\u6709\u963b\u6321");
            }
        } else {
            TeleportFeature.sendMsg("\u00a7c\u6ca1\u6709\u7784\u51c6\u5230\u65b9\u5757");
        }
    }

    public static void teleportTo(double x, double y, double z, boolean snap) {
        TeleportConfig cfg = TeleportConfig.getInstance();
        if (!cfg.enabled) {
            TeleportFeature.sendMsg("\u00a7c\u77ac\u79fb\u529f\u80fd\u672a\u5f00\u542f");
            return;
        }
        LocalPlayer p = TeleportFeature.mc.player;
        if (p == null || TeleportFeature.mc.f_91073_ == null) {
            return;
        }
        double ty = y;
        if (snap && TeleportFeature.mc.f_91073_ != null) {
            BlockPos bp = BlockPos.m_274561_(x, y, z);
            for (int dy = 0; dy >= -10; --dy) {
                BlockPos check = bp.m_7918_(0, dy, 0);
                BlockState state = TeleportFeature.mc.f_91073_.m_8055_(check);
                if (state.m_60795_()) continue;
                ty = check.m_123342_() + 1.0;
                break;
            }
        }
        Vec3 target = new Vec3(x, ty, z);
        if (TeleportFeature.mc.f_91073_.m_45756_((Entity)p, p.m_20191_().m_82383_(target.subtract(p.position())))) {
            p.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.PosRot(target.x, target.y, target.z, p.m_146908_(), p.m_146909_(), false));
            TeleportFeature.sendMsg("\u00a7a\u77ac\u79fb\u5230 " + String.format("%.1f %.1f %.1f", x, ty, z));
        } else {
            TeleportFeature.sendMsg("\u00a7c\u76ee\u6807\u4f4d\u7f6e\u6709\u963b\u6321");
        }
    }

    private static void sendMsg(String msg) {
        if (TeleportFeature.mc.player != null) {
            TeleportFeature.mc.player.m_5661_(Component.literal((String)("\u00a77[\u77ac\u79fb] " + msg)), false);
        }
    }
}

