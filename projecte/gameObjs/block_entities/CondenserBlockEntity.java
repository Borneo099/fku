package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.event.PlayerAttemptCondenserSetEvent;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.emc.nbt.NBTManager;
import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CondenserBlockEntity extends EmcChestBlockEntity {
   protected final ItemStackHandler inputInventory;
   private final ItemStackHandler outputInventory;
   private @Nullable ItemInfo lockInfo;
   private boolean isAcceptingEmc;
   public long displayEmc;
   public long requiredEmc;
   private int loadIndex;

   public CondenserBlockEntity(BlockPos pos, BlockState state) {
      this(PEBlockEntityTypes.CONDENSER, pos, state);
   }

   protected CondenserBlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.inputInventory = this.createInput();
      this.outputInventory = this.createOutput();
      this.loadIndex = EMCMappingHandler.getLoadIndex() - 1;
      this.itemHandlerResolver = BasicCapabilityResolver.getBasicItemHandlerResolver(this::createAutomationInventory);
   }

   protected boolean canAcceptEmc() {
      return this.isAcceptingEmc;
   }

   protected boolean canProvideEmc() {
      return false;
   }

   public final @Nullable ItemInfo getLockInfo() {
      return this.requiredEmc != 0L || this.f_58857_ != null && this.f_58857_.f_46443_ ? this.lockInfo : null;
   }

   public ItemStackHandler getInput() {
      return this.inputInventory;
   }

   public ItemStackHandler getOutput() {
      return this.outputInventory;
   }

   protected ItemStackHandler createInput() {
      return new EmcBlockEntity.StackHandler(91);
   }

   protected ItemStackHandler createOutput() {
      return this.inputInventory;
   }

   protected @NotNull IItemHandler createAutomationInventory() {
      return new WrappedItemHandler(this.inputInventory, WrappedItemHandler.WriteMode.IN_OUT) {
         public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return SlotPredicates.HAS_EMC.test(stack) && !CondenserBlockEntity.this.isStackEqualToLock(stack) ? super.insertItem(slot, stack, simulate) : stack;
         }

         public @NotNull ItemStack extractItem(int slot, int max, boolean simulate) {
            return !this.getStackInSlot(slot).m_41619_() && CondenserBlockEntity.this.isStackEqualToLock(this.getStackInSlot(slot)) ? super.extractItem(slot, max, simulate) : ItemStack.f_41583_;
         }
      };
   }

   public static void tickServer(Level level, BlockPos pos, BlockState state, CondenserBlockEntity condenser) {
      condenser.checkLockAndUpdate(false);
      condenser.displayEmc = condenser.getStoredEmc();
      if (condenser.getLockInfo() != null) {
         condenser.condense();
      }

      condenser.updateComparators();
   }

   private void checkLockAndUpdate(boolean force) {
      if (force || this.loadIndex != EMCMappingHandler.getLoadIndex()) {
         this.loadIndex = EMCMappingHandler.getLoadIndex();
         if (this.lockInfo != null) {
            long lockEmc = EMCHelper.getEmcValue(this.lockInfo);
            if (lockEmc > 0L) {
               if (this.requiredEmc != lockEmc) {
                  this.requiredEmc = lockEmc;
                  this.isAcceptingEmc = true;
               }

               return;
            }
         }

         this.displayEmc = 0L;
         this.requiredEmc = 0L;
         this.isAcceptingEmc = false;
      }
   }

   protected void condense() {
      for(int i = 0; i < this.inputInventory.getSlots(); ++i) {
         ItemStack stack = this.inputInventory.getStackInSlot(i);
         if (!stack.m_41619_() && !this.isStackEqualToLock(stack)) {
            this.inputInventory.extractItem(i, 1, false);
            this.forceInsertEmc(EMCHelper.getEmcSellValue(stack), IEmcStorage.EmcAction.EXECUTE);
            break;
         }
      }

      if (this.getStoredEmc() >= this.requiredEmc && this.hasSpace()) {
         this.forceExtractEmc(this.requiredEmc, IEmcStorage.EmcAction.EXECUTE);
         this.pushStack();
      }

   }

   protected final void pushStack() {
      ItemInfo lockInfo = this.getLockInfo();
      if (lockInfo != null) {
         ItemHandlerHelper.insertItemStacked(this.outputInventory, lockInfo.createStack(), false);
      }

   }

   protected boolean hasSpace() {
      for(int i = 0; i < this.outputInventory.getSlots(); ++i) {
         ItemStack stack = this.outputInventory.getStackInSlot(i);
         if (stack.m_41619_() || this.isStackEqualToLock(stack) && stack.m_41613_() < stack.m_41741_()) {
            return true;
         }
      }

      return false;
   }

   public boolean isStackEqualToLock(ItemStack stack) {
      ItemInfo lockInfo = this.getLockInfo();
      return lockInfo != null && !stack.m_41619_() ? lockInfo.equals(NBTManager.getPersistentInfo(ItemInfo.fromStack(stack))) : false;
   }

   public void setLockInfoFromPacket(@Nullable ItemInfo lockInfo) {
      this.lockInfo = lockInfo;
   }

   public boolean attemptCondenserSet(Player player) {
      if (this.f_58857_ != null && !this.f_58857_.f_46443_) {
         if (this.getLockInfo() == null) {
            ItemStack stack = player.f_36096_.m_142621_();
            if (!stack.m_41619_()) {
               ItemInfo sourceInfo = ItemInfo.fromStack(stack);
               ItemInfo reducedInfo = NBTManager.getPersistentInfo(sourceInfo);
               if (!MinecraftForge.EVENT_BUS.post(new PlayerAttemptCondenserSetEvent(player, sourceInfo, reducedInfo))) {
                  this.lockInfo = reducedInfo;
                  this.checkLockAndUpdate(true);
                  this.markDirty(false);
                  return true;
               }

               return false;
            }

            if (this.lockInfo == null) {
               return false;
            }
         }

         this.lockInfo = null;
         this.checkLockAndUpdate(true);
         this.markDirty(false);
         return true;
      } else {
         return false;
      }
   }

   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.inputInventory.deserializeNBT(nbt.m_128469_("Input"));
      this.lockInfo = ItemInfo.read(nbt.m_128469_("LockInfo"));
   }

   protected void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128365_("Input", this.inputInventory.serializeNBT());
      if (this.lockInfo != null) {
         tag.m_128365_("LockInfo", this.lockInfo.write(new CompoundTag()));
      }

   }

   public AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerIn) {
      return new CondenserContainer(windowId, playerInventory, this);
   }

   public @NotNull Component m_5446_() {
      return TextComponentUtil.build(PEBlocks.CONDENSER);
   }
}
