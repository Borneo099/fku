package moze_intel.projecte.api.capabilities;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public interface IKnowledgeProvider extends INBTSerializable {
   boolean hasFullKnowledge();

   void setFullKnowledge(boolean var1);

   void clearKnowledge();

   default boolean hasKnowledge(@NotNull ItemStack stack) {
      return !stack.m_41619_() && this.hasKnowledge(ItemInfo.fromStack(stack));
   }

   boolean hasKnowledge(@NotNull ItemInfo var1);

   default boolean addKnowledge(@NotNull ItemStack stack) {
      return !stack.m_41619_() && this.addKnowledge(ItemInfo.fromStack(stack));
   }

   boolean addKnowledge(@NotNull ItemInfo var1);

   default boolean removeKnowledge(@NotNull ItemStack stack) {
      return !stack.m_41619_() && this.removeKnowledge(ItemInfo.fromStack(stack));
   }

   boolean removeKnowledge(@NotNull ItemInfo var1);

   @NotNull Set getKnowledge();

   @NotNull IItemHandler getInputAndLocks();

   BigInteger getEmc();

   void setEmc(BigInteger var1);

   void sync(@NotNull ServerPlayer var1);

   void syncEmc(@NotNull ServerPlayer var1);

   void syncKnowledgeChange(@NotNull ServerPlayer var1, ItemInfo var2, boolean var3);

   void syncInputAndLocks(@NotNull ServerPlayer var1, List var2, TargetUpdateType var3);

   void receiveInputsAndLocks(Map var1);

   public static enum TargetUpdateType {
      NONE,
      IF_NEEDED,
      ALL;

      // $FF: synthetic method
      private static TargetUpdateType[] $values() {
         return new TargetUpdateType[]{NONE, IF_NEEDED, ALL};
      }
   }
}
