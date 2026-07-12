package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.Objects;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "projecte",
   value = {Dist.CLIENT}
)
public class ClientKeyHelper {
   private static ImmutableBiMap mcToPe = ImmutableBiMap.of();
   private static ImmutableBiMap peToMc = ImmutableBiMap.of();

   @SubscribeEvent
   public static void keyPress(TickEvent.ClientTickEvent event) {
      UnmodifiableIterator var1 = mcToPe.keySet().iterator();

      while(var1.hasNext()) {
         KeyMapping k = (KeyMapping)var1.next();

         while(k.m_90859_()) {
            PacketHandler.sendToServer(new KeyPressPKT((PEKeybind)mcToPe.get(k)));
         }
      }

   }

   public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
      ImmutableBiMap.Builder builder = ImmutableBiMap.builder();
      addKeyBinding(builder, PEKeybind.HELMET_TOGGLE, KeyModifier.SHIFT, 88);
      addKeyBinding(builder, PEKeybind.BOOTS_TOGGLE, KeyModifier.NONE, 88);
      addKeyBinding(builder, PEKeybind.CHARGE, KeyModifier.NONE, 86);
      addKeyBinding(builder, PEKeybind.EXTRA_FUNCTION, KeyModifier.NONE, 67);
      addKeyBinding(builder, PEKeybind.FIRE_PROJECTILE, KeyModifier.NONE, 82);
      addKeyBinding(builder, PEKeybind.MODE, KeyModifier.NONE, 71);
      mcToPe = builder.build();
      peToMc = mcToPe.inverse();
      ImmutableSet var10000 = mcToPe.keySet();
      Objects.requireNonNull(event);
      var10000.forEach(event::register);
   }

   private static void addKeyBinding(ImmutableBiMap.Builder builder, PEKeybind keyBind, KeyModifier modifier, int keyCode) {
      builder.put(new KeyMapping(keyBind.getTranslationKey(), KeyConflictContext.IN_GAME, modifier, Type.KEYSYM, keyCode, PELang.PROJECTE.getTranslationKey()), keyBind);
   }

   public static Component getKeyName(PEKeybind k) {
      return (Component)(peToMc.containsKey(k) ? ((KeyMapping)peToMc.get(k)).m_90863_() : TextComponentUtil.build(k));
   }
}
