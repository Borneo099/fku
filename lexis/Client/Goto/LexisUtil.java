package lexis.Client.Goto;

import net.minecraft.client.Minecraft;

public class LexisUtil {
   public static final Minecraft MC = Minecraft.m_91087_();
   public static final LexisUtil INSTANCE = new LexisUtil();
   private RotationFaker rotationFaker = new RotationFaker();

   public RotationFaker getRotationFaker() {
      return this.rotationFaker;
   }
}
