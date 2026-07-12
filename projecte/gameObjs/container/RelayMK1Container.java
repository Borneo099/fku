package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.block_entities.RelayMK1BlockEntity;
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

public class RelayMK1Container extends PEContainer {
   public final RelayMK1BlockEntity relay;
   private final DataSlot kleinChargeProgress;
   private final DataSlot inputBurnProgress;
   public final PEContainer.BoxedLong emc;

   public RelayMK1Container(int windowId, Inventory playerInv, RelayMK1BlockEntity relay) {
      this(PEContainerTypes.RELAY_MK1_CONTAINER, windowId, playerInv, relay);
   }

   protected RelayMK1Container(ContainerTypeRegistryObject type, int windowId, Inventory playerInv, RelayMK1BlockEntity relay) {
      super(type, windowId, playerInv);
      this.kleinChargeProgress = DataSlot.m_39401_();
      this.inputBurnProgress = DataSlot.m_39401_();
      this.emc = new PEContainer.BoxedLong();
      this.longFields.add(this.emc);
      this.m_38895_(this.kleinChargeProgress);
      this.m_38895_(this.inputBurnProgress);
      this.relay = relay;
      this.initSlots();
   }

   void initSlots() {
      IItemHandler input = this.relay.getInput();
      IItemHandler output = this.relay.getOutput();
      this.m_38897_(new ValidatedSlot(output, 0, 127, 43, SlotPredicates.EMC_HOLDER));
      this.m_38897_(new ValidatedSlot(input, 0, 67, 43, SlotPredicates.RELAY_INV));
      int counter = 1;

      for(int i = 1; i >= 0; --i) {
         for(int j = 2; j >= 0; --j) {
            this.m_38897_(new ValidatedSlot(input, counter++, 27 + i * 18, 17 + j * 18, SlotPredicates.RELAY_INV));
         }
      }

      this.addPlayerInventory(8, 95);
   }

   protected void broadcastPE(boolean all) {
      this.emc.set(this.relay.getStoredEmc());
      this.kleinChargeProgress.m_6422_((int)(this.relay.getItemChargeProportion() * 8000.0));
      this.inputBurnProgress.m_6422_((int)(this.relay.getInputBurnProportion() * 8000.0));
      super.broadcastPE(all);
   }

   protected BlockRegistryObject getValidBlock() {
      return PEBlocks.RELAY;
   }

   public boolean m_6875_(@NotNull Player player) {
      return stillValid(player, this.relay, this.getValidBlock());
   }

   public double getKleinChargeProgress() {
      return (double)this.kleinChargeProgress.m_6501_() / 8000.0;
   }

   public double getInputBurnProgress() {
      return (double)this.inputBurnProgress.m_6501_() / 8000.0;
   }
}
