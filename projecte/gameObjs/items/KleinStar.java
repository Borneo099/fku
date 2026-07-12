package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.capability.EmcHolderItemCapabilityWrapper;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class KleinStar extends ItemPE implements IItemEmcHolder, IBarHelper {
   public final EnumKleinTier tier;

   public KleinStar(Item.Properties props, EnumKleinTier tier) {
      super(props);
      this.tier = tier;
      this.addItemCapability(EmcHolderItemCapabilityWrapper::new);
      this.addItemCapability("curios", IntegrationHelper.CURIO_CAP_SUPPLIER);
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return stack.m_41782_();
   }

   public float getWidthForBar(ItemStack stack) {
      long starEmc = getEmc(stack);
      return starEmc == 0L ? 1.0F : (float)(1.0 - (double)starEmc / (double)EMCHelper.getKleinStarMaxEmc(stack));
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return this.getScaledBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return this.getColorForBar(stack);
   }

   public @NotNull InteractionResultHolder m_7203_(Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!level.f_46443_ && !FMLEnvironment.production) {
         setEmc(stack, EMCHelper.getKleinStarMaxEmc(stack));
         return InteractionResultHolder.m_19090_(stack);
      } else {
         return InteractionResultHolder.m_19098_(stack);
      }
   }

   public long insertEmc(@NotNull ItemStack stack, long toInsert, IEmcStorage.EmcAction action) {
      if (toInsert < 0L) {
         return this.extractEmc(stack, -toInsert, action);
      } else {
         long toAdd = Math.min(this.getNeededEmc(stack), toInsert);
         if (action.execute()) {
            ItemPE.addEmcToStack(stack, toAdd);
         }

         return toAdd;
      }
   }

   public long extractEmc(@NotNull ItemStack stack, long toExtract, IEmcStorage.EmcAction action) {
      if (toExtract < 0L) {
         return this.insertEmc(stack, -toExtract, action);
      } else {
         long storedEmc = this.getStoredEmc(stack);
         long toRemove = Math.min(storedEmc, toExtract);
         if (action.execute()) {
            ItemPE.setEmc(stack, storedEmc - toRemove);
         }

         return toRemove;
      }
   }

   public @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getStoredEmc(@NotNull ItemStack stack) {
      return ItemPE.getEmc(stack);
   }

   public @Range(
   from = 1L,
   to = Long.MAX_VALUE
) long getMaximumEmc(@NotNull ItemStack stack) {
      return EMCHelper.getKleinStarMaxEmc(stack);
   }

   public static enum EnumKleinTier {
      EIN("ein"),
      ZWEI("zwei"),
      DREI("drei"),
      VIER("vier"),
      SPHERE("sphere"),
      OMEGA("omega");

      public final String name;

      private EnumKleinTier(String name) {
         this.name = name;
      }

      // $FF: synthetic method
      private static EnumKleinTier[] $values() {
         return new EnumKleinTier[]{EIN, ZWEI, DREI, VIER, SPHERE, OMEGA};
      }
   }
}
