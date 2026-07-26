package fku.org.example.fku.utils.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 碰撞检测辅助类 — 用于 A* 寻路中的方块碰撞检测
 * 改编自 Lexis 客户端
 */
public class CollisionHelper {
    private final Level level;
    private static final double FAT_WIDTH = 0.85;
    private static final double PLAYER_HEIGHT = 1.8;

    public CollisionHelper(Level level) {
        this.level = level;
    }

    public boolean canRaycast(Vec3 start, Vec3 end) {
        return canSweep(start, end);
    }

    public boolean isSafe(Vec3 pos) {
        return isSafeBox(pos.x, pos.y, pos.z);
    }

    public double getFloorHeight(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);
        return shape.isEmpty() ? 0.0 : shape.max(Direction.Axis.Y);
    }

    private boolean isSafeBox(double x, double y, double z) {
        AABB box = new AABB(x - 0.425, y + 0.01, z - 0.425, x + 0.425, y + 1.8, z + 0.425);
        return level.noCollision((Entity) null, box);
    }

    public boolean canSweep(Vec3 start, Vec3 end) {
        double dist = start.distanceTo(end);
        if (dist < 0.001) return true;
        int steps = (int) Math.ceil(dist / 0.05);
        Vec3 dir = end.subtract(start).scale(1.0 / steps);
        Vec3 current = start;
        for (int i = 1; i <= steps; i++) {
            current = current.add(dir);
            if (!isSafe(current)) return false;
        }
        return true;
    }

    public boolean isStrictDiagonalSafe(BlockPos start, BlockPos end) {
        BlockPos c1 = new BlockPos(start.getX(), start.getY(), end.getZ());
        BlockPos c2 = new BlockPos(end.getX(), start.getY(), start.getZ());
        return !isBlockSolid(c1) && !isBlockSolid(c2);
    }

    private boolean isBlockSolid(BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}