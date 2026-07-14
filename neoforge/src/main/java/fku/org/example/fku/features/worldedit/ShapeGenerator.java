package fku.org.example.fku.features.worldedit; /* water */

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 形状生成器 — 生成各种形状的方块坐标列表
 *
 * 设计思想：
 * - 所有方法返回 BlockPos 列表，供 TaskQueue 分帧执行
 * - 空心模式仅在表面放置方块
 * - 坐标相对于 origin 偏移
 */
public class ShapeGenerator {

    /**
     * 球体（实心）
     * dx²+dy²+dz² ≤ radius²
     */
    public static List<BlockPos> sphere(BlockPos origin, int radius, boolean hollow) {
        List<BlockPos> result = new ArrayList<>();
        int r2 = radius * radius;
        int hollowR2 = radius > 0 ? (radius - 1) * (radius - 1) : 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int dist2 = x * x + y * y + z * z;
                    if (dist2 > r2) continue;
                    if (hollow && dist2 <= hollowR2) continue;
                    result.add(origin.offset(x, y, z));
                }
            }
        }
        return result;
    }

    /**
     * 圆柱体（实心）
     * dx²+dz² ≤ radius² and |dy| ≤ height/2
     */
    public static List<BlockPos> cylinder(BlockPos origin, int radius, int height, boolean hollow) {
        List<BlockPos> result = new ArrayList<>();
        int r2 = radius * radius;
        int hollowR2 = radius > 0 ? (radius - 1) * (radius - 1) : 0;
        int halfH = height / 2;

        for (int y = -halfH; y <= height - halfH - 1; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    int dist2 = x * x + z * z;
                    if (dist2 > r2) continue;
                    if (hollow && dist2 <= hollowR2) continue;
                    result.add(origin.offset(x, y, z));
                }
            }
        }
        return result;
    }

    /**
     * 金字塔（实心）
     * 每层收缩1格，直到顶端
     */
    public static List<BlockPos> pyramid(BlockPos origin, int size, boolean hollow) {
        List<BlockPos> result = new ArrayList<>();
        int y = 0;
        while (size > 0) {
            for (int x = -size; x <= size; x++) {
                for (int z = -size; z <= size; z++) {
                    if (hollow) {
                        // 仅边缘
                        if (Math.abs(x) == size || Math.abs(z) == size) {
                            result.add(origin.offset(x, y, z));
                        }
                    } else {
                        result.add(origin.offset(x, y, z));
                    }
                }
            }
            size--;
            y++;
        }
        return result;
    }

    /**
     * 墙壁 — 选区四周的垂直围墙
     * 从 min 到 max 的立方体范围，仅四周垂直面
     */
    public static List<BlockPos> wall(BlockPos min, BlockPos max) {
        List<BlockPos> result = new ArrayList<>();
        int minX = Math.min(min.getX(), max.getX());
        int minY = Math.min(min.getY(), max.getY());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxX = Math.max(min.getX(), max.getX());
        int maxY = Math.max(min.getY(), max.getY());
        int maxZ = Math.max(min.getZ(), max.getZ());

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                result.add(new BlockPos(x, y, minZ));
                if (maxZ != minZ) result.add(new BlockPos(x, y, maxZ));
            }
            for (int z = minZ + 1; z < maxZ; z++) {
                result.add(new BlockPos(minX, y, z));
                if (maxX != minX) result.add(new BlockPos(maxX, y, z));
            }
        }
        return result;
    }

    /**
     * 屋顶 — 在选区顶部生成斜面屋顶
     */
    public static List<BlockPos> roof(BlockPos min, BlockPos max) {
        List<BlockPos> result = new ArrayList<>();
        int minX = Math.min(min.getX(), max.getX());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxX = Math.max(min.getX(), max.getX());
        int maxZ = Math.max(min.getZ(), max.getZ());
        int topY = Math.max(min.getY(), max.getY());

        int widthX = maxX - minX + 1;
        int widthZ = maxZ - minZ + 1;
        int maxSize = Math.max(widthX, widthZ);

        for (int layer = 0; layer < maxSize; layer++) {
            int y = topY + layer;
            int x1 = minX + layer;
            int x2 = maxX - layer;
            int z1 = minZ + layer;
            int z2 = maxZ - layer;

            if (x1 > x2 || z1 > z2) break;

            for (int x = x1; x <= x2; x++) {
                for (int z = z1; z <= z2; z++) {
                    // 只放边缘
                    if (x == x1 || x == x2 || z == z1 || z == z2) {
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 选区内的所有方块坐标
     */
    public static List<BlockPos> cuboid(BlockPos pos1, BlockPos pos2) {
        List<BlockPos> result = new ArrayList<>();
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    result.add(new BlockPos(x, y, z));
                }
            }
        }
        return result;
    }

    /**
     * 选区边缘（用于 //outline 或空心 set）
     */
    public static List<BlockPos> cuboidHollow(BlockPos pos1, BlockPos pos2) {
        List<BlockPos> result = new ArrayList<>();
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 将选区向指定方向扩展指定格数
     */
    public static BlockPos[] expand(BlockPos pos1, BlockPos pos2, int amount, net.minecraft.core.Direction dir) {
        BlockPos p1 = pos1;
        BlockPos p2 = pos2;
        switch (dir) {
            case UP:
                if (p1.getY() >= p2.getY()) p1 = p1.above(amount);
                else p2 = p2.above(amount);
                break;
            case DOWN:
                if (p1.getY() <= p2.getY()) p1 = p1.below(amount);
                else p2 = p2.below(amount);
                break;
            case NORTH:
                if (p1.getZ() <= p2.getZ()) p1 = p1.north(amount);
                else p2 = p2.north(amount);
                break;
            case SOUTH:
                if (p1.getZ() >= p2.getZ()) p1 = p1.south(amount);
                else p2 = p2.south(amount);
                break;
            case WEST:
                if (p1.getX() <= p2.getX()) p1 = p1.west(amount);
                else p2 = p2.west(amount);
                break;
            case EAST:
                if (p1.getX() >= p2.getX()) p1 = p1.east(amount);
                else p2 = p2.east(amount);
                break;
        }
        return new BlockPos[]{p1, p2};
    }

    /**
     * 将选区向指定方向收缩指定格数
     */
    public static BlockPos[] contract(BlockPos pos1, BlockPos pos2, int amount, net.minecraft.core.Direction dir) {
        return expand(pos2, pos1, -amount, dir);
    }
}
