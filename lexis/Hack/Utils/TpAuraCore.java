package lexis.Hack.Utils;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lexis.Hack.Utils.pathfinding.AStarPathFinder;
import lexis.Hack.Utils.pathfinding.CollisionHelper;
import lexis.Hack.Utils.pathfinding.PathRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TpAuraCore {
   private final AStarPathFinder pathFinder;
   private final CollisionHelper collisionHelper;
   private final PathRenderer pathRenderer = new PathRenderer();
   private final ExecutorService pool = Executors.newFixedThreadPool(1);
   private volatile List currentPath = new ArrayList();
   public Vec3 desyncPos = null;
   private final AtomicBoolean isCalculating = new AtomicBoolean(false);
   private final Player localPlayer;

   public TpAuraCore(Level level, Player player) {
      this.localPlayer = player;
      this.pathFinder = new AStarPathFinder(level);
      this.collisionHelper = new CollisionHelper(level);
   }

   public Vec3 getBestAttackPos(Entity target, double reach) {
      Vec3 targetPos = target.m_20182_();
      if (this.collisionHelper.isSafe(targetPos)) {
         return targetPos;
      } else {
         Vec3 playerPos = this.localPlayer.m_20182_();
         Vec3 dir = playerPos.m_82546_(targetPos).m_82541_();

         for(double d = 0.5; d < reach - 0.5; d += 0.5) {
            Vec3 testPos = targetPos.m_82549_(dir.m_82490_(d));
            double floorY = this.collisionHelper.getFloorHeight(BlockPos.m_274446_(testPos));
            Vec3 fixedTestPos = new Vec3(testPos.f_82479_, Math.floor(testPos.f_82480_) + floorY, testPos.f_82481_);
            if (this.collisionHelper.isSafe(fixedTestPos)) {
               return fixedTestPos;
            }
         }

         return targetPos;
      }
   }

   public void updatePathfinding(Vec3 start, Entity target) {
      if (!this.isCalculating.get()) {
         Vec3 finalDest = this.getBestAttackPos(target, 4.0);
         List takeoffPath = new ArrayList();
         Vec3 aStarStart = start;

         for(double h = 1.0; h <= 3.0; ++h) {
            Vec3 checkPos = start.m_82520_(0.0, h, 0.0);
            if (!this.collisionHelper.isSafe(checkPos)) {
               break;
            }

            takeoffPath.add(checkPos);
            aStarStart = checkPos;
         }

         List prefix = new ArrayList(takeoffPath);
         this.isCalculating.set(true);
         CompletableFuture.supplyAsync(() -> {
            return this.pathFinder.findPath(aStarStart, finalDest, 9.0);
         }, this.pool).thenAccept((path) -> {
            if (path != null) {
               List fullPath = new ArrayList();
               fullPath.add(start);
               fullPath.addAll(prefix);
               fullPath.addAll(path);
               this.currentPath = fullPath;
            }

            this.isCalculating.set(false);
         });
      }
   }

   public void updatePathfinding(Vec3 start, Vec3 targetPos, double maxStep) {
      if (!this.isCalculating.get()) {
         this.isCalculating.set(true);
         CompletableFuture.supplyAsync(() -> {
            return this.pathFinder.findPath(start, targetPos, maxStep);
         }, this.pool).thenAccept((path) -> {
            if (path != null) {
               this.currentPath = new ArrayList(path);
            }

            this.isCalculating.set(false);
         });
      }
   }

   public List getEfficientPath(double maxStep) {
      return this.compressPath(this.currentPath, maxStep);
   }

   public List getChunkedFromSnapshot(List path, double maxStep) {
      return this.compressPath(path, maxStep);
   }

   private List compressPath(List inputPath, double maxStep) {
      if (inputPath != null && !inputPath.isEmpty()) {
         List raw = new ArrayList(inputPath);
         List corners = new ArrayList();
         corners.add((Vec3)raw.get(0));

         Vec3 p1;
         Vec3 p2;
         for(int i = 1; i < raw.size() - 1; ++i) {
            Vec3 prev = (Vec3)raw.get(i - 1);
            Vec3 curr = (Vec3)raw.get(i);
            Vec3 next = (Vec3)raw.get(i + 1);
            p1 = curr.m_82546_(prev).m_82541_();
            p2 = next.m_82546_(curr).m_82541_();
            if (p1.m_82557_(p2) > 1.0E-4) {
               corners.add(curr);
            }
         }

         if (raw.size() > 1) {
            corners.add((Vec3)raw.get(raw.size() - 1));
         }

         List finalPath = new ArrayList();
         if (corners.isEmpty()) {
            return finalPath;
         } else {
            finalPath.add((Vec3)corners.get(0));

            int furthest;
            for(int i = 0; i < corners.size() - 1; i = furthest) {
               furthest = i + 1;

               for(int j = i + 1; j < corners.size(); furthest = j++) {
                  p1 = (Vec3)corners.get(i);
                  p2 = (Vec3)corners.get(j);
                  if (p1.m_82554_(p2) > maxStep - 0.05 || !this.collisionHelper.canSweep(p1, p2)) {
                     break;
                  }
               }

               finalPath.add((Vec3)corners.get(furthest));
            }

            return finalPath;
         }
      } else {
         return new ArrayList();
      }
   }

   public double getFloorHeightAt(Vec3 pos) {
      return this.collisionHelper.getFloorHeight(BlockPos.m_274446_(pos));
   }

   public boolean isOnGround(Vec3 pos) {
      BlockPos below = BlockPos.m_274561_(pos.f_82479_, pos.f_82480_ - 0.1, pos.f_82481_);
      return !this.localPlayer.m_9236_().m_8055_(below).m_60795_();
   }

   public void setAirPath(boolean b) {
      this.pathFinder.setAirPath(b);
   }

   public void setHClip(boolean b) {
      this.pathFinder.setHClip(b);
   }

   public void setAttackRange(double range) {
      this.pathFinder.setAttackRange(range);
   }

   public List getCurrentPath() {
      return new ArrayList(this.currentPath);
   }

   public void renderPath(PoseStack poseStack, List path, Color color, double step) {
      this.pathRenderer.renderFixedSnapshot(poseStack, path, color, step, this);
   }

   public void cleanup() {
      this.pool.shutdownNow();
   }
}
