package moze_intel.projecte.gameObjs.items.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.IStepAssister;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GemFeet extends GemArmorBase implements IFlightProvider, IStepAssister {
   private static final UUID MODIFIER = UUID.fromString("A4334312-DFF8-4582-9F4F-62AD0C070475");
   private final Multimap attributes;

   public GemFeet(Item.Properties props) {
      super(Type.BOOTS, props);
      ImmutableMultimap.Builder attributesBuilder = ImmutableMultimap.builder();
      attributesBuilder.putAll(this.m_7167_(EquipmentSlot.FEET));
      attributesBuilder.put(Attributes.f_22279_, new AttributeModifier(MODIFIER, "Armor modifier", 1.0, Operation.MULTIPLY_TOTAL));
      this.attributes = attributesBuilder.build();
   }

   public void toggleStepAssist(ItemStack boots, Player player) {
      CompoundTag bootsTag = boots.m_41784_();
      boolean value;
      if (bootsTag.m_128425_("StepAssist", 1)) {
         value = !bootsTag.m_128471_("StepAssist");
         bootsTag.m_128379_("StepAssist", value);
      } else {
         bootsTag.m_128379_("StepAssist", true);
         value = true;
      }

      if (value) {
         player.m_213846_(PELang.STEP_ASSIST.translate(new Object[]{ChatFormatting.GREEN, PELang.GEM_ENABLED}));
      } else {
         player.m_213846_(PELang.STEP_ASSIST.translate(new Object[]{ChatFormatting.RED, PELang.GEM_DISABLED}));
      }

   }

   private static boolean isJumpPressed() {
      return (Boolean)DistExecutor.unsafeRunForDist(() -> {
         return () -> {
            return Minecraft.m_91087_().f_91066_.f_92089_.m_90857_();
         };
      }, () -> {
         return () -> {
            return false;
         };
      });
   }

   public void onArmorTick(ItemStack stack, Level level, Player player) {
      if (!level.f_46443_) {
         ServerPlayer playerMP = (ServerPlayer)player;
         playerMP.f_19789_ = 0.0F;
      } else {
         if (!player.m_150110_().f_35935_ && isJumpPressed()) {
            player.m_20256_(player.m_20184_().m_82520_(0.0, 0.1, 0.0));
         }

         if (!player.m_20096_()) {
            if (player.m_20184_().m_7098_() <= 0.0) {
               player.m_20256_(player.m_20184_().m_82542_(1.0, 0.9, 1.0));
            }

            if (!player.m_150110_().f_35935_) {
               if (player.f_20902_ < 0.0F) {
                  player.m_20256_(player.m_20184_().m_82542_(0.9, 1.0, 0.9));
               } else if (player.f_20902_ > 0.0F && player.m_20184_().m_82556_() < 3.0) {
                  player.m_20256_(player.m_20184_().m_82542_(1.1, 1.0, 1.1));
               }
            }
         }
      }

   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(PELang.GEM_LORE_FEET.translate(new Object[0]));
      tooltips.add(PELang.STEP_ASSIST_PROMPT.translate(new Object[]{ClientKeyHelper.getKeyName(PEKeybind.BOOTS_TOGGLE)}));
      if (ItemHelper.checkItemNBT(stack, "StepAssist")) {
         tooltips.add(PELang.STEP_ASSIST.translate(new Object[]{ChatFormatting.GREEN, PELang.GEM_ENABLED}));
      } else {
         tooltips.add(PELang.STEP_ASSIST.translate(new Object[]{ChatFormatting.RED, PELang.GEM_DISABLED}));
      }

   }

   public @NotNull Multimap getAttributeModifiers(@NotNull EquipmentSlot slot, ItemStack stack) {
      return slot == EquipmentSlot.FEET ? this.attributes : super.getAttributeModifiers(slot, stack);
   }

   public boolean canProvideFlight(ItemStack stack, ServerPlayer player) {
      return player.m_6844_(EquipmentSlot.FEET) == stack;
   }

   public boolean canAssistStep(ItemStack stack, ServerPlayer player) {
      return player.m_6844_(EquipmentSlot.FEET) == stack && ItemHelper.checkItemNBT(stack, "StepAssist");
   }
}
