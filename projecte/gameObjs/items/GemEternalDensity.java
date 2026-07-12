package moze_intel.projecte.gameObjs.items;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.capability.AlchBagItemCapabilityWrapper;
import moze_intel.projecte.capability.AlchChestItemCapabilityWrapper;
import moze_intel.projecte.capability.ModeChangerItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.block_entities.EmcBlockEntity;
import moze_intel.projecte.gameObjs.container.EternalDensityContainer;
import moze_intel.projecte.gameObjs.container.inventory.EternalDensityInventory;
import moze_intel.projecte.gameObjs.registration.impl.ItemRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GemEternalDensity extends ItemPE implements IAlchBagItem, IAlchChestItem, IItemMode {
   private static final ILangEntry[] modes;

   public GemEternalDensity(Item.Properties props) {
      super(props);
      this.addItemCapability(AlchBagItemCapabilityWrapper::new);
      this.addItemCapability(AlchChestItemCapabilityWrapper::new);
      this.addItemCapability(ModeChangerItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   public void m_6883_(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slot, boolean isHeld) {
      if (!level.f_46443_ && entity instanceof Player) {
         entity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).ifPresent((inv) -> {
            condense(stack, inv);
         });
      }

   }

   private static boolean condense(ItemStack gem, IItemHandler inv) {
      if (gem.m_41784_().m_128471_("Active") && ItemPE.getEmc(gem) != Long.MAX_VALUE) {
         ItemStack target = getTarget(gem);
         long targetEmc = EMCHelper.getEmcValue(target);
         if (targetEmc == 0L) {
            return false;
         } else {
            boolean hasChanged = false;
            boolean isWhitelist = ItemHelper.checkItemNBT(gem, "Whitelist");
            List whitelist = getWhitelist(gem);

            for(int i = 0; i < inv.getSlots(); ++i) {
               ItemStack stack = inv.getStackInSlot(i);
               if (!stack.m_41619_()) {
                  Lazy filtered = Lazy.of(() -> {
                     return whitelist.stream().anyMatch((s) -> {
                        return ItemHandlerHelper.canItemStacksStack(s, stack);
                     });
                  });
                  if (stack.m_41753_() || isWhitelist && (Boolean)filtered.get()) {
                     long emcValue = EMCHelper.getEmcValue(stack);
                     if (emcValue != 0L && emcValue < targetEmc && !inv.extractItem(i, stack.m_41613_() == 1 ? 1 : stack.m_41613_() / 2, true).m_41619_() && isWhitelist == (Boolean)filtered.get()) {
                        ItemStack copy = inv.extractItem(i, stack.m_41613_() == 1 ? 1 : stack.m_41613_() / 2, false);
                        addToList(gem, copy);
                        ItemPE.addEmcToStack(gem, EMCHelper.getEmcValue(copy) * (long)copy.m_41613_());
                        hasChanged = true;
                        break;
                     }
                  }
               }
            }

            long value = EMCHelper.getEmcValue(target);
            if (value == 0L) {
               return hasChanged;
            } else {
               while(getEmc(gem) >= value) {
                  ItemStack remain = ItemHandlerHelper.insertItemStacked(inv, target.m_41777_(), false);
                  if (!remain.m_41619_()) {
                     return false;
                  }

                  ItemPE.removeEmc(gem, value);
                  setItems(gem, new ArrayList());
                  hasChanged = true;
               }

               return hasChanged;
            }
         }
      } else {
         return false;
      }
   }

   public @NotNull InteractionResultHolder m_7203_(Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!level.f_46443_) {
         if (player.m_36341_()) {
            CompoundTag nbt = stack.m_41784_();
            if (nbt.m_128471_("Active")) {
               List items = getItems(stack);
               if (!items.isEmpty()) {
                  WorldHelper.createLootDrop(items, level, player.m_20185_(), player.m_20186_(), player.m_20189_());
                  setItems(stack, new ArrayList());
                  ItemPE.setEmc(stack, 0L);
               }

               nbt.m_128379_("Active", false);
            } else {
               nbt.m_128379_("Active", true);
            }
         } else {
            NetworkHooks.openScreen((ServerPlayer)player, new ContainerProvider(hand, stack), (buf) -> {
               buf.m_130068_(hand);
               buf.writeByte(player.m_150109_().f_35977_);
            });
         }
      }

      return InteractionResultHolder.m_19090_(stack);
   }

   private static ItemStack getTarget(ItemStack stack) {
      Item item = stack.m_41720_();
      if (item instanceof GemEternalDensity) {
         GemEternalDensity gem = (GemEternalDensity)item;
         byte target = gem.getMode(stack);
         ItemStack var10000;
         switch (target) {
            case 0:
               var10000 = new ItemStack(Items.f_42416_);
               break;
            case 1:
               var10000 = new ItemStack(Items.f_42417_);
               break;
            case 2:
               var10000 = new ItemStack(Items.f_42415_);
               break;
            case 3:
               var10000 = new ItemStack(PEItems.DARK_MATTER);
               break;
            case 4:
               var10000 = new ItemStack(PEItems.RED_MATTER);
               break;
            default:
               PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Invalid target for gem of eternal density: {}", target);
               var10000 = ItemStack.f_41583_;
         }

         return var10000;
      } else {
         PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Invalid gem of eternal density: {}", stack);
         return ItemStack.f_41583_;
      }
   }

   private static void setItems(ItemStack stack, List list) {
      ListTag tList = new ListTag();
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         ItemStack s = (ItemStack)var3.next();
         CompoundTag nbt = new CompoundTag();
         s.m_41739_(nbt);
         tList.add(nbt);
      }

      stack.m_41784_().m_128365_("Consumed", tList);
   }

   private static List getItems(ItemStack stack) {
      List list = new ArrayList();
      if (stack.m_41782_()) {
         ListTag tList = stack.m_41784_().m_128437_("Consumed", 10);

         for(int i = 0; i < tList.size(); ++i) {
            list.add(ItemStack.m_41712_(tList.m_128728_(i)));
         }
      }

      return list;
   }

   private static void addToList(ItemStack gem, ItemStack stack) {
      List list = getItems(gem);
      addToList(list, stack);
      setItems(gem, list);
   }

   private static void addToList(List list, ItemStack stack) {
      boolean hasFound = false;
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         ItemStack s = (ItemStack)var3.next();
         if (s.m_41613_() < s.m_41741_() && ItemHandlerHelper.canItemStacksStack(s, stack)) {
            int remain = s.m_41741_() - s.m_41613_();
            if (stack.m_41613_() <= remain) {
               s.m_41769_(stack.m_41613_());
               hasFound = true;
               break;
            }

            s.m_41769_(remain);
            stack.m_41774_(remain);
         }
      }

      if (!hasFound) {
         list.add(stack);
      }

   }

   public @Nullable CompoundTag getShareTag(ItemStack stack) {
      if (stack.m_41720_() instanceof GemEternalDensity) {
         CompoundTag nbt = stack.m_41783_();
         return nbt != null && nbt.m_128425_("Consumed", 9) ? ItemHelper.copyNBTSkipKey(nbt, "Consumed") : nbt;
      } else {
         return super.getShareTag(stack);
      }
   }

   private static List getWhitelist(ItemStack stack) {
      if (stack.m_41782_()) {
         CompoundTag compound = stack.m_41784_().m_128469_("Items");
         ListTag list = compound.m_128437_("Items", 10);
         List result = new ArrayList(list.size());

         for(int i = 0; i < list.size(); ++i) {
            ItemStack s = ItemStack.m_41712_(list.m_128728_(i));
            if (!s.m_41619_() && result.stream().noneMatch((r) -> {
               return ItemHandlerHelper.canItemStacksStack(r, s);
            })) {
               result.add(s);
            }
         }

         return result;
      } else {
         return Collections.emptyList();
      }
   }

   public ILangEntry getModeSwitchEntry() {
      return PELang.DENSITY_MODE_TARGET;
   }

   public ILangEntry[] getModeLangEntries() {
      return modes;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(PELang.TOOLTIP_GEM_DENSITY_1.translate(new Object[0]));
      if (stack.m_41782_()) {
         tooltips.add(PELang.TOOLTIP_GEM_DENSITY_2.translate(new Object[]{this.getModeLangEntry(stack)}));
      }

      tooltips.add(PELang.TOOLTIP_GEM_DENSITY_3.translate(new Object[]{ClientKeyHelper.getKeyName(PEKeybind.MODE)}));
      tooltips.add(PELang.TOOLTIP_GEM_DENSITY_4.translate(new Object[0]));
      tooltips.add(PELang.TOOLTIP_GEM_DENSITY_5.translate(new Object[0]));
   }

   public boolean updateInAlchChest(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemStack stack) {
      if (!level.f_46443_ && ItemHelper.checkItemNBT(stack, "Active")) {
         EmcBlockEntity chest = (EmcBlockEntity)WorldHelper.getBlockEntity(EmcBlockEntity.class, level, pos, true);
         if (chest != null) {
            chest.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((inv) -> {
               if (condense(stack, inv)) {
                  chest.m_6596_();
               }

            });
         }
      }

      return false;
   }

   public boolean updateInAlchBag(@NotNull IItemHandler inv, @NotNull Player player, @NotNull ItemStack stack) {
      return !player.m_9236_().f_46443_ && condense(stack, inv);
   }

   static {
      ILangEntry[] var10000 = new ILangEntry[5];
      Item var10003 = Items.f_42416_;
      Objects.requireNonNull(var10003);
      var10000[0] = var10003::m_5524_;
      var10003 = Items.f_42417_;
      Objects.requireNonNull(var10003);
      var10000[1] = var10003::m_5524_;
      var10003 = Items.f_42415_;
      Objects.requireNonNull(var10003);
      var10000[2] = var10003::m_5524_;
      ItemRegistryObject var0 = PEItems.DARK_MATTER;
      Objects.requireNonNull(var0);
      var10000[3] = var0::getTranslationKey;
      var0 = PEItems.RED_MATTER;
      Objects.requireNonNull(var0);
      var10000[4] = var0::getTranslationKey;
      modes = var10000;
   }

   private static record ContainerProvider(InteractionHand hand, ItemStack stack) implements MenuProvider {
      private ContainerProvider(InteractionHand hand, ItemStack stack) {
         this.hand = hand;
         this.stack = stack;
      }

      public @NotNull AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
         return new EternalDensityContainer(windowId, playerInventory, this.hand, playerInventory.f_35977_, new EternalDensityInventory(this.stack));
      }

      public @NotNull Component m_5446_() {
         return TextComponentUtil.build(PEItems.GEM_OF_ETERNAL_DENSITY.get());
      }

      public InteractionHand hand() {
         return this.hand;
      }

      public ItemStack stack() {
         return this.stack;
      }
   }
}
