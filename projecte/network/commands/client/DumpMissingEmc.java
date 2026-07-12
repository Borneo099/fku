package moze_intel.projecte.network.commands.client;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.registries.ForgeRegistries;

public class DumpMissingEmc {
   public static ArgumentBuilder register(CommandBuildContext context) {
      return Commands.m_82127_("dumpmissingemc").executes(DumpMissingEmc::execute);
   }

   private static int execute(CommandContext ctx) {
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      Set allItems = new HashSet(ForgeRegistries.ITEMS.getValues());
      allItems.remove(Items.f_41852_);
      Set missing = new HashSet();
      CreativeModeTab tab = CreativeModeTabRegistry.getTab(CreativeModeTabs.f_256750_.m_135782_());
      Iterator var5;
      ItemInfo itemInfo;
      if (tab != null) {
         if (tab.m_261235_().isEmpty()) {
            initTab(tab);
         }

         var5 = tab.m_261235_().iterator();

         while(var5.hasNext()) {
            ItemStack stack = (ItemStack)var5.next();
            if (!stack.m_41619_()) {
               itemInfo = ItemInfo.fromStack(stack);
               if (EMCHelper.getEmcValue(itemInfo) == 0L) {
                  missing.add(itemInfo);
               } else {
                  allItems.remove(stack.m_41720_());
               }
            }
         }
      }

      var5 = allItems.iterator();

      while(var5.hasNext()) {
         Item item = (Item)var5.next();
         itemInfo = ItemInfo.fromItem(item);
         if (EMCHelper.getEmcValue(itemInfo) == 0L) {
            missing.add(itemInfo);
         }
      }

      int missingCount = missing.size();
      PELang var10001;
      if (missingCount == 0) {
         var10001 = PELang.DUMP_MISSING_EMC_NONE_MISSING;
         Objects.requireNonNull(var10001);
         source.m_288197_(() -> {
            return var10001.translate(new Object[0]);
         }, true);
      } else {
         if (missingCount == 1) {
            var10001 = PELang.DUMP_MISSING_EMC_ONE_MISSING;
            Objects.requireNonNull(var10001);
            source.m_288197_(() -> {
               return var10001.translate(new Object[0]);
            }, true);
         } else {
            source.m_288197_(() -> {
               return PELang.DUMP_MISSING_EMC_MULTIPLE_MISSING.translate(new Object[]{missingCount});
            }, true);
         }

         Iterator var10 = missing.iterator();

         while(var10.hasNext()) {
            itemInfo = (ItemInfo)var10.next();
            PECore.LOGGER.info(itemInfo.toString());
         }
      }

      return missingCount;
   }

   private static void initTab(CreativeModeTab tab) {
      Minecraft minecraft = Minecraft.m_91087_();
      if (minecraft.f_91073_ != null) {
         FeatureFlagSet features = (FeatureFlagSet)Optional.ofNullable(minecraft.f_91074_).map((p) -> {
            return p.f_108617_.m_247016_();
         }).orElse(FeatureFlags.f_244332_);
         boolean hasPermissions = (Boolean)minecraft.f_91066_.m_257871_().m_231551_() || minecraft.f_91074_ != null && minecraft.f_91074_.m_36337_();
         CreativeModeTab.ItemDisplayParameters displayParameters = new CreativeModeTab.ItemDisplayParameters(features, hasPermissions, minecraft.f_91073_.m_9598_());

         try {
            tab.m_269498_(displayParameters);
         } catch (LinkageError | RuntimeException var6) {
         }
      }

   }
}
