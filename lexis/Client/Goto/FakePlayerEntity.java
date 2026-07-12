package lexis.Client.Goto;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FakePlayerEntity extends RemotePlayer {
   private static final Minecraft mc = Minecraft.m_91087_();
   private final LocalPlayer player;
   private final ClientLevel world;

   public FakePlayerEntity() {
      super(mc.f_91073_, mc.f_91074_.m_36316_());
      this.player = mc.f_91074_;
      this.world = mc.f_91073_;
      this.m_20359_(this.player);
      this.copyInventory();
      this.copyPlayerModel();
      this.copyRotation();
      this.resetCapeMovement();
      this.spawn();
   }

   private void copyInventory() {
      int i;
      ItemStack stack;
      for(i = 0; i < this.player.m_150109_().f_35974_.size(); ++i) {
         stack = (ItemStack)this.player.m_150109_().f_35974_.get(i);
         if (!stack.m_41619_()) {
            this.m_150109_().f_35974_.set(i, stack.m_41777_());
         }
      }

      for(i = 0; i < this.player.m_150109_().f_35975_.size(); ++i) {
         stack = (ItemStack)this.player.m_150109_().f_35975_.get(i);
         if (!stack.m_41619_()) {
            this.m_150109_().f_35975_.set(i, stack.m_41777_());
         }
      }

      ItemStack offhand = (ItemStack)this.player.m_150109_().f_35976_.get(0);
      if (!offhand.m_41619_()) {
         this.m_150109_().f_35976_.set(0, offhand.m_41777_());
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

   private void resetCapeMovement() {
      this.f_19854_ = this.m_20185_();
      this.f_19855_ = this.m_20186_();
      this.f_19856_ = this.m_20189_();
   }

   private void spawn() {
      this.m_6034_(this.player.m_20185_(), this.player.m_20186_(), this.player.m_20189_());
      this.world.m_7967_(this);
   }

   public void despawn() {
      this.m_146870_();
   }

   public void resetPlayerPosition() {
      this.player.m_6021_(this.m_20185_(), this.m_20186_(), this.m_20189_());
      this.player.m_146922_(this.m_146908_());
      this.player.m_146926_(this.m_146909_());
      this.player.f_20885_ = this.f_20885_;
      this.player.f_20883_ = this.f_20883_;
   }
}
