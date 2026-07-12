package moze_intel.projecte.gameObjs.block_entities;

import java.util.Optional;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.capability.managing.ICapabilityResolver;
import moze_intel.projecte.capability.managing.SidedItemHandlerResolver;
import moze_intel.projecte.gameObjs.EnumRelayTier;
import moze_intel.projecte.gameObjs.container.RelayMK1Container;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.PELang;
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
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RelayMK1BlockEntity extends CapabilityEmcBlockEntity implements MenuProvider {
   private final EmcBlockEntity.CompactableStackHandler input;
   private final ItemStackHandler output;
   private final long chargeRate;
   private double bonusEMC;

   public RelayMK1BlockEntity(BlockPos pos, BlockState state) {
      this(PEBlockEntityTypes.RELAY, pos, state, 7, EnumRelayTier.MK1);
   }

   RelayMK1BlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state, int sizeInv, EnumRelayTier tier) {
      super(type, pos, state, tier.getStorage());
      this.output = new EmcBlockEntity.StackHandler(1);
      this.chargeRate = tier.getChargeRate();
      this.input = new EmcBlockEntity.CompactableStackHandler(sizeInv) {
         public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return SlotPredicates.RELAY_INV.test(stack) ? super.insertItem(slot, stack, simulate) : stack;
         }
      };
      this.itemHandlerResolver = new RelayItemHandlerProvider();
   }

   public boolean isRelay() {
      return true;
   }

   private ItemStack getCharging() {
      return this.output.getStackInSlot(0);
   }

   private ItemStack getBurn() {
      return this.input.getStackInSlot(0);
   }

   public IItemHandler getInput() {
      return this.input;
   }

   public IItemHandler getOutput() {
      return this.output;
   }

   protected boolean emcAffectsComparators() {
      return true;
   }

   public static void tickServer(Level level, BlockPos pos, BlockState state, RelayMK1BlockEntity relay) {
      relay.sendEmc();
      relay.input.compact();
      ItemStack stack = relay.getBurn();
      if (!stack.m_41619_()) {
         Optional holderCapability = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
         if (holderCapability.isPresent()) {
            IItemEmcHolder emcHolder = (IItemEmcHolder)holderCapability.get();
            long simulatedVal = relay.forceInsertEmc(emcHolder.extractEmc(stack, relay.chargeRate, IEmcStorage.EmcAction.SIMULATE), IEmcStorage.EmcAction.SIMULATE);
            if (simulatedVal > 0L) {
               relay.forceInsertEmc(emcHolder.extractEmc(stack, simulatedVal, IEmcStorage.EmcAction.EXECUTE), IEmcStorage.EmcAction.EXECUTE);
            }
         } else {
            long emcVal = EMCHelper.getEmcSellValue(stack);
            if (emcVal > 0L && emcVal <= relay.getNeededEmc()) {
               relay.forceInsertEmc(emcVal, IEmcStorage.EmcAction.EXECUTE);
               relay.getBurn().m_41774_(1);
               relay.input.onContentsChanged(0);
            }
         }
      }

      ItemStack chargeable = relay.getCharging();
      if (!chargeable.m_41619_() && relay.getStoredEmc() > 0L) {
         chargeable.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).ifPresent((emcHolderx) -> {
            long actualSent = emcHolderx.insertEmc(chargeable, Math.min(relay.getStoredEmc(), relay.chargeRate), IEmcStorage.EmcAction.EXECUTE);
            relay.forceExtractEmc(actualSent, IEmcStorage.EmcAction.EXECUTE);
         });
      }

      relay.updateComparators();
   }

   private void sendEmc() {
      if (this.getStoredEmc() != 0L) {
         if (this.getStoredEmc() <= this.chargeRate) {
            this.sendToAllAcceptors(this.getStoredEmc());
         } else {
            this.sendToAllAcceptors(this.chargeRate);
         }

      }
   }

   public double getItemChargeProportion() {
      ItemStack charging = this.getCharging();
      if (!charging.m_41619_()) {
         Optional holderCapability = charging.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
         if (holderCapability.isPresent()) {
            IItemEmcHolder emcHolder = (IItemEmcHolder)holderCapability.get();
            return (double)emcHolder.getStoredEmc(charging) / (double)emcHolder.getMaximumEmc(charging);
         }
      }

      return 0.0;
   }

   public double getInputBurnProportion() {
      ItemStack burn = this.getBurn();
      if (burn.m_41619_()) {
         return 0.0;
      } else {
         Optional holderCapability = burn.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
         if (holderCapability.isPresent()) {
            IItemEmcHolder emcHolder = (IItemEmcHolder)holderCapability.get();
            return (double)emcHolder.getStoredEmc(burn) / (double)emcHolder.getMaximumEmc(burn);
         } else {
            return (double)burn.m_41613_() / (double)burn.m_41741_();
         }
      }
   }

   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.input.deserializeNBT(nbt.m_128469_("Input"));
      this.output.deserializeNBT(nbt.m_128469_("Output"));
      this.bonusEMC = nbt.m_128459_("BonusEMC");
   }

   protected void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128365_("Input", this.input.serializeNBT());
      tag.m_128365_("Output", this.output.serializeNBT());
      tag.m_128347_("BonusEMC", this.bonusEMC);
   }

   protected double getBonusToAdd() {
      return 0.05;
   }

   public void addBonus() {
      this.bonusEMC += this.getBonusToAdd();
      if (this.bonusEMC >= 1.0) {
         long emcToInsert = (long)this.bonusEMC;
         this.forceInsertEmc(emcToInsert, IEmcStorage.EmcAction.EXECUTE);
         this.bonusEMC -= (double)emcToInsert;
      }

      this.markDirty(false);
   }

   public @NotNull AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
      return new RelayMK1Container(windowId, playerInventory, this);
   }

   public @NotNull Component m_5446_() {
      return PELang.GUI_RELAY_MK1.translate(new Object[0]);
   }

   private class RelayItemHandlerProvider extends SidedItemHandlerResolver {
      private final ICapabilityResolver automationOutput;
      private final ICapabilityResolver automationInput;
      private final ICapabilityResolver joined;

      protected RelayItemHandlerProvider() {
         NonNullLazy automationInput = NonNullLazy.of(() -> {
            return new WrappedItemHandler(RelayMK1BlockEntity.this.input, WrappedItemHandler.WriteMode.IN);
         });
         NonNullLazy automationOutput = NonNullLazy.of(() -> {
            return new WrappedItemHandler(RelayMK1BlockEntity.this.output, WrappedItemHandler.WriteMode.IN_OUT) {
               public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                  return SlotPredicates.EMC_HOLDER.test(stack) ? super.insertItem(slot, stack, simulate) : stack;
               }

               public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                  ItemStack stack = this.getStackInSlot(slot);
                  if (!stack.m_41619_()) {
                     Optional holderCapability = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
                     if (holderCapability.isPresent()) {
                        IItemEmcHolder emcHolder = (IItemEmcHolder)holderCapability.get();
                        if (emcHolder.getNeededEmc(stack) == 0L) {
                           return super.extractItem(slot, amount, simulate);
                        }

                        return ItemStack.f_41583_;
                     }
                  }

                  return super.extractItem(slot, amount, simulate);
               }
            };
         });
         this.automationInput = BasicCapabilityResolver.getBasicItemHandlerResolver((NonNullSupplier)automationInput);
         this.automationOutput = BasicCapabilityResolver.getBasicItemHandlerResolver((NonNullSupplier)automationOutput);
         this.joined = BasicCapabilityResolver.getBasicItemHandlerResolver(() -> {
            return new CombinedInvWrapper(new IItemHandlerModifiable[]{(IItemHandlerModifiable)automationInput.get(), (IItemHandlerModifiable)automationOutput.get()});
         });
      }

      protected ICapabilityResolver getResolver(@Nullable Direction side) {
         if (side == null) {
            return this.joined;
         } else {
            return side.m_122434_().m_122478_() ? this.automationOutput : this.automationInput;
         }
      }

      public void invalidateAll() {
         this.joined.invalidateAll();
         this.automationInput.invalidateAll();
         this.automationOutput.invalidateAll();
      }
   }
}
