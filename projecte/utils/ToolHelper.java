package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.blocks.IMatterBlock;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.registries.PEDamageTypes;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToolHelper {
   public static final ToolAction HAMMER_DIG = ToolAction.get("hammer_dig");
   public static final ToolAction KATAR_DIG = ToolAction.get("katar_dig");
   public static final ToolAction MORNING_STAR_DIG = ToolAction.get("morning_star_dig");
   public static final Set DEFAULT_PE_HAMMER_ACTIONS;
   public static final Set DEFAULT_PE_KATAR_ACTIONS;
   public static final Set DEFAULT_PE_MORNING_STAR_ACTIONS;
   private static final Predicate SHEARABLE;
   private static final Predicate SLAY_MOB;
   private static final Predicate SLAY_ALL;

   private static Set of(ToolAction... actions) {
      return (Set)Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
   }

   @SafeVarargs
   public static InteractionResult performActions(InteractionResult firstAction, Supplier... secondaryActions) {
      if (firstAction.m_19077_()) {
         return firstAction;
      } else {
         boolean hasFailed = firstAction == InteractionResult.FAIL;
         Supplier[] var4 = secondaryActions;
         int var5 = secondaryActions.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Supplier secondaryAction = var4[var6];
            InteractionResult result = (InteractionResult)secondaryAction.get();
            if (result.m_19077_()) {
               return result;
            }

            hasFailed &= result == InteractionResult.FAIL;
         }

         if (hasFailed) {
            return InteractionResult.FAIL;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public static InteractionResult clearTagAOE(Level level, Player player, InteractionHand hand, ItemStack stack, long emcCost, TagKey tag) {
      if (ProjectEConfig.server.items.disableAllRadiusMining.get()) {
         return InteractionResult.PASS;
      } else {
         int charge = getCharge(stack);
         if (charge == 0) {
            return InteractionResult.PASS;
         } else {
            int scaled1 = 5 * charge;
            int scaled2 = 10 * charge;
            BlockPos corner1 = player.m_20183_().m_7918_(-scaled1, -scaled2, -scaled1);
            BlockPos corner2 = player.m_20183_().m_7918_(scaled1, scaled2, scaled1);
            boolean hasAction = false;
            List drops = new ArrayList();
            Iterator var14 = WorldHelper.getPositionsFromBox(corner1, corner2).iterator();

            while(var14.hasNext()) {
               BlockPos pos = (BlockPos)var14.next();
               BlockState state = level.m_8055_(pos);
               if (state.m_204336_(tag)) {
                  if (level.f_46443_) {
                     return InteractionResult.SUCCESS;
                  }

                  pos = pos.m_7949_();
                  if (PlayerHelper.hasBreakPermission((ServerPlayer)player, pos)) {
                     if (!ItemPE.consumeFuel(player, stack, emcCost, true)) {
                        break;
                     }

                     drops.addAll(Block.m_49874_(state, (ServerLevel)level, pos, WorldHelper.getBlockEntity(level, pos), player, stack));
                     level.m_7471_(pos, false);
                     hasAction = true;
                     if (level.f_46441_.m_188503_(5) == 0) {
                        ((ServerLevel)level).m_8767_(ParticleTypes.f_123755_, (double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_(), 2, 0.0, 0.0, 0.0, 0.0);
                     }
                  }
               }
            }

            if (hasAction) {
               WorldHelper.createLootDrop(drops, level, player.m_20185_(), player.m_20186_(), player.m_20189_());
               return InteractionResult.SUCCESS;
            } else {
               return InteractionResult.PASS;
            }
         }
      }
   }

   public static InteractionResult dowseCampfire(UseOnContext context, BlockState state) {
      Player player = context.m_43723_();
      if (player == null) {
         return InteractionResult.PASS;
      } else if (state.m_60734_() instanceof CampfireBlock && (Boolean)state.m_61143_(CampfireBlock.f_51227_)) {
         Level level = context.m_43725_();
         BlockPos pos = context.m_8083_();
         if (!level.m_5776_()) {
            level.m_5898_((Player)null, 1009, pos, 0);
         }

         CampfireBlock.m_152749_(player, level, pos, state);
         if (!level.m_5776_()) {
            level.m_7731_(pos, (BlockState)state.m_61124_(CampfireBlock.f_51227_, Boolean.FALSE), 11);
         }

         return InteractionResult.m_19078_(level.f_46443_);
      } else {
         return InteractionResult.PASS;
      }
   }

   public static InteractionResult tillAOE(UseOnContext context, BlockState clickedState, long emcCost) {
      return useAOE(context, clickedState, emcCost, ToolActions.HOE_TILL, SoundEvents.f_11955_, -1, new HoeToolAOEData());
   }

   public static InteractionResult flattenAOE(UseOnContext context, BlockState clickedState, long emcCost) {
      Direction sideHit = context.m_43719_();
      return sideHit == Direction.DOWN ? InteractionResult.PASS : useAOE(context, clickedState, emcCost, ToolActions.SHOVEL_FLATTEN, SoundEvents.f_12406_, -1, new ShovelToolAOEData());
   }

   public static InteractionResult stripLogsAOE(UseOnContext context, BlockState clickedState, long emcCost) {
      return useAxeAOE(context, clickedState, emcCost, ToolActions.AXE_STRIP, SoundEvents.f_11688_, -1);
   }

   public static InteractionResult scrapeAOE(UseOnContext context, BlockState clickedState, long emcCost) {
      return useAxeAOE(context, clickedState, emcCost, ToolActions.AXE_SCRAPE, SoundEvents.f_144059_, 3005);
   }

   public static InteractionResult waxOffAOE(UseOnContext context, BlockState clickedState, long emcCost) {
      return useAxeAOE(context, clickedState, emcCost, ToolActions.AXE_WAX_OFF, SoundEvents.f_144060_, 3004);
   }

   private static InteractionResult useAxeAOE(UseOnContext context, BlockState clickedState, long emcCost, ToolAction action, SoundEvent sound, int particle) {
      return useAOE(context, clickedState, emcCost, action, sound, particle, new AxeToolAOEData());
   }

   private static InteractionResult useAOE(UseOnContext context, BlockState clickedState, long emcCost, ToolAction action, SoundEvent sound, int particle, IToolAOEData toolAOEData) {
      Player player = context.m_43723_();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         Level level = context.m_43725_();
         BlockPos pos = context.m_8083_();
         if (!toolAOEData.isValid(level, pos, clickedState)) {
            return InteractionResult.PASS;
         } else {
            BlockState modifiedState = clickedState.getToolModifiedState(context, action, false);
            if (modifiedState == null) {
               return InteractionResult.PASS;
            } else if (level.f_46443_) {
               return InteractionResult.SUCCESS;
            } else {
               level.m_7731_(pos, modifiedState, 11);
               level.m_5594_((Player)null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
               if (particle != -1) {
                  level.m_5898_((Player)null, particle, pos, 0);
               }

               ItemStack stack = context.m_43722_();
               int charge = getCharge(stack);
               if (charge > 0) {
                  Direction side = context.m_43719_();
                  toolAOEData.persistData(level, pos, clickedState, side);
                  Iterator var15 = toolAOEData.getTargetPositions(pos, side, charge).iterator();

                  while(var15.hasNext()) {
                     BlockPos newPos = (BlockPos)var15.next();
                     if (!pos.equals(newPos)) {
                        BlockState state = level.m_8055_(newPos);
                        UseOnContext adjustedContext = new UseOnContext(level, context.m_43723_(), context.m_43724_(), context.m_43722_(), new BlockHitResult(context.m_43720_().m_82520_((double)(newPos.m_123341_() - pos.m_123341_()), (double)(newPos.m_123342_() - pos.m_123342_()), (double)(newPos.m_123343_() - pos.m_123343_())), context.m_43719_(), newPos, context.m_43721_()));
                        if (toolAOEData.isValid(level, newPos, state) && modifiedState == state.getToolModifiedState(adjustedContext, action, true)) {
                           if (!ItemPE.consumeFuel(player, stack, emcCost, true)) {
                              break;
                           }

                           newPos = newPos.m_7949_();
                           state.getToolModifiedState(adjustedContext, action, false);
                           level.m_7731_(newPos, modifiedState, 11);
                           if (particle != -1) {
                              level.m_5898_((Player)null, particle, newPos, 0);
                           }
                        }
                     }
                  }
               }

               level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
               return InteractionResult.CONSUME;
            }
         }
      }
   }

   public static void digBasedOnMode(ItemStack stack, Level level, BlockPos pos, LivingEntity living, RayTracePointer tracePointer) {
      if (!level.f_46443_ && !ProjectEConfig.server.items.disableAllRadiusMining.get() && living instanceof Player player) {
         byte mode = getMode(stack);
         if (mode != 0) {
            HitResult mop = tracePointer.rayTrace(level, player, Fluid.NONE);
            if (mop instanceof BlockHitResult) {
               BlockHitResult result = (BlockHitResult)mop;
               if (result.m_6662_() != Type.MISS && pos.equals(result.m_82425_())) {
                  AABB var10000;
                  Direction sideHit = result.m_82434_();
                  label47:
                  switch (mode) {
                     case 1:
                        var10000 = new AABB(pos.m_7495_(), pos.m_7494_());
                        break;
                     case 2:
                        switch (sideHit.m_122434_()) {
                           case X:
                              var10000 = new AABB(pos.m_122019_(), pos.m_122012_());
                              break label47;
                           case Z:
                              var10000 = new AABB(pos.m_122024_(), pos.m_122029_());
                              break label47;
                           case Y:
                              switch (player.m_6350_().m_122434_()) {
                                 case X:
                                    var10000 = new AABB(pos.m_122019_(), pos.m_122012_());
                                    break label47;
                                 case Z:
                                    var10000 = new AABB(pos.m_122024_(), pos.m_122029_());
                                    break label47;
                                 default:
                                    var10000 = new AABB(pos, pos);
                                    break label47;
                              }
                           default:
                              throw new IncompatibleClassChangeError();
                        }
                     case 3:
                        var10000 = new AABB(pos, pos.m_5484_(sideHit.m_122424_(), 2));
                        break;
                     default:
                        var10000 = new AABB(pos, pos);
                  }

                  AABB box = var10000;
                  List drops = new ArrayList();
                  Iterator var12 = WorldHelper.getPositionsFromBox(box).iterator();

                  while(var12.hasNext()) {
                     BlockPos digPos = (BlockPos)var12.next();
                     if (!level.m_46859_(digPos)) {
                        BlockState state = level.m_8055_(digPos);
                        if (state.m_60800_(level, digPos) != -1.0F && stack.m_41735_(state)) {
                           digPos = digPos.m_7949_();
                           if (PlayerHelper.hasBreakPermission((ServerPlayer)player, digPos)) {
                              drops.addAll(Block.m_49874_(state, (ServerLevel)level, digPos, WorldHelper.getBlockEntity(level, digPos), player, stack));
                              level.m_7471_(digPos, false);
                           }
                        }
                     }
                  }

                  WorldHelper.createLootDrop(drops, level, pos);
                  return;
               }
            }

         }
      }
   }

   public static InteractionResult digAOE(Level level, Player player, InteractionHand hand, ItemStack stack, BlockPos pos, Direction sideHit, boolean affectDepth, long emcCost) {
      if (ProjectEConfig.server.items.disableAllRadiusMining.get()) {
         return InteractionResult.PASS;
      } else {
         int charge = getCharge(stack);
         if (charge == 0) {
            return InteractionResult.PASS;
         } else {
            AABB box = affectDepth ? WorldHelper.getBroadDeepBox(pos, sideHit, charge) : WorldHelper.getFlatYBox(pos, charge);
            boolean hasAction = false;
            List drops = new ArrayList();
            Iterator var13 = WorldHelper.getPositionsFromBox(box).iterator();

            while(var13.hasNext()) {
               BlockPos newPos = (BlockPos)var13.next();
               if (!level.m_46859_(newPos)) {
                  BlockState state = level.m_8055_(newPos);
                  if (state.m_60800_(level, newPos) != -1.0F && stack.m_41735_(state)) {
                     if (level.f_46443_) {
                        return InteractionResult.SUCCESS;
                     }

                     newPos = newPos.m_7949_();
                     if (PlayerHelper.hasBreakPermission((ServerPlayer)player, newPos)) {
                        if (!ItemPE.consumeFuel(player, stack, emcCost, true)) {
                           break;
                        }

                        drops.addAll(Block.m_49874_(state, (ServerLevel)level, newPos, WorldHelper.getBlockEntity(level, newPos), player, stack));
                        level.m_7471_(newPos, false);
                        hasAction = true;
                     }
                  }
               }
            }

            if (hasAction) {
               WorldHelper.createLootDrop(drops, level, pos);
               player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.DESTRUCT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
               return InteractionResult.SUCCESS;
            } else {
               return InteractionResult.PASS;
            }
         }
      }
   }

   public static void attackWithCharge(ItemStack stack, LivingEntity damaged, LivingEntity damager, float baseDmg) {
      if (damager instanceof Player player) {
         if (!damager.m_9236_().f_46443_) {
            int charge = getCharge(stack);
            float totalDmg = baseDmg;
            DamageSource dmg;
            if (charge > 0) {
               dmg = PEDamageTypes.BYPASS_ARMOR_PLAYER_ATTACK.source(player);
               totalDmg = baseDmg + (float)charge;
            } else {
               dmg = damager.m_269291_().m_269075_(player);
            }

            damaged.m_6469_(dmg, totalDmg);
            return;
         }
      }

   }

   public static void attackAOE(ItemStack stack, Player player, boolean slayAll, float damage, long emcCost, InteractionHand hand) {
      Level level = player.m_9236_();
      if (!level.f_46443_) {
         int charge = getCharge(stack);
         List toAttack = level.m_6249_(player, player.m_20191_().m_82400_((double)(2.5F * (float)charge)), slayAll ? SLAY_ALL : SLAY_MOB);
         DamageSource src = PEDamageTypes.BYPASS_ARMOR_PLAYER_ATTACK.source(player);
         boolean hasAction = false;

         for(Iterator var12 = toAttack.iterator(); var12.hasNext(); hasAction = true) {
            Entity entity = (Entity)var12.next();
            if (!ItemPE.consumeFuel(player, stack, emcCost, true)) {
               break;
            }

            entity.m_6469_(src, damage);
         }

         if (hasAction) {
            level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            PlayerHelper.swingItem(player, hand);
         }

      }
   }

   public static InteractionResult shearBlock(ItemStack stack, BlockPos pos, Player player) {
      Level level = player.m_9236_();
      Block block = level.m_8055_(pos).m_60734_();
      if (block instanceof IForgeShearable target) {
         if (target.isShearable(stack, level, pos) && (level.f_46443_ || PlayerHelper.hasBreakPermission((ServerPlayer)player, pos))) {
            List drops = target.onSheared(player, stack, level, pos, stack.getEnchantmentLevel(Enchantments.f_44987_));
            if (!drops.isEmpty()) {
               if (!level.f_46443_) {
                  WorldHelper.createLootDrop(new ArrayList(drops), level, pos);
                  player.m_6278_(Stats.f_12949_.m_12902_(block), 1);
                  level.m_142346_(player, GameEvent.f_157781_, pos);
               }

               return InteractionResult.SUCCESS;
            }
         }
      }

      return InteractionResult.PASS;
   }

   public static InteractionResult shearEntityAOE(Player player, InteractionHand hand, long emcCost) {
      Level level = player.m_9236_();
      ItemStack stack = player.m_21120_(hand);
      int fortune = stack.getEnchantmentLevel(Enchantments.f_44987_);
      int offset = (int)Math.pow(2.0, (double)(2 + getCharge(stack)));
      List list = level.m_6443_(Entity.class, player.m_20191_().m_82377_((double)offset, (double)offset / 2.0, (double)offset), SHEARABLE);
      boolean hasAction = false;
      List drops = new ArrayList();
      Iterator var11 = list.iterator();

      while(var11.hasNext()) {
         Entity ent = (Entity)var11.next();
         BlockPos entityPosition = ent.m_20183_();
         IForgeShearable target = (IForgeShearable)ent;
         if (target.isShearable(stack, level, entityPosition)) {
            if (level.f_46443_) {
               return InteractionResult.SUCCESS;
            }

            if (!ItemPE.consumeFuel(player, stack, emcCost, true)) {
               break;
            }

            List entDrops = target.onSheared(player, stack, level, entityPosition, fortune);
            ent.m_146852_(GameEvent.f_157781_, player);
            if (!entDrops.isEmpty()) {
               drops.addAll(entDrops);
               drops.addAll(entDrops);
            }

            hasAction = true;
         }

         if (!level.f_46443_ && Math.random() < 0.01) {
            Entity e = ent.m_6095_().m_20615_(level);
            if (e != null) {
               e.m_6034_(ent.m_20185_(), ent.m_20186_(), ent.m_20189_());
               if (e instanceof Mob) {
                  Mob mob = (Mob)e;
                  mob.m_6518_((ServerLevel)level, level.m_6436_(entityPosition), MobSpawnType.EVENT, (SpawnGroupData)null, (CompoundTag)null);
               }

               if (e instanceof Sheep) {
                  Sheep sheep = (Sheep)e;
                  sheep.m_29855_(DyeColor.m_41053_(MathUtils.randomIntInRange(0, 15)));
               }

               if (e instanceof AgeableMob) {
                  AgeableMob mob = (AgeableMob)e;
                  mob.m_146762_(-24000);
               }

               level.m_7967_(e);
            }
         }
      }

      if (hasAction) {
         WorldHelper.createLootDrop(drops, level, player.m_20185_(), player.m_20186_(), player.m_20189_());
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public static InteractionResult tryVeinMine(Player player, ItemStack stack, BlockPos pos, Direction sideHit) {
      if (ProjectEConfig.server.items.disableAllRadiusMining.get()) {
         return InteractionResult.PASS;
      } else {
         Level level = player.m_9236_();
         BlockState target = level.m_8055_(pos);
         if (!(target.m_60800_(level, pos) <= -1.0F) && stack.m_41735_(target)) {
            boolean hasAction = false;
            List drops = new ArrayList();
            Iterator var8 = WorldHelper.getPositionsFromBox(WorldHelper.getBroadDeepBox(pos, sideHit, getCharge(stack))).iterator();

            while(var8.hasNext()) {
               BlockPos newPos = (BlockPos)var8.next();
               if (!level.m_46859_(newPos)) {
                  BlockState state = level.m_8055_(newPos);
                  if (target.m_60734_() == state.m_60734_()) {
                     if (level.f_46443_) {
                        return InteractionResult.SUCCESS;
                     }

                     if (WorldHelper.harvestVein(level, player, stack, newPos.m_7949_(), state.m_60734_(), drops, 0) > 0) {
                        hasAction = true;
                     }
                  }
               }
            }

            if (hasAction) {
               WorldHelper.createLootDrop(drops, level, pos);
               level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.DESTRUCT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
               return InteractionResult.SUCCESS;
            } else {
               return InteractionResult.PASS;
            }
         } else {
            return InteractionResult.FAIL;
         }
      }
   }

   public static InteractionResult mineOreVeinsInAOE(Player player, InteractionHand hand) {
      if (ProjectEConfig.server.items.disableAllRadiusMining.get()) {
         return InteractionResult.PASS;
      } else {
         Level level = player.m_9236_();
         ItemStack stack = player.m_21120_(hand);
         boolean hasAction = false;
         List drops = new ArrayList();
         Iterator var6 = WorldHelper.getPositionsFromBox(player.m_20191_().m_82400_((double)(getCharge(stack) + 3))).iterator();

         while(var6.hasNext()) {
            BlockPos pos = (BlockPos)var6.next();
            if (!level.m_46859_(pos)) {
               BlockState state = level.m_8055_(pos);
               if (ItemHelper.isOre(state) && state.m_60800_(level, pos) != -1.0F && stack.m_41735_(state)) {
                  if (level.f_46443_) {
                     return InteractionResult.SUCCESS;
                  }

                  if (WorldHelper.harvestVein(level, player, stack, pos.m_7949_(), state.m_60734_(), drops, 0) > 0) {
                     hasAction = true;
                  }
               }
            }
         }

         if (hasAction) {
            WorldHelper.createLootDrop(drops, level, player.m_20185_(), player.m_20186_(), player.m_20189_());
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public static float getDestroySpeed(float parentDestroySpeed, EnumMatterType matterType, int charge) {
      return parentDestroySpeed == 1.0F ? parentDestroySpeed : parentDestroySpeed + matterType.getChargeModifier() * (float)charge;
   }

   public static boolean canMatterMine(EnumMatterType matterType, Block block) {
      boolean var10000;
      if (block instanceof IMatterBlock matterBlock) {
         if (matterBlock.getMatterType().getMatterTier() <= matterType.getMatterTier()) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   private static int getCharge(ItemStack stack) {
      return (Integer)stack.getCapability(PECapabilities.CHARGE_ITEM_CAPABILITY).map((itemCharge) -> {
         return itemCharge.getCharge(stack);
      }).orElse(0);
   }

   private static byte getMode(ItemStack stack) {
      return (Byte)stack.getCapability(PECapabilities.MODE_CHANGER_ITEM_CAPABILITY).map((itemMode) -> {
         return itemMode.getMode(stack);
      }).orElse((byte)0);
   }

   static {
      DEFAULT_PE_HAMMER_ACTIONS = of(HAMMER_DIG);
      DEFAULT_PE_KATAR_ACTIONS = of(KATAR_DIG);
      DEFAULT_PE_MORNING_STAR_ACTIONS = of(MORNING_STAR_DIG);
      SHEARABLE = (entity) -> {
         return !entity.m_5833_() && entity instanceof IForgeShearable;
      };
      SLAY_MOB = (entity) -> {
         return !entity.m_5833_() && entity instanceof Enemy;
      };
      SLAY_ALL = (entity) -> {
         return !entity.m_5833_() && (entity instanceof Enemy || entity instanceof LivingEntity);
      };
   }

   private static class HoeToolAOEData extends FlatToolAOEData {
      public boolean isValid(Level level, BlockPos pos, BlockState state) {
         return true;
      }
   }

   private interface IToolAOEData {
      boolean isValid(Level var1, BlockPos var2, BlockState var3);

      default void persistData(Level level, BlockPos pos, BlockState state, Direction side) {
      }

      Iterable getTargetPositions(BlockPos var1, Direction var2, int var3);
   }

   private static class ShovelToolAOEData extends FlatToolAOEData {
      public boolean isValid(Level level, BlockPos pos, BlockState state) {
         BlockPos abovePos = pos.m_7494_();
         BlockState aboveState = level.m_8055_(abovePos);
         if (aboveState.m_60795_()) {
            return true;
         } else if (!aboveState.m_204336_(PETags.Blocks.FARMING_OVERRIDE) && (!aboveState.m_247087_() || !(aboveState.m_60734_() instanceof IPlantable))) {
            return false;
         } else {
            return aboveState.m_60819_().m_76178_() && !aboveState.m_60804_(level, abovePos);
         }
      }
   }

   private static class AxeToolAOEData implements IToolAOEData {
      @Nullable
      private Direction.@Nullable Axis axis;
      private boolean isSet;

      public boolean isValid(Level level, BlockPos blockPos, BlockState state) {
         return !this.isSet || this.axis == this.getAxis(state);
      }

      public void persistData(Level level, BlockPos pos, BlockState state, Direction side) {
         this.axis = this.getAxis(state);
         this.isSet = true;
      }

      public Iterable getTargetPositions(BlockPos pos, Direction side, int radius) {
         return WorldHelper.getPositionsFromBox(WorldHelper.getBroadBox(pos, side, radius));
      }

      @Nullable
      private Direction.@Nullable Axis getAxis(BlockState state) {
         return state.m_61138_(RotatedPillarBlock.f_55923_) ? (Direction.Axis)state.m_61143_(RotatedPillarBlock.f_55923_) : null;
      }
   }

   @FunctionalInterface
   public interface RayTracePointer {
      HitResult rayTrace(Level var1, Player var2, ClipContext.Fluid var3);
   }

   public static class ChargeAttributeCache {
      private static final UUID CHARGE_MODIFIER = UUID.fromString("69ADE509-46FF-3725-92AC-F59FB052BEC7");
      private final Int2ObjectMap cachedMaps = new Int2ObjectArrayMap();

      public Multimap addChargeAttributeModifier(Multimap currentModifiers, @NotNull EquipmentSlot slot, ItemStack stack) {
         if (slot == EquipmentSlot.MAINHAND) {
            int charge = ToolHelper.getCharge(stack);
            if (charge > 0) {
               return (Multimap)this.cachedMaps.computeIfAbsent(charge, (c) -> {
                  ImmutableMultimap.Builder attributesBuilder = ImmutableMultimap.builder();
                  attributesBuilder.putAll(currentModifiers);
                  attributesBuilder.put(Attributes.f_22281_, new AttributeModifier(CHARGE_MODIFIER, "Charge modifier", (double)c, Operation.ADDITION));
                  return attributesBuilder.build();
               });
            }
         }

         return currentModifiers;
      }
   }

   private abstract static class FlatToolAOEData implements IToolAOEData {
      public Iterable getTargetPositions(BlockPos pos, Direction side, int radius) {
         return BlockPos.m_121940_(pos.m_7918_(-radius, 0, -radius), pos.m_7918_(radius, 0, radius));
      }
   }
}
