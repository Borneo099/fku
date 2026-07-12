package lexis.Hack.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityUtils {
   private static final Minecraft mc = Minecraft.m_91087_();

   public static Vec3 getLerpedPos(Entity e, float partialTicks) {
      if (e.m_213877_()) {
         return e.m_20182_();
      } else {
         double x = Mth.m_14139_((double)partialTicks, e.f_19854_, e.m_20185_());
         double y = Mth.m_14139_((double)partialTicks, e.f_19855_, e.m_20186_());
         double z = Mth.m_14139_((double)partialTicks, e.f_19856_, e.m_20189_());
         return new Vec3(x, y, z);
      }
   }

   public static AABB getLerpedBox(Entity e, float partialTicks) {
      if (e.m_213877_()) {
         return e.m_20191_();
      } else {
         Vec3 offset = getLerpedPos(e, partialTicks).m_82546_(e.m_20182_());
         return e.m_20191_().m_82383_(offset);
      }
   }
}
