package fku.org.example.fku.utils.pathfinding;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A* 寻路算法 — 用于 TpGoto 功能中的路径规划
 * 改编自 Lexis 客户端
 */
public class AStarPathFinder {
    private final Level level;
    private final CollisionHelper collisionHelper;
    private boolean airPath = true;
    private double attackRange = 3.0;
    private static final int MAX_ITERATIONS = 15000;
    private final BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos headPos = new BlockPos.MutableBlockPos();

    public AStarPathFinder(Level level) {
        this.level = level;
        this.collisionHelper = new CollisionHelper(level);
    }

    public void setAirPath(boolean b) { this.airPath = b; }
    public void setAttackRange(double range) { this.attackRange = range; }

    public List<Vec3> findPath(Vec3 start, Vec3 target, double maxStep) {
        BlockPos playerBP = BlockPos.containing(start);
        BlockPos enemyBP = BlockPos.containing(target);
        Long2ObjectOpenHashMap<Node> allNodes = new Long2ObjectOpenHashMap<>();
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Node best = null;
        int ex = enemyBP.getX();
        int ey = enemyBP.getY();
        int ez = enemyBP.getZ();
        int range = (int) Math.ceil(attackRange);

        // 从目标周围的安全点开始搜索
        for (int px = ex - range; px <= ex + range; px++) {
            for (int py = ey - range; py <= ey + range; py++) {
                for (int pz = ez - range; pz <= ez + range; pz++) {
                    double distSq = (px - ex) * (px - ex) + (py - ey) * (py - ey) + (pz - ez) * (pz - ez);
                    if (distSq <= attackRange * attackRange && isTwoBlocksHighSafe(px, py, pz)) {
                        long posLong = BlockPos.asLong(px, py, pz);
                        double h = getHeuristic(px, py, pz, playerBP.getX(), playerBP.getY(), playerBP.getZ());
                        Node startNode = new Node(posLong, null, 0.0, h);
                        openSet.add(startNode);
                        allNodes.put(posLong, startNode);
                        if (best == null || startNode.f < best.f) best = startNode;
                    }
                }
            }
        }

        if (openSet.isEmpty()) return new ArrayList<>();

        int px = playerBP.getX();
        int py = playerBP.getY();
        int pz = playerBP.getZ();

        for (int iterations = 0; !openSet.isEmpty() && iterations++ < MAX_ITERATIONS; ) {
            Node curr = openSet.poll();
            int cx = BlockPos.getX(curr.posLong);
            int cy = BlockPos.getY(curr.posLong);
            int cz = BlockPos.getZ(curr.posLong);

            if (Math.abs(cx - px) <= 1 && Math.abs(cy - py) <= 1 && Math.abs(cz - pz) <= 1
                    && collisionHelper.canSweep(toVec3(cx, cy, cz), start)) {
                best = new Node(BlockPos.asLong(px, py, pz), curr, 0.0, 0.0);
                break;
            }

            double currentDistToPlayer = getDistanceSq(cx, cy, cz, px, py, pz);
            int bx = BlockPos.getX(best.posLong);
            int by = BlockPos.getY(best.posLong);
            int bz = BlockPos.getZ(best.posLong);
            if (currentDistToPlayer < getDistanceSq(bx, by, bz, px, py, pz)) {
                best = curr;
            }

            expandNode(cx, cy, cz, curr, px, py, pz, openSet, allNodes);
        }

        return buildPath(best, start, target, maxStep);
    }

    private void expandNode(int cx, int cy, int cz, Node curr, int px, int py, int pz,
                            PriorityQueue<Node> openSet, Long2ObjectOpenHashMap<Node> allNodes) {
        processNeighbor(cx + 1, cy, cz, curr, px, py, pz, 1.0, openSet, allNodes);
        processNeighbor(cx - 1, cy, cz, curr, px, py, pz, 1.0, openSet, allNodes);
        processNeighbor(cx, cy, cz + 1, curr, px, py, pz, 1.0, openSet, allNodes);
        processNeighbor(cx, cy, cz - 1, curr, px, py, pz, 1.0, openSet, allNodes);
        processNeighbor(cx, cy + 1, cz, curr, px, py, pz, 1.0, openSet, allNodes);
        processNeighbor(cx, cy - 1, cz, curr, px, py, pz, 1.0, openSet, allNodes);

        if (airPath) {
            for (int i = 2; i <= 10; i++) {
                if (isTwoBlocksHighSafe(cx, cy + i, cz)) {
                    processNeighbor(cx, cy + i, cz, curr, px, py, pz, (double) i, openSet, allNodes);
                    break;
                }
            }
            for (int i = 2; i <= 10; i++) {
                if (isTwoBlocksHighSafe(cx, cy - i, cz)) {
                    processNeighbor(cx, cy - i, cz, curr, px, py, pz, (double) i, openSet, allNodes);
                    break;
                }
            }
        }
    }

    private void processNeighbor(int x, int y, int z, Node curr, int tx, int ty, int tz,
                                 double costAdd, PriorityQueue<Node> openSet, Long2ObjectOpenHashMap<Node> allNodes) {
        if (isTwoBlocksHighSafe(x, y, z)) {
            long posLong = BlockPos.asLong(x, y, z);
            double cost = curr.g + costAdd;
            Node existing = allNodes.get(posLong);
            if (existing == null || existing.g > cost) {
                double f = cost + getHeuristic(x, y, z, tx, ty, tz);
                Node newNode = new Node(posLong, curr, cost, f);
                allNodes.put(posLong, newNode);
                openSet.add(newNode);
            }
        }
    }

    private boolean isTwoBlocksHighSafe(int x, int y, int z) {
        checkPos.set(x, y, z);
        Vec3 exactPos = toVec3(x, y, z);
        if (!collisionHelper.isSafe(exactPos)) return false;
        headPos.set(x, y + 1, z);
        BlockState headState = level.getBlockState(headPos);
        if (!headState.getCollisionShape(level, headPos).isEmpty()) {
            double headBlockMinY = headPos.getY() + headState.getCollisionShape(level, headPos).min(Direction.Axis.Y);
            if (headBlockMinY - exactPos.y < 1.95) return false;
        }
        return true;
    }

    private double getHeuristic(int x1, int y1, int z1, int x2, int y2, int z2) {
        double dx = Math.abs(x1 - x2);
        double dy = Math.abs(y1 - y2);
        double dz = Math.abs(z1 - z2);
        return Math.sqrt(dx * dx + dy * dy + dz * dz) + dx + dy + dz;
    }

    private double getDistanceSq(int x1, int y1, int z1, int x2, int y2, int z2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);
    }

    private Vec3 toVec3(int x, int y, int z) {
        BlockPos bp = new BlockPos(x, y, z);
        double floorY = collisionHelper.getFloorHeight(bp);
        return new Vec3(x + 0.5, y + floorY, z + 0.5);
    }

    private List<Vec3> buildPath(Node bestNodeNearPlayer, Vec3 realStart, Vec3 realTarget, double maxStep) {
        if (bestNodeNearPlayer == null) return new ArrayList<>();
        List<Long> blockPath = new ArrayList<>();
        for (Node node = bestNodeNearPlayer; node != null; node = node.parent) {
            blockPath.add(node.posLong);
        }
        List<Vec3> vecPath = new ArrayList<>();
        vecPath.add(realStart);
        for (int i = 1; i < blockPath.size() - 1; i++) {
            long posLong = blockPath.get(i);
            vecPath.add(toVec3(BlockPos.getX(posLong), BlockPos.getY(posLong), BlockPos.getZ(posLong)));
        }
        vecPath.add(realTarget);
        return simplify(vecPath, maxStep);
    }

    private List<Vec3> simplify(List<Vec3> path, double maxStep) {
        if (path.size() <= 2) return path;
        List<Vec3> simple = new ArrayList<>();
        Vec3 lastPos = path.get(0);
        simple.add(lastPos);
        for (int i = 1; i < path.size(); i++) {
            Vec3 current = path.get(i);
            if (i < path.size() - 1) {
                Vec3 next = path.get(i + 1);
                // ★ 垂直步长限制：单次垂直传送最多10格，否则服务端会回弹
                double vertDist = Math.abs(next.y - lastPos.y);
                if (lastPos.distanceTo(next) > maxStep || vertDist > 10 || !collisionHelper.canSweep(lastPos, next)) {
                    simple.add(current);
                    lastPos = current;
                }
            }
        }
        simple.add(path.get(path.size() - 1));
        return simple;
    }

    private static class Node {
        long posLong;
        Node parent;
        double g;
        double f;
        Node(long posLong, Node parent, double g, double f) {
            this.posLong = posLong;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }
    }
}