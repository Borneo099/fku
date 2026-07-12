package lexis.Hack.Hackutil.EntityEsp;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.EntityEspHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FakeGlowHandler {
   @SubscribeEvent
   public void onRenderLiving(RenderLivingEvent.Pre event) {
      Iterator var3 = HackManager.getInstance().getHacks().iterator();

      while(var3.hasNext()) {
         Hack hack = (Hack)var3.next();
         if (hack instanceof EntityEspHack esp && hack.isEnabled()) {
            break;
         }
      }

      if (esp != null) {
         LivingEntity entity = event.getEntity();
         String entityId = entity.m_6095_().m_204041_().m_205785_().m_135782_().toString();
         EntityEspHack.EntitySettings settings = (EntityEspHack.EntitySettings)esp.getEntitySettings().get(entityId);
         if (settings != null && settings.fakeGlow) {
            entity.m_146915_(true);
         }

      }
   }
}
