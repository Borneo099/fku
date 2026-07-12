package moze_intel.projecte.events;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.AlchBagContainer;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.handlers.CommonInternalAbilities;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.IItemHandler;

@EventBusSubscriber(
   modid = "projecte"
)
public class TickEvents {
   @SubscribeEvent
   public static void playerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         event.player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent((provider) -> {
            Set colorsChanged = EnumSet.noneOf(DyeColor.class);
            Iterator var3 = getBagColorsPresent(event.player).iterator();

            ItemStack heldItem;
            while(var3.hasNext()) {
               DyeColor color = (DyeColor)var3.next();
               IItemHandler inv = provider.getBag(color);

               for(int i = 0; i < inv.getSlots(); ++i) {
                  heldItem = inv.getStackInSlot(i);
                  if (!heldItem.m_41619_()) {
                     heldItem.getCapability(PECapabilities.ALCH_BAG_ITEM_CAPABILITY).ifPresent((alchBagItem) -> {
                        if (alchBagItem.updateInAlchBag(inv, event.player, heldItem)) {
                           colorsChanged.add(color);
                        }

                     });
                  }
               }
            }

            Player patt1923$temp = event.player;
            if (patt1923$temp instanceof ServerPlayer serverPlayer) {
               Iterator var12 = colorsChanged.iterator();

               while(true) {
                  AlchemicalBag bag;
                  DyeColor e;
                  do {
                     if (!var12.hasNext()) {
                        return;
                     }

                     e = (DyeColor)var12.next();
                     AbstractContainerMenu patt2089$temp = serverPlayer.f_36096_;
                     if (!(patt2089$temp instanceof AlchBagContainer)) {
                        break;
                     }

                     AlchBagContainer container = (AlchBagContainer)patt2089$temp;
                     heldItem = serverPlayer.m_21120_(container.hand);
                     Item patt2232$temp = heldItem.m_41720_();
                     if (!(patt2232$temp instanceof AlchemicalBag)) {
                        break;
                     }

                     bag = (AlchemicalBag)patt2232$temp;
                  } while(bag.color == e);

                  provider.sync(e, serverPlayer);
               }
            }
         });
         event.player.getCapability(CommonInternalAbilities.CAPABILITY).ifPresent(CommonInternalAbilities::tick);
         if (event.side.isServer()) {
            event.player.getCapability(InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::tick);
            event.player.getCapability(InternalTimers.CAPABILITY).ifPresent(InternalTimers::tick);
            if (event.player.m_6060_() && shouldPlayerResistFire((ServerPlayer)event.player)) {
               event.player.m_20095_();
            }
         }
      }

   }

   public static boolean shouldPlayerResistFire(ServerPlayer player) {
      Iterator var1 = player.m_6168_().iterator();

      ItemStack stack;
      IFireProtector protector;
      Item var4;
      while(var1.hasNext()) {
         stack = (ItemStack)var1.next();
         if (!stack.m_41619_()) {
            var4 = stack.m_41720_();
            if (var4 instanceof IFireProtector) {
               protector = (IFireProtector)var4;
               if (protector.canProtectAgainstFire(stack, player)) {
                  return true;
               }
            }
         }
      }

      for(int i = 0; i < Inventory.m_36059_(); ++i) {
         stack = player.m_150109_().m_8020_(i);
         if (!stack.m_41619_()) {
            var4 = stack.m_41720_();
            if (var4 instanceof IFireProtector) {
               protector = (IFireProtector)var4;
               if (protector.canProtectAgainstFire(stack, player)) {
                  return true;
               }
            }
         }
      }

      IItemHandler curios = PlayerHelper.getCurios(player);
      if (curios != null) {
         for(int i = 0; i < curios.getSlots(); ++i) {
            ItemStack stack = curios.getStackInSlot(i);
            if (!stack.m_41619_()) {
               Item var5 = stack.m_41720_();
               if (var5 instanceof IFireProtector) {
                  IFireProtector protector = (IFireProtector)var5;
                  if (protector.canProtectAgainstFire(stack, player)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static Set getBagColorsPresent(Player player) {
      Set bagsPresent = EnumSet.noneOf(DyeColor.class);
      player.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((inv) -> {
         for(int i = 0; i < inv.getSlots(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.m_41619_()) {
               Item patt4219$temp = stack.m_41720_();
               if (patt4219$temp instanceof AlchemicalBag) {
                  AlchemicalBag bag = (AlchemicalBag)patt4219$temp;
                  bagsPresent.add(bag.color);
               }
            }
         }

      });
      return bagsPresent;
   }
}
