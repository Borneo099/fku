package moze_intel.projecte.gameObjs.items.rings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.capability.ProjectileShooterItemCapabilityWrapper;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SWRG extends ItemPE implements IPedestalItem, IFlightProvider, IProjectileShooter {
   public SWRG(Item.Properties props) {
      super(props);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
      this.addItemCapability(ProjectileShooterItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   private void tick(ItemStack stack, Player player) {
      CompoundTag nbt = stack.m_41784_();
      if (nbt.m_128451_("Mode") > 1) {
         WorldHelper.repelEntitiesSWRG(player.m_9236_(), player.m_20191_().m_82400_(5.0), player);
      }

      if (!player.m_9236_().f_46443_) {
         ServerPlayer playerMP = (ServerPlayer)player;
         if (getEmc(stack) == 0L && !consumeFuel(player, stack, 64L, false)) {
            if (nbt.m_128451_("Mode") > 0) {
               this.changeMode(player, stack, 0);
            }

            if (playerMP.m_150110_().f_35936_) {
               playerMP.getCapability(InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::disableSwrgFlightOverride);
            }

         } else {
            if (!playerMP.m_150110_().f_35936_) {
               playerMP.getCapability(InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::enableSwrgFlightOverride);
            }

            if (playerMP.m_150110_().f_35935_) {
               if (!this.isFlyingEnabled(nbt)) {
                  this.changeMode(player, stack, nbt.m_128451_("Mode") == 0 ? 1 : 3);
               }
            } else if (this.isFlyingEnabled(nbt)) {
               this.changeMode(player, stack, nbt.m_128451_("Mode") == 1 ? 0 : 2);
            }

            float toRemove = 0.0F;
            if (playerMP.m_150110_().f_35935_) {
               toRemove = 0.32F;
            }

            if (nbt.m_128451_("Mode") == 2) {
               toRemove = 0.32F;
            } else if (nbt.m_128451_("Mode") == 3) {
               toRemove = 0.64F;
            }

            removeEmc(stack, EMCHelper.removeFractionalEMC(stack, (double)toRemove));
            playerMP.f_19789_ = 0.0F;
         }
      }
   }

   private boolean isFlyingEnabled(CompoundTag nbt) {
      return nbt.m_128451_("Mode") == 1 || nbt.m_128451_("Mode") == 3;
   }

   public void m_6883_(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int invSlot, boolean isHeldItem) {
      if (invSlot < Inventory.m_36059_() && entity instanceof Player player) {
         this.tick(stack, player);
      }

   }

   public @NotNull InteractionResultHolder m_7203_(Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!level.f_46443_) {
         byte var10000;
         switch (stack.m_41784_().m_128451_("Mode")) {
            case 0:
               var10000 = 2;
               break;
            case 1:
               var10000 = 3;
               break;
            case 2:
               var10000 = 0;
               break;
            case 3:
               var10000 = 1;
               break;
            default:
               var10000 = 0;
         }

         int newMode = var10000;
         this.changeMode(player, stack, newMode);
      }

      return InteractionResultHolder.m_19090_(stack);
   }

   public void changeMode(Player player, ItemStack stack, int mode) {
      CompoundTag nbt = stack.m_41784_();
      int oldMode = nbt.m_128451_("Mode");
      if (mode != oldMode) {
         nbt.m_128405_("Mode", mode);
         if (player != null) {
            if (mode != 0 && oldMode != 3) {
               if (oldMode == 0 || mode == 3) {
                  player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.HEAL.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
               }
            } else {
               player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.UNCHARGE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
            }

         }
      }
   }

   public boolean canProvideFlight(ItemStack stack, ServerPlayer player) {
      return false;
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return false;
   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      if (!level.f_46443_ && ProjectEConfig.server.cooldown.pedestal.swrg.get() != -1) {
         if (((IDMPedestal)pedestal).getActivityCooldown() <= 0) {
            Iterator var5 = level.m_6443_(Mob.class, ((IDMPedestal)pedestal).getEffectBounds(), (ent) -> {
               boolean var10000;
               label23: {
                  if (!ent.m_5833_()) {
                     if (!(ent instanceof TamableAnimal)) {
                        break label23;
                     }

                     TamableAnimal tamableAnimal = (TamableAnimal)ent;
                     if (!tamableAnimal.m_21824_()) {
                        break label23;
                     }
                  }

                  var10000 = false;
                  return var10000;
               }

               var10000 = true;
               return var10000;
            }).iterator();

            while(var5.hasNext()) {
               Mob living = (Mob)var5.next();
               LightningBolt lightning = (LightningBolt)EntityType.f_20465_.m_20615_(level);
               if (lightning != null) {
                  lightning.m_20219_(living.m_20182_());
                  level.m_7967_(lightning);
               }
            }

            ((IDMPedestal)pedestal).setActivityCooldown(ProjectEConfig.server.cooldown.pedestal.swrg.get());
         } else {
            ((IDMPedestal)pedestal).decrementActivityCooldown();
         }
      }

      return false;
   }

   public @NotNull List getPedestalDescription() {
      List list = new ArrayList();
      if (ProjectEConfig.server.cooldown.pedestal.swrg.get() != -1) {
         list.add(PELang.PEDESTAL_SWRG_1.translateColored(ChatFormatting.BLUE, new Object[0]));
         list.add(PELang.PEDESTAL_SWRG_2.translateColored(ChatFormatting.BLUE, new Object[]{MathUtils.tickToSecFormatted(ProjectEConfig.server.cooldown.pedestal.swrg.get())}));
      }

      return list;
   }

   public boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, @Nullable InteractionHand hand) {
      EntitySWRGProjectile projectile = new EntitySWRGProjectile(player, false, player.m_9236_());
      projectile.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 1.5F, 1.0F);
      player.m_9236_().m_7967_(projectile);
      return true;
   }
}
