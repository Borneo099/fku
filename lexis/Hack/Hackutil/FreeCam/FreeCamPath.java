package lexis.Hack.Hackutil.FreeCam;

import java.util.ArrayList;
import java.util.List;
import lexis.Hack.Hacks.Render.FreeCamHack;
import net.minecraft.world.phys.Vec3;

public class FreeCamPath {
   private final FreeCamHack freeCam;
   private final List entries = new ArrayList();

   public FreeCamPath(FreeCamHack freeCam) {
      this.freeCam = freeCam;
   }

   public void add(double time) {
      if (this.freeCam.isActive()) {
         double clamped = Math.max(0.0, Math.min(time, 3600000.0));
         this.entries.add(new Entry(new Vec3(this.freeCam.getX(), this.freeCam.getY(), this.freeCam.getZ()), (double)this.freeCam.getXRot(), (double)this.freeCam.getYRot(), clamped));
      }

   }

   public void clear() {
      this.entries.clear();
   }

   public List getEntries() {
      return this.entries;
   }

   public Entry interpolate(double elapsedMillis) {
      for(int i = 1; i < this.entries.size(); ++i) {
         Entry e2 = (Entry)this.entries.get(i);
         if (elapsedMillis < e2.time) {
            Entry e1 = (Entry)this.entries.get(i - 1);
            double factor = elapsedMillis / e2.time;
            return this.interpolate(e1, e2, factor);
         }

         elapsedMillis -= e2.time;
      }

      return null;
   }

   private Entry interpolate(Entry e1, Entry e2, double factor) {
      Vec3 pos = new Vec3(e1.position.f_82479_ + (e2.position.f_82479_ - e1.position.f_82479_) * factor, e1.position.f_82480_ + (e2.position.f_82480_ - e1.position.f_82480_) * factor, e1.position.f_82481_ + (e2.position.f_82481_ - e1.position.f_82481_) * factor);
      double xRot = e1.xRot + (e2.xRot - e1.xRot) * factor;
      double yRot = e1.yRot + (e2.yRot - e1.yRot) * factor;
      return new Entry(pos, xRot, yRot, 0.0);
   }

   public static record Entry(Vec3 position, double xRot, double yRot, double time) {
      public Entry(Vec3 position, double xRot, double yRot, double time) {
         this.position = position;
         this.xRot = xRot;
         this.yRot = yRot;
         this.time = time;
      }

      public Vec3 position() {
         return this.position;
      }

      public double xRot() {
         return this.xRot;
      }

      public double yRot() {
         return this.yRot;
      }

      public double time() {
         return this.time;
      }
   }
}
