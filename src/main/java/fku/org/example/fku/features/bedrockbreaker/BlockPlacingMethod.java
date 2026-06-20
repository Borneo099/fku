package fku.org.example.fku.features.bedrockbreaker; /* water */

import net.minecraft.core.Direction;

/**
 * 方块放置方式 —— 严格参考 CheatUtils BlockPlacingMethod 枚举
 * (https://github.com/Zergatul/cheatutils/blob/1.21.11/common/java/com/zergatul/cheatutils/blocks/BlockPlacingMethod.java)
 *
 * 每种放置方式定义了：
 * - 目标旋转角度（getTargetRotation）
 * - 目标朝向（getTargetDirection）
 * - 允许的相邻面方向（getAllowedDirections）
 */
public enum BlockPlacingMethod {
    FACING_TOP,
    FACING_BOTTOM,
    FACING_NORTH,
    FACING_SOUTH,
    FACING_EAST,
    FACING_WEST,
    FROM_HORIZONTAL;

    /**
     * 根据 Direction 返回对应的放置方式
     */
    public static BlockPlacingMethod facing(Direction direction) {
        return switch (direction) {
            case UP -> FACING_TOP;
            case DOWN -> FACING_BOTTOM;
            case NORTH -> FACING_NORTH;
            case SOUTH -> FACING_SOUTH;
            case EAST -> FACING_EAST;
            case WEST -> FACING_WEST;
        };
    }

    /**
     * 获取目标旋转角度（pitch, yaw）
     * NaN 表示该分量不需要改变
     */
    public Rotation getTargetRotation() {
        return switch (this) {
            case FACING_TOP -> new Rotation(90, Float.NaN);
            case FACING_BOTTOM -> new Rotation(-90, Float.NaN);
            case FACING_NORTH -> new Rotation(0, 0);
            case FACING_SOUTH -> new Rotation(0, -180);
            case FACING_EAST -> new Rotation(0, 90);
            case FACING_WEST -> new Rotation(0, -90);
            default -> null;
        };
    }

    /**
     * 获取目标朝向（用于 Rotation.findClosest 计算最接近的旋转）
     */
    public Direction getTargetDirection() {
        return switch (this) {
            case FACING_TOP -> Direction.DOWN;
            case FACING_BOTTOM -> Direction.UP;
            case FACING_NORTH -> Direction.SOUTH;
            case FACING_SOUTH -> Direction.NORTH;
            case FACING_EAST -> Direction.WEST;
            case FACING_WEST -> Direction.EAST;
            default -> null;
        };
    }

    /**
     * 获取允许搜索的相邻面方向
     */
    public Direction[] getAllowedDirections() {
        return Direction.values(); // 默认所有方向
    }
}