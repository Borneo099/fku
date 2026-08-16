package fku.org.example.fku.features.entitycontrol;

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 实体控制功能 — 移植自 Lexis EntityControlHack
 *
 * 设计前提：必须先“骑上”目标实体（player.getVehicle() != null）。
 * 开启后仅当玩家处于骑乘状态时，把玩家移动按键映射成坐骑的速度向量，
 * 通过 vehicle.setDeltaMovement(...) 设置并在每 tick 发送合法的
 * ServerboundMoveVehiclePacket（即原版骑乘本就会发的那个网络包）上报服务器，
 * 从而自由驱动坐骑移动 / 飞行。为避免服务器反作弊踢出，提供 antiKick 周期性微调 y。
 *
 * 注意：本功能依赖坐骑移动这一“合法网络通道”，并非纯本地改坐标（本地改坐标会被服务器拉回）。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EntityControlFeature {

    private static Minecraft getMc() { return Minecraft.getInstance(); }

    public static void init() {
        EntityControlConfig.load();
        if (isEnabled()) Fku.LOGGER.debug("[EntityControl] 配置恢复: 已启用");
    }

    public static void toggleEnabled() { setEnabled(!isEnabled()); }

    public static void setEnabled(boolean val) {
        EntityControlConfig cfg = EntityControlConfig.getInstance();
        cfg.setEnabled(val);
        Fku.LOGGER.debug("[EntityControl] " + (val ? "已启用" : "已禁用"));
    }

    public static boolean isEnabled() { return EntityControlConfig.getInstance().enabled; }

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = getMc();
        if (mc == null || mc.player == null) return;
        EntityControlConfig cfg = EntityControlConfig.getInstance();
        if (!isEnabled()) return;

        LocalPlayer player = mc.player;
        Entity vehicle = player.getVehicle();
        if (vehicle == null) return; // 仅在骑乘时接管坐骑

        // ── 读取按键 ──
        float forward = 0.0F;
        float strafe = 0.0F;
        float vertical = 0.0F;
        if (mc.options.keyUp.isDown()) forward += 1.0F;
        if (mc.options.keyDown.isDown()) forward -= 1.0F;
        if (mc.options.keyLeft.isDown()) strafe += 1.0F;
        if (mc.options.keyRight.isDown()) strafe -= 1.0F;
        if (cfg.flightMode && mc.options.keyJump.isDown()) vertical += 1.0F;

        // 左 Alt 下降：让坐骑向下移动，避免飞高后摔死（飞行模式或任何骑乘状态下都生效）
        boolean altDown = isLeftAltDown(mc);
        if (altDown) vertical -= 1.0F;

        // ── 计算移动向量（以玩家视角为基准）──
        Vec3 movement = calculateMovement(player, forward, strafe, (float) cfg.horizontalSpeed);
        if (cfg.flightMode || altDown) {
            // 飞行 / 下降模式：完全接管竖直速度（上=上升、下=下降）
            if (vertical > 0.0F) {
                movement = new Vec3(movement.x, cfg.verticalSpeed, movement.z);
            } else if (vertical < 0.0F) {
                movement = new Vec3(movement.x, -cfg.verticalSpeed, movement.z);
            } else {
                movement = new Vec3(movement.x, 0.0, movement.z);
            }
        } else {
            // 保留坐骑自身竖直速度（重力 / 跳跃），仅控制水平
            movement = new Vec3(movement.x, vehicle.getDeltaMovement().y, movement.z);
        }

        vehicle.setDeltaMovement(movement);

        if (cfg.lockYaw) {
            vehicle.setYRot(player.getYRot());
            vehicle.setYHeadRot(player.getYRot());
        }

        // 发送合法的坐骑移动包（原版骑乘本就会发）
        if (player.connection != null) {
            player.connection.send(new ServerboundMoveVehiclePacket(vehicle));
        }

        // ── 反踢：周期性上下微调 y，规避服务器踢出 ──
        if (cfg.antiKick && cfg.flightMode) {
            tickCounter++;
            if (tickCounter > cfg.antiKickInterval + 1) tickCounter = 0;
            if (tickCounter == 0) {
                vehicle.setPos(vehicle.getX(), vehicle.getY() - cfg.antiKickDistance, vehicle.getZ());
            } else if (tickCounter == 1) {
                vehicle.setPos(vehicle.getX(), vehicle.getY() + cfg.antiKickDistance, vehicle.getZ());
            }
            if ((tickCounter == 0 || tickCounter == 1) && player.connection != null) {
                player.connection.send(new ServerboundMoveVehiclePacket(vehicle));
            }
        } else {
            tickCounter = 0;
        }
    }

    /** 按玩家视角把前向/横移转化为世界坐标速度向量 */
    private static Vec3 calculateMovement(LocalPlayer player, float forward, float strafe, float speed) {
        if (forward == 0.0F && strafe == 0.0F) return Vec3.ZERO;
        float yaw = player.getYRot();
        double rad = Math.toRadians(yaw);
        double moveX = 0.0;
        double moveZ = 0.0;
        if (forward != 0.0F) {
            moveX -= Math.sin(rad) * forward;
            moveZ += Math.cos(rad) * forward;
        }
        if (strafe != 0.0F) {
            moveX -= Math.sin(rad - 1.5707963267948966) * strafe;
            moveZ += Math.cos(rad - 1.5707963267948966) * strafe;
        }
        double len = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (len > 0.0) {
            moveX = moveX / len * speed;
            moveZ = moveZ / len * speed;
        }
        return new Vec3(moveX, 0.0, moveZ);
    }

    /** 检测左 Alt 是否处于按下状态（用于下降控制） */
    private static boolean isLeftAltDown(Minecraft mc) {
        try {
            long window = mc.getWindow().getWindow();
            return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS
                    || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
        } catch (Exception ignored) {
            return false;
        }
    }
}
