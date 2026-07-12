package lexis.Hack.Utils.FakePlayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.damagesource.DamageSource;

public class FakePlayerEntity extends RemotePlayer {
   public FakePlayerEntity(ClientLevel level, GameProfile profile) {
      super(level, profile);
   }

   public boolean m_6469_(DamageSource source, float amount) {
      this.f_20916_ = 10;
      this.f_20917_ = 10;
      this.f_263750_ = 0.0F;
      this.m_6053_(0.0F);
      float h = this.m_21223_() - amount;
      if (h < 1.0F) {
         h = 1.0F;
      }

      this.m_21153_(h);
      return true;
   }

   public boolean m_6084_() {
      return true;
   }

   public boolean m_21224_() {
      return false;
   }

   public boolean m_20147_() {
      return false;
   }

   public boolean m_6094_() {
      return false;
   }

   public boolean m_6052_() {
      return true;
   }

   public void m_8119_() {
      this.m_6075_();
      this.m_21203_();
      this.m_21217_();
      if (this.f_20916_ > 0) {
         --this.f_20916_;
      }

   }
}
