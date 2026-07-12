package moze_intel.projecte.gameObjs.block_entities;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.gameObjs.container.AlchChestContainer;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
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
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class AlchBlockEntityChest extends EmcChestBlockEntity {
   private final EmcBlockEntity.StackHandler inventory = new EmcBlockEntity.StackHandler(104) {
      public void onContentsChanged(int slot) {
         super.onContentsChanged(slot);
         if (AlchBlockEntityChest.this.f_58857_ != null && !AlchBlockEntityChest.this.f_58857_.f_46443_) {
            AlchBlockEntityChest.this.inventoryChanged = true;
         }

      }
   };
   private boolean inventoryChanged;

   public AlchBlockEntityChest(BlockPos pos, BlockState state) {
      super(PEBlockEntityTypes.ALCHEMICAL_CHEST, pos, state, 1000L);
      this.itemHandlerResolver = BasicCapabilityResolver.getBasicItemHandlerResolver((IItemHandler)this.inventory);
   }

   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.inventory.deserializeNBT(nbt);
   }

   protected void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128391_(this.inventory.serializeNBT());
   }

   public static void tickClient(Level level, BlockPos pos, BlockState state, AlchBlockEntityChest alchChest) {
      for(int i = 0; i < alchChest.inventory.getSlots(); ++i) {
         ItemStack stack = alchChest.inventory.getStackInSlot(i);
         if (!stack.m_41619_()) {
            stack.getCapability(PECapabilities.ALCH_CHEST_ITEM_CAPABILITY).ifPresent((alchChestItem) -> {
               alchChestItem.updateInAlchChest(level, pos, stack);
            });
         }
      }

      EmcChestBlockEntity.lidAnimateTick(level, pos, state, alchChest);
   }

   public static void tickServer(Level level, BlockPos pos, BlockState state, AlchBlockEntityChest alchChest) {
      for(int i = 0; i < alchChest.inventory.getSlots(); ++i) {
         ItemStack stack = alchChest.inventory.getStackInSlot(i);
         if (!stack.m_41619_()) {
            stack.getCapability(PECapabilities.ALCH_CHEST_ITEM_CAPABILITY).ifPresent((alchChestItem) -> {
               if (alchChestItem.updateInAlchChest(level, pos, stack)) {
                  alchChest.inventory.onContentsChanged(i);
               }

            });
         }
      }

      if (alchChest.inventoryChanged) {
         alchChest.inventoryChanged = false;
         level.m_7260_(pos, state, state, 2);
      }

      alchChest.updateComparators();
   }

   public @NotNull AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerIn) {
      return new AlchChestContainer(windowId, playerInventory, this);
   }

   public @NotNull Component m_5446_() {
      return TextComponentUtil.build(PEBlocks.ALCHEMICAL_CHEST);
   }
}
