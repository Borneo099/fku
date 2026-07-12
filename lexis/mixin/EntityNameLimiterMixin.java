package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Protect.EntityNameLimiterHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.NotificationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public class EntityNameLimiterMixin {
   @Inject(
      method = {"getDisplayName"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetDisplayName(CallbackInfoReturnable cir) {
      boolean hackEnabled = false;
      int maxLength = 32;
      Iterator var4 = HackManager.getInstance().getHacks().iterator();

      while(var4.hasNext()) {
         Hack hack = (Hack)var4.next();
         if (hack instanceof EntityNameLimiterHack && hack.isEnabled()) {
            hackEnabled = true;
            maxLength = EntityNameLimiterHack.getMaxNameLength();
            break;
         }
      }

      if (hackEnabled) {
         Component originalName = (Component)cir.getReturnValue();
         String nameString = originalName.getString();
         if (nameString.length() > maxLength) {
            String var10000 = nameString.substring(0, maxLength);
            String truncatedName = var10000 + "§c...";
            cir.setReturnValue(Component.m_237113_(truncatedName));
            Entity entity = (Entity)this;
            int entityId = entity.m_19879_();
            if (EntityNameLimiterHack.shouldWarn(entityId)) {
               String entityPos = String.format("(%.1f, %.1f, %.1f)", entity.m_20185_(), entity.m_20186_(), entity.m_20189_());
               NotificationManager.warning("实体名称限制：", "检测到过长实体名称，已经被处理！位置: " + entityPos, 5);
               EntityNameLimiterHack.addWarnedEntity(entityId);
            }
         }

      }
   }
}
