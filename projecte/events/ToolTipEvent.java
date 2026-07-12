package moze_intel.projecte.events;

import java.util.List;
import java.util.Optional;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "projecte",
   value = {Dist.CLIENT}
)
public class ToolTipEvent {
   @SubscribeEvent
   public static void tTipEvent(ItemTooltipEvent event) {
      ItemStack current = event.getItemStack();
      if (!current.m_41619_()) {
         Player clientPlayer = Minecraft.m_91087_().f_91074_;
         if (ProjectEConfig.client.pedestalToolTips.get()) {
            current.getCapability(PECapabilities.PEDESTAL_ITEM_CAPABILITY).ifPresent((pedestalItem) -> {
               event.getToolTip().add(PELang.PEDESTAL_ON.translateColored(ChatFormatting.DARK_PURPLE, new Object[0]));
               List description = pedestalItem.getPedestalDescription();
               if (description.isEmpty()) {
                  event.getToolTip().add(PELang.PEDESTAL_DISABLED.translateColored(ChatFormatting.RED, new Object[0]));
               } else {
                  event.getToolTip().addAll(description);
               }

            });
         }

         if (ProjectEConfig.client.tagToolTips.get()) {
            current.m_204131_().forEach((tagx) -> {
               event.getToolTip().add(Component.m_237113_("#" + tagx.f_203868_()));
            });
         }

         long value;
         if (ProjectEConfig.client.emcToolTips.get() && (!ProjectEConfig.client.shiftEmcToolTips.get() || Screen.m_96638_())) {
            value = EMCHelper.getEmcValue(current);
            if (value > 0L) {
               event.getToolTip().add(EMCHelper.getEmcTextComponent(value, 1));
               if (current.m_41613_() > 1) {
                  event.getToolTip().add(EMCHelper.getEmcTextComponent(value, current.m_41613_()));
               }

               if (clientPlayer != null && (!ProjectEConfig.client.shiftLearnedToolTips.get() || Screen.m_96638_())) {
                  if ((Boolean)clientPlayer.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).map((k) -> {
                     return k.hasKnowledge(current);
                  }).orElse(false)) {
                     event.getToolTip().add(PELang.EMC_HAS_KNOWLEDGE.translateColored(ChatFormatting.YELLOW, new Object[0]));
                  } else {
                     event.getToolTip().add(PELang.EMC_NO_KNOWLEDGE.translateColored(ChatFormatting.RED, new Object[0]));
                  }
               }
            }
         }

         if (current.m_41782_()) {
            CompoundTag tag = current.m_41784_();
            if (tag.m_128425_("StoredEMC", 4)) {
               value = tag.m_128454_("StoredEMC");
            } else {
               Optional holderCapability = current.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).resolve();
               if (!holderCapability.isPresent()) {
                  return;
               }

               value = ((IItemEmcHolder)holderCapability.get()).getStoredEmc(current);
            }

            event.getToolTip().add(PELang.EMC_STORED.translateColored(ChatFormatting.YELLOW, new Object[]{ChatFormatting.WHITE, Constants.EMC_FORMATTER.format(value)}));
         }

      }
   }
}
