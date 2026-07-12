package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.block_entities.CollectorMK1BlockEntity;
import moze_intel.projecte.gameObjs.container.slots.SlotGhost;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class CollectorMK1Container extends PEContainer {
   public final CollectorMK1BlockEntity collector;
   public final DataSlot sunLevel;
   public final PEContainer.BoxedLong emc;
   private final DataSlot kleinChargeProgress;
   private final DataSlot fuelProgress;
   public final PEContainer.BoxedLong kleinEmc;

   public CollectorMK1Container(int windowId, Inventory playerInv, CollectorMK1BlockEntity collector) {
      this(PEContainerTypes.COLLECTOR_MK1_CONTAINER, windowId, playerInv, collector);
   }

   protected CollectorMK1Container(ContainerTypeRegistryObject type, int windowId, Inventory playerInv, CollectorMK1BlockEntity collector) {
      super(type, windowId, playerInv);
      this.sunLevel = DataSlot.m_39401_();
      this.emc = new PEContainer.BoxedLong();
      this.kleinChargeProgress = DataSlot.m_39401_();
      this.fuelProgress = DataSlot.m_39401_();
      this.kleinEmc = new PEContainer.BoxedLong();
      this.longFields.add(this.emc);
      this.m_38895_(this.sunLevel);
      this.m_38895_(this.kleinChargeProgress);
      this.m_38895_(this.fuelProgress);
      this.longFields.add(this.kleinEmc);
      this.collector = collector;
      this.initSlots();
   }

   void initSlots() {
      IItemHandler aux = this.collector.getAux();
      IItemHandler main = this.collector.getInput();
      this.m_38897_(new ValidatedSlot(aux, 0, 124, 58, SlotPredicates.COLLECTOR_INV));
      int counter = 0;

      for(int i = 1; i >= 0; --i) {
         for(int j = 3; j >= 0; --j) {
            this.m_38897_(new ValidatedSlot(main, counter++, 20 + i * 18, 8 + j * 18, SlotPredicates.COLLECTOR_INV));
         }
      }

      this.m_38897_(new ValidatedSlot(aux, 1, 124, 13, SlotPredicates.ALWAYS_FALSE));
      this.m_38897_(new SlotGhost(aux, 2, 153, 36, SlotPredicates.COLLECTOR_LOCK));
      this.addPlayerInventory(8, 84);
   }

   public void m_150399_(int slotID, int button, @NotNull ClickType flag, @NotNull Player player) {
      Slot slot = this.tryGetSlot(slotID);
      if (slot instanceof SlotGhost && !slot.m_7993_().m_41619_()) {
         slot.m_5852_(ItemStack.f_41583_);
      } else {
         super.m_150399_(slotID, button, flag, player);
      }

   }

   protected void broadcastPE(boolean all) {
      this.emc.set(this.collector.getStoredEmc());
      this.sunLevel.m_6422_(this.collector.getSunLevel());
      this.kleinChargeProgress.m_6422_((int)(this.collector.getItemChargeProportion() * 8000.0));
      this.fuelProgress.m_6422_((int)(this.collector.getFuelProgress() * 8000.0));
      this.kleinEmc.set(this.collector.getItemCharge());
      super.broadcastPE(all);
   }

   protected BlockRegistryObject getValidBlock() {
      return PEBlocks.COLLECTOR;
   }

   public boolean m_6875_(@NotNull Player player) {
      return stillValid(player, this.collector, this.getValidBlock());
   }

   public double getKleinChargeProgress() {
      return (double)this.kleinChargeProgress.m_6501_() / 8000.0;
   }

   public double getFuelProgress() {
      return (double)this.fuelProgress.m_6501_() / 8000.0;
   }
}
