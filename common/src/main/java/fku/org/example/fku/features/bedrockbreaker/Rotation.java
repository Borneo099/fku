package fku.org.example.fku.features.bedrockbreaker; /* water */

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/**
 * 旋转角度计算工具类 —— 严格参考 CheatUtils Rotation
 *
 * 职责：
 *   计算目标旋转角度，确保活塞放置朝向正确。
 *   findClosest() 逐步调整当前旋转到目标方向，nearestDirection() 判断当前朝向。
 */
public record Rotation(float xRot, float yRot) {

    /**
     * 找到最接近目标方向的旋转角度
     * @param current 当前旋转
     * @param target 目标旋转（NaN 表示该分量不需要改变）
     * @param direction 目标方向
     * @return 最接近的旋转，或 null
     */
    public static Rotation findClosest(Rotation current, Rotation target, Direction direction) {
        if (target == null) {
            return null;
        }
        final float step = 2.5f;
        float xRot = current.xRot();
        float yRot = current.yRot();
        while (nearestDirection(xRot, yRot) != direction) {
            float deltaXRot = getDeltaXRot(target.xRot, xRot);
            float deltaYRot = getDeltaYRot(target.yRot, yRot);
            double len = Math.sqrt(deltaXRot * deltaXRot + deltaYRot * deltaYRot);
            double xRotFactor = Math.abs(deltaXRot) / len;
            double yRotFactor = Math.abs(deltaYRot) / len;
            xRot += (float) (Math.signum(deltaXRot) * step * xRotFactor);
            yRot += (float) (Math.signum(deltaYRot) * step * yRotFactor);
        }
        return new Rotation(xRot, yRot);
    }

    /**
     * 根据 Pitch 和 Yaw 判断最近的朝向
     */
    public static Direction nearestDirection(float xRotDegrees, float yRotDegrees) {
        float xRad = xRotDegrees * ((float) Math.PI / 180F);
        float yRad = -yRotDegrees * ((float) Math.PI / 180F);
        float sinX = Mth.sin(xRad);
        float cosX = Mth.cos(xRad);
        float sinY = Mth.sin(yRad);
        float cosY = Mth.cos(yRad);
        boolean east = sinY > 0F;
        boolean up = sinX < 0F;
        boolean south = cosY > 0F;
        float magEW = east ? sinY : -sinY;
        float magUD = up ? -sinX : sinX;
        float magNS = south ? cosY : -cosY;
        float projEW = magEW * cosX;
        float projNS = magNS * cosX;
        Direction dirEW = east ? Direction.EAST : Direction.WEST;
        Direction dirUD = up ? Direction.UP : Direction.DOWN;
        Direction dirNS = south ? Direction.SOUTH : Direction.NORTH;
        if (magEW > magNS) {
            return (magUD > projEW) ? dirUD : dirEW;
        } else {
            return (magUD > projNS) ? dirUD : dirNS;
        }
    }

    private static float getDeltaXRot(float xRot1, float xRot2) {
        return xRot1 - xRot2;
    }

    private static float getDeltaYRot(float yRot1, float yRot2) {
        if (Float.isNaN(yRot1) || Float.isNaN(yRot2)) {
            return 0;
        }
        float delta = yRot1 - yRot2;
        while (delta < -180) {
            delta += 360;
        }
        while (delta >= 180) {
            delta -= 360;
        }
        return delta;
    }
}