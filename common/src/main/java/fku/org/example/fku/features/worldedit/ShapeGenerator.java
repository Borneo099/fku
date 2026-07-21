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
                    result.add(origin.m_7918_(x, y, z));
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
                    result.add(origin.m_7918_(x, y, z));
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
                        result.add(origin.m_7918_(x, y, z));
                        continue;
                    }
                    result.add(origin.m_7918_(x, y, z));
                }
            }
            --size;
            ++y;
        }
        return result;
    }

    public static List<BlockPos> wall(BlockPos min, BlockPos max) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        int minX = Math.min(min.m_123341_(), max.m_123341_());
        int minY = Math.min(min.m_123342_(), max.m_123342_());
        int minZ = Math.min(min.m_123343_(), max.m_123343_());
        int maxX = Math.max(min.m_123341_(), max.m_123341_());
        int maxY = Math.max(min.m_123342_(), max.m_123342_());
        int maxZ = Math.max(min.m_123343_(), max.m_123343_());
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
        int minX = Math.min(min.m_123341_(), max.m_123341_());
        int minZ = Math.min(min.m_123343_(), max.m_123343_());
        int maxX = Math.max(min.m_123341_(), max.m_123341_());
        int maxZ = Math.max(min.m_123343_(), max.m_123343_());
        int topY = Math.max(min.m_123342_(), max.m_123342_());
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
        int minX = Math.min(pos1.m_123341_(), pos2.m_123341_());
        int minY = Math.min(pos1.m_123342_(), pos2.m_123342_());
        int minZ = Math.min(pos1.m_123343_(), pos2.m_123343_());
        int maxX = Math.max(pos1.m_123341_(), pos2.m_123341_());
        int maxY = Math.max(pos1.m_123342_(), pos2.m_123342_());
        int maxZ = Math.max(pos1.m_123343_(), pos2.m_123343_());
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
        int minX = Math.min(pos1.m_123341_(), pos2.m_123341_());
        int minY = Math.min(pos1.m_123342_(), pos2.m_123342_());
        int minZ = Math.min(pos1.m_123343_(), pos2.m_123343_());
        int maxX = Math.max(pos1.m_123341_(), pos2.m_123341_());
        int maxY = Math.max(pos1.m_123342_(), pos2.m_123342_());
        int maxZ = Math.max(pos1.m_123343_(), pos2.m_123343_());
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
                if (p1.m_123342_() >= p2.m_123342_()) {
                    p1 = p1.m_6630_(amount);
                    break;
                }
                p2 = p2.m_6630_(amount);
                break;
            }
            case DOWN: {
                if (p1.m_123342_() <= p2.m_123342_()) {
                    p1 = p1.m_6625_(amount);
                    break;
                }
                p2 = p2.m_6625_(amount);
                break;
            }
            case NORTH: {
                if (p1.m_123343_() <= p2.m_123343_()) {
                    p1 = p1.m_122013_(amount);
                    break;
                }
                p2 = p2.m_122013_(amount);
                break;
            }
            case SOUTH: {
                if (p1.m_123343_() >= p2.m_123343_()) {
                    p1 = p1.m_122020_(amount);
                    break;
                }
                p2 = p2.m_122020_(amount);
                break;
            }
            case WEST: {
                if (p1.m_123341_() <= p2.m_123341_()) {
                    p1 = p1.m_122025_(amount);
                    break;
                }
                p2 = p2.m_122025_(amount);
                break;
            }
            case EAST: {
                if (p1.m_123341_() >= p2.m_123341_()) {
                    p1 = p1.m_122030_(amount);
                    break;
                }
                p2 = p2.m_122030_(amount);
            }
        }
        return new BlockPos[]{p1, p2};
    }

    public static BlockPos[] contract(BlockPos pos1, BlockPos pos2, int amount, Direction dir) {
        return ShapeGenerator.expand(pos2, pos1, -amount, dir);
    }
}

