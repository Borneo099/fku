package lexis.Hack.events;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;

public interface RenderListener extends Listener {
   void onRender(PoseStack var1, float var2);

   public static class RenderEvent extends Event {
      private final PoseStack matrixStack;
      private final float partialTicks;

      public RenderEvent(PoseStack matrixStack, float partialTicks) {
         this.matrixStack = matrixStack;
         this.partialTicks = partialTicks;
      }

      public PoseStack getMatrixStack() {
         return this.matrixStack;
      }

      public float getPartialTicks() {
         return this.partialTicks;
      }

      public void fire(ArrayList listeners) {
         Iterator var2 = listeners.iterator();

         while(var2.hasNext()) {
            RenderListener listener = (RenderListener)var2.next();
            listener.onRender(this.matrixStack, this.partialTicks);
         }

      }

      public Class getListenerType() {
         return RenderListener.class;
      }
   }
}
