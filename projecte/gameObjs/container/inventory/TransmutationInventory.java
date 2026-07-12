package moze_intel.projecte.gameObjs.container.inventory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.api.event.PlayerAttemptLearnEvent;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.emc.nbt.NBTManager;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class TransmutationInventory extends CombinedInvWrapper {
   public final Player player;
   public final IKnowledgeProvider provider;
   private final IItemHandlerModifiable inputLocks;
   private final IItemHandlerModifiable learning;
   public final IItemHandlerModifiable outputs;
   private static final int LOCK_INDEX = 8;
   private static final int FUEL_START = 12;
   public int learnFlag = 0;
   public int unlearnFlag = 0;
   public String filter = "";
   public int searchpage = 0;
   private List knowledge = Collections.emptyList();

   public TransmutationInventory(Player player) {
      super(new IItemHandlerModifiable[]{(IItemHandlerModifiable)((IKnowledgeProvider)player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElseThrow(NullPointerException::new)).getInputAndLocks(), new ItemStackHandler(2), new ItemStackHandler(16)});
      this.player = player;
      this.provider = (IKnowledgeProvider)player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElseThrow(NullPointerException::new);
      this.inputLocks = this.itemHandler[0];
      this.learning = this.itemHandler[1];
      this.outputs = this.itemHandler[2];
      if (!this.isServer()) {
         this.updateClientTargets();
      }

   }

   public boolean isServer() {
      return !this.player.m_9236_().f_46443_;
   }

   public void handleKnowledge(ItemStack stack) {
      if (!stack.m_41619_()) {
         this.handleKnowledge(ItemInfo.fromStack(stack));
      }

   }

   public void handleKnowledge(ItemInfo info) {
      ItemInfo cleanedInfo = NBTManager.getPersistentInfo(info);
      if (!this.provider.hasKnowledge(cleanedInfo) && !MinecraftForge.EVENT_BUS.post(new PlayerAttemptLearnEvent(this.player, info, cleanedInfo)) && this.provider.addKnowledge(cleanedInfo)) {
         this.provider.syncKnowledgeChange((ServerPlayer)this.player, cleanedInfo, true);
      }

   }

   public void itemLearned() {
      this.learnFlag = 300;
      this.unlearnFlag = 0;
      this.updateClientTargets();
   }

   public void handleUnlearn(ItemStack stack) {
      if (!stack.m_41619_()) {
         this.handleUnlearn(ItemInfo.fromStack(stack));
      }

   }

   public void handleUnlearn(ItemInfo info) {
      ItemInfo cleanedInfo = NBTManager.getPersistentInfo(info);
      if (this.provider.hasKnowledge(cleanedInfo) && this.provider.removeKnowledge(cleanedInfo)) {
         this.provider.syncKnowledgeChange((ServerPlayer)this.player, cleanedInfo, false);
      }

   }

   public void itemUnlearned() {
      this.unlearnFlag = 300;
      this.learnFlag = 0;
      this.updateClientTargets();
   }

   public void checkForUpdates() {
      long matterEmc = EMCHelper.getEmcValue(this.outputs.getStackInSlot(0));
      long fuelEmc = EMCHelper.getEmcValue(this.outputs.getStackInSlot(12));
      if (BigInteger.valueOf(Math.max(matterEmc, fuelEmc)).compareTo(this.getAvailableEmc()) > 0) {
         this.updateClientTargets();
      }

   }

   public void updateClientTargets() {
      if (!this.isServer()) {
         this.knowledge = (List)this.provider.getKnowledge().stream().filter(EMCHelper::doesItemHaveEmc).sorted(Collections.reverseOrder(Comparator.comparing(EMCHelper::getEmcValue))).collect(Collectors.toList());

         int pagecounter;
         for(pagecounter = 0; pagecounter < this.outputs.getSlots(); ++pagecounter) {
            this.outputs.setStackInSlot(pagecounter, ItemStack.f_41583_);
         }

         pagecounter = 0;
         int desiredPage = this.searchpage * 12;
         ItemInfo lockInfo = null;
         BigInteger availableEMC = this.getAvailableEmc();
         Iterator iter;
         ItemInfo info;
         if (!this.inputLocks.getStackInSlot(8).m_41619_()) {
            lockInfo = NBTManager.getPersistentInfo(ItemInfo.fromStack(this.inputLocks.getStackInSlot(8)));
            long reqEmc = EMCHelper.getEmcValue(lockInfo);
            if (availableEMC.compareTo(BigInteger.valueOf(reqEmc)) < 0) {
               return;
            }

            iter = this.knowledge.iterator();

            label82:
            while(true) {
               while(true) {
                  if (!iter.hasNext()) {
                     break label82;
                  }

                  info = (ItemInfo)iter.next();
                  if (EMCHelper.getEmcValue(info) <= reqEmc && !info.equals(lockInfo) && this.doesItemMatchFilter(info)) {
                     if (pagecounter < desiredPage) {
                        ++pagecounter;
                        iter.remove();
                     }
                  } else {
                     iter.remove();
                  }
               }
            }
         } else {
            Iterator iter = this.knowledge.iterator();

            label70:
            while(true) {
               while(true) {
                  if (!iter.hasNext()) {
                     break label70;
                  }

                  ItemInfo info = (ItemInfo)iter.next();
                  if (availableEMC.compareTo(BigInteger.valueOf(EMCHelper.getEmcValue(info))) >= 0 && this.doesItemMatchFilter(info)) {
                     if (pagecounter < desiredPage) {
                        ++pagecounter;
                        iter.remove();
                     }
                  } else {
                     iter.remove();
                  }
               }
            }
         }

         int matterCounter = 0;
         int fuelCounter = 0;
         if (lockInfo != null && this.provider.hasKnowledge(lockInfo)) {
            ItemStack lockStack = lockInfo.createStack();
            if (FuelMapper.isStackFuel(lockStack)) {
               this.outputs.setStackInSlot(12, lockStack);
               ++fuelCounter;
            } else {
               this.outputs.setStackInSlot(0, lockStack);
               ++matterCounter;
            }
         }

         iter = this.knowledge.iterator();

         while(iter.hasNext()) {
            info = (ItemInfo)iter.next();
            ItemStack stack = info.createStack();
            if (FuelMapper.isStackFuel(stack)) {
               if (fuelCounter < 4) {
                  this.outputs.setStackInSlot(12 + fuelCounter, stack);
                  ++fuelCounter;
               }
            } else if (matterCounter < 12) {
               this.outputs.setStackInSlot(matterCounter, stack);
               ++matterCounter;
            }
         }

      }
   }

   private boolean doesItemMatchFilter(ItemInfo info) {
      if (this.filter.isEmpty()) {
         return true;
      } else {
         try {
            return info.createStack().m_41786_().getString().toLowerCase(Locale.ROOT).contains(this.filter);
         } catch (Exception var3) {
            PECore.LOGGER.error("Failed to check filter", var3);
            return true;
         }
      }
   }

   public void writeIntoOutputSlot(int slot, ItemStack item) {
      long emcValue = EMCHelper.getEmcValue(item);
      if (emcValue > 0L && BigInteger.valueOf(emcValue).compareTo(this.getAvailableEmc()) <= 0 && this.provider.hasKnowledge(item)) {
         this.outputs.setStackInSlot(slot, item);
      } else {
         this.outputs.setStackInSlot(slot, ItemStack.f_41583_);
      }

   }

   public void addEmc(BigInteger value) {
      int compareToZero = value.compareTo(BigInteger.ZERO);
      if (compareToZero != 0) {
         if (compareToZero < 0) {
            this.removeEmc(value.negate());
         } else {
            List inputLocksChanged = new ArrayList();

            for(int slotIndex = 0; slotIndex < this.inputLocks.getSlots(); ++slotIndex) {
               if (slotIndex != 8) {
                  ItemStack stack = this.inputLocks.getStackInSlot(slotIndex);
                  if (!stack.m_41619_()) {
                     Optional holderCapability = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
                     if (holderCapability.isPresent()) {
                        IItemEmcHolder emcHolder = (IItemEmcHolder)holderCapability.get();
                        long shrunkenValue = MathUtils.clampToLong(value);
                        long actualInserted = emcHolder.insertEmc(stack, shrunkenValue, IEmcStorage.EmcAction.EXECUTE);
                        if (actualInserted > 0L) {
                           inputLocksChanged.add(slotIndex);
                           value = value.subtract(BigInteger.valueOf(actualInserted));
                           if (value.compareTo(BigInteger.ZERO) == 0) {
                              this.syncChangedSlots(inputLocksChanged, IKnowledgeProvider.TargetUpdateType.ALL);
                              return;
                           }
                        }
                     }
                  }
               }
            }

            this.syncChangedSlots(inputLocksChanged, IKnowledgeProvider.TargetUpdateType.NONE);
            this.updateEmcAndSync(this.provider.getEmc().add(value));
         }
      }
   }

   public void removeEmc(BigInteger value) {
      int compareToZero = value.compareTo(BigInteger.ZERO);
      if (compareToZero != 0) {
         if (compareToZero < 0) {
            this.addEmc(value.negate());
         } else {
            BigInteger currentEmc = this.provider.getEmc();
            if (value.compareTo(currentEmc) > 0) {
               List inputLocksChanged = new ArrayList();
               BigInteger toRemove = value.subtract(currentEmc);
               value = currentEmc;

               for(int slotIndex = 0; slotIndex < this.inputLocks.getSlots(); ++slotIndex) {
                  if (slotIndex != 8) {
                     ItemStack stack = this.inputLocks.getStackInSlot(slotIndex);
                     if (!stack.m_41619_()) {
                        Optional holderCapability = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
                        if (holderCapability.isPresent()) {
                           IItemEmcHolder emcHolder = (IItemEmcHolder)holderCapability.get();
                           long shrunkenToRemove = MathUtils.clampToLong(toRemove);
                           long actualExtracted = emcHolder.extractEmc(stack, shrunkenToRemove, IEmcStorage.EmcAction.EXECUTE);
                           if (actualExtracted > 0L) {
                              inputLocksChanged.add(slotIndex);
                              toRemove = toRemove.subtract(BigInteger.valueOf(actualExtracted));
                              if (toRemove.compareTo(BigInteger.ZERO) == 0) {
                                 this.syncChangedSlots(inputLocksChanged, IKnowledgeProvider.TargetUpdateType.IF_NEEDED);
                                 if (currentEmc.compareTo(BigInteger.ZERO) > 0) {
                                    this.updateEmcAndSync(BigInteger.ZERO);
                                 }

                                 return;
                              }
                           }
                        }
                     }
                  }
               }

               this.syncChangedSlots(inputLocksChanged, IKnowledgeProvider.TargetUpdateType.NONE);
            }

            this.updateEmcAndSync(currentEmc.subtract(value));
         }
      }
   }

   public void syncChangedSlots(List slotsChanged, IKnowledgeProvider.TargetUpdateType updateTargets) {
      this.provider.syncInputAndLocks((ServerPlayer)this.player, slotsChanged, updateTargets);
   }

   private void updateEmcAndSync(BigInteger emc) {
      if (emc.compareTo(BigInteger.ZERO) < 0) {
         emc = BigInteger.ZERO;
      }

      this.provider.setEmc(emc);
      this.provider.syncEmc((ServerPlayer)this.player);
      PlayerHelper.updateScore((ServerPlayer)this.player, PlayerHelper.SCOREBOARD_EMC, emc);
   }

   public IItemHandlerModifiable getHandlerForSlot(int slot) {
      return super.getHandlerFromIndex(super.getIndexForSlot(slot));
   }

   public int getIndexFromSlot(int slot) {
      IItemHandlerModifiable[] var2 = this.itemHandler;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         IItemHandlerModifiable h = var2[var4];
         if (slot >= h.getSlots()) {
            slot -= h.getSlots();
         }
      }

      return slot;
   }

   public BigInteger getAvailableEmc() {
      BigInteger emc = this.provider.getEmc();

      for(int i = 0; i < this.inputLocks.getSlots(); ++i) {
         if (i != 8) {
            ItemStack stack = this.inputLocks.getStackInSlot(i);
            if (!stack.m_41619_()) {
               Optional emcHolder = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
               if (emcHolder.isPresent()) {
                  emc = emc.add(BigInteger.valueOf(((IItemEmcHolder)emcHolder.get()).getStoredEmc(stack)));
               }
            }
         }
      }

      return emc;
   }

   public int getKnowledgeSize() {
      return this.knowledge.size();
   }
}
