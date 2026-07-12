package moze_intel.projecte.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.imc.CustomEMCRegistration;
import moze_intel.projecte.api.imc.NSSCreatorInfo;
import moze_intel.projecte.api.imc.WorldTransmutationEntry;
import moze_intel.projecte.emc.json.NSSSerializer;
import moze_intel.projecte.emc.mappers.APICustomEMCMapper;
import moze_intel.projecte.utils.WorldTransmutations;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;

public class IMCHandler {
   public static void handleMessages(InterModProcessEvent event) {
      List entries = new ArrayList();
      event.getIMCStream("register_world_transmutation"::equals).filter((msg) -> {
         return msg.messageSupplier().get() instanceof WorldTransmutationEntry;
      }).forEach((msg) -> {
         WorldTransmutationEntry transmutationEntry = (WorldTransmutationEntry)msg.messageSupplier().get();
         entries.add(transmutationEntry);
         if (transmutationEntry.altResult() == null) {
            PECore.debugLog("Mod: '{}' registered World Transmutation from: '{}', to: '{}'", msg.senderModId(), transmutationEntry.origin(), transmutationEntry.result());
         } else {
            PECore.debugLog("Mod: '{}' registered World Transmutation from: '{}', to: '{}', with sneak output of: '{}'", msg.senderModId(), transmutationEntry.origin(), transmutationEntry.result(), transmutationEntry.altResult());
         }

      });
      WorldTransmutations.setWorldTransmutation(entries);
      event.getIMCStream("register_custom_emc"::equals).filter((msg) -> {
         return msg.messageSupplier().get() instanceof CustomEMCRegistration;
      }).forEach((msg) -> {
         APICustomEMCMapper.INSTANCE.registerCustomEMC(msg.senderModId(), (CustomEMCRegistration)msg.messageSupplier().get());
      });
      Map creators = new HashMap();
      event.getIMCStream("register_nss_serializer"::equals).filter((msg) -> {
         return msg.messageSupplier().get() instanceof NSSCreatorInfo;
      }).forEach((msg) -> {
         NSSCreatorInfo creatorInfo = (NSSCreatorInfo)msg.messageSupplier().get();
         String key = creatorInfo.key();
         if (creators.containsKey(key)) {
            PECore.LOGGER.warn("Mod: '{}' tried to register NSS creator with key: '{}', but another mod already registered that key.", msg.senderModId(), key);
         } else {
            creators.put(key, creatorInfo.creator());
            PECore.debugLog("Mod: '{}' registered NSS creator with key: '{}'", msg.senderModId(), key);
         }

      });
      NSSSerializer.INSTANCE.setCreators(creators);
   }
}
