package moze_intel.projecte.gameObjs.items;

import java.util.Iterator;
import java.util.Optional;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.AlchBagContainer;
import moze_intel.projecte.gameObjs.items.rings.BlackHoleBand;
import moze_intel.projecte.gameObjs.items.rings.VoidRing;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class AlchemicalBag extends ItemPE {
   public final DyeColor color;

   public AlchemicalBag(Item.Properties props, DyeColor color) {
      super(props);
      this.color = color;
   }

   public @NotNull InteractionResultHolder m_7203_(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
      if (!level.f_46443_) {
         NetworkHooks.openScreen((ServerPlayer)player, new ContainerProvider(player.m_21120_(hand), hand), (buf) -> {
            buf.m_130068_(hand);
            buf.writeByte(player.m_150109_().f_35977_);
            buf.writeBoolean(false);
         });
      }

      return InteractionResultHolder.m_19090_(player.m_21120_(hand));
   }

   public static ItemStack getFirstBagWithSuctionItem(Player player, NonNullList inventory) {
      Optional cap = Optional.empty();
      Iterator var3 = inventory.iterator();

      while(var3.hasNext()) {
         ItemStack stack = (ItemStack)var3.next();
         if (!stack.m_41619_()) {
            Item var6 = stack.m_41720_();
            if (var6 instanceof AlchemicalBag) {
               AlchemicalBag bag = (AlchemicalBag)var6;
               if (cap.isEmpty()) {
                  cap = player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).resolve();
                  if (cap.isEmpty()) {
                     break;
                  }
               }

               IItemHandler inv = ((IAlchBagProvider)cap.get()).getBag(bag.color);

               for(int i = 0; i < inv.getSlots(); ++i) {
                  ItemStack ring = inv.getStackInSlot(i);
                  if (!ring.m_41619_() && (ring.m_41720_() instanceof BlackHoleBand || ring.m_41720_() instanceof VoidRing) && ItemHelper.checkItemNBT(ring, "Active")) {
                     return stack;
                  }
               }
            }
         }
      }

      return ItemStack.f_41583_;
   }

   private class ContainerProvider implements MenuProvider {
      private final ItemStack stack;
      private final InteractionHand hand;

      private ContainerProvider(ItemStack stack, InteractionHand hand) {
         this.stack = stack;
         this.hand = hand;
      }

      public @NotNull AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
         IItemHandlerModifiable inv = (IItemHandlerModifiable)((IAlchBagProvider)player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).orElseThrow(NullPointerException::new)).getBag(AlchemicalBag.this.color);
         return new AlchBagContainer(windowId, playerInventory, this.hand, inv, playerInventory.f_35977_, false);
      }

      public @NotNull Component m_5446_() {
         return this.stack.m_41786_();
      }
   }
}
