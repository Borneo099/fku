package lexis.Hack.Hacks.Fun;

import java.util.Random;
import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

public class SkinDerpHack extends Hack implements UpdateListener {
   private final Random random = new Random();

   public SkinDerpHack() {
      super("皮肤混乱", "随机闪烁玩家的皮肤", Hack.Category.FUN, true);
   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      if (mc.f_91074_ != null) {
         int mask = 0;
         PlayerModelPart[] var2 = PlayerModelPart.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            PlayerModelPart part = var2[var4];
            mask |= part.m_36445_();
         }

         mc.f_91074_.m_20088_().m_135381_(Player.f_36089_, (byte)mask);
      }

   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         if (this.random.nextInt(4) == 0) {
            Player player = mc.f_91074_;
            int currentMask = (Byte)player.m_20088_().m_135370_(Player.f_36089_);
            int newMask = 0;
            PlayerModelPart[] var4 = PlayerModelPart.values();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               PlayerModelPart part = var4[var6];
               boolean enabled = (currentMask & part.m_36445_()) != 0;
               if (this.random.nextBoolean()) {
                  enabled = !enabled;
               }

               if (enabled) {
                  newMask |= part.m_36445_();
               }
            }

            player.m_20088_().m_135381_(Player.f_36089_, (byte)newMask);
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
