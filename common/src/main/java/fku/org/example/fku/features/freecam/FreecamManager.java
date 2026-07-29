package fku.org.example.fku.features.freecam; /* water */

import net.minecraft.world.phys.Vec3;

/**
 * ★ 自由相机管理器（单例）
 *
 * 管理相机的独立位置/旋转/速度，与玩家实体完全解耦。
 * 被 CameraMixin 和 FreecamFeature / StandAttackFeature 共同使用。
 *
 * 参考自 Lexis FreeCamHack 的相机控制逻辑：
 * - 独立保存相机位置/旋转（xy/prevX）
 * - 平滑速度插值（forwardVelocity * smoothness 三次方缓动）
 * - 惯性衰减（inertia = 0.98^(delta*20)）
 * 该管理器由赛博教员实现
 */
public class FreecamManager {

    private static boolean active = false;

    // 当前位置
    private static double x, y, z;
    // 上一帧位置（用于渲染插值）
    private static double prevX, prevY, prevZ;

    // 当前旋转
    private static float yRot, xRot;
    // 上一帧旋转
    private static float prevYRot, prevXRot;

    // 平滑速度（参考 Lexis 的 inertia 系统）
    private static double forwardVelocity, leftVelocity, upVelocity;
    private static double maxSpeed = 50.0;
    private static double smoothness = 20.0;

    // ============ 状态控制 ============

    public static boolean isActive() { return active; }

    /** 激活自由相机，初始化位置/旋转 */
    public static void activate(double x, double y, double z, float yRot, float xRot) {
        FreecamManager.x = FreecamManager.prevX = x;
        FreecamManager.y = FreecamManager.prevY = y;
        FreecamManager.z = FreecamManager.prevZ = z;
        FreecamManager.yRot = FreecamManager.prevYRot = yRot;
        FreecamManager.xRot = FreecamManager.prevXRot = xRot;
        FreecamManager.forwardVelocity = 0;
        FreecamManager.leftVelocity = 0;
        FreecamManager.upVelocity = 0;
        FreecamManager.active = true;
    }

    /** 停用自由相机 */
    public static void deactivate() {
        FreecamManager.active = false;
    }

    // ============ 渲染插值（参考 Wurst 的 prevCamPos + getCamPos(partialTicks)） ============

    /**
     * 获取插值后的相机位置（用于渲染帧，实现 60fps 平滑移动）
     * 相机位置在 tick 事件（20Hz）中更新，渲染时使用 partialTicks 在 prev/current 之间线性插值。
     * 参考自 Wurst FreecamHack.getCamPos(float partialTicks)
     */
    public static Vec3 getInterpolatedPosition(float partialTicks) {
        return new Vec3(
            prevX + (x - prevX) * partialTicks,
            prevY + (y - prevY) * partialTicks,
            prevZ + (z - prevZ) * partialTicks
        );
    }

    public static float getInterpolatedYRot(float partialTicks) {
        return prevYRot + (yRot - prevYRot) * partialTicks;
    }

    public static float getInterpolatedXRot(float partialTicks) {
        return prevXRot + (xRot - prevXRot) * partialTicks;
    }

    // ============ Getter ============

    public static Vec3 getPosition() { return new Vec3(x, y, z); }

    public static float getYRot() { return yRot; }
    public static float getXRot() { return xRot; }

    public static double getMaxSpeed() { return maxSpeed; }
    public static double getSmoothness() { return smoothness; }

    // ============ Setter ============

    public static void setPosition(double x, double y, double z) {
        FreecamManager.prevX = FreecamManager.x;
        FreecamManager.prevY = FreecamManager.y;
        FreecamManager.prevZ = FreecamManager.z;
        FreecamManager.x = x;
        FreecamManager.y = y;
        FreecamManager.z = z;
    }

    public static void setRotation(float yRot, float xRot) {
        FreecamManager.prevYRot = FreecamManager.yRot;
        FreecamManager.prevXRot = FreecamManager.xRot;
        FreecamManager.yRot = yRot;
        FreecamManager.xRot = xRot;
    }

    public static void setMaxSpeed(double v) { maxSpeed = Math.max(1, Math.min(500, v)); }
    public static void setSmoothness(double v) { smoothness = Math.max(1, Math.min(100, v)); }

    // ============ 鼠标旋转 ============

    /** 处理鼠标移动（由 InputEvent.MouseMovement 调用） */
    public static void onMouseTurn(double yawDelta, double pitchDelta) {
        if (!active) return;
        FreecamManager.prevYRot = FreecamManager.yRot;
        FreecamManager.prevXRot = FreecamManager.xRot;
        FreecamManager.yRot += (float) yawDelta * 0.15f;
        FreecamManager.xRot += (float) pitchDelta * 0.15f;
        FreecamManager.xRot = net.minecraft.util.Mth.clamp(FreecamManager.xRot, -90.0f, 90.0f);
    }

    // ============ 平滑移动（参考 Lexis） ============

    /**
     * 更新自由相机位置（每tick调用）
     *
     * @param forward  前/后输入（+1 前 / -1 后 / 0 无）
     * @param strafe   左/右输入（+1 右 / -1 左 / 0 无）
     * @param up       上/下输入（+1 上 / -1 下 / 0 无）
     * @param delta    帧时间差（秒）
     */
    public static void updateMovement(float forward, float strafe, float up, float delta) {
        if (!active) return;
        if (delta > 0.1f) delta = 0.1f;

        // 计算方向向量（参考 Lexis Quaternion 旋转）
        org.joml.Quaternionf rotation = new org.joml.Quaternionf()
            .rotationYXZ(-yRot * (float)Math.PI / 180.0f, xRot * (float)Math.PI / 180.0f, 0.0f);
        org.joml.Vector3f forwards = new org.joml.Vector3f(0.0f, 0.0f, 1.0f).rotate(rotation);
        org.joml.Vector3f left = new org.joml.Vector3f(1.0f, 0.0f, 0.0f).rotate(rotation);
        org.joml.Vector3f upV = new org.joml.Vector3f(0.0f, 1.0f, 0.0f).rotate(rotation);

        // 目标速度
        double targetForward = (double) forward * maxSpeed;
        double targetStrafe = (double) strafe * maxSpeed;
        double targetUp = (double) up * maxSpeed;

        // 平滑插值（三次方缓动，参考 Lexis）
        double factor = Math.min(1.0, delta * smoothness);
        factor = 1.0 - Math.pow(1.0 - factor, 3.0);
        forwardVelocity += (targetForward - forwardVelocity) * factor;
        leftVelocity += (targetStrafe - leftVelocity) * factor;
        upVelocity += (targetUp - upVelocity) * factor;

        // 惯性衰减
        double inertia = Math.pow(0.98, delta * 20.0);
        forwardVelocity *= inertia;
        leftVelocity *= inertia;
        upVelocity *= inertia;

        // 计算位移
        double dx = (double) forwards.x() * forwardVelocity + (double) left.x() * leftVelocity;
        double dy = (double) forwards.y() * forwardVelocity + upVelocity + (double) left.y() * leftVelocity;
        double dz = (double) forwards.z() * forwardVelocity + (double) left.z() * leftVelocity;
        dx *= delta;
        dy *= delta;
        dz *= delta;

        // 更新位置
        prevX = x; prevY = y; prevZ = z;
        x += dx;
        y += dy;
        z += dz;
    }
}