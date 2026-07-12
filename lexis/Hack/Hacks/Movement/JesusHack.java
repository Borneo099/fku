package lexis.Hack.Hacks.Movement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketOutputListener;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class JesusHack extends Hack implements UpdateListener, PacketOutputListener {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "液体行走";
   private int tickTimer = 10;
   private int packetTimer = 0;
   private static JesusHack instance;

   public JesusHack() {
      super("液体行走", new String[]{"可以在水或岩浆上行走"}, Hack.Category.MOVEMENT, true);
      instance = this;
      this.loadConfig();
   }

   private void loadConfig() {
   }

   public static boolean isActive() {
      return instance != null && instance.isEnabled();
   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
      EventManager.add(PacketOutputListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      EventManager.remove(PacketOutputListener.class, this);
   }

   public void onUpdate() {
      LocalPlayer player = mc.f_91074_;
      if (player != null) {
         if (!mc.f_91066_.f_92090_.m_90857_()) {
            if (!player.m_20069_() && !player.m_20077_()) {
               if (this.tickTimer == 0) {
                  player.m_20334_(player.m_20184_().f_82479_, 0.3, player.m_20184_().f_82481_);
               } else if (this.tickTimer == 1) {
                  player.m_20334_(player.m_20184_().f_82479_, 0.0, player.m_20184_().f_82481_);
               }

               ++this.tickTimer;
            } else {
               player.m_20334_(player.m_20184_().f_82479_, 0.11, player.m_20184_().f_82481_);
               this.tickTimer = 0;
            }

         }
      }
   }

   public void onPacketOutput(PacketOutputListener.PacketOutputEvent event) {
      Packet packet = event.getPacket();
      if (packet instanceof ServerboundMovePlayerPacket) {
         LocalPlayer player = mc.f_91074_;
         if (player == null) {
            return;
         }

         if (player.m_20069_()) {
            return;
         }

         if (!this.isOverLiquid()) {
            return;
         }

         if (player.f_19789_ > 3.0F) {
            return;
         }

         ServerboundMovePlayerPacket movePacket = (ServerboundMovePlayerPacket)packet;
         ++this.packetTimer;
         if (this.packetTimer >= 4) {
            event.cancel();
            double x = movePacket.m_134129_(0.0);
            double y = movePacket.m_134140_(0.0);
            double z = movePacket.m_134146_(0.0);
            y += 0.05;
            Object newPacket;
            if (packet instanceof ServerboundMovePlayerPacket.Pos) {
               newPacket = new ServerboundMovePlayerPacket.Pos(x, y, z, true);
            } else if (packet instanceof ServerboundMovePlayerPacket.Rot) {
               newPacket = new ServerboundMovePlayerPacket.Rot(movePacket.m_134131_(0.0F), movePacket.m_134142_(0.0F), true);
            } else {
               newPacket = new ServerboundMovePlayerPacket.PosRot(x, y, z, movePacket.m_134131_(0.0F), movePacket.m_134142_(0.0F), true);
            }

            if (player.f_108617_ != null) {
               player.f_108617_.m_104955_((Packet)newPacket);
            }
         }
      }

   }

   private boolean isOverLiquid() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         AABB boundingBox = mc.f_91074_.m_20191_();
         AABB checkBox = boundingBox.m_82386_(0.0, -0.5, 0.0);
         boolean foundLiquid = false;
         boolean foundSolid = false;
         List shapes = new ArrayList();
         Iterable var10000 = mc.f_91073_.m_186434_(mc.f_91074_, checkBox);
         Objects.requireNonNull(shapes);
         var10000.forEach(shapes::add);
         Iterator var6 = shapes.iterator();

         while(var6.hasNext()) {
            VoxelShape shape = (VoxelShape)var6.next();
            Iterator var8 = shape.m_83299_().iterator();

            while(var8.hasNext()) {
               AABB box = (AABB)var8.next();
               BlockPos pos = BlockPos.m_274446_(box.m_82399_());
               Block block = mc.f_91073_.m_8055_(pos).m_60734_();
               if (block instanceof LiquidBlock) {
                  foundLiquid = true;
               } else if (!(block instanceof LiquidBlock)) {
                  foundSolid = true;
               }
            }
         }

         return foundLiquid && !foundSolid;
      } else {
         return false;
      }
   }

   public void onClick() {
      this.toggle();
   }
}
