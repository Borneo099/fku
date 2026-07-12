package lexis.Client.Goto;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SoulSandBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class PathFinder {
   private final Minecraft MC = Minecraft.m_91087_();
   private final boolean invulnerable;
   private final boolean creativeFlying;
   protected final boolean flying;
   private final boolean immuneToFallDamage;
   private final boolean noWaterSlowdown;
   private final boolean jesus;
   private final boolean spider;
   protected boolean fallingAllowed = true;
   protected boolean divingAllowed = true;
   private final PathPos start;
   protected PathPos current;
   private final BlockPos goal;
   private final HashMap costMap = new HashMap();
   protected final HashMap prevPosMap = new HashMap();
   private final PathQueue queue = new PathQueue();
   public int thinkSpeed = 2048;
   public int thinkTime = 50;
   private int iterations;
   protected boolean done;
   protected boolean failed;
   private final ArrayList path = new ArrayList();

   public PathFinder(BlockPos goal) {
      if (this.MC.f_91074_ == null) {
         throw new IllegalStateException("Player is null");
      } else {
         this.invulnerable = this.MC.f_91074_.m_150110_().f_35937_;
         this.creativeFlying = this.MC.f_91074_.m_150110_().f_35935_;
         this.flying = this.creativeFlying;
         this.immuneToFallDamage = this.invulnerable;
         this.noWaterSlowdown = false;
         this.jesus = false;
         this.spider = false;
         if (this.MC.f_91074_.m_20096_()) {
            this.start = new PathPos(BlockPos.m_274561_(this.MC.f_91074_.m_20185_(), this.MC.f_91074_.m_20186_() + 0.5, this.MC.f_91074_.m_20189_()));
         } else {
            this.start = new PathPos(BlockPos.m_274446_(this.MC.f_91074_.m_20182_()));
         }

         this.goal = goal;
         this.costMap.put(this.start, 0.0F);
         this.queue.add(this.start, this.getHeuristic(this.start));
      }
   }

   public void think() {
      if (!this.done) {
         int i = 0;

         label42:
         while(i < this.thinkSpeed && !this.checkFailed()) {
            this.current = this.queue.poll();
            if (this.current == null) {
               this.failed = true;
               return;
            }

            if (this.checkDone()) {
               return;
            }

            Iterator var2 = this.getNeighbors(this.current).iterator();

            while(true) {
               PathPos next;
               float newCost;
               do {
                  if (!var2.hasNext()) {
                     ++i;
                     continue label42;
                  }

                  next = (PathPos)var2.next();
                  newCost = (Float)this.costMap.get(this.current) + this.getCost(this.current, next);
               } while(this.costMap.containsKey(next) && !((Float)this.costMap.get(next) > newCost));

               this.costMap.put(next, newCost);
               this.prevPosMap.put(next, this.current);
               this.queue.add(next, newCost + this.getHeuristic(next));
            }
         }

         this.iterations += this.thinkSpeed;
      }
   }

   protected boolean checkDone() {
      return this.done = this.goal.equals(this.current);
   }

   private boolean checkFailed() {
      return this.failed = this.queue.isEmpty() || this.iterations >= this.thinkSpeed * this.thinkTime;
   }

   private boolean isWater(BlockPos pos) {
      BlockState state = BlockUtils.getState(pos);
      return state != null && state.m_60819_().m_205070_(FluidTags.f_13131_);
   }

   private boolean isLava(BlockPos pos) {
      BlockState state = BlockUtils.getState(pos);
      return state != null && state.m_60819_().m_205070_(FluidTags.f_13132_);
   }

   public void renderPath(PoseStack poseStack, boolean debugMode, boolean depthTest) {
      if (!this.path.isEmpty()) {
         Vec3 cameraPos = Minecraft.m_91087_().f_91063_.m_109153_().m_90583_();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         if (!depthTest) {
            RenderSystem.disableDepthTest();
         }

         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.lineWidth(2.0F);
         poseStack.m_85836_();
         poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
         PathPos prev = null;

         Iterator var9;
         PathPos pos;
         for(var9 = this.path.iterator(); var9.hasNext(); prev = pos) {
            pos = (PathPos)var9.next();
            if (prev != null) {
               buffer.m_252986_(matrix, (float)prev.m_123341_() + 0.5F, (float)prev.m_123342_() + 0.5F, (float)prev.m_123343_() + 0.5F).m_6122_(0, 255, 0, 255).m_5752_();
               buffer.m_252986_(matrix, (float)pos.m_123341_() + 0.5F, (float)pos.m_123342_() + 0.5F, (float)pos.m_123343_() + 0.5F).m_6122_(0, 255, 0, 255).m_5752_();
            }
         }

         tesselator.m_85914_();
         buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
         var9 = this.path.iterator();

         while(var9.hasNext()) {
            pos = (PathPos)var9.next();
            this.renderNode(buffer, matrix, pos, 0, 255, 0, 255);
         }

         tesselator.m_85914_();
         poseStack.m_85849_();
         if (!depthTest) {
            RenderSystem.enableDepthTest();
         }

         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   private void renderNode(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, int r, int g, int b, int a) {
      float x = (float)pos.m_123341_() + 0.5F;
      float y = (float)pos.m_123342_() + 0.5F;
      float z = (float)pos.m_123343_() + 0.5F;
      float size = 0.3F;
      float x1 = x - size;
      float y1 = y - size;
      float z1 = z - size;
      float x2 = x + size;
      float y2 = y + size;
      float z2 = z + size;
      buffer.m_252986_(matrix, x1, y1, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z1).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y1, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x2, y2, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y1, z2).m_6122_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, x1, y2, z2).m_6122_(r, g, b, a).m_5752_();
   }

   private ArrayList getNeighbors(PathPos pos) {
      ArrayList neighbors = new ArrayList();
      int maxDistance = 512;
      if (Math.abs(this.goal.m_123341_() - pos.m_123341_()) <= maxDistance && Math.abs(this.goal.m_123343_() - pos.m_123343_()) <= maxDistance) {
         BlockPos north = pos.m_122012_();
         BlockPos east = pos.m_122029_();
         BlockPos south = pos.m_122019_();
         BlockPos west = pos.m_122024_();
         BlockPos northEast = north.m_122029_();
         BlockPos southEast = south.m_122029_();
         BlockPos southWest = south.m_122024_();
         BlockPos northWest = north.m_122024_();
         BlockPos up = pos.m_7494_();
         BlockPos down = pos.m_7495_();
         boolean flying = this.canFlyAt(pos);
         boolean onGround = this.canBeSolid(down);
         if (flying || onGround || pos.isJumping() || this.canMoveSidewaysInMidairAt(pos) || this.canClimbUpAt(pos.m_7495_())) {
            if (this.checkHorizontalMovement(pos, north)) {
               neighbors.add(new PathPos(north));
            }

            if (this.checkHorizontalMovement(pos, east)) {
               neighbors.add(new PathPos(east));
            }

            if (this.checkHorizontalMovement(pos, south)) {
               neighbors.add(new PathPos(south));
            }

            if (this.checkHorizontalMovement(pos, west)) {
               neighbors.add(new PathPos(west));
            }

            if (this.checkDiagonalMovement(pos, Direction.NORTH, Direction.EAST)) {
               neighbors.add(new PathPos(northEast));
            }

            if (this.checkDiagonalMovement(pos, Direction.SOUTH, Direction.EAST)) {
               neighbors.add(new PathPos(southEast));
            }

            if (this.checkDiagonalMovement(pos, Direction.SOUTH, Direction.WEST)) {
               neighbors.add(new PathPos(southWest));
            }

            if (this.checkDiagonalMovement(pos, Direction.NORTH, Direction.WEST)) {
               neighbors.add(new PathPos(northWest));
            }
         }

         if (pos.m_123342_() < this.MC.f_91073_.m_151558_() && this.canGoThrough(up.m_7494_()) && (flying || onGround || this.canClimbUpAt(pos)) && (flying || this.canClimbUpAt(pos) || this.goal.equals(up) || this.canSafelyStandOn(north) || this.canSafelyStandOn(east) || this.canSafelyStandOn(south) || this.canSafelyStandOn(west)) && (this.divingAllowed || !this.isWater(up.m_7494_()))) {
            neighbors.add(new PathPos(up, onGround));
         }

         if (pos.m_123342_() > this.MC.f_91073_.m_141937_() && this.canGoThrough(down) && this.canGoAbove(down.m_7495_()) && (flying || this.canFallBelow(pos)) && (this.divingAllowed || !this.isWater(pos))) {
            neighbors.add(new PathPos(down));
         }

         return neighbors;
      } else {
         return neighbors;
      }
   }

   private boolean checkHorizontalMovement(BlockPos current, BlockPos next) {
      return this.isPassable(next) && (this.canFlyAt(current) || this.canGoThrough(next.m_7495_()) || this.canSafelyStandOn(next.m_7495_()));
   }

   private boolean checkDiagonalMovement(BlockPos current, Direction dir1, Direction dir2) {
      BlockPos horizontal1 = current.m_121945_(dir1);
      BlockPos horizontal2 = current.m_121945_(dir2);
      BlockPos next = horizontal1.m_121945_(dir2);
      return this.isPassable(horizontal1) && this.isPassable(horizontal2) && this.checkHorizontalMovement(current, next);
   }

   protected boolean isPassable(BlockPos pos) {
      if (!this.canGoThrough(pos)) {
         return false;
      } else {
         BlockPos up = pos.m_7494_();
         if (!this.canGoThrough(up)) {
            return false;
         } else if (!this.canGoAbove(pos.m_7495_())) {
            return false;
         } else {
            return this.divingAllowed || !this.isWater(up);
         }
      }
   }

   protected boolean canBeSolid(BlockPos pos) {
      BlockState state = BlockUtils.getState(pos);
      if (state == null) {
         return false;
      } else {
         Block block = state.m_60734_();
         return state.m_280296_() && !(block instanceof SignBlock) || block instanceof LadderBlock || this.jesus && (this.isWater(pos) || this.isLava(pos));
      }
   }

   private boolean canGoThrough(BlockPos pos) {
      if (!this.MC.f_91073_.m_46749_(pos)) {
         return false;
      } else {
         BlockState state = BlockUtils.getState(pos);
         if (state == null) {
            return false;
         } else {
            Block block = state.m_60734_();
            if (state.m_280296_() && !(block instanceof SignBlock)) {
               return false;
            } else if (!(block instanceof TripWireBlock) && !(block instanceof PressurePlateBlock)) {
               return this.invulnerable || !this.isLava(pos) && state.m_60819_().m_76178_();
            } else {
               return false;
            }
         }
      }
   }

   private boolean canGoAbove(BlockPos pos) {
      Block block = BlockUtils.getBlock(pos);
      return !(block instanceof FenceBlock) && !(block instanceof WallBlock) && !(block instanceof FenceGateBlock);
   }

   private boolean canSafelyStandOn(BlockPos pos) {
      BlockState state = BlockUtils.getState(pos);
      if (state == null) {
         return false;
      } else if (!this.canBeSolid(pos)) {
         return false;
      } else {
         return this.invulnerable || !this.isLava(pos) && !(state.m_60734_() instanceof CactusBlock);
      }
   }

   private boolean canFallBelow(PathPos pos) {
      BlockPos down2 = pos.m_6625_(2);
      if (this.fallingAllowed && this.canGoThrough(down2)) {
         return true;
      } else if (!this.canSafelyStandOn(down2)) {
         return false;
      } else if (this.immuneToFallDamage && this.fallingAllowed) {
         return true;
      } else if (BlockUtils.getBlock(down2) instanceof SlimeBlock && this.fallingAllowed) {
         return true;
      } else {
         PathPos prevPos = pos;

         for(int i = 0; i <= (this.fallingAllowed ? 3 : 1); ++i) {
            if (prevPos == null) {
               return true;
            }

            if (!pos.m_6625_(i).equals(prevPos)) {
               return true;
            }

            Block prevBlock = BlockUtils.getBlock(prevPos);
            BlockState prevState = BlockUtils.getState(prevPos);
            if (prevState != null && (this.isWater(prevPos) || prevBlock instanceof LadderBlock || prevBlock instanceof VineBlock || prevBlock instanceof WebBlock)) {
               return true;
            }

            prevPos = (PathPos)this.prevPosMap.get(prevPos);
         }

         return false;
      }
   }

   private boolean canFlyAt(BlockPos pos) {
      return this.flying || !this.noWaterSlowdown && this.isWater(pos);
   }

   private boolean canClimbUpAt(BlockPos pos) {
      Block block = BlockUtils.getBlock(pos);
      if (!this.spider && !(block instanceof LadderBlock) && !(block instanceof VineBlock)) {
         return false;
      } else {
         BlockPos up = pos.m_7494_();
         return this.canBeSolid(pos.m_122012_()) || this.canBeSolid(pos.m_122029_()) || this.canBeSolid(pos.m_122019_()) || this.canBeSolid(pos.m_122024_()) || this.canBeSolid(up.m_122012_()) || this.canBeSolid(up.m_122029_()) || this.canBeSolid(up.m_122019_()) || this.canBeSolid(up.m_122024_());
      }
   }

   private boolean canMoveSidewaysInMidairAt(BlockPos pos) {
      Block blockFeet = BlockUtils.getBlock(pos);
      BlockState stateFeet = BlockUtils.getState(pos);
      if (stateFeet != null && !stateFeet.m_280296_() && !(blockFeet instanceof LadderBlock) && !(blockFeet instanceof VineBlock) && !(blockFeet instanceof WebBlock)) {
         BlockState stateHead = BlockUtils.getState(pos.m_7494_());
         return stateHead != null && (stateHead.m_280296_() || BlockUtils.getBlock(pos.m_7494_()) instanceof WebBlock);
      } else {
         return true;
      }
   }

   private float getCost(BlockPos current, BlockPos next) {
      float cost = 1.0F;
      if (this.isWater(next)) {
         cost *= 2.0F;
      }

      if (BlockUtils.getBlock(next) instanceof SoulSandBlock) {
         cost *= 2.0F;
      }

      if (next.m_123342_() > current.m_123342_()) {
         cost *= 1.2F;
      }

      return cost;
   }

   private float getHeuristic(BlockPos pos) {
      float dx = (float)Math.abs(pos.m_123341_() - this.goal.m_123341_());
      float dy = (float)Math.abs(pos.m_123342_() - this.goal.m_123342_());
      float dz = (float)Math.abs(pos.m_123343_() - this.goal.m_123343_());
      return 1.001F * (dx + dy + dz - 0.58578646F * Math.min(dx, dz));
   }

   public boolean isDone() {
      return this.done;
   }

   public boolean isFailed() {
      return this.failed;
   }

   public ArrayList formatPath() {
      this.path.clear();

      for(PathPos pos = this.current; pos != null; pos = (PathPos)this.prevPosMap.get(pos)) {
         this.path.add(0, pos);
      }

      return this.path;
   }

   public BlockPos getGoal() {
      return this.goal;
   }

   public PathPos getCurrentPos() {
      return this.current;
   }

   public int countProcessedBlocks() {
      return this.prevPosMap.size();
   }

   public int getQueueSize() {
      return this.queue.size();
   }

   public float getCost(BlockPos pos) {
      return (Float)this.costMap.get(pos);
   }

   public List getPath() {
      return Collections.unmodifiableList(this.path);
   }

   public PathProcessor getProcessor() {
      return new WalkPathProcessor(this.path);
   }
}
