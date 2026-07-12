package moze_intel.projecte.gameObjs.container;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import moze_intel.projecte.gameObjs.block_entities.DMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.container.slots.MatterFurnaceOutputSlot;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class DMFurnaceContainer extends PEContainer {
   public final DMFurnaceBlockEntity furnace;

   public DMFurnaceContainer(int windowId, Inventory playerInv, DMFurnaceBlockEntity furnace) {
      this(PEContainerTypes.DM_FURNACE_CONTAINER, windowId, playerInv, furnace);
   }

   protected DMFurnaceContainer(ContainerTypeRegistryObject type, int windowId, Inventory playerInv, DMFurnaceBlockEntity furnace) {
      super(type, windowId, playerInv);
      this.furnace = furnace;
      this.initSlots();
      this.addDataSlot(() -> {
         return this.furnace.furnaceCookTime;
      }, (value) -> {
         this.furnace.furnaceCookTime = value;
      });
      this.addDataSlot(() -> {
         return this.furnace.furnaceBurnTime;
      }, (value) -> {
         this.furnace.furnaceBurnTime = value;
      });
      this.addDataSlot(() -> {
         return this.furnace.currentItemBurnTime;
      }, (value) -> {
         this.furnace.currentItemBurnTime = value;
      });
   }

   private void addDataSlot(final IntSupplier getter, final IntConsumer setter) {
      this.m_38895_(new DataSlot() {
         public int m_6501_() {
            return getter.getAsInt();
         }

         public void m_6422_(int value) {
            setter.accept(value);
         }
      });
   }

   void initSlots() {
      IItemHandler fuel = this.furnace.getFuel();
      IItemHandler input = this.furnace.getInput();
      IItemHandler output = this.furnace.getOutput();
      this.m_38897_(new ValidatedSlot(fuel, 0, 49, 53, SlotPredicates.FURNACE_FUEL));
      Predicate inputPredicate = (stack) -> {
         return !this.furnace.getSmeltingResult(stack).m_41619_();
      };
      this.m_38897_(new ValidatedSlot(input, 0, 49, 17, inputPredicate));
      int counter = 1;

      int i;
      int j;
      for(i = 1; i >= 0; --i) {
         for(j = 3; j >= 0; --j) {
            this.m_38897_(new ValidatedSlot(input, counter++, 13 + i * 18, 8 + j * 18, inputPredicate));
         }
      }

      counter = output.getSlots() - 1;
      this.m_38897_(new MatterFurnaceOutputSlot(this.playerInv.f_35978_, output, counter--, 109, 35));

      for(i = 0; i < 2; ++i) {
         for(j = 0; j < 4; ++j) {
            this.m_38897_(new MatterFurnaceOutputSlot(this.playerInv.f_35978_, output, counter--, 131 + i * 18, 8 + j * 18));
         }
      }

      this.addPlayerInventory(8, 84);
   }

   protected BlockRegistryObject getValidBlock() {
      return PEBlocks.DARK_MATTER_FURNACE;
   }

   public boolean m_6875_(@NotNull Player player) {
      return stillValid(player, this.furnace, this.getValidBlock());
   }
}
