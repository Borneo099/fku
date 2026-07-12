package lexis.Client.Goto;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.phys.Vec3;

public class WalkPathProcessor extends PathProcessor {
   private static final Minecraft MC = Minecraft.m_91087_();

   public WalkPathProcessor(ArrayList path) {
      super(path);
   }

   public void process() {
      if (MC.f_91074_ != null) {
         LocalPlayer player = MC.f_91074_;
         if (this.index >= this.path.size() - 1) {
            this.done = true;
            PathProcessor.releaseControls();
         }

         if (!this.done && this.index < this.path.size()) {
            BlockPos pos = BlockPos.m_274446_(player.m_20182_());
            PathPos nextPos = (PathPos)this.path.get(this.index);
            BlockPos frontPos = this.getFrontBlockPos(pos, nextPos);
            if (frontPos != null) {
               Block frontBlock = BlockUtils.getBlock(frontPos);
               Block frontBlockUp = BlockUtils.getBlock(frontPos.m_7494_());
               if (!this.isWalkableBlock(frontBlock) && !this.isWalkableBlock(frontBlockUp) && player.m_20096_()) {
                  player.m_6135_();
               }
            }

            if (nextPos.m_123342_() > pos.m_123342_() && player.m_20096_()) {
               player.m_6135_();
            }

            double distanceToGoal = player.m_20238_(Vec3.m_82512_((Vec3i)this.path.get(this.path.size() - 1)));
            if (distanceToGoal < 1.5) {
               this.releaseLocalControls();
               this.done = true;
            } else {
               int posIndex = this.path.indexOf(pos);
               if (posIndex == -1) {
                  ++this.ticksOffPath;
                  if (this.ticksOffPath > 40) {
                     this.releaseLocalControls();
                     this.done = true;
                     return;
                  }
               } else {
                  this.ticksOffPath = 0;
                  if (posIndex > this.index) {
                     this.index = posIndex + 1;
                     if (this.index >= this.path.size()) {
                        this.releaseLocalControls();
                        this.done = true;
                     }

                     return;
                  }
               }

               if (pos.equals(nextPos)) {
                  ++this.index;
                  if (this.index >= this.path.size()) {
                     this.releaseLocalControls();
                     this.done = true;
                  }

               } else {
                  PathProcessor.lockControls();
                  player.m_150110_().f_35935_ = false;
                  this.facePosition(nextPos);
                  float angleDiff = Math.abs(RotationUtils.getHorizontalAngleToLookVec(Vec3.m_82512_(nextPos)));
                  if (!(angleDiff > 90.0F)) {
                     this.resetMovementKeys();
                     if (pos.m_123341_() == nextPos.m_123341_() && pos.m_123343_() == nextPos.m_123343_()) {
                        if (pos.m_123342_() != nextPos.m_123342_()) {
                           if (pos.m_123342_() < nextPos.m_123342_()) {
                              Block block = BlockUtils.getBlock(pos);
                              if (!(block instanceof LadderBlock) && !(block instanceof VineBlock)) {
                                 if (this.index < this.path.size() - 1 && !nextPos.m_7494_().equals(this.path.get(this.index + 1))) {
                                    ++this.index;
                                 }

                                 if (MC.f_91066_.f_92089_ != null) {
                                    MC.f_91066_.f_92089_.m_7249_(true);
                                 }
                              } else if (MC.f_91066_.f_92085_ != null) {
                                 MC.f_91066_.f_92085_.m_7249_(true);
                              }
                           } else {
                              while(this.index < this.path.size() - 1 && ((PathPos)this.path.get(this.index)).m_7495_().equals(this.path.get(this.index + 1))) {
                                 ++this.index;
                              }

                              if (player.m_20096_() && MC.f_91066_.f_92085_ != null) {
                                 MC.f_91066_.f_92085_.m_7249_(true);
                              }
                           }
                        }
                     } else {
                        if (MC.f_91066_.f_92085_ != null) {
                           MC.f_91066_.f_92085_.m_7249_(true);
                        }

                        if ((this.index > 0 && ((PathPos)this.path.get(this.index - 1)).isJumping() || pos.m_123342_() < nextPos.m_123342_()) && MC.f_91066_.f_92089_ != null) {
                           MC.f_91066_.f_92089_.m_7249_(true);
                        }
                     }

                  }
               }
            }
         } else {
            this.releaseLocalControls();
            this.done = true;
         }
      }
   }

   private BlockPos getFrontBlockPos(BlockPos current, BlockPos next) {
      int dx = next.m_123341_() - current.m_123341_();
      int dz = next.m_123343_() - current.m_123343_();
      if (dx == 0 && dz == 0) {
         return null;
      } else {
         if (dx != 0) {
            dx = dx > 0 ? 1 : -1;
         }

         if (dz != 0) {
            dz = dz > 0 ? 1 : -1;
         }

         return current.m_7918_(dx, 0, dz);
      }
   }

   private boolean isWalkableBlock(Block block) {
      return block == null || block instanceof AirBlock || block instanceof FenceBlock || block instanceof WallBlock || block instanceof FenceGateBlock || block instanceof TrapDoorBlock || block instanceof SlabBlock || block instanceof StairBlock;
   }

   private void resetMovementKeys() {
      if (MC.f_91066_ != null) {
         if (MC.f_91066_.f_92085_ != null) {
            MC.f_91066_.f_92085_.m_7249_(false);
         }

         if (MC.f_91066_.f_92087_ != null) {
            MC.f_91066_.f_92087_.m_7249_(false);
         }

         if (MC.f_91066_.f_92086_ != null) {
            MC.f_91066_.f_92086_.m_7249_(false);
         }

         if (MC.f_91066_.f_92088_ != null) {
            MC.f_91066_.f_92088_.m_7249_(false);
         }

         if (MC.f_91066_.f_92089_ != null) {
            MC.f_91066_.f_92089_.m_7249_(false);
         }

         if (MC.f_91066_.f_92090_ != null) {
            MC.f_91066_.f_92090_.m_7249_(false);
         }

      }
   }

   private void releaseLocalControls() {
      this.resetMovementKeys();
      PathProcessor.releaseControls();
   }
}
