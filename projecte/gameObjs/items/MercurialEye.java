package moze_intel.projecte.gameObjs.items;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.capability.ExtraFunctionItemCapabilityWrapper;
import moze_intel.projecte.capability.IItemCapabilitySerializable;
import moze_intel.projecte.capability.ItemCapability;
import moze_intel.projecte.gameObjs.container.MercurialEyeContainer;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MercurialEye extends ItemMode implements IExtraFunction {
   private static final int CREATION_MODE = 0;
   private static final int EXTENSION_MODE = 1;
   private static final int EXTENSION_MODE_CLASSIC = 2;
   private static final int TRANSMUTATION_MODE = 3;
   private static final int TRANSMUTATION_MODE_CLASSIC = 4;
   private static final int PILLAR_MODE = 5;

   public MercurialEye(Item.Properties props) {
      super(props, 4, PELang.MODE_MERCURIAL_EYE_1, PELang.MODE_MERCURIAL_EYE_2, PELang.MODE_MERCURIAL_EYE_3, PELang.MODE_MERCURIAL_EYE_4, PELang.MODE_MERCURIAL_EYE_5, PELang.MODE_MERCURIAL_EYE_6);
      this.addItemCapability(ExtraFunctionItemCapabilityWrapper::new);
      this.addItemCapability(EyeInventoryHandler::new);
   }

   public boolean doExtraFunction(@NotNull ItemStack stack, @NotNull Player player, InteractionHand hand) {
      int selected = player.m_150109_().f_35977_;
      MenuProvider provider = new SimpleMenuProvider((id, inv, pl) -> {
         return new MercurialEyeContainer(id, inv, hand, selected);
      }, stack.m_41786_());
      NetworkHooks.openScreen((ServerPlayer)player, provider, (b) -> {
         b.m_130068_(hand);
         b.writeByte(selected);
      });
      return true;
   }

   public @NotNull InteractionResult m_6225_(UseOnContext ctx) {
      ItemStack stack = ctx.m_43722_();
      return ctx.m_43725_().f_46443_ ? InteractionResult.SUCCESS : this.formBlocks(stack, ctx.m_43723_(), ctx.m_43724_(), ctx.m_8083_(), ctx.m_43719_());
   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (this.getMode(stack) == 0) {
         if (level.f_46443_) {
            return InteractionResultHolder.m_19090_(stack);
         } else {
            Vec3 eyeVec = new Vec3(player.m_20185_(), player.m_20186_() + (double)player.m_20192_(), player.m_20189_());
            Vec3 lookVec = player.m_20154_();
            Vec3 targVec = eyeVec.m_82520_(lookVec.f_82479_ * 2.0, lookVec.f_82480_ * 2.0, lookVec.f_82481_ * 2.0);
            return ItemHelper.actionResultFromType(this.formBlocks(stack, player, hand, BlockPos.m_274446_(targVec), (Direction)null), stack);
         }
      } else {
         return InteractionResultHolder.m_19098_(stack);
      }
   }

   private void playNoEMCSound(Player player) {
      player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.UNCHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
   }

   private InteractionResult formBlocks(ItemStack eye, Player player, InteractionHand hand, BlockPos startingPos, @Nullable Direction facing) {
      Optional inventoryCapability = eye.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
      if (inventoryCapability.isEmpty()) {
         return InteractionResult.FAIL;
      } else {
         IItemHandler inventory = (IItemHandler)inventoryCapability.get();
         ItemStack klein = inventory.getStackInSlot(0);
         if (!klein.m_41619_() && klein.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).isPresent()) {
            Level level = player.m_9236_();
            BlockState startingState = level.m_8055_(startingPos);
            long startingBlockEmc = EMCHelper.getEmcValue(new ItemStack(startingState.m_60734_()));
            ItemStack target = inventory.getStackInSlot(1);
            byte mode = this.getMode(eye);
            BlockHitResult hitResult;
            if (facing == null) {
               hitResult = new BlockHitResult(Vec3.m_82512_(startingPos), Direction.UP, startingPos, true);
            } else {
               hitResult = new BlockHitResult(Vec3.m_82512_(startingPos.m_121945_(facing)), facing, startingPos, false);
            }

            BlockState newState;
            long newBlockEmc;
            BlockPlaceContext context;
            if (!target.m_41619_()) {
               context = new BlockPlaceContext(level, player, hand, target.m_41777_(), hitResult);
               newState = ItemHelper.stackToState(target, context);
               newBlockEmc = EMCHelper.getEmcValue(target);
               if (newBlockEmc == 0L) {
                  return InteractionResult.FAIL;
               }
            } else {
               if (startingBlockEmc == 0L || mode != 1 && mode != 2) {
                  return InteractionResult.FAIL;
               }

               newState = startingState;
               newBlockEmc = startingBlockEmc;
               context = new BlockPlaceContext(level, player, hand, new ItemStack(startingState.m_60734_()), hitResult);
            }

            if (newState != null && !newState.m_60795_()) {
               NonNullList drops = NonNullList.m_122779_();
               int charge = this.getCharge(eye);
               int hitTargets = 0;
               if (mode == 0) {
                  if (facing != null && (!context.m_7058_() || player.m_36341_() && !startingState.m_60795_())) {
                     BlockPos offsetPos = startingPos.m_121945_(facing);
                     BlockState offsetState = level.m_8055_(offsetPos);
                     if (!offsetState.m_60629_(context)) {
                        return InteractionResult.FAIL;
                     }

                     long offsetBlockEmc = EMCHelper.getEmcValue(new ItemStack(offsetState.m_60734_()));
                     if (this.doBlockPlace(player, offsetState, offsetPos, newState, eye, offsetBlockEmc, newBlockEmc, drops)) {
                        ++hitTargets;
                     }
                  } else if (this.doBlockPlace(player, startingState, startingPos, newState, eye, startingBlockEmc, newBlockEmc, drops)) {
                     ++hitTargets;
                  }
               } else if (mode == 5) {
                  hitTargets += this.fillGaps(eye, player, level, context, startingState, newState, newBlockEmc, this.getCorners(startingPos, facing, 1, 3 * charge + 2), drops);
               } else if (mode == 2) {
                  hitTargets += this.fillGaps(eye, player, level, context, startingState, newState, newBlockEmc, this.getCorners(startingPos, facing, charge, 0), drops);
               } else if (mode == 4) {
                  Pair corners = this.getCorners(startingPos, facing, charge, 0);
                  Iterator var42 = WorldHelper.getPositionsFromBox(new AABB((BlockPos)corners.getLeft(), (BlockPos)corners.getRight())).iterator();

                  while(var42.hasNext()) {
                     BlockPos pos = (BlockPos)var42.next();
                     BlockState placedState = level.m_8055_(pos);
                     if (placedState == startingState && this.doBlockPlace(player, placedState, pos.m_7949_(), newState, eye, startingBlockEmc, newBlockEmc, drops)) {
                        ++hitTargets;
                     }
                  }
               } else {
                  if (startingState.m_60795_() || facing == null) {
                     return InteractionResult.FAIL;
                  }

                  LinkedList possibleBlocks = new LinkedList();
                  Set visited = new HashSet();
                  possibleBlocks.add(startingPos);
                  visited.add(startingPos);
                  int side = 2 * charge + 1;
                  int size = side * side;
                  int totalTries = size * 4;

                  for(int attemptedTargets = 0; attemptedTargets < totalTries && !possibleBlocks.isEmpty(); ++attemptedTargets) {
                     BlockPos pos = (BlockPos)possibleBlocks.poll();
                     BlockState checkState = level.m_8055_(pos);
                     if (startingState == checkState) {
                        BlockPos offsetPos = pos.m_121945_(facing);
                        BlockState offsetState = level.m_8055_(offsetPos);
                        if (!offsetState.m_60783_(level, offsetPos, facing)) {
                           boolean hit = false;
                           if (mode == 1) {
                              VoxelShape cbBox = startingState.m_60812_(level, offsetPos);
                              if (level.m_5450_((Entity)null, cbBox)) {
                                 long offsetBlockEmc = EMCHelper.getEmcValue((ItemLike)offsetState.m_60734_());
                                 hit = this.doBlockPlace(player, offsetState, offsetPos, newState, eye, offsetBlockEmc, newBlockEmc, drops);
                              }
                           } else if (mode == 3) {
                              hit = this.doBlockPlace(player, checkState, pos, newState, eye, startingBlockEmc, newBlockEmc, drops);
                           }

                           if (hit) {
                              ++hitTargets;
                              if (hitTargets >= size) {
                                 break;
                              }

                              Direction[] var47 = Direction.values();
                              int var48 = var47.length;

                              for(int var36 = 0; var36 < var48; ++var36) {
                                 Direction e = var47[var36];
                                 if (facing.m_122434_() != e.m_122434_()) {
                                    BlockPos offset = pos.m_121945_(e);
                                    if (visited.add(offset)) {
                                       possibleBlocks.offer(offset);
                                    }

                                    BlockPos offsetOpposite = pos.m_121945_(e.m_122424_());
                                    if (visited.add(offsetOpposite)) {
                                       possibleBlocks.offer(offsetOpposite);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (hitTargets > 0) {
                  level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.POWER.get(), SoundSource.PLAYERS, 0.8F, 2.0F / ((float)charge / (float)this.getNumCharges(eye) + 2.0F));
                  if (!drops.isEmpty()) {
                     WorldHelper.createLootDrop(drops, player.m_9236_(), startingPos);
                  }
               }

               return InteractionResult.CONSUME;
            } else {
               return InteractionResult.FAIL;
            }
         } else {
            this.playNoEMCSound(player);
            return InteractionResult.FAIL;
         }
      }
   }

   private boolean doBlockPlace(Player player, BlockState oldState, BlockPos placePos, BlockState newState, ItemStack eye, long oldEMC, long newEMC, NonNullList drops) {
      Optional inventoryCapability = eye.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
      if (inventoryCapability.isEmpty()) {
         return false;
      } else {
         IItemHandler inventory = (IItemHandler)inventoryCapability.get();
         ItemStack klein = inventory.getStackInSlot(0);
         if (klein.m_41619_()) {
            this.playNoEMCSound(player);
            return false;
         } else {
            Optional holderCapability = klein.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
            if (holderCapability.isEmpty()) {
               this.playNoEMCSound(player);
               return false;
            } else if (oldState == newState) {
               return false;
            } else if (ItemPE.getEmc(klein) < newEMC - oldEMC) {
               this.playNoEMCSound(player);
               return false;
            } else if (WorldHelper.getBlockEntity(player.m_9236_(), placePos) != null) {
               return false;
            } else if (oldEMC == 0L && oldState.m_60800_(player.m_9236_(), placePos) == -1.0F) {
               return false;
            } else if (PlayerHelper.checkedReplaceBlock((ServerPlayer)player, placePos, newState)) {
               IItemEmcHolder emcHolder = (IItemEmcHolder)holderCapability.get();
               if (oldEMC == 0L) {
                  drops.addAll(Block.m_49874_(oldState, ((ServerPlayer)player).m_284548_(), placePos, (BlockEntity)null, player, eye));
                  emcHolder.extractEmc(klein, newEMC, IEmcStorage.EmcAction.EXECUTE);
               } else if (oldEMC > newEMC) {
                  emcHolder.insertEmc(klein, oldEMC - newEMC, IEmcStorage.EmcAction.EXECUTE);
               } else if (oldEMC < newEMC) {
                  emcHolder.extractEmc(klein, newEMC - oldEMC, IEmcStorage.EmcAction.EXECUTE);
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   private int fillGaps(ItemStack eye, Player player, Level level, BlockPlaceContext context, BlockState startingState, BlockState newState, long newBlockEmc, Pair corners, NonNullList drops) {
      int hitTargets = 0;
      Iterator var12 = WorldHelper.getPositionsFromBox(new AABB((BlockPos)corners.getLeft(), (BlockPos)corners.getRight())).iterator();

      while(var12.hasNext()) {
         BlockPos pos = (BlockPos)var12.next();
         VoxelShape bb = startingState.m_60812_(level, pos);
         if (level.m_5450_((Entity)null, bb)) {
            BlockPlaceContext adjustedContext = BlockPlaceContext.m_43644_(context, pos, context.m_43719_());
            if (adjustedContext.m_7058_()) {
               BlockState placeState = level.m_8055_(pos);
               long placeBlockEmc = EMCHelper.getEmcValue((ItemLike)placeState.m_60734_());
               if (this.doBlockPlace(player, placeState, pos.m_7949_(), newState, eye, placeBlockEmc, newBlockEmc, drops)) {
                  ++hitTargets;
               }
            }
         }
      }

      return hitTargets;
   }

   private Pair getCorners(BlockPos startingPos, Direction facing, int strength, int depth) {
      if (facing == null) {
         return new ImmutablePair(startingPos, startingPos);
      } else {
         BlockPos start = startingPos;
         BlockPos end = startingPos;
         switch (facing) {
            case UP:
               start = startingPos.m_7918_(-strength, -depth, -strength);
               end = startingPos.m_7918_(strength, 0, strength);
               break;
            case DOWN:
               start = startingPos.m_7918_(-strength, 0, -strength);
               end = startingPos.m_7918_(strength, depth, strength);
               break;
            case SOUTH:
               start = startingPos.m_7918_(-strength, -strength, -depth);
               end = startingPos.m_7918_(strength, strength, 0);
               break;
            case NORTH:
               start = startingPos.m_7918_(-strength, -strength, 0);
               end = startingPos.m_7918_(strength, strength, depth);
               break;
            case EAST:
               start = startingPos.m_7918_(-depth, -strength, -strength);
               end = startingPos.m_7918_(0, strength, strength);
               break;
            case WEST:
               start = startingPos.m_7918_(0, -strength, -strength);
               end = startingPos.m_7918_(depth, strength, strength);
         }

         return new ImmutablePair(start, end);
      }
   }

   private static class EyeInventoryHandler extends ItemCapability implements IItemCapabilitySerializable {
      private final ItemStackHandler inv = new ItemStackHandler(2);
      private final LazyOptional invInst = LazyOptional.of(() -> {
         return this.inv;
      });

      public Tag serializeNBT() {
         return this.inv.serializeNBT();
      }

      public void deserializeNBT(Tag nbt) {
         if (nbt instanceof CompoundTag tag) {
            this.inv.deserializeNBT(tag);
         }

      }

      public Capability getCapability() {
         return ForgeCapabilities.ITEM_HANDLER;
      }

      public LazyOptional getLazyCapability() {
         return this.invInst;
      }

      public String getStorageKey() {
         return "EyeInventory";
      }
   }
}
