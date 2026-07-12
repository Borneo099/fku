package lexis.Client.Goto;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.List;

public class RenderEvent extends Event {
   private final PoseStack poseStack;
   private final float partialTick;

   public RenderEvent(PoseStack poseStack, float partialTick) {
      this.poseStack = poseStack;
      this.partialTick = partialTick;
   }

   public void fire(List listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         RenderListener listener = (RenderListener)var2.next();

         try {
            listener.onRender(this.poseStack, this.partialTick);
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

   }

   public Class getListenerType() {
      return RenderListener.class;
   }
}
