package moze_intel.projecte.gameObjs.block_entities;

import java.util.Optional;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.capability.managing.ICapabilityResolver;
import moze_intel.projecte.capability.managing.SidedItemHandlerResolver;
import moze_intel.projecte.gameObjs.blocks.MatterFurnace;
import moze_intel.projecte.gameObjs.container.DMFurnaceContainer;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags.Items;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class DMFurnaceBlockEntity extends CapabilityEmcBlockEntity implements MenuProvider {
   private static final long EMC_CONSUMPTION = 2L;
   private final EmcBlockEntity.CompactableStackHandler inputInventory;
   private final EmcBlockEntity.CompactableStackHandler outputInventory;
   private final EmcBlockEntity.StackHandler fuelInv;
   protected final int ticksBeforeSmelt;
   private final int efficiencyBonus;
   private final RecipeWrapper dummyFurnace;
   public int furnaceBurnTime;
   public int currentItemBurnTime;
   public int furnaceCookTime;

   public DMFurnaceBlockEntity(BlockPos pos, BlockState state) {
      this(PEBlockEntityTypes.DARK_MATTER_FURNACE, pos, state, 10, 3);
   }

   protected DMFurnaceBlockEntity(BlockEntityTypeRegistryObject type, BlockPos pos, BlockState state, int ticksBeforeSmelt, int efficiencyBonus) {
      super(type, pos, state, 64L);
      this.inputInventory = new EmcBlockEntity.CompactableStackHandler(this.getInvSize());
      this.outputInventory = new EmcBlockEntity.CompactableStackHandler(this.getInvSize());
      this.fuelInv = new EmcBlockEntity.StackHandler(1);
      this.dummyFurnace = new RecipeWrapper(new ItemStackHandler());
      this.ticksBeforeSmelt = ticksBeforeSmelt;
      this.efficiencyBonus = efficiencyBonus;
      this.itemHandlerResolver = new DMFurnaceItemHandlerProvider();
   }

   protected boolean canProvideEmc() {
      return false;
   }

   protected @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getEmcInsertLimit() {
      return 2L;
   }

   protected int getInvSize() {
      return 9;
   }

   protected float getOreDoubleChance() {
      return 0.5F;
   }

   protected float getRawOreDoubleChance() {
      return this.getOreDoubleChance() * 2.0F / 3.0F;
   }

   public int getCookProgressScaled(int value) {
      return this.furnaceCookTime * value / this.ticksBeforeSmelt;
   }

   public @NotNull AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInv, @NotNull Player playerIn) {
      return new DMFurnaceContainer(windowId, playerInv, this);
   }

   public @NotNull Component m_5446_() {
      return PELang.GUI_DARK_MATTER_FURNACE.translate(new Object[0]);
   }

   public IItemHandler getFuel() {
      return this.fuelInv;
   }

   private ItemStack getFuelItem() {
      return this.fuelInv.getStackInSlot(0);
   }

   public IItemHandler getInput() {
      return this.inputInventory;
   }

   public IItemHandler getOutput() {
      return this.outputInventory;
   }

   public static void tickServer(Level level, BlockPos pos, BlockState state, DMFurnaceBlockEntity furnace) {
      boolean wasBurning = furnace.isBurning();
      int lastFurnaceBurnTime = furnace.furnaceBurnTime;
      int lastFurnaceCookTime = furnace.furnaceCookTime;
      if (furnace.isBurning()) {
         --furnace.furnaceBurnTime;
      }

      furnace.inputInventory.compact();
      furnace.outputInventory.compact();
      furnace.pullFromInventories();
      boolean canSmelt = furnace.canSmelt();
      ItemStack fuelItem = furnace.getFuelItem();
      if (canSmelt && !fuelItem.m_41619_()) {
         fuelItem.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).ifPresent((emcHolder) -> {
            long simulatedExtraction = emcHolder.extractEmc(fuelItem, 2L, IEmcStorage.EmcAction.SIMULATE);
            if (simulatedExtraction == 2L) {
               furnace.forceInsertEmc(emcHolder.extractEmc(fuelItem, simulatedExtraction, IEmcStorage.EmcAction.EXECUTE), IEmcStorage.EmcAction.EXECUTE);
            }

            furnace.markDirty(false);
         });
      }

      if (furnace.getStoredEmc() >= 2L) {
         furnace.furnaceBurnTime = 1;
         furnace.forceExtractEmc(2L, IEmcStorage.EmcAction.EXECUTE);
      }

      if (canSmelt) {
         if (furnace.furnaceBurnTime == 0) {
            furnace.currentItemBurnTime = furnace.furnaceBurnTime = furnace.getItemBurnTime(fuelItem);
            if (furnace.isBurning() && !fuelItem.m_41619_()) {
               ItemStack copy = fuelItem.m_41777_();
               fuelItem.m_41774_(1);
               furnace.fuelInv.onContentsChanged(0);
               if (fuelItem.m_41619_()) {
                  furnace.fuelInv.setStackInSlot(0, copy.m_41720_().getCraftingRemainingItem(copy));
               }

               furnace.markDirty(false);
            }
         }

         if (furnace.isBurning() && ++furnace.furnaceCookTime == furnace.ticksBeforeSmelt) {
            furnace.furnaceCookTime = 0;
            furnace.smeltItem();
         }
      }

      if (wasBurning != furnace.isBurning()) {
         if (state.m_60734_() instanceof MatterFurnace) {
            level.m_46597_(pos, (BlockState)state.m_61124_(MatterFurnace.f_48684_, furnace.isBurning()));
         }

         furnace.m_6596_();
      }

      furnace.pushToInventories();
      if (lastFurnaceBurnTime != furnace.furnaceBurnTime || lastFurnaceCookTime != furnace.furnaceCookTime) {
         furnace.markDirty(false);
      }

      furnace.updateComparators();
   }

   public boolean isBurning() {
      return this.furnaceBurnTime > 0;
   }

   private void pullFromInventories() {
      BlockEntity blockEntity = WorldHelper.getBlockEntity(this.f_58857_, this.f_58858_.m_7494_());
      if (blockEntity != null && !(blockEntity instanceof HopperBlockEntity) && !(blockEntity instanceof DropperBlockEntity)) {
         IItemHandler handler = WorldHelper.getItemHandler(blockEntity, Direction.DOWN);
         if (handler != null) {
            for(int i = 0; i < handler.getSlots(); ++i) {
               ItemStack extractTest = handler.extractItem(i, Integer.MAX_VALUE, true);
               if (!extractTest.m_41619_()) {
                  IItemHandler targetInv = !AbstractFurnaceBlockEntity.m_58399_(extractTest) && !extractTest.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).isPresent() ? this.inputInventory : this.fuelInv;
                  this.transferItem((IItemHandler)targetInv, i, extractTest, handler);
               }
            }

         }
      }
   }

   private void pushToInventories() {
      if (!this.outputInventory.isEmpty()) {
         BlockEntity blockEntity = WorldHelper.getBlockEntity(this.f_58857_, this.f_58858_.m_7495_());
         if (blockEntity != null && !(blockEntity instanceof HopperBlockEntity)) {
            IItemHandler targetInv = WorldHelper.getItemHandler(blockEntity, Direction.UP);
            if (targetInv != null) {
               for(int i = 0; i < this.outputInventory.getSlots(); ++i) {
                  ItemStack extractTest = this.outputInventory.extractItem(i, Integer.MAX_VALUE, true);
                  if (!extractTest.m_41619_()) {
                     this.transferItem(targetInv, i, extractTest, this.outputInventory);
                  }
               }

            }
         }
      }
   }

   private void transferItem(IItemHandler targetInv, int i, ItemStack extractTest, IItemHandler outputInventory) {
      ItemStack remainderTest = ItemHandlerHelper.insertItemStacked(targetInv, extractTest, true);
      int successfullyTransferred = extractTest.m_41613_() - remainderTest.m_41613_();
      if (successfullyTransferred > 0) {
         ItemStack toInsert = outputInventory.extractItem(i, successfullyTransferred, false);
         ItemStack result = ItemHandlerHelper.insertItemStacked(targetInv, toInsert, false);

         assert result.m_41619_();
      }

   }

   public ItemStack getSmeltingResult(ItemStack in) {
      this.dummyFurnace.m_6836_(0, in);
      Optional recipe = this.f_58857_.m_7465_().m_44015_(RecipeType.f_44108_, this.dummyFurnace, this.f_58857_);
      this.dummyFurnace.m_6836_(0, ItemStack.f_41583_);
      return (ItemStack)recipe.map((r) -> {
         return r.m_8043_(this.f_58857_.m_9598_());
      }).orElse(ItemStack.f_41583_);
   }

   private void smeltItem() {
      ItemStack toSmelt = this.inputInventory.getStackInSlot(0);
      ItemStack smeltResult = this.getSmeltingResult(toSmelt).m_41777_();
      if (toSmelt.m_204117_(Items.ORES)) {
         if (this.f_58857_ != null && this.f_58857_.f_46441_.m_188501_() < this.getOreDoubleChance()) {
            smeltResult.m_41769_(smeltResult.m_41613_());
         }
      } else if (toSmelt.m_204117_(Items.RAW_MATERIALS) && this.f_58857_ != null && this.f_58857_.f_46441_.m_188501_() < this.getRawOreDoubleChance()) {
         smeltResult.m_41769_(smeltResult.m_41613_());
      }

      ItemHandlerHelper.insertItemStacked(this.outputInventory, smeltResult, false);
      toSmelt.m_41774_(1);
      this.inputInventory.onContentsChanged(0);
   }

   protected boolean canSmelt() {
      ItemStack toSmelt = this.inputInventory.getStackInSlot(0);
      if (toSmelt.m_41619_()) {
         return false;
      } else {
         ItemStack smeltResult = this.getSmeltingResult(toSmelt);
         if (smeltResult.m_41619_()) {
            return false;
         } else {
            ItemStack currentSmelted = this.outputInventory.getStackInSlot(this.outputInventory.getSlots() - 1);
            if (currentSmelted.m_41619_()) {
               return true;
            } else if (!ItemHandlerHelper.canItemStacksStack(smeltResult, currentSmelted)) {
               return false;
            } else {
               int result = currentSmelted.m_41613_() + smeltResult.m_41613_();
               return result <= currentSmelted.m_41741_();
            }
         }
      }
   }

   private int getItemBurnTime(ItemStack stack) {
      return ForgeHooks.getBurnTime(stack, RecipeType.f_44108_) * this.ticksBeforeSmelt / 200 * this.efficiencyBonus;
   }

   public int getBurnTimeRemainingScaled(int value) {
      if (this.currentItemBurnTime == 0) {
         this.currentItemBurnTime = this.ticksBeforeSmelt;
      }

      return this.furnaceBurnTime * value / this.currentItemBurnTime;
   }

   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.furnaceBurnTime = nbt.m_128451_("BurnTime");
      this.furnaceCookTime = nbt.m_128451_("CookTime");
      this.inputInventory.deserializeNBT(nbt.m_128469_("Input"));
      this.outputInventory.deserializeNBT(nbt.m_128469_("Output"));
      this.fuelInv.deserializeNBT(nbt.m_128469_("Fuel"));
      this.currentItemBurnTime = this.getItemBurnTime(this.getFuelItem());
   }

   protected void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128405_("BurnTime", this.furnaceBurnTime);
      tag.m_128405_("CookTime", this.furnaceCookTime);
      tag.m_128365_("Input", this.inputInventory.serializeNBT());
      tag.m_128365_("Output", this.outputInventory.serializeNBT());
      tag.m_128365_("Fuel", this.fuelInv.serializeNBT());
   }

   private class DMFurnaceItemHandlerProvider extends SidedItemHandlerResolver {
      private final ICapabilityResolver joined;
      private final ICapabilityResolver automationInput;
      private final ICapabilityResolver automationOutput;
      private final ICapabilityResolver automationSides;

      protected DMFurnaceItemHandlerProvider() {
         NonNullLazy automationInput = NonNullLazy.of(() -> {
            return new WrappedItemHandler(DMFurnaceBlockEntity.this.inputInventory, WrappedItemHandler.WriteMode.IN) {
               public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                  return !DMFurnaceBlockEntity.this.getSmeltingResult(stack).m_41619_() ? super.insertItem(slot, stack, simulate) : stack;
               }
            };
         });
         NonNullLazy automationFuel = NonNullLazy.of(() -> {
            return new WrappedItemHandler(DMFurnaceBlockEntity.this.fuelInv, WrappedItemHandler.WriteMode.IN) {
               public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                  return SlotPredicates.FURNACE_FUEL.test(stack) ? super.insertItem(slot, stack, simulate) : stack;
               }
            };
         });
         NonNullLazy automationOutput = NonNullLazy.of(() -> {
            return new WrappedItemHandler(DMFurnaceBlockEntity.this.outputInventory, WrappedItemHandler.WriteMode.OUT);
         });
         this.joined = BasicCapabilityResolver.getBasicItemHandlerResolver(() -> {
            return new CombinedInvWrapper(new IItemHandlerModifiable[]{(IItemHandlerModifiable)automationInput.get(), (IItemHandlerModifiable)automationFuel.get(), (IItemHandlerModifiable)automationOutput.get()});
         });
         this.automationInput = BasicCapabilityResolver.getBasicItemHandlerResolver((NonNullSupplier)automationInput);
         this.automationOutput = BasicCapabilityResolver.getBasicItemHandlerResolver((NonNullSupplier)automationOutput);
         this.automationSides = BasicCapabilityResolver.getBasicItemHandlerResolver(() -> {
            return new CombinedInvWrapper(new IItemHandlerModifiable[]{(IItemHandlerModifiable)automationFuel.get(), (IItemHandlerModifiable)automationOutput.get()});
         });
      }

      protected ICapabilityResolver getResolver(@Nullable Direction side) {
         if (side == null) {
            return this.joined;
         } else if (side == Direction.UP) {
            return this.automationInput;
         } else {
            return side == Direction.DOWN ? this.automationOutput : this.automationSides;
         }
      }

      public void invalidateAll() {
         this.joined.invalidateAll();
         this.automationInput.invalidateAll();
         this.automationOutput.invalidateAll();
         this.automationSides.invalidateAll();
      }
   }
}
