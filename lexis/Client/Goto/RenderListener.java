package lexis.Client.Goto;

import com.mojang.blaze3d.vertex.PoseStack;

public interface RenderListener extends Listener {
   void onRender(PoseStack poseStack, float partialTick);
}
