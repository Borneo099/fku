package fku.org.example.fku.features.bedrockbreaker;

import fku.org.example.fku.features.bedrockbreaker.Rotation;
import net.minecraft.core.Direction;

public enum BlockPlacingMethod {
    FACING_TOP,
    FACING_BOTTOM,
    FACING_NORTH,
    FACING_SOUTH,
    FACING_EAST,
    FACING_WEST,
    FROM_HORIZONTAL;


    public static BlockPlacingMethod facing(Direction direction) {
        return switch (direction) {
            default -> throw new IncompatibleClassChangeError();
            case UP -> FACING_TOP;
            case DOWN -> FACING_BOTTOM;
            case NORTH -> FACING_NORTH;
            case SOUTH -> FACING_SOUTH;
            case EAST -> FACING_EAST;
            case WEST -> FACING_WEST;
        };
    }

    public Rotation getTargetRotation() {
        return switch (this) {
            case FACING_TOP -> new Rotation(90.0f, Float.NaN);
            case FACING_BOTTOM -> new Rotation(-90.0f, Float.NaN);
            case FACING_NORTH -> new Rotation(0.0f, 0.0f);
            case FACING_SOUTH -> new Rotation(0.0f, -180.0f);
            case FACING_EAST -> new Rotation(0.0f, 90.0f);
            case FACING_WEST -> new Rotation(0.0f, -90.0f);
            default -> null;
        };
    }

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

    public Direction[] getAllowedDirections() {
        return Direction.values();
    }
}

