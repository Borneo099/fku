package moze_intel.projecte.network.packets.to_server;

import java.util.Optional;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import moze_intel.projecte.gameObjs.items.armor.GemChest;
import moze_intel.projecte.gameObjs.items.armor.GemFeet;
import moze_intel.projecte.gameObjs.items.armor.GemHelmet;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.NonNullPredicate;
import net.minecraftforge.network.NetworkEvent;

public record KeyPressPKT(PEKeybind key) implements IPEPacket {
   public KeyPressPKT(PEKeybind key) {
      this.key = key;
   }

   public void handle(NetworkEvent.Context context) {
      ServerPlayer player = context.getSender();
      if (player != null && !player.m_5833_()) {
         ItemStack boots;
         if (this.key == PEKeybind.HELMET_TOGGLE) {
            boots = player.m_6844_(EquipmentSlot.HEAD);
            if (!boots.m_41619_() && boots.m_41720_() instanceof GemHelmet) {
               GemHelmet.toggleNightVision(boots, player);
            }

         } else if (this.key == PEKeybind.BOOTS_TOGGLE) {
            boots = player.m_6844_(EquipmentSlot.FEET);
            if (!boots.m_41619_() && boots.m_41720_() instanceof GemFeet) {
               ((GemFeet)boots.m_41720_()).toggleStepAssist(boots, player);
            }

         } else {
            Optional cap = player.getCapability(InternalAbilities.CAPABILITY).resolve();
            if (!cap.isEmpty()) {
               InternalAbilities internalAbilities = (InternalAbilities)cap.get();
               InteractionHand[] var5 = InteractionHand.values();
               int var6 = var5.length;

               for(int var7 = 0; var7 < var6; ++var7) {
                  InteractionHand hand = var5[var7];
                  ItemStack stack = player.m_21120_(hand);
                  ItemStack helmet;
                  Item var12;
                  switch (this.key) {
                     case CHARGE:
                        if (tryPerformCapability(stack, PECapabilities.CHARGE_ITEM_CAPABILITY, (capability) -> {
                           return capability.changeCharge(player, stack, hand);
                        })) {
                           return;
                        }

                        if (hand == InteractionHand.MAIN_HAND && isSafe(stack) && GemArmorBase.hasAnyPiece(player)) {
                           internalAbilities.setGemState(!internalAbilities.getGemState());
                           ILangEntry langEntry = internalAbilities.getGemState() ? PELang.GEM_ACTIVATE : PELang.GEM_DEACTIVATE;
                           player.m_213846_(langEntry.translate());
                           return;
                        }
                        break;
                     case EXTRA_FUNCTION:
                        if (tryPerformCapability(stack, PECapabilities.EXTRA_FUNCTION_ITEM_CAPABILITY, (capability) -> {
                           return capability.doExtraFunction(stack, player, hand);
                        })) {
                           return;
                        }

                        if (hand == InteractionHand.MAIN_HAND && isSafe(stack) && internalAbilities.getGemState()) {
                           helmet = player.m_6844_(EquipmentSlot.CHEST);
                           if (!helmet.m_41619_()) {
                              var12 = helmet.m_41720_();
                              if (var12 instanceof GemChest) {
                                 GemChest chest = (GemChest)var12;
                                 if (internalAbilities.getGemCooldown() == 0) {
                                    chest.doExplode(player);
                                    internalAbilities.resetGemCooldown();
                                    return;
                                 }
                              }
                           }
                        }
                        break;
                     case FIRE_PROJECTILE:
                        if (!stack.m_41619_() && internalAbilities.getProjectileCooldown() == 0 && tryPerformCapability(stack, PECapabilities.PROJECTILE_SHOOTER_ITEM_CAPABILITY, (capability) -> {
                           return capability.shootProjectile(player, stack, hand);
                        })) {
                           PlayerHelper.swingItem(player, hand);
                           internalAbilities.resetProjectileCooldown();
                        }

                        if (hand == InteractionHand.MAIN_HAND && isSafe(stack) && internalAbilities.getGemState()) {
                           helmet = player.m_6844_(EquipmentSlot.HEAD);
                           if (!helmet.m_41619_()) {
                              var12 = helmet.m_41720_();
                              if (var12 instanceof GemHelmet) {
                                 GemHelmet gemHelmet = (GemHelmet)var12;
                                 gemHelmet.doZap(player);
                                 return;
                              }
                           }
                        }
                        break;
                     case MODE:
                        if (tryPerformCapability(stack, PECapabilities.MODE_CHANGER_ITEM_CAPABILITY, (capability) -> {
                           return capability.changeMode(player, stack, hand);
                        })) {
                           return;
                        }
                  }
               }

            }
         }
      }
   }

   private static boolean tryPerformCapability(ItemStack stack, Capability capability, NonNullPredicate perform) {
      return !stack.m_41619_() && stack.getCapability(capability).filter(perform).isPresent();
   }

   private static boolean isSafe(ItemStack stack) {
      return ProjectEConfig.server.misc.unsafeKeyBinds.get() || stack.m_41619_();
   }

   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.key);
   }

   public static KeyPressPKT decode(FriendlyByteBuf buf) {
      return new KeyPressPKT((PEKeybind)buf.m_130066_(PEKeybind.class));
   }

   public PEKeybind key() {
      return this.key;
   }
}
