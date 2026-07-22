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
        if (p == null || TeleportFeature.mc.level == null) {
            return;
        }
        double max = cfg.maxDistance;
        Vec3 from = p.getEyePosition(1.0f);
        BlockHitResult hit = TeleportFeature.mc.level.clip(new ClipContext(from, from.add(look = p.getLookAngle().scale(max)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos bp = hit.getBlockPos();
            Vec3 tp = new Vec3(bp.getX() + 0.5, bp.getY() + 1.0, bp.getZ() + 0.5);
            if (TeleportFeature.mc.level.noCollision((Entity)p, p.getBoundingBox().move(tp.subtract(p.position())))) {
                p.setPos(tp.x, tp.y, tp.z);
                p.connection.send((Packet)new ServerboundMovePlayerPacket.PosRot(tp.x, tp.y, tp.z, p.getYRot(), p.getXRot(), false));
                TeleportFeature.sendMsg("\u00a7a\u77ac\u79fb\u5230 " + bp.getX() + " " + bp.getY() + " " + bp.getZ());
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
        if (p == null || TeleportFeature.mc.level == null) {
            return;
        }
        double ty = y;
        if (snap && TeleportFeature.mc.level != null) {
            BlockPos bp = BlockPos.containing(x, y, z);
            for (int dy = 0; dy >= -10; --dy) {
                BlockPos check = bp.offset(0, dy, 0);
                BlockState state = TeleportFeature.mc.level.getBlockState(check);
                if (state.isAir()) continue;
                ty = check.getY() + 1.0;
                break;
            }
        }
        Vec3 target = new Vec3(x, ty, z);
        if (TeleportFeature.mc.level.noCollision((Entity)p, p.getBoundingBox().move(target.subtract(p.position())))) {
            p.connection.send((Packet)new ServerboundMovePlayerPacket.PosRot(target.x, target.y, target.z, p.getYRot(), p.getXRot(), false));
            TeleportFeature.sendMsg("\u00a7a\u77ac\u79fb\u5230 " + String.format("%.1f %.1f %.1f", x, ty, z));
        } else {
            TeleportFeature.sendMsg("\u00a7c\u76ee\u6807\u4f4d\u7f6e\u6709\u963b\u6321");
        }
    }

    private static void sendMsg(String msg) {
        if (TeleportFeature.mc.player != null) {
            TeleportFeature.mc.player.displayClientMessage(Component.literal((String)("\u00a77[\u77ac\u79fb] " + msg)), false);
        }
    }
}

