package lexis.Hack.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class LexisFakePlayer extends RemotePlayer {
   private static final Minecraft mc = Minecraft.m_91087_();
   private final LocalPlayer player;
   private final ClientLevel world;

   public LexisFakePlayer() {
      super(mc.f_91073_, mc.f_91074_.m_36316_());
      this.player = mc.f_91074_;
      this.world = mc.f_91073_;
      this.copyPlayerData();
      this.spawn();
   }

   private void copyPlayerData() {
      this.m_20359_(this.player);
      this.copyInventory();
      this.copyPlayerModel();
      this.copyRotation();
      this.copyEquipment();
      this.m_20340_(true);
      this.m_146915_(true);
      this.m_6842_(false);
      this.m_20242_(true);
      this.m_20331_(true);
      this.m_20011_(this.m_142242_());
   }

   private void copyInventory() {
      for(int i = 0; i < this.player.m_150109_().f_35974_.size(); ++i) {
         ItemStack stack = (ItemStack)this.player.m_150109_().f_35974_.get(i);
         if (!stack.m_41619_()) {
            this.m_150109_().f_35974_.set(i, stack.m_41777_());
         }
      }

   }

   private void copyPlayerModel() {
      this.m_20088_().m_135381_(Player.f_36089_, (Byte)this.player.m_20088_().m_135370_(Player.f_36089_));
   }

   private void copyRotation() {
      this.m_146922_(this.player.m_146908_());
      this.m_146926_(this.player.m_146909_());
      this.f_20885_ = this.player.f_20885_;
      this.f_20883_ = this.player.f_20883_;
   }

   private void copyEquipment() {
      EquipmentSlot[] var1 = EquipmentSlot.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         EquipmentSlot slot = var1[var3];
         ItemStack stack = this.player.m_6844_(slot);
         if (!stack.m_41619_()) {
            this.m_8061_(slot, stack.m_41777_());
         }
      }

   }

   private void spawn() {
      this.m_6034_(this.player.m_20185_(), this.player.m_20186_(), this.player.m_20189_());
      this.world.m_7967_(this);
   }

   public void despawn() {
      this.m_146870_();
   }

   public void resetPlayerPosition() {
      this.player.m_6034_(this.m_20185_(), this.m_20186_(), this.m_20189_());
      this.player.m_146922_(this.m_146908_());
      this.player.m_146926_(this.m_146909_());
   }

   public Vec3 getFakePlayerPos() {
      return new Vec3(this.m_20185_(), this.m_20186_(), this.m_20189_());
   }
}
