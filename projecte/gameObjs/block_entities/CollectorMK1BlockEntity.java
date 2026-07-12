package moze_intel.projecte.gameObjs.block_entities;

import java.util.Optional;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.capability.managing.ICapabilityResolver;
import moze_intel.projecte.capability.managing.SidedItemHandlerResolver;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.EnumCollectorTier;
import moze_intel.projecte.gameObjs.container.CollectorMK1Container;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class CollectorMK1BlockEntity extends CapabilityEmcBlockEntity implements MenuProvider {
   private final ItemStackHandler input;
   private final EmcBlockEntity.StackHandler auxSlots;
   private final CombinedInvWrapper toSort;
   public static final int UPGRADING_SLOT = 0;
   public static final int UPGRADE_SLOT = 1;
   public static final int LOCK_SLOT = 2;
   private final long emcGen;
   private boolean hasChargeableItem;
   private boolean hasFuel;
   private double unprocessedEMC;
   private boolean needsCompacting;

   public CollectorMK1BlockEntity(BlockPos pos, BlockState state) {
      this(PEBlockEntityTypes.COLLECTOR, pos, state, EnumCollectorTier.MK1);
   }

   public CollectorMK1BlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state, EnumCollectorTier tier) {
      super(type, pos, state, tier.getStorage());
      this.input = new EmcBlockEntity.StackHandler(this.getInvSize()) {
         protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            CollectorMK1BlockEntity.this.needsCompacting = true;
         }
      };
      this.auxSlots = new EmcBlockEntity.StackHandler(3) {
         protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (slot == 0) {
               CollectorMK1BlockEntity.this.needsCompacting = true;
            }

         }
      };
      this.toSort = new CombinedInvWrapper(new IItemHandlerModifiable[]{new RangedWrapper(this.auxSlots, 0, 1), this.input});
      this.needsCompacting = true;
      this.emcGen = tier.getGenRate();
      this.itemHandlerResolver = new CollectorItemHandlerProvider();
   }

   protected boolean canAcceptEmc() {
      return this.hasFuel || this.hasChargeableItem;
   }

   public IItemHandler getInput() {
      return this.input;
   }

   public IItemHandler getAux() {
      return this.auxSlots;
   }

   protected int getInvSize() {
      return 8;
   }

   private ItemStack getUpgraded() {
      return this.auxSlots.getStackInSlot(1);
   }

   private ItemStack getLock() {
      return this.auxSlots.getStackInSlot(2);
   }

   private ItemStack getUpgrading() {
      return this.auxSlots.getStackInSlot(0);
   }

   public void clearLocked() {
      this.auxSlots.setStackInSlot(2, ItemStack.f_41583_);
   }

   protected boolean emcAffectsComparators() {
      return true;
   }

   public static void tickServer(Level level, BlockPos pos, BlockState state, CollectorMK1BlockEntity collector) {
      if (collector.needsCompacting) {
         ItemHelper.compactInventory(collector.toSort);
         collector.needsCompacting = false;
      }

      collector.checkFuelOrKlein();
      collector.updateEmc();
      collector.rotateUpgraded();
      collector.updateComparators();
   }

   private void rotateUpgraded() {
      ItemStack upgraded = this.getUpgraded();
      if (!upgraded.m_41619_() && (this.getLock().m_41619_() || upgraded.m_41720_() != this.getLock().m_41720_() || upgraded.m_41613_() >= upgraded.m_41741_())) {
         this.auxSlots.setStackInSlot(1, ItemHandlerHelper.insertItemStacked(this.input, upgraded.m_41777_(), false));
      }

   }

   private void checkFuelOrKlein() {
      ItemStack upgrading = this.getUpgrading();
      if (!upgrading.m_41619_()) {
         Optional emcHolder = upgrading.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
         if (emcHolder.isPresent()) {
            if (((IItemEmcHolder)emcHolder.get()).getNeededEmc(upgrading) > 0L) {
               this.hasChargeableItem = true;
               this.hasFuel = false;
            } else {
               this.hasChargeableItem = false;
            }
         } else {
            this.hasFuel = true;
            this.hasChargeableItem = false;
         }
      } else {
         this.hasFuel = false;
         this.hasChargeableItem = false;
      }

   }

   private void updateEmc() {
      if (!this.hasMaxedEmc()) {
         this.unprocessedEMC += (double)((float)this.emcGen * ((float)this.getSunLevel() / 320.0F));
         if (this.unprocessedEMC >= 1.0) {
            this.unprocessedEMC -= (double)this.forceInsertEmc((long)this.unprocessedEMC, IEmcStorage.EmcAction.EXECUTE);
         }

         this.markDirty(false);
      }

      if (this.getStoredEmc() > 0L) {
         ItemStack upgrading = this.getUpgrading();
         if (this.hasChargeableItem) {
            upgrading.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).ifPresent((emcHolder) -> {
               long actualInserted = emcHolder.insertEmc(upgrading, Math.min(this.getStoredEmc(), this.emcGen), IEmcStorage.EmcAction.EXECUTE);
               this.forceExtractEmc(actualInserted, IEmcStorage.EmcAction.EXECUTE);
            });
         } else if (this.hasFuel) {
            if (FuelMapper.getFuelUpgrade(upgrading).m_41619_()) {
               this.auxSlots.setStackInSlot(0, ItemStack.f_41583_);
            }

            ItemStack result = this.getLock().m_41619_() ? FuelMapper.getFuelUpgrade(upgrading) : this.getLock().m_41777_();
            long upgradeCost = EMCHelper.getEmcValue(result) - EMCHelper.getEmcValue(upgrading);
            if (upgradeCost >= 0L && this.getStoredEmc() >= upgradeCost) {
               ItemStack upgrade = this.getUpgraded();
               if (this.getUpgraded().m_41619_()) {
                  this.forceExtractEmc(upgradeCost, IEmcStorage.EmcAction.EXECUTE);
                  this.auxSlots.setStackInSlot(1, result);
                  upgrading.m_41774_(1);
               } else if (result.m_41720_() == upgrade.m_41720_() && upgrade.m_41613_() < upgrade.m_41741_()) {
                  this.forceExtractEmc(upgradeCost, IEmcStorage.EmcAction.EXECUTE);
                  this.getUpgraded().m_41769_(1);
                  upgrading.m_41774_(1);
                  this.auxSlots.onContentsChanged(1);
               }
            }
         } else {
            long toSend = this.getStoredEmc() < this.emcGen ? this.getStoredEmc() : this.emcGen;
            this.sendToAllAcceptors(toSend);
            this.sendRelayBonus();
         }
      }

   }

   public @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcToNextGoal() {
      ItemStack lock = this.getLock();
      ItemStack upgrading = this.getUpgrading();
      long targetEmc;
      if (lock.m_41619_()) {
         targetEmc = EMCHelper.getEmcValue(FuelMapper.getFuelUpgrade(upgrading));
      } else {
         targetEmc = EMCHelper.getEmcValue(lock);
      }

      return Math.max(targetEmc - EMCHelper.getEmcValue(upgrading), 0L);
   }

   public long getItemCharge() {
      ItemStack upgrading = this.getUpgrading();
      return !upgrading.m_41619_() ? (Long)upgrading.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).map((emcHolder) -> {
         return emcHolder.getStoredEmc(upgrading);
      }).orElse(-1L) : -1L;
   }

   public double getItemChargeProportion() {
      ItemStack upgrading = this.getUpgrading();
      long charge = this.getItemCharge();
      if (!upgrading.m_41619_() && charge > 0L) {
         Optional emcHolder = upgrading.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
         if (emcHolder.isPresent()) {
            long max = ((IItemEmcHolder)emcHolder.get()).getMaximumEmc(upgrading);
            return charge >= max ? 1.0 : (double)charge / (double)max;
         } else {
            return -1.0;
         }
      } else {
         return -1.0;
      }
   }

   public int getSunLevel() {
      return this.f_58857_.m_6042_().f_63857_() ? 16 : this.f_58857_.m_46803_(this.f_58858_.m_7494_()) + 1;
   }

   public double getFuelProgress() {
      if (!this.getUpgrading().m_41619_() && FuelMapper.isStackFuel(this.getUpgrading())) {
         long reqEmc;
         if (!this.getLock().m_41619_()) {
            reqEmc = EMCHelper.getEmcValue(this.getLock()) - EMCHelper.getEmcValue(this.getUpgrading());
            if (reqEmc < 0L) {
               return 0.0;
            }
         } else {
            if (FuelMapper.getFuelUpgrade(this.getUpgrading()).m_41619_()) {
               this.auxSlots.setStackInSlot(0, ItemStack.f_41583_);
               return 0.0;
            }

            reqEmc = EMCHelper.getEmcValue(FuelMapper.getFuelUpgrade(this.getUpgrading())) - EMCHelper.getEmcValue(this.getUpgrading());
         }

         return this.getStoredEmc() >= reqEmc ? 1.0 : (double)this.getStoredEmc() / (double)reqEmc;
      } else {
         return 0.0;
      }
   }

   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.input.deserializeNBT(nbt.m_128469_("Input"));
      this.auxSlots.deserializeNBT(nbt.m_128469_("AuxSlots"));
      this.unprocessedEMC = nbt.m_128459_("UnprocessedEMC");
   }

   protected void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128365_("Input", this.input.serializeNBT());
      tag.m_128365_("AuxSlots", this.auxSlots.serializeNBT());
      tag.m_128347_("UnprocessedEMC", this.unprocessedEMC);
   }

   private void sendRelayBonus() {
      Direction[] var1 = Direction.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Direction dir = var1[var3];
         RelayMK1BlockEntity relay = (RelayMK1BlockEntity)WorldHelper.getBlockEntity(RelayMK1BlockEntity.class, this.f_58857_, this.f_58858_.m_121945_(dir));
         if (relay != null) {
            relay.addBonus();
         }
      }

   }

   public @NotNull AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerIn) {
      return new CollectorMK1Container(windowId, playerInventory, this);
   }

   public @NotNull Component m_5446_() {
      return TextComponentUtil.build(PEBlocks.COLLECTOR);
   }

   private class CollectorItemHandlerProvider extends SidedItemHandlerResolver {
      private final ICapabilityResolver automationAuxSlots;
      private final ICapabilityResolver automationInput;
      private final ICapabilityResolver joined;

      protected CollectorItemHandlerProvider() {
         NonNullLazy automationInput = NonNullLazy.of(() -> {
            return new WrappedItemHandler(CollectorMK1BlockEntity.this.input, WrappedItemHandler.WriteMode.IN) {
               public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                  return SlotPredicates.COLLECTOR_INV.test(stack) ? super.insertItem(slot, stack, simulate) : stack;
               }
            };
         });
         NonNullLazy automationAuxSlots = NonNullLazy.of(() -> {
            return new WrappedItemHandler(CollectorMK1BlockEntity.this.auxSlots, WrappedItemHandler.WriteMode.OUT) {
               public @NotNull ItemStack extractItem(int slot, int count, boolean simulate) {
                  return slot == 1 ? super.extractItem(slot, count, simulate) : ItemStack.f_41583_;
               }
            };
         });
         this.automationInput = BasicCapabilityResolver.getBasicItemHandlerResolver((NonNullSupplier)automationInput);
         this.automationAuxSlots = BasicCapabilityResolver.getBasicItemHandlerResolver((NonNullSupplier)automationAuxSlots);
         this.joined = BasicCapabilityResolver.getBasicItemHandlerResolver(() -> {
            return new CombinedInvWrapper(new IItemHandlerModifiable[]{(IItemHandlerModifiable)automationInput.get(), (IItemHandlerModifiable)automationAuxSlots.get()});
         });
      }

      protected ICapabilityResolver getResolver(@Nullable Direction side) {
         if (side == null) {
            return this.joined;
         } else {
            return side.m_122434_().m_122478_() ? this.automationAuxSlots : this.automationInput;
         }
      }

      public void invalidateAll() {
         this.joined.invalidateAll();
         this.automationInput.invalidateAll();
         this.automationAuxSlots.invalidateAll();
      }
   }
}
