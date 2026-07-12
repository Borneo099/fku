package moze_intel.projecte.events;

import moze_intel.projecte.gameObjs.items.armor.GemFeet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "projecte",
   value = {Dist.CLIENT}
)
public class PlayerRender {
   @SubscribeEvent
   public static void onFOVUpdateEvent(ComputeFovModifierEvent evt) {
      if (!evt.getPlayer().m_6844_(EquipmentSlot.FEET).m_41619_() && evt.getPlayer().m_6844_(EquipmentSlot.FEET).m_41720_() instanceof GemFeet) {
         evt.setNewFovModifier(evt.getNewFovModifier() - 0.5F * ((Double)Minecraft.m_91087_().f_91066_.m_231925_().m_231551_()).floatValue());
      }

   }
}
