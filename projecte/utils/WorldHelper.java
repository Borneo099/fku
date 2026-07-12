package moze_intel.projecte.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import moze_intel.projecte.PECore;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.HangingRootsBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.level.block.NetherSproutsBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.NetherrackBlock;
import net.minecraft.world.level.block.NyliumBlock;
import net.minecraft.world.level.block.RootsBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WorldHelper {
   private static final Predicate SWRG_REPEL_PREDICATE = (entity) -> {
      return validRepelEntity(entity, PETags.Entities.BLACKLIST_SWRG);
   };
   private static final Predicate INTERDICTION_REPEL_PREDICATE = (entity) -> {
      return validRepelEntity(entity, PETags.Entities.BLACKLIST_INTERDICTION);
   };
   private static final Predicate INTERDICTION_REPEL_HOSTILE_PREDICATE = (entity) -> {
      return validRepelEntity(entity, PETags.Entities.BLACKLIST_INTERDICTION) && (entity instanceof Enemy || entity instanceof Projectile);
   };

   public static void createLootDrop(List drops, Level level, BlockPos pos) {
      createLootDrop(drops, level, (double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_());
   }

   public static void createLootDrop(List drops, Level level, double x, double y, double z) {
      if (!drops.isEmpty()) {
         ItemHelper.compactItemListNoStacksize(drops);
         Iterator var8 = drops.iterator();

         while(var8.hasNext()) {
            ItemStack drop = (ItemStack)var8.next();
            level.m_7967_(new ItemEntity(level, x, y, z, drop));
         }
      }

   }

   public static void createNovaExplosion(Level level, Entity exploder, double x, double y, double z, float power) {
      NovaExplosion explosion = new NovaExplosion(level, exploder, x, y, z, power, true, BlockInteraction.DESTROY);
      if (!MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Start(level, explosion))) {
         explosion.m_46061_();
         explosion.m_46075_(true);
      }

   }

   public static void drainFluid(Level level, BlockPos pos, BlockState state, Fluid toMatch) {
      Block block = state.m_60734_();
      if (block instanceof IFluidBlock fluidBlock) {
         if (fluidBlock.getFluid().m_6212_(toMatch)) {
            fluidBlock.drain(level, pos, FluidAction.EXECUTE);
            return;
         }
      }

      if (block instanceof BucketPickup bucketPickup) {
         bucketPickup.m_142598_(level, pos, state);
      }

   }

   public static void dropInventory(IItemHandler inv, Level level, BlockPos pos) {
      if (inv != null) {
         for(int i = 0; i < inv.getSlots(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.m_41619_()) {
               level.m_7967_(new ItemEntity(level, (double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_(), stack));
            }
         }

      }
   }

   public static void extinguishNearby(Level level, Player player) {
      BlockPos.m_121990_(player.m_20183_().m_7918_(-1, -1, -1), player.m_20183_().m_7918_(1, 1, 1)).forEach((pos) -> {
         pos = pos.m_7949_();
         if (level.m_8055_(pos).m_60734_() == Blocks.f_50083_ && PlayerHelper.hasBreakPermission((ServerPlayer)player, pos)) {
            level.m_7471_(pos, false);
         }

      });
   }

   public static void freezeInBoundingBox(Level level, AABB box, Player player, boolean random) {
      Iterator var4 = getPositionsFromBox(box).iterator();

      while(true) {
         while(var4.hasNext()) {
            BlockPos pos = (BlockPos)var4.next();
            BlockState state = level.m_8055_(pos);
            Block b = state.m_60734_();
            pos = pos.m_7949_();
            if (b == Blocks.f_49990_ && (!random || level.f_46441_.m_188503_(128) == 0)) {
               if (player != null) {
                  PlayerHelper.checkedReplaceBlock((ServerPlayer)player, pos, Blocks.f_50126_.m_49966_());
               } else {
                  level.m_46597_(pos, Blocks.f_50126_.m_49966_());
               }
            } else if (Block.m_49918_(state.m_60812_(level, pos.m_7495_()), Direction.UP)) {
               BlockPos up = pos.m_7494_();
               BlockState stateUp = level.m_8055_(up);
               BlockState newState = null;
               if (stateUp.m_60795_() && (!random || level.f_46441_.m_188503_(128) == 0)) {
                  newState = Blocks.f_50125_.m_49966_();
               } else if (stateUp.m_60734_() == Blocks.f_50125_ && (Integer)stateUp.m_61143_(SnowLayerBlock.f_56581_) < 8 && level.f_46441_.m_188503_(512) == 0) {
                  newState = (BlockState)stateUp.m_61124_(SnowLayerBlock.f_56581_, (Integer)stateUp.m_61143_(SnowLayerBlock.f_56581_) + 1);
               }

               if (newState != null) {
                  if (player != null) {
                     PlayerHelper.checkedReplaceBlock((ServerPlayer)player, up, newState);
                  } else {
                     level.m_46597_(up, newState);
                  }
               }
            }
         }

         return;
      }
   }

   public static boolean isLiquidContainerForFluid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
      Block var5 = state.m_60734_();
      boolean var10000;
      if (var5 instanceof LiquidBlockContainer liquidBlockContainer) {
         if (liquidBlockContainer.m_6044_(level, pos, state, fluid)) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   public static void placeFluid(@Nullable ServerPlayer player, Level level, BlockPos pos, Direction sideHit, FlowingFluid fluid, boolean checkWaterVaporize) {
      if (isLiquidContainerForFluid(level, pos, level.m_8055_(pos), fluid)) {
         placeFluid(player, level, pos, fluid, checkWaterVaporize);
      } else {
         placeFluid(player, level, pos.m_121945_(sideHit), fluid, checkWaterVaporize);
      }

   }

   public static void placeFluid(@Nullable ServerPlayer player, Level level, BlockPos pos, FlowingFluid fluid, boolean checkWaterVaporize) {
      BlockState blockState = level.m_8055_(pos);
      if (checkWaterVaporize && level.m_6042_().f_63857_() && fluid.m_205067_(FluidTags.f_13131_)) {
         level.m_5594_((Player)null, pos, SoundEvents.f_11937_, SoundSource.PLAYERS, 0.5F, 2.6F + (level.f_46441_.m_188501_() - level.f_46441_.m_188501_()) * 0.8F);

         for(int l = 0; l < 8; ++l) {
            level.m_7106_(ParticleTypes.f_123755_, (double)pos.m_123341_() + Math.random(), (double)pos.m_123342_() + Math.random(), (double)pos.m_123343_() + Math.random(), 0.0, 0.0, 0.0);
         }
      } else if (isLiquidContainerForFluid(level, pos, blockState, fluid)) {
         ((LiquidBlockContainer)blockState.m_60734_()).m_7361_(level, pos, blockState, fluid.m_76068_(false));
         level.m_142346_(player, GameEvent.f_157769_, pos);
      } else {
         if (blockState.m_60722_(fluid) && !blockState.m_278721_()) {
            level.m_46961_(pos, true);
         }

         if (player == null) {
            level.m_46597_(pos, fluid.m_76145_().m_76188_());
            level.m_142346_((Entity)null, GameEvent.f_157769_, pos);
         } else if (PlayerHelper.checkedPlaceBlock(player, pos, fluid.m_76145_().m_76188_())) {
            level.m_142346_(player, GameEvent.f_157769_, pos);
         }
      }

   }

   public static @Nullable IItemHandler getItemHandler(@NotNull BlockEntity blockEntity, @Nullable Direction direction) {
      Optional capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).resolve();
      if (capability.isPresent()) {
         return (IItemHandler)capability.get();
      } else if (blockEntity instanceof WorldlyContainer) {
         WorldlyContainer container = (WorldlyContainer)blockEntity;
         return new SidedInvWrapper(container, direction);
      } else if (blockEntity instanceof Container) {
         Container container = (Container)blockEntity;
         return new InvWrapper(container);
      } else {
         return null;
      }
   }

   public static AABB getBroadDeepBox(BlockPos pos, Direction direction, int offset) {
      AABB var10000;
      switch (direction) {
         case EAST:
            var10000 = new AABB((double)(pos.m_123341_() - offset), (double)(pos.m_123342_() - offset), (double)(pos.m_123343_() - offset), (double)pos.m_123341_(), (double)(pos.m_123342_() + offset), (double)(pos.m_123343_() + offset));
            break;
         case WEST:
            var10000 = new AABB((double)pos.m_123341_(), (double)(pos.m_123342_() - offset), (double)(pos.m_123343_() - offset), (double)(pos.m_123341_() + offset), (double)(pos.m_123342_() + offset), (double)(pos.m_123343_() + offset));
            break;
         case UP:
            var10000 = new AABB((double)(pos.m_123341_() - offset), (double)(pos.m_123342_() - offset), (double)(pos.m_123343_() - offset), (double)(pos.m_123341_() + offset), (double)pos.m_123342_(), (double)(pos.m_123343_() + offset));
            break;
         case DOWN:
            var10000 = new AABB((double)(pos.m_123341_() - offset), (double)pos.m_123342_(), (double)(pos.m_123343_() - offset), (double)(pos.m_123341_() + offset), (double)(pos.m_123342_() + offset), (double)(pos.m_123343_() + offset));
            break;
         case SOUTH:
            var10000 = new AABB((double)(pos.m_123341_() - offset), (double)(pos.m_123342_() - offset), (double)(pos.m_123343_() - offset), (double)(pos.m_123341_() + offset), (double)(pos.m_123342_() + offset), (double)pos.m_123343_());
            break;
         case NORTH:
            var10000 = new AABB((double)(pos.m_123341_() - offset), (double)(pos.m_123342_() - offset), (double)pos.m_123343_(), (double)(pos.m_123341_() + offset), (double)(pos.m_123342_() + offset), (double)(pos.m_123343_() + offset));
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static AABB getDeepBox(BlockPos pos, Direction direction, int depth) {
      AABB var10000;
      switch (direction) {
         case EAST:
            var10000 = new AABB((double)(pos.m_123341_() - depth), (double)(pos.m_123342_() - 1), (double)(pos.m_123343_() - 1), (double)pos.m_123341_(), (double)(pos.m_123342_() + 1), (double)(pos.m_123343_() + 1));
            break;
         case WEST:
            var10000 = new AABB((double)pos.m_123341_(), (double)(pos.m_123342_() - 1), (double)(pos.m_123343_() - 1), (double)(pos.m_123341_() + depth), (double)(pos.m_123342_() + 1), (double)(pos.m_123343_() + 1));
            break;
         case UP:
            var10000 = new AABB((double)(pos.m_123341_() - 1), (double)(pos.m_123342_() - depth), (double)(pos.m_123343_() - 1), (double)(pos.m_123341_() + 1), (double)pos.m_123342_(), (double)(pos.m_123343_() + 1));
            break;
         case DOWN:
            var10000 = new AABB((double)(pos.m_123341_() - 1), (double)pos.m_123342_(), (double)(pos.m_123343_() - 1), (double)(pos.m_123341_() + 1), (double)(pos.m_123342_() + depth), (double)(pos.m_123343_() + 1));
            break;
         case SOUTH:
            var10000 = new AABB((double)(pos.m_123341_() - 1), (double)(pos.m_123342_() - 1), (double)(pos.m_123343_() - depth), (double)(pos.m_123341_() + 1), (double)(pos.m_123342_() + 1), (double)pos.m_123343_());
            break;
         case NORTH:
            var10000 = new AABB((double)(pos.m_123341_() - 1), (double)(pos.m_123342_() - 1), (double)pos.m_123343_(), (double)(pos.m_123341_() + 1), (double)(pos.m_123342_() + 1), (double)(pos.m_123343_() + depth));
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static AABB getBroadBox(BlockPos pos, Direction direction, int size) {
      AABB var10000;
      switch (direction) {
         case EAST:
         case WEST:
            var10000 = new AABB((double)pos.m_123341_(), (double)(pos.m_123342_() - size), (double)(pos.m_123343_() - size), (double)pos.m_123341_(), (double)(pos.m_123342_() + size), (double)(pos.m_123343_() + size));
            break;
         case UP:
         case DOWN:
            var10000 = new AABB((double)(pos.m_123341_() - size), (double)pos.m_123342_(), (double)(pos.m_123343_() - size), (double)(pos.m_123341_() + size), (double)pos.m_123342_(), (double)(pos.m_123343_() + size));
            break;
         case SOUTH:
         case NORTH:
            var10000 = new AABB((double)(pos.m_123341_() - size), (double)(pos.m_123342_() - size), (double)pos.m_123343_(), (double)(pos.m_123341_() + size), (double)(pos.m_123342_() + size), (double)pos.m_123343_());
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static AABB getFlatYBox(BlockPos pos, int offset) {
      return new AABB((double)(pos.m_123341_() - offset), (double)pos.m_123342_(), (double)(pos.m_123343_() - offset), (double)(pos.m_123341_() + offset), (double)pos.m_123342_(), (double)(pos.m_123343_() + offset));
   }

   public static Iterable getPositionsFromBox(AABB box) {
      return getPositionsFromBox(BlockPos.m_274561_(box.f_82288_, box.f_82289_, box.f_82290_), BlockPos.m_274561_(box.f_82291_, box.f_82292_, box.f_82293_));
   }

   public static Iterable getPositionsFromBox(BlockPos corner1, BlockPos corner2) {
      return () -> {
         return BlockPos.m_121990_(corner1, corner2).iterator();
      };
   }

   public static List getBlockEntitiesWithinAABB(Level level, AABB bBox) {
      List list = new ArrayList();
      Iterator var3 = getPositionsFromBox(bBox).iterator();

      while(var3.hasNext()) {
         BlockPos pos = (BlockPos)var3.next();
         BlockEntity blockEntity = getBlockEntity(level, pos);
         if (blockEntity != null) {
            list.add(blockEntity);
         }
      }

      return list;
   }

   public static void gravitateEntityTowards(Entity ent, double x, double y, double z) {
      double dX = x - ent.m_20185_();
      double dY = y - ent.m_20186_();
      double dZ = z - ent.m_20189_();
      double dist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
      double vel = 1.0 - dist / 15.0;
      if (vel > 0.0) {
         vel *= vel;
         ent.m_20256_(ent.m_20184_().m_82520_(dX / dist * vel * 0.1, dY / dist * vel * 0.2, dZ / dist * vel * 0.1));
      }

   }

   public static void growNearbyRandomly(boolean harvest, Level level, BlockPos pos, Player player) {
      if (level instanceof ServerLevel serverLevel) {
         boolean grewWater = false;
         int chance = harvest ? 16 : 32;
         Iterator var7 = getPositionsFromBox(pos.m_7918_(-5, -3, -5), pos.m_7918_(5, 3, 5)).iterator();

         while(true) {
            BlockPos currentPos;
            BlockState state;
            Block crop;
            do {
               do {
                  label124:
                  do {
                     while(true) {
                        while(true) {
                           while(var7.hasNext()) {
                              currentPos = (BlockPos)var7.next();
                              currentPos = currentPos.m_7949_();
                              state = serverLevel.m_8055_(currentPos);
                              crop = state.m_60734_();
                              if (!(crop instanceof IForgeShearable) && !(crop instanceof FlowerBlock) && !(crop instanceof DoublePlantBlock) && !(crop instanceof RootsBlock) && !(crop instanceof NetherSproutsBlock) && !(crop instanceof HangingRootsBlock)) {
                                 if (crop instanceof BonemealableBlock) {
                                    BonemealableBlock growable = (BonemealableBlock)crop;
                                    if (!growable.m_7370_(serverLevel, currentPos, state, false)) {
                                       continue label124;
                                    }

                                    if ((ProjectEConfig.server.items.harvBandGrass.get() || !isGrassLikeBlock(crop)) && serverLevel.f_46441_.m_188503_(chance) == 0) {
                                       growable.m_214148_(serverLevel, serverLevel.f_46441_, currentPos, state);
                                       level.m_46796_(1505, currentPos, 0);
                                    }
                                 } else if (!(crop instanceof IPlantable)) {
                                    if (!grewWater && serverLevel.f_46441_.m_188503_(512) == 0 && growWaterPlant(serverLevel, currentPos, state, (Direction)null)) {
                                       level.m_46796_(1505, currentPos, 0);
                                       grewWater = true;
                                    }
                                 } else {
                                    int i;
                                    if (serverLevel.f_46441_.m_188503_(chance / 4) == 0) {
                                       for(i = 0; i < (harvest ? 8 : 4); ++i) {
                                          state.m_222972_(serverLevel, currentPos, serverLevel.f_46441_);
                                       }
                                    }

                                    if (harvest) {
                                       if (crop != Blocks.f_50130_ && crop != Blocks.f_50128_) {
                                          if (crop == Blocks.f_50200_ && (Integer)state.m_61143_(NetherWartBlock.f_54967_) == 3) {
                                             harvestBlock(serverLevel, currentPos, (ServerPlayer)player);
                                          }
                                       } else if (serverLevel.m_8055_(currentPos.m_7494_()).m_60713_(crop) && serverLevel.m_8055_(currentPos.m_6630_(2)).m_60713_(crop)) {
                                          for(i = crop == Blocks.f_50130_ ? 1 : 0; i < 3; ++i) {
                                             harvestBlock(serverLevel, currentPos.m_6630_(i), (ServerPlayer)player);
                                          }
                                       }
                                    }
                                 }
                              } else if (harvest) {
                                 harvestBlock(serverLevel, currentPos, (ServerPlayer)player);
                              }
                           }

                           return;
                        }
                     }
                  } while(!harvest);
               } while(state.m_204336_(PETags.Blocks.BLACKLIST_HARVEST));
            } while(leaveBottomBlock(crop) && !serverLevel.m_8055_(currentPos.m_7495_()).m_60713_(crop));

            harvestBlock(serverLevel, currentPos, (ServerPlayer)player);
         }
      }
   }

   private static boolean leaveBottomBlock(Block crop) {
      return crop == Blocks.f_50576_ || crop == Blocks.f_50571_;
   }

   private static boolean isGrassLikeBlock(Block crop) {
      return crop instanceof GrassBlock || crop instanceof NyliumBlock || crop instanceof NetherrackBlock || crop instanceof MossBlock;
   }

   private static void harvestBlock(Level level, BlockPos pos, @Nullable ServerPlayer player) {
      if (player == null || PlayerHelper.hasBreakPermission(player, pos)) {
         level.m_46953_(pos, true, player);
      }

   }

   public static boolean growWaterPlant(ServerLevel level, BlockPos pos, BlockState state, @Nullable Direction side) {
      boolean success = false;
      if (state.m_60713_(Blocks.f_49990_) && state.m_60819_().m_76186_() == 8) {
         RandomSource random = level.m_213780_();

         label73:
         for(int i = 0; i < 128; ++i) {
            BlockPos blockpos = pos;

            for(int j = 0; j < i / 16; ++j) {
               blockpos = blockpos.m_7918_(random.m_188503_(3) - 1, (random.m_188503_(3) - 1) * random.m_188503_(3) / 2, random.m_188503_(3) - 1);
               if (level.m_8055_(blockpos).m_60838_(level, blockpos)) {
                  continue label73;
               }
            }

            BlockState newState = Blocks.f_50037_.m_49966_();
            Holder biome = level.m_204166_(blockpos);
            if (biome.m_203565_(Biomes.f_48166_)) {
               if (i == 0 && side != null && side.m_122434_().m_122479_()) {
                  newState = getRandomState(BlockTags.f_13052_, random, newState);
                  if (newState.m_61138_(BaseCoralWallFanBlock.f_49192_)) {
                     newState = (BlockState)newState.m_61124_(BaseCoralWallFanBlock.f_49192_, side);
                  }
               } else if (random.m_188503_(4) == 0) {
                  newState = getRandomState(BlockTags.f_13050_, random, newState);
               }
            }

            if (newState.m_204338_(BlockTags.f_13052_, (s) -> {
               return s.m_61138_(BaseCoralWallFanBlock.f_49192_);
            })) {
               for(int k = 0; !newState.m_60710_(level, blockpos) && k < 4; ++k) {
                  newState = (BlockState)newState.m_61124_(BaseCoralWallFanBlock.f_49192_, Plane.HORIZONTAL.m_235690_(random));
               }
            }

            if (newState.m_60710_(level, blockpos)) {
               BlockState stateToReplace = level.m_8055_(blockpos);
               if (stateToReplace.m_60713_(Blocks.f_49990_) && stateToReplace.m_60819_().m_76186_() == 8) {
                  level.m_46597_(blockpos, newState);
                  success = true;
               } else if (stateToReplace.m_60713_(Blocks.f_50037_) && random.m_188503_(10) == 0) {
                  ((BonemealableBlock)Blocks.f_50037_).m_214148_(level, random, blockpos, stateToReplace);
                  success = true;
               }
            }
         }
      }

      return success;
   }

   private static BlockState getRandomState(TagKey key, RandomSource random, BlockState fallback) {
      return (BlockState)LazyTagLookup.tagManager(ForgeRegistries.BLOCKS).getTag(key).getRandomElement(random).map(Block::m_49966_).orElse(fallback);
   }

   public static int harvestVein(Level level, Player player, ItemStack stack, BlockPos pos, Block target, List currentDrops, int numMined) {
      if (numMined >= 250) {
         return numMined;
      } else {
         AABB b = new AABB((double)(pos.m_123341_() - 1), (double)(pos.m_123342_() - 1), (double)(pos.m_123343_() - 1), (double)(pos.m_123341_() + 1), (double)(pos.m_123342_() + 1), (double)(pos.m_123343_() + 1));
         Iterator var8 = getPositionsFromBox(b).iterator();

         while(var8.hasNext()) {
            BlockPos currentPos = (BlockPos)var8.next();
            BlockState currentState = level.m_8055_(currentPos);
            if (currentState.m_60734_() == target) {
               currentPos = currentPos.m_7949_();
               if (PlayerHelper.hasBreakPermission((ServerPlayer)player, currentPos)) {
                  ++numMined;
                  currentDrops.addAll(Block.m_49874_(currentState, (ServerLevel)level, currentPos, getBlockEntity(level, currentPos), player, stack));
                  level.m_7471_(currentPos, false);
                  numMined = harvestVein(level, player, stack, currentPos, target, currentDrops, numMined);
                  if (numMined >= 250) {
                     break;
                  }
               }
            }
         }

         return numMined;
      }
   }

   public static void igniteNearby(Level level, Player player) {
      Iterator var2 = BlockPos.m_121940_(player.m_20183_().m_7918_(-8, -5, -8), player.m_20183_().m_7918_(8, 5, 8)).iterator();

      while(var2.hasNext()) {
         BlockPos pos = (BlockPos)var2.next();
         if (level.f_46441_.m_188503_(128) == 0 && level.m_46859_(pos)) {
            PlayerHelper.checkedPlaceBlock((ServerPlayer)player, pos.m_7949_(), Blocks.f_50083_.m_49966_());
         }
      }

   }

   private static boolean validRepelEntity(Entity entity, TagKey blacklistTag) {
      if (!entity.m_5833_() && !entity.m_6095_().m_204039_(blacklistTag)) {
         if (entity instanceof Projectile) {
            return !entity.m_20096_();
         } else {
            return entity instanceof Mob;
         }
      } else {
         return false;
      }
   }

   public static void repelEntitiesInterdiction(Level level, AABB effectBounds, double x, double y, double z) {
      Vec3 vec = new Vec3(x, y, z);
      Predicate repelPredicate = ProjectEConfig.server.effects.interdictionMode.get() ? INTERDICTION_REPEL_HOSTILE_PREDICATE : INTERDICTION_REPEL_PREDICATE;
      Iterator var10 = level.m_6443_(Entity.class, effectBounds, repelPredicate).iterator();

      while(var10.hasNext()) {
         Entity ent = (Entity)var10.next();
         repelEntity(vec, ent);
      }

   }

   public static void repelEntitiesSWRG(Level level, AABB effectBounds, Player player) {
      Vec3 playerVec = player.m_20182_();
      Iterator var4 = level.m_6443_(Entity.class, effectBounds, SWRG_REPEL_PREDICATE).iterator();

      while(true) {
         Entity ent;
         Entity owner;
         do {
            if (!var4.hasNext()) {
               return;
            }

            ent = (Entity)var4.next();
            if (!(ent instanceof Projectile)) {
               break;
            }

            Projectile projectile = (Projectile)ent;
            owner = projectile.m_19749_();
         } while(level.m_5776_() && owner == null || owner != null && player.m_20148_().equals(owner.m_20148_()));

         repelEntity(playerVec, ent);
      }
   }

   private static void repelEntity(Vec3 vec, Entity entity) {
      Vec3 t = new Vec3(entity.m_20185_(), entity.m_20186_(), entity.m_20189_());
      Vec3 r = new Vec3(t.f_82479_ - vec.f_82479_, t.f_82480_ - vec.f_82480_, t.f_82481_ - vec.f_82481_);
      double distance = vec.m_82554_(t) + 0.1;
      entity.m_20256_(entity.m_20184_().m_82549_(r.m_82490_(0.6666666666666666 / distance)));
   }

   public static @NotNull InteractionResult igniteBlock(UseOnContext ctx) {
      Player player = ctx.m_43723_();
      if (player == null) {
         return InteractionResult.FAIL;
      } else {
         Level level = ctx.m_43725_();
         BlockPos pos = ctx.m_8083_();
         Direction side = ctx.m_43719_();
         BlockState state = level.m_8055_(pos);
         if (BaseFireBlock.m_49255_(level, pos, side)) {
            if (!level.f_46443_ && PlayerHelper.hasBreakPermission((ServerPlayer)player, pos)) {
               level.m_46597_(pos, BaseFireBlock.m_49245_(level, pos));
               level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
         } else if (CampfireBlock.m_51321_(state)) {
            if (!level.f_46443_ && PlayerHelper.hasBreakPermission((ServerPlayer)player, pos)) {
               level.m_46597_(pos, (BlockState)state.m_61124_(BlockStateProperties.f_61443_, true));
               level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
         } else {
            if (!state.isFlammable(level, pos, side)) {
               return InteractionResult.PASS;
            }

            if (!level.f_46443_ && PlayerHelper.hasBreakPermission((ServerPlayer)player, pos)) {
               state.onCaughtFire(level, pos, side, player);
               if (state.m_60734_() instanceof TntBlock) {
                  level.m_7471_(pos, false);
               }

               level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
         }

         return InteractionResult.m_19078_(level.f_46443_);
      }
   }

   public static boolean isBlockLoaded(@Nullable BlockGetter world, @NotNull BlockPos pos) {
      if (world == null) {
         return false;
      } else if (world instanceof LevelReader) {
         LevelReader reader = (LevelReader)world;
         if (reader instanceof Level) {
            Level level = (Level)reader;
            if (!level.m_46739_(pos)) {
               return false;
            }
         }

         return reader.m_46805_(pos);
      } else {
         return true;
      }
   }

   public static @Nullable BlockEntity getBlockEntity(@Nullable BlockGetter level, @NotNull BlockPos pos) {
      return !isBlockLoaded(level, pos) ? null : level.m_7702_(pos);
   }

   public static @Nullable BlockEntity getBlockEntity(@NotNull Class clazz, @Nullable BlockGetter level, @NotNull BlockPos pos) {
      return getBlockEntity(clazz, level, pos, false);
   }

   public static @Nullable BlockEntity getBlockEntity(@NotNull Class clazz, @Nullable BlockGetter level, @NotNull BlockPos pos, boolean logWrongType) {
      BlockEntity blockEntity = getBlockEntity(level, pos);
      if (blockEntity == null) {
         return null;
      } else if (clazz.isInstance(blockEntity)) {
         return (BlockEntity)clazz.cast(blockEntity);
      } else {
         if (logWrongType) {
            PECore.LOGGER.warn("Unexpected block entity class at {}, expected {}, but found: {}", new Object[]{pos, clazz, blockEntity.getClass()});
         }

         return null;
      }
   }
}
