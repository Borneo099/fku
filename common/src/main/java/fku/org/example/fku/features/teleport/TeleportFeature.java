package fku.org.example.fku.features.teleport; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 瞬移功能 — 改为命令驱动模式
 *
 * 使用方式：/fku tp <x> <y> <z> [snap]
 * 通过快捷指令绑定热键后，用户可自由定义瞬移快捷键
 */
public class TeleportFeature {
    private static final Minecraft mc = Minecraft.getInstance();

    public static void init() { TeleportConfig.getInstance(); }

    /** 传送到准星所指方块 */
    public static void teleportToCrosshair() {
        var cfg = TeleportConfig.getInstance();
        if (!cfg.enabled) { sendMsg("§c瞬移功能未开启"); return; }
        var p = mc.player; if (p == null || mc.level == null) return;
        double max = cfg.maxDistance;
        Vec3 from = p.getEyePosition(1);
        Vec3 look = p.getLookAngle().scale(max);
        var hit = mc.level.clip(new ClipContext(from, from.add(look), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos bp = ((BlockHitResult)hit).getBlockPos();
            Vec3 tp = new Vec3(bp.getX() + 0.5, bp.getY() + 1.0, bp.getZ() + 0.5);
            if (mc.level.noCollision(p, p.getBoundingBox().move(tp.subtract(p.position())))) {
                // 同时更新客户端位置 + 发服务端包（确保服务端也认可）
                p.setPos(tp.x, tp.y, tp.z);
                p.connection.send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot(tp.x, tp.y, tp.z, p.getYRot(), p.getXRot(), false));
                sendMsg("§a瞬移到 " + bp.getX() + " " + bp.getY() + " " + bp.getZ());
            } else sendMsg("§c目标位置有阻挡");
        } else sendMsg("§c没有瞄准到方块");
    }

    /**
     * 传送到指定坐标
     * @param x y z 目标坐标
     * @param snap 是否启用方块吸附（落点检测）
     */
    public static void teleportTo(double x, double y, double z, boolean snap) {
        var cfg = TeleportConfig.getInstance();
        if (!cfg.enabled) { sendMsg("§c瞬移功能未开启"); return; }
        var p = mc.player; if (p == null || mc.level == null) return;

        double ty = y;
        if (snap && mc.level != null) {
            BlockPos bp = BlockPos.containing(x, y, z);
            // 向上查找最近的方块
            for (int dy = 0; dy >= -10; dy--) {
                BlockPos check = bp.offset(0, dy, 0);
                BlockState state = mc.level.getBlockState(check);
                if (!state.isAir()) {
                    ty = check.getY() + 1.0;
                    break;
                }
            }
        }

        Vec3 target = new Vec3(x, ty, z);
        if (mc.level.noCollision(p, p.getBoundingBox().move(target.subtract(p.position())))) {
            p.connection.send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot(target.x, target.y, target.z, p.getYRot(), p.getXRot(), false));
            sendMsg("§a瞬移到 " + String.format("%.1f %.1f %.1f", x, ty, z));
        } else {
            sendMsg("§c目标位置有阻挡");
        }
    }

    private static void sendMsg(String msg) {
        if (mc.player != null) mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7[瞬移] " + msg), false);
    }
}
