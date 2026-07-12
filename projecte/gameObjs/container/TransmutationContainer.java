package moze_intel.projecte.gameObjs.container;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.gameObjs.container.slots.transmutation.SlotConsume;
import moze_intel.projecte.gameObjs.container.slots.transmutation.SlotInput;
import moze_intel.projecte.gameObjs.container.slots.transmutation.SlotLock;
import moze_intel.projecte.gameObjs.container.slots.transmutation.SlotOutput;
import moze_intel.projecte.gameObjs.container.slots.transmutation.SlotUnlearn;
import moze_intel.projecte.gameObjs.items.Tome;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_server.SearchUpdatePKT;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class TransmutationContainer extends PEHandContainer {
   private final List inputSlots = new ArrayList();
   public final TransmutationInventory transmutationInventory;
   private SlotUnlearn unlearn;

   public static TransmutationContainer fromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
      return buf.readBoolean() ? new TransmutationContainer(windowId, playerInv, (InteractionHand)buf.m_130066_(InteractionHand.class), buf.readByte()) : new TransmutationContainer(windowId, playerInv);
   }

   public TransmutationContainer(int windowId, Inventory playerInv) {
      super(PEContainerTypes.TRANSMUTATION_CONTAINER, windowId, playerInv, (InteractionHand)null, 0);
      this.transmutationInventory = new TransmutationInventory(this.playerInv.f_35978_);
      this.initSlots();
   }

   public TransmutationContainer(int windowId, Inventory playerInv, InteractionHand hand, int selected) {
      super(PEContainerTypes.TRANSMUTATION_CONTAINER, windowId, playerInv, hand, selected);
      this.transmutationInventory = new TransmutationInventory(this.playerInv.f_35978_);
      this.initSlots();
   }

   private void initSlots() {
      this.m_38897_(new SlotInput(this.transmutationInventory, 0, 43, 23));
      this.m_38897_(new SlotInput(this.transmutationInventory, 1, 34, 41));
      this.m_38897_(new SlotInput(this.transmutationInventory, 2, 52, 41));
      this.m_38897_(new SlotInput(this.transmutationInventory, 3, 16, 50));
      this.m_38897_(new SlotInput(this.transmutationInventory, 4, 70, 50));
      this.m_38897_(new SlotInput(this.transmutationInventory, 5, 34, 59));
      this.m_38897_(new SlotInput(this.transmutationInventory, 6, 52, 59));
      this.m_38897_(new SlotInput(this.transmutationInventory, 7, 43, 77));
      this.m_38897_(new SlotLock(this.transmutationInventory, 8, 158, 50));
      this.m_38897_(new SlotConsume(this.transmutationInventory, 9, 107, 97));
      this.m_38897_(this.unlearn = new SlotUnlearn(this.transmutationInventory, 10, 89, 97));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 11, 158, 9));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 12, 176, 13));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 13, 193, 30));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 14, 199, 50));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 15, 193, 70));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 16, 176, 87));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 17, 158, 91));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 18, 140, 87));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 19, 123, 70));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 20, 116, 50));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 21, 123, 30));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 22, 140, 13));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 23, 158, 31));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 24, 177, 50));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 25, 158, 69));
      this.m_38897_(new SlotOutput(this.transmutationInventory, 26, 139, 50));
      this.addPlayerInventory(35, 117);
   }

   protected @NotNull Slot m_38897_(@NotNull Slot slot) {
      if (slot instanceof SlotInput input) {
         this.inputSlots.add(input);
      }

      return super.m_38897_(slot);
   }

   public void m_6877_(@NotNull Player player) {
      super.m_6877_(player);
      if (player.m_6084_()) {
         label26: {
            if (player instanceof ServerPlayer) {
               ServerPlayer serverPlayer = (ServerPlayer)player;
               if (serverPlayer.m_9232_()) {
                  break label26;
               }
            }

            player.m_150109_().m_150079_(this.unlearn.m_7993_());
            return;
         }
      }

      player.m_36176_(this.unlearn.m_7993_(), false);
   }

   public @NotNull ItemStack m_7648_(@NotNull Player player, int slotIndex) {
      if (slotIndex >= 9 && slotIndex != 10) {
         Slot currentSlot = this.tryGetSlot(slotIndex);
         if (currentSlot != null && currentSlot.m_6657_()) {
            ItemStack slotStack;
            BigInteger emcBigInt;
            if (slotIndex >= 11 && slotIndex <= 26) {
               slotStack = currentSlot.m_7993_().m_41777_();
               long itemEmc = EMCHelper.getEmcValue(slotStack);
               if (itemEmc > 0L) {
                  slotStack.m_41764_(slotStack.m_41741_());
                  int stackSize = slotStack.m_41613_() - ItemHelper.simulateFit(player.m_150109_().f_35974_, slotStack);
                  if (stackSize > 0) {
                     emcBigInt = this.transmutationInventory.getAvailableEmc();
                     BigInteger emc = BigInteger.valueOf(itemEmc);
                     BigInteger totalEmc = emc.multiply(BigInteger.valueOf((long)stackSize));
                     if (totalEmc.compareTo(emcBigInt) > 0) {
                        BigInteger numOperations = emcBigInt.divide(emc);
                        stackSize = numOperations.intValueExact();
                        totalEmc = emc.multiply(numOperations);
                        if (stackSize <= 0) {
                           return ItemStack.f_41583_;
                        }
                     }

                     slotStack.m_41764_(stackSize);
                     IItemHandler inv = (IItemHandler)player.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
                     if (this.transmutationInventory.isServer()) {
                        this.transmutationInventory.removeEmc(totalEmc);
                     }

                     ItemHandlerHelper.insertItemStacked(inv, slotStack, false);
                  }
               }
            } else if (slotIndex > 26) {
               slotStack = currentSlot.m_7993_();
               ItemStack stackToInsert = slotStack;
               if (slotStack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).isPresent()) {
                  stackToInsert = insertItem(this.inputSlots, slotStack, true);
                  if (slotStack.m_41613_() == stackToInsert.m_41613_()) {
                     stackToInsert = insertItem(this.inputSlots, stackToInsert, false);
                  }

                  if (slotStack.m_41613_() != stackToInsert.m_41613_()) {
                     return this.transferSuccess(currentSlot, player, slotStack, stackToInsert);
                  }
               }

               long emc = EMCHelper.getEmcSellValue(stackToInsert);
               if (emc > 0L || stackToInsert.m_41720_() instanceof Tome) {
                  if (this.transmutationInventory.isServer()) {
                     emcBigInt = BigInteger.valueOf(emc);
                     this.transmutationInventory.handleKnowledge(stackToInsert);
                     this.transmutationInventory.addEmc(emcBigInt.multiply(BigInteger.valueOf((long)stackToInsert.m_41613_())));
                  }

                  currentSlot.m_5852_(ItemStack.f_41583_);
               }
            }

            return ItemStack.f_41583_;
         } else {
            return ItemStack.f_41583_;
         }
      } else {
         return super.m_7648_(player, slotIndex);
      }
   }

   public void clickPostValidate(int slotIndex, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
      if (player.m_9236_().f_46443_ && this.transmutationInventory.getHandlerForSlot(slotIndex) == this.transmutationInventory.outputs) {
         Slot slot = this.tryGetSlot(slotIndex);
         if (slot != null) {
            PacketHandler.sendToServer(new SearchUpdatePKT(this.transmutationInventory.getIndexFromSlot(slotIndex), slot.m_7993_()));
         }
      }

      super.clickPostValidate(slotIndex, dragType, clickType, player);
   }

   public boolean m_5622_(@NotNull Slot slot) {
      return !(slot instanceof SlotConsume) && !(slot instanceof SlotUnlearn) && !(slot instanceof SlotInput) && !(slot instanceof SlotLock) && !(slot instanceof SlotOutput);
   }
}
