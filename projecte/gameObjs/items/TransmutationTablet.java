package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class TransmutationTablet extends ItemPE {
   public TransmutationTablet(Item.Properties props) {
      super(props);
   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      if (!level.f_46443_) {
         NetworkHooks.openScreen((ServerPlayer)player, new ContainerProvider(hand), (buf) -> {
            buf.writeBoolean(true);
            buf.m_130068_(hand);
            buf.writeByte(player.m_150109_().f_35977_);
         });
      }

      return InteractionResultHolder.m_19090_(player.m_21120_(hand));
   }

   private static record ContainerProvider(InteractionHand hand) implements MenuProvider {
      private ContainerProvider(InteractionHand hand) {
         this.hand = hand;
      }

      public AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
         return new TransmutationContainer(windowId, playerInventory, this.hand, playerInventory.f_35977_);
      }

      public @NotNull Component m_5446_() {
         return PELang.TRANSMUTATION_TRANSMUTE.translate(new Object[0]);
      }

      public InteractionHand hand() {
         return this.hand;
      }
   }
}
