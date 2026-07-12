package lexis.Hack.Utils.pathfinding;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AStarPathFinder {
   private final Level level;
   private final CollisionHelper collisionHelper;
   private boolean airPath = true;
   private boolean hClip = true;
   private double attackRange = 3.0;
   private static final int MAX_ITERATIONS = 15000;
   private final BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
   private final BlockPos.MutableBlockPos headPos = new BlockPos.MutableBlockPos();

   public AStarPathFinder(Level level) {
      this.level = level;
      this.collisionHelper = new CollisionHelper(level);
   }

   public void setAirPath(boolean b) {
      this.airPath = b;
   }

   public void setHClip(boolean b) {
      this.hClip = b;
   }

   public void setAttackRange(double range) {
      this.attackRange = range;
   }

   public List findPath(Vec3 start, Vec3 target, double maxStep) {
      BlockPos playerBP = BlockPos.m_274446_(start);
      BlockPos enemyBP = BlockPos.m_274446_(target);
      Long2ObjectOpenHashMap allNodes = new Long2ObjectOpenHashMap();
      PriorityQueue openSet = new PriorityQueue(Comparator.comparingDouble((n) -> {
         return n.f;
      }));
      Node best = null;
      int ex = enemyBP.m_123341_();
      int ey = enemyBP.m_123342_();
      int ez = enemyBP.m_123343_();
      int range = (int)Math.ceil(this.attackRange);

      int px;
      int py;
      int pz;
      for(px = ex - range; px <= ex + range; ++px) {
         for(py = ey - range; py <= ey + range; ++py) {
            for(pz = ez - range; pz <= ez + range; ++pz) {
               double distSq = (double)((px - ex) * (px - ex) + (py - ey) * (py - ey) + (pz - ez) * (pz - ez));
               if (distSq <= this.attackRange * this.attackRange && this.isTwoBlocksHighSafe(px, py, pz)) {
                  long posLong = BlockPos.m_121882_(px, py, pz);
                  double h = this.getHeuristic(px, py, pz, playerBP.m_123341_(), playerBP.m_123342_(), playerBP.m_123343_());
                  Node startNode = new Node(posLong, (Node)null, 0.0, h);
                  openSet.add(startNode);
                  allNodes.put(posLong, startNode);
                  if (best == null || startNode.f < best.f) {
                     best = startNode;
                  }
               }
            }
         }
      }

      if (openSet.isEmpty()) {
         return new ArrayList();
      } else {
         px = playerBP.m_123341_();
         py = playerBP.m_123342_();
         pz = playerBP.m_123343_();

         Node curr;
         int cy;
         int cx;
         int cz;
         for(int iterations = 0; !openSet.isEmpty() && iterations++ < 15000; this.expandNode(cx, cy, cz, curr, px, py, pz, openSet, allNodes)) {
            curr = (Node)openSet.poll();
            cx = BlockPos.m_121983_(curr.posLong);
            cy = BlockPos.m_122008_(curr.posLong);
            cz = BlockPos.m_122015_(curr.posLong);
            if (Math.abs(cx - px) <= 1 && Math.abs(cy - py) <= 1 && Math.abs(cz - pz) <= 1 && this.collisionHelper.canSweep(this.toVec3(cx, cy, cz), start)) {
               best = new Node(BlockPos.m_121882_(px, py, pz), curr, 0.0, 0.0);
               break;
            }

            double currentDistToPlayer = this.getDistanceSq(cx, cy, cz, px, py, pz);
            int bx = BlockPos.m_121983_(best.posLong);
            int by = BlockPos.m_122008_(best.posLong);
            int bz = BlockPos.m_122015_(best.posLong);
            if (currentDistToPlayer < this.getDistanceSq(bx, by, bz, px, py, pz)) {
               best = curr;
            }
         }

         return this.buildPath(best, start, target, maxStep);
      }
   }

   private void expandNode(int cx, int cy, int cz, Node curr, int px, int py, int pz, PriorityQueue openSet, Long2ObjectOpenHashMap allNodes) {
      this.processNeighbor(cx + 1, cy, cz, curr, px, py, pz, 1.0, openSet, allNodes);
      this.processNeighbor(cx - 1, cy, cz, curr, px, py, pz, 1.0, openSet, allNodes);
      this.processNeighbor(cx, cy, cz + 1, curr, px, py, pz, 1.0, openSet, allNodes);
      this.processNeighbor(cx, cy, cz - 1, curr, px, py, pz, 1.0, openSet, allNodes);
      this.processNeighbor(cx, cy + 1, cz, curr, px, py, pz, 1.0, openSet, allNodes);
      this.processNeighbor(cx, cy - 1, cz, curr, px, py, pz, 1.0, openSet, allNodes);
      if (this.airPath) {
         int i;
         for(i = 2; i <= 10; ++i) {
            if (this.isTwoBlocksHighSafe(cx, cy + i, cz)) {
               this.processNeighbor(cx, cy + i, cz, curr, px, py, pz, (double)i, openSet, allNodes);
               break;
            }
         }

         for(i = 2; i <= 10; ++i) {
            if (this.isTwoBlocksHighSafe(cx, cy - i, cz)) {
               this.processNeighbor(cx, cy - i, cz, curr, px, py, pz, (double)i, openSet, allNodes);
               break;
            }
         }
      }

      if (this.hClip) {
         if (!this.isTwoBlocksHighSafe(cx + 1, cy, cz) && this.isTwoBlocksHighSafe(cx + 2, cy, cz)) {
            this.processNeighbor(cx + 2, cy, cz, curr, px, py, pz, 2.0, openSet, allNodes);
         }

         if (!this.isTwoBlocksHighSafe(cx - 1, cy, cz) && this.isTwoBlocksHighSafe(cx - 2, cy, cz)) {
            this.processNeighbor(cx - 2, cy, cz, curr, px, py, pz, 2.0, openSet, allNodes);
         }

         if (!this.isTwoBlocksHighSafe(cx, cy, cz + 1) && this.isTwoBlocksHighSafe(cx, cy, cz + 2)) {
            this.processNeighbor(cx, cy, cz + 2, curr, px, py, pz, 2.0, openSet, allNodes);
         }

         if (!this.isTwoBlocksHighSafe(cx, cy, cz - 1) && this.isTwoBlocksHighSafe(cx, cy, cz - 2)) {
            this.processNeighbor(cx, cy, cz - 2, curr, px, py, pz, 2.0, openSet, allNodes);
         }
      }

   }

   private void processNeighbor(int x, int y, int z, Node curr, int tx, int ty, int tz, double costAdd, PriorityQueue openSet, Long2ObjectOpenHashMap allNodes) {
      if (this.isTwoBlocksHighSafe(x, y, z)) {
         long posLong = BlockPos.m_121882_(x, y, z);
         double cost = curr.g + costAdd;
         Node existing = (Node)allNodes.get(posLong);
         if (existing == null || !(existing.g <= cost)) {
            double f = cost + this.getHeuristic(x, y, z, tx, ty, tz);
            Node newNode = new Node(posLong, curr, cost, f);
            allNodes.put(posLong, newNode);
            openSet.add(newNode);
         }
      }
   }

   private boolean isTwoBlocksHighSafe(int x, int y, int z) {
      this.checkPos.m_122178_(x, y, z);
      Vec3 exactPos = this.toVec3(x, y, z);
      if (!this.collisionHelper.isSafe(exactPos)) {
         return false;
      } else {
         this.headPos.m_122178_(x, y + 1, z);
         BlockState headState = this.level.m_8055_(this.headPos);
         if (!headState.m_60812_(this.level, this.headPos).m_83281_()) {
            double headBlockMinY = (double)this.headPos.m_123342_() + headState.m_60812_(this.level, this.headPos).m_83288_(Axis.Y);
            if (headBlockMinY - exactPos.f_82480_ < 1.95) {
               return false;
            }
         }

         return true;
      }
   }

   private double getHeuristic(int x1, int y1, int z1, int x2, int y2, int z2) {
      double dx = (double)Math.abs(x1 - x2);
      double dy = (double)Math.abs(y1 - y2);
      double dz = (double)Math.abs(z1 - z2);
      return Math.sqrt(dx * dx + dy * dy + dz * dz) + dx + dy + dz;
   }

   private double getDistanceSq(int x1, int y1, int z1, int x2, int y2, int z2) {
      return (double)((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
   }

   private Vec3 toVec3(int x, int y, int z) {
      BlockPos bp = new BlockPos(x, y, z);
      double floorY = this.collisionHelper.getFloorHeight(bp);
      return new Vec3((double)x + 0.5, (double)y + floorY, (double)z + 0.5);
   }

   private List buildPath(Node bestNodeNearPlayer, Vec3 realStart, Vec3 realTarget, double maxStep) {
      if (bestNodeNearPlayer == null) {
         return new ArrayList();
      } else {
         List blockPath = new ArrayList();

         for(Node node = bestNodeNearPlayer; node != null; node = node.parent) {
            blockPath.add(node.posLong);
         }

         List vecPath = new ArrayList();
         vecPath.add(realStart);

         for(int i = 1; i < blockPath.size() - 1; ++i) {
            long posLong = (Long)blockPath.get(i);
            vecPath.add(this.toVec3(BlockPos.m_121983_(posLong), BlockPos.m_122008_(posLong), BlockPos.m_122015_(posLong)));
         }

         vecPath.add(realTarget);
         return this.simplify(vecPath, maxStep);
      }
   }

   private List simplify(List path, double maxStep) {
      if (path.size() <= 2) {
         return path;
      } else {
         List simple = new ArrayList();
         Vec3 lastPos = (Vec3)path.get(0);
         simple.add(lastPos);

         for(int i = 1; i < path.size(); ++i) {
            Vec3 current = (Vec3)path.get(i);
            if (i < path.size() - 1) {
               Vec3 next = (Vec3)path.get(i + 1);
               double distance = lastPos.m_82554_(next);
               if (distance > maxStep || !this.collisionHelper.canSweep(lastPos, next)) {
                  simple.add(current);
                  lastPos = current;
               }
            }
         }

         simple.add((Vec3)path.get(path.size() - 1));
         return simple;
      }
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
