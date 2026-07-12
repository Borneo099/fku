package lexis.Hack.Utils.FakePlayer;

import com.mojang.authlib.GameProfile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public final class FakePlayerManager {
   private static int nextId = -100000;
   private static final Map FAKES = new HashMap();

   private FakePlayerManager() {
   }

   public static FakePlayerEntity spawnClone() {
      Minecraft mc = Minecraft.m_91087_();
      ClientLevel level = mc.f_91073_;
      LocalPlayer p = mc.f_91074_;
      if (level != null && p != null) {
         GameProfile src = p.m_36316_();
         GameProfile profile = new GameProfile(UUID.randomUUID(), src.getName());
         profile.getProperties().putAll(src.getProperties());
         FakePlayerEntity fake = new FakePlayerEntity(level, profile);
         fake.m_6034_(p.m_20185_(), p.m_20186_(), p.m_20189_());
         fake.m_146922_(p.m_146908_());
         fake.m_146926_(p.m_146909_());
         fake.f_20883_ = p.f_20883_;
         fake.f_20885_ = p.f_20885_;
         fake.f_20884_ = p.f_20884_;
         fake.f_20886_ = p.f_20886_;
         Inventory inv = p.m_150109_();
         Inventory dst = fake.m_150109_();

         int id;
         for(id = 0; id < inv.m_6643_(); ++id) {
            dst.m_6836_(id, inv.m_8020_(id).m_41777_());
         }

         dst.f_35977_ = inv.f_35977_;
         EquipmentSlot[] var12 = EquipmentSlot.values();
         int var9 = var12.length;

         for(int var10 = 0; var10 < var9; ++var10) {
            EquipmentSlot slot = var12[var10];
            fake.m_8061_(slot, p.m_6844_(slot).m_41777_());
         }

         fake.m_21153_(p.m_21223_());
         fake.m_20088_().m_135381_(Player.f_36089_, (Byte)p.m_20088_().m_135370_(Player.f_36089_));
         fake.m_20124_(p.m_20089_());
         id = nextId--;
         fake.m_20234_(id);
         level.m_104630_(id, fake);
         FAKES.put(id, fake);
         return fake;
      } else {
         return null;
      }
   }

   public static void removeAll() {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91073_ == null) {
         FAKES.clear();
      } else {
         Iterator var1 = FAKES.keySet().iterator();

         while(var1.hasNext()) {
            Integer id = (Integer)var1.next();
            mc.f_91073_.m_171642_(id, RemovalReason.DISCARDED);
         }

         FAKES.clear();
      }
   }

   public static int count() {
      return FAKES.size();
   }

   public static boolean isFake(Entity e) {
      return e instanceof FakePlayerEntity;
   }

   public static boolean isFakeId(int id) {
      return FAKES.containsKey(id);
   }
}
