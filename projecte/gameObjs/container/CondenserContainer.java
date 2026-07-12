package moze_intel.projecte.gameObjs.container;

import java.util.Objects;
import java.util.function.Predicate;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.gameObjs.block_entities.CondenserBlockEntity;
import moze_intel.projecte.gameObjs.container.slots.SlotCondenserLock;
import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.network.packets.to_client.UpdateCondenserLockPKT;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CondenserContainer extends EmcChestBlockEntityContainer {
   public final PEContainer.BoxedLong displayEmc;
   public final PEContainer.BoxedLong requiredEmc;
   private @Nullable ItemInfo lastLockInfo;

   public CondenserContainer(int windowId, Inventory playerInv, CondenserBlockEntity condenser) {
      this(PEContainerTypes.CONDENSER_CONTAINER, windowId, playerInv, condenser);
   }

   protected CondenserContainer(ContainerTypeRegistryObject type, int windowId, Inventory playerInv, CondenserBlockEntity condenser) {
      super(type, windowId, playerInv, condenser);
      this.displayEmc = new PEContainer.BoxedLong();
      this.requiredEmc = new PEContainer.BoxedLong();
      this.longFields.add(this.displayEmc);
      this.longFields.add(this.requiredEmc);
      this.initSlots();
   }

   protected void initSlots() {
      CondenserBlockEntity var10003 = (CondenserBlockEntity)this.blockEntity;
      Objects.requireNonNull(var10003);
      this.m_38897_(new SlotCondenserLock(var10003::getLockInfo, 0, 12, 6));
      Predicate validator = (s) -> {
         return SlotPredicates.HAS_EMC.test(s) && !((CondenserBlockEntity)this.blockEntity).isStackEqualToLock(s);
      };
      IItemHandler handler = ((CondenserBlockEntity)this.blockEntity).getInput();

      for(int i = 0; i < 7; ++i) {
         for(int j = 0; j < 13; ++j) {
            this.m_38897_(new ValidatedSlot(handler, j + i * 13, 12 + j * 18, 26 + i * 18, validator));
         }
      }

      this.addPlayerInventory(48, 154);
   }

   protected void broadcastPE(boolean all) {
      this.displayEmc.set(((CondenserBlockEntity)this.blockEntity).displayEmc);
      this.requiredEmc.set(((CondenserBlockEntity)this.blockEntity).requiredEmc);
      ItemInfo lockInfo = ((CondenserBlockEntity)this.blockEntity).getLockInfo();
      if (all || !Objects.equals(lockInfo, this.lastLockInfo)) {
         this.lastLockInfo = lockInfo;
         this.syncDataChange(new UpdateCondenserLockPKT((short)this.f_38840_, lockInfo));
      }

      super.broadcastPE(all);
   }

   protected BlockRegistryObject getValidBlock() {
      return PEBlocks.CONDENSER;
   }

   public boolean m_6875_(@NotNull Player player) {
      return stillValid(player, this.blockEntity, this.getValidBlock());
   }

   public void m_150399_(int slot, int button, @NotNull ClickType flag, @NotNull Player player) {
      if (slot == 0) {
         if (((CondenserBlockEntity)this.blockEntity).attemptCondenserSet(player)) {
            this.m_38946_();
         }
      } else {
         super.m_150399_(slot, button, flag, player);
      }

   }

   public int getProgressScaled() {
      if (this.requiredEmc.get() == 0L) {
         return 0;
      } else {
         return this.displayEmc.get() >= this.requiredEmc.get() ? 102 : (int)(102.0 * ((double)this.displayEmc.get() / (double)this.requiredEmc.get()));
      }
   }

   public void updateLockInfo(@Nullable ItemInfo lockInfo) {
      ((CondenserBlockEntity)this.blockEntity).setLockInfoFromPacket(lockInfo);
   }
}
