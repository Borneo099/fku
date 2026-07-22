package fku.org.example.fku.features.worldedit;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ShapeGenerator {
    public static List<BlockPos> sphere(BlockPos origin, int radius, boolean hollow) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        int r2 = radius * radius;
        int hollowR2 = radius > 0 ? (radius - 1) * (radius - 1) : 0;
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    int dist2 = x * x + y * y + z * z;
                    if (dist2 > r2 || hollow && dist2 <= hollowR2) continue;
                    result.add(origin.offset(x, y, z));
                }
            }
        }
        return result;
    }

    public static List<BlockPos> cylinder(BlockPos origin, int radius, int height, boolean hollow) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        int r2 = radius * radius;
        int hollowR2 = radius > 0 ? (radius - 1) * (radius - 1) : 0;
        int halfH = height / 2;
        for (int y = -halfH; y <= height - halfH - 1; ++y) {
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    int dist2 = x * x + z * z;
                    if (dist2 > r2 || hollow && dist2 <= hollowR2) continue;
                    result.add(origin.offset(x, y, z));
                }
            }
        }
        return result;
    }

    public static List<BlockPos> pyramid(BlockPos origin, int size, boolean hollow) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        int y = 0;
        while (size > 0) {
            for (int x = -size; x <= size; ++x) {
                for (int z = -size; z <= size; ++z) {
                    if (hollow) {
                        if (Math.abs(x) != size && Math.abs(z) != size) continue;
                        result.add(origin.offset(x, y, z));
                        continue;
                    }
                    result.add(origin.offset(x, y, z));
                }
            }
            --size;
            ++y;
        }
        return result;
    }

    public static List<BlockPos> wall(BlockPos min, BlockPos max) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        int minX = Math.min(min.getX(), max.getX());
        int minY = Math.min(min.getY(), max.getY());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxX = Math.max(min.getX(), max.getX());
        int maxY = Math.max(min.getY(), max.getY());
        int maxZ = Math.max(min.getZ(), max.getZ());
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                result.add(new BlockPos(x, y, minZ));
                if (maxZ == minZ) continue;
                result.add(new BlockPos(x, y, maxZ));
            }
            for (int z = minZ + 1; z < maxZ; ++z) {
                result.add(new BlockPos(minX, y, z));
                if (maxX == minX) continue;
                result.add(new BlockPos(maxX, y, z));
            }
        }
        return result;
    }

    public static List<BlockPos> roof(BlockPos min, BlockPos max) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        int minX = Math.min(min.getX(), max.getX());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxX = Math.max(min.getX(), max.getX());
        int maxZ = Math.max(min.getZ(), max.getZ());
        int topY = Math.max(min.getY(), max.getY());
        int widthX = maxX - minX + 1;
        int widthZ = maxZ - minZ + 1;
        int maxSize = Math.max(widthX, widthZ);
        for (int layer = 0; layer < maxSize; ++layer) {
            int y = topY + layer;
            int x1 = minX + layer;
            int x2 = maxX - layer;
            int z1 = minZ + layer;
            int z2 = maxZ - layer;
            if (x1 > x2 || z1 > z2) break;
            for (int x = x1; x <= x2; ++x) {
                for (int z = z1; z <= z2; ++z) {
                    if (x != x1 && x != x2 && z != z1 && z != z2) continue;
                    result.add(new BlockPos(x, y, z));
                }
            }
        }
        return result;
    }

    public static List<BlockPos> cuboid(BlockPos pos1, BlockPos pos2) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    result.add(new BlockPos(x, y, z));
                }
            }
        }
        return result;
    }

    public static List<BlockPos> cuboidHollow(BlockPos pos1, BlockPos pos2) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (x != minX && x != maxX && y != minY && y != maxY && z != minZ && z != maxZ) continue;
                    result.add(new BlockPos(x, y, z));
                }
            }
        }
        return result;
    }

    public static BlockPos[] expand(BlockPos pos1, BlockPos pos2, int amount, Direction dir) {
        BlockPos p1 = pos1;
        BlockPos p2 = pos2;
        switch (dir) {
            case UP: {
                if (p1.getY() >= p2.getY()) {
                    p1 = p1.above(amount);
                    break;
                }
                p2 = p2.above(amount);
                break;
            }
            case DOWN: {
                if (p1.getY() <= p2.getY()) {
                    p1 = p1.below(amount);
                    break;
                }
                p2 = p2.below(amount);
                break;
            }
            case NORTH: {
                if (p1.getZ() <= p2.getZ()) {
                    p1 = p1.north(amount);
                    break;
                }
                p2 = p2.north(amount);
                break;
            }
            case SOUTH: {
                if (p1.getZ() >= p2.getZ()) {
                    p1 = p1.south(amount);
                    break;
                }
                p2 = p2.south(amount);
                break;
            }
            case WEST: {
                if (p1.getX() <= p2.getX()) {
                    p1 = p1.west(amount);
                    break;
                }
                p2 = p2.west(amount);
                break;
            }
            case EAST: {
                if (p1.getX() >= p2.getX()) {
                    p1 = p1.east(amount);
                    break;
                }
                p2 = p2.east(amount);
            }
        }
        return new BlockPos[]{p1, p2};
    }

    public static BlockPos[] contract(BlockPos pos1, BlockPos pos2, int amount, Direction dir) {
        return ShapeGenerator.expand(pos2, pos1, -amount, dir);
    }
}

