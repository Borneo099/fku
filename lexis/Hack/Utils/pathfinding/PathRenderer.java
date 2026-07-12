package lexis.Hack.Utils.pathfinding;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Utils.Color;
import lexis.Hack.Utils.TpAuraCore;
import lexis.Hack.Utils.Render.RenderUtils;
import net.minecraft.world.phys.Vec3;

public class PathRenderer {
   private static final double W = 0.3;
   private static final double H = 1.8;

   public void renderPath(PoseStack poseStack, List path, Color color, boolean isRaw) {
      if (path != null && path.size() >= 2) {
         Vec3 p;
         for(int i = 0; i < path.size() - 1; ++i) {
            p = (Vec3)path.get(i);
            Vec3 e = (Vec3)path.get(i + 1);
            RenderUtils.drawLine(poseStack, p.f_82479_, p.f_82480_ + 0.05, p.f_82481_, e.f_82479_, e.f_82480_ + 0.05, e.f_82481_, color);
         }

         if (!isRaw) {
            Iterator var8 = path.iterator();

            while(var8.hasNext()) {
               p = (Vec3)var8.next();
               RenderUtils.drawFilledBox(poseStack, p.f_82479_ - 0.3, p.f_82480_, p.f_82481_ - 0.3, p.f_82479_ + 0.3, p.f_82480_ + 1.8, p.f_82481_ + 0.3, new Color(color.r, color.g, color.b, 40), color, 2.0F);
            }
         }

      }
   }

   public void renderFixedSnapshot(PoseStack poseStack, List path, Color pathColor, double step, TpAuraCore core) {
      if (path != null && !path.isEmpty()) {
         this.renderPath(poseStack, path, new Color(150, 150, 150, 100), true);
         List chunked = core.getChunkedFromSnapshot(path, step);
         if (!chunked.isEmpty()) {
            this.renderPath(poseStack, chunked, pathColor, false);
         }

         if (core.desyncPos != null) {
            Vec3 d = core.desyncPos;
            RenderUtils.drawFilledBox(poseStack, d.f_82479_ - 0.3, d.f_82480_, d.f_82481_ - 0.3, d.f_82479_ + 0.3, d.f_82480_ + 1.8, d.f_82481_ + 0.3, new Color(255, 0, 0, 80), new Color(255, 0, 0, 255), 2.0F);
         }

      }
   }
}
