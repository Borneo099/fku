package moze_intel.projecte.gameObjs.items.rings;

import com.google.common.collect.Lists;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import moze_intel.projecte.api.block_entity.IDMPedestal;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.capability.AlchBagItemCapabilityWrapper;
import moze_intel.projecte.capability.AlchChestItemCapabilityWrapper;
import moze_intel.projecte.capability.PedestalItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.block_entities.EmcBlockEntity;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class BlackHoleBand extends PEToggleItem implements IAlchBagItem, IAlchChestItem, IPedestalItem {
   public BlackHoleBand(Item.Properties props) {
      super(props);
      this.addItemCapability(AlchBagItemCapabilityWrapper::new);
      this.addItemCapability(AlchChestItemCapabilityWrapper::new);
      this.addItemCapability(PedestalItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   private InteractionResult tryPickupFluid(Level level, Player player, ItemStack stack) {
      BlockHitResult result = m_41435_(level, player, Fluid.SOURCE_ONLY);
      if (result.m_6662_() != Type.BLOCK) {
         return InteractionResult.PASS;
      } else {
         BlockPos fluidPos = result.m_82425_();
         BlockState state = level.m_8055_(fluidPos);
         if (level.m_7966_(player, fluidPos) && player.m_36204_(fluidPos, result.m_82434_(), stack)) {
            Block var8 = state.m_60734_();
            if (var8 instanceof BucketPickup) {
               BucketPickup pickup = (BucketPickup)var8;
               Optional sound = pickup.getPickupSound(state);
               ItemStack itemStack = pickup.m_142598_(level, fluidPos, state);
               if (!itemStack.m_41619_()) {
                  sound.ifPresent((soundEvent) -> {
                     player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
                  });
                  return InteractionResult.m_19078_(level.f_46443_);
               }
            }
         }

         return InteractionResult.PASS;
      }
   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      InteractionResult result = this.tryPickupFluid(level, player, stack);
      if (!result.m_19077_() && this.changeMode(player, stack, hand)) {
         result = InteractionResult.m_19078_(level.f_46443_);
      }

      return ItemHelper.actionResultFromType(result, stack);
   }

   public void m_6883_(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean held) {
      if (entity instanceof Player player) {
         if (ItemHelper.checkItemNBT(stack, "Active")) {
            Iterator var7 = level.m_45976_(ItemEntity.class, player.m_20191_().m_82400_(7.0)).iterator();

            while(var7.hasNext()) {
               ItemEntity item = (ItemEntity)var7.next();
               if (ItemHelper.simulateFit(player.m_150109_().f_35974_, item.m_32055_()) < item.m_32055_().m_41613_()) {
                  WorldHelper.gravitateEntityTowards(item, player.m_20185_(), player.m_20186_(), player.m_20189_());
               }
            }
         }
      }

   }

   public boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockEntity pedestal) {
      Map nearbyHandlers = new EnumMap(Direction.class);
      Iterator var6 = level.m_6443_(ItemEntity.class, ((IDMPedestal)pedestal).getEffectBounds(), (ent) -> {
         return !ent.m_5833_() && ent.m_6084_();
      }).iterator();

      while(true) {
         while(true) {
            ItemEntity item;
            do {
               do {
                  if (!var6.hasNext()) {
                     return false;
                  }

                  item = (ItemEntity)var6.next();
                  WorldHelper.gravitateEntityTowards(item, (double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5);
               } while(level.f_46443_);
            } while(!(item.m_20275_((double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5) < 1.21));

            Direction[] var8 = Direction.values();
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
               Direction dir = var8[var10];
               IItemHandler inv = (IItemHandler)nearbyHandlers.computeIfAbsent(dir, (direction) -> {
                  BlockEntity candidate = WorldHelper.getBlockEntity(level, pos.m_121945_(dir));
                  return candidate == null ? null : WorldHelper.getItemHandler(candidate, dir);
               });
               ItemStack result = ItemHandlerHelper.insertItemStacked(inv, item.m_32055_(), false);
               if (result.m_41619_()) {
                  item.m_146870_();
                  break;
               }

               item.m_32045_(result);
            }
         }
      }
   }

   public @NotNull List getPedestalDescription() {
      return Lists.newArrayList(new Component[]{PELang.PEDESTAL_BLACK_HOLE_BAND_1.translateColored(ChatFormatting.BLUE, new Object[0]), PELang.PEDESTAL_BLACK_HOLE_BAND_2.translateColored(ChatFormatting.BLUE, new Object[0])});
   }

   public boolean updateInAlchChest(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemStack stack) {
      if (ItemHelper.checkItemNBT(stack, "Active")) {
         EmcBlockEntity chest = (EmcBlockEntity)WorldHelper.getBlockEntity(EmcBlockEntity.class, level, pos, true);
         if (chest != null) {
            int x = pos.m_123341_();
            int y = pos.m_123342_();
            int z = pos.m_123343_();
            AABB aabb = new AABB((double)(x - 5), (double)(y - 5), (double)(z - 5), (double)(x + 5), (double)(y + 5), (double)(z + 5));
            double centeredX = (double)x + 0.5;
            double centeredY = (double)y + 0.5;
            double centeredZ = (double)z + 0.5;
            Iterator var15 = level.m_6443_(ItemEntity.class, aabb, (ent) -> {
               return !ent.m_5833_() && ent.m_6084_();
            }).iterator();

            while(var15.hasNext()) {
               ItemEntity e = (ItemEntity)var15.next();
               WorldHelper.gravitateEntityTowards(e, centeredX, centeredY, centeredZ);
               if (!level.f_46443_ && e.m_20275_(centeredX, centeredY, centeredZ) < 1.21) {
                  chest.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((inv) -> {
                     ItemStack result = ItemHandlerHelper.insertItemStacked(inv, e.m_32055_(), false);
                     if (!result.m_41619_()) {
                        e.m_32045_(result);
                     } else {
                        e.m_146870_();
                     }

                  });
               }
            }
         }
      }

      return false;
   }

   public boolean updateInAlchBag(@NotNull IItemHandler inv, @NotNull Player player, @NotNull ItemStack stack) {
      if (ItemHelper.checkItemNBT(stack, "Active")) {
         Iterator var4 = player.m_9236_().m_45976_(ItemEntity.class, player.m_20191_().m_82400_(5.0)).iterator();

         while(var4.hasNext()) {
            ItemEntity e = (ItemEntity)var4.next();
            WorldHelper.gravitateEntityTowards(e, player.m_20185_(), player.m_20186_(), player.m_20189_());
         }
      }

      return false;
   }
}
