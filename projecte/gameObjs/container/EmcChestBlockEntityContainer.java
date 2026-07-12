package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.block_entities.EmcChestBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public abstract class EmcChestBlockEntityContainer extends PEContainer {
   protected final EmcChestBlockEntity blockEntity;

   protected EmcChestBlockEntityContainer(ContainerTypeRegistryObject typeRO, int windowId, Inventory playerInv, EmcChestBlockEntity blockEntity) {
      super(typeRO, windowId, playerInv);
      this.blockEntity = blockEntity;
      this.blockEntity.startOpen(playerInv.f_35978_);
   }

   public void m_6877_(@NotNull Player player) {
      super.m_6877_(player);
      this.blockEntity.stopOpen(player);
   }

   public boolean blockEntityMatches(EmcChestBlockEntity chest) {
      return chest == this.blockEntity;
   }
}
