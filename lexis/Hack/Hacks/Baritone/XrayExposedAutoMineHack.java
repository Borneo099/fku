package lexis.Hack.Hacks.Baritone;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.XrayExposedHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Utils.BaritoneBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class XrayExposedAutoMineHack extends Hack {
   private static final String REQUIRED_HACK = "Xray(露出版)";
   private State state;
   private BlockPos currentTarget;
   private int waitTicks;
   private int range;
   public static final Map ORE_MAP = new LinkedHashMap();
   private final Set enabledOres;

   public static boolean suppressBaritoneMessages() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      while(var0.hasNext()) {
         Hack hack = (Hack)var0.next();
         if (hack instanceof XrayExposedAutoMineHack am) {
            if (am.isEnabled() && am.state != XrayExposedAutoMineHack.State.IDLE) {
               return true;
            }
         }
      }

      return false;
   }

   public XrayExposedAutoMineHack() {
      super("Xray露出的自动挖矿", new String[]{"需要与 Xray(露出版) 开启！这是自动挖矿露出的矿石 无视反矿透 插件/模组"}, Hack.Category.BARITONE, true);
      this.state = XrayExposedAutoMineHack.State.IDLE;
      this.range = 64;
      this.enabledOres = Collections.newSetFromMap(new IdentityHashMap());
      this.addSetting(new Hack.Setting("扫描范围", "查找最近矿石的范围", 64, 16, 256));
      Iterator var1 = ORE_MAP.entrySet().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         Hack.Setting s = new Hack.Setting((String)entry.getValue(), "是否挖 " + (String)entry.getValue(), true);
         this.addSetting(s);
         if (s.getBoolean()) {
            this.enabledOres.add((Block)entry.getKey());
         }
      }

   }

   public void onEnable() {
      Hack xray = findXrayHack();
      if (xray != null && xray.isEnabled()) {
         this.state = XrayExposedAutoMineHack.State.IDLE;
         this.currentTarget = null;
         this.waitTicks = 0;
      } else {
         this.setEnabled(false);
         if (mc.f_91074_ != null) {
            mc.f_91074_.m_5661_(Component.m_237113_("[Lexis] 你需要开启一个功能 \"Xray(露出版)\" ！ 最后再开启当前功能！"), false);
         }

      }
   }

   public void onDisable() {
      BaritoneBridge.executeCommand("sel clear");
      this.state = XrayExposedAutoMineHack.State.IDLE;
      this.currentTarget = null;
      this.waitTicks = 0;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         if (BaritoneBridge.isAvailable()) {
            Iterator var1 = this.getSettings().iterator();

            Hack.Setting s;
            while(var1.hasNext()) {
               s = (Hack.Setting)var1.next();
               if (s.getName().equals("扫描范围")) {
                  this.range = s.getInt();
               }
            }

            this.enabledOres.clear();
            var1 = this.getSettings().iterator();

            while(var1.hasNext()) {
               s = (Hack.Setting)var1.next();
               Iterator var3 = ORE_MAP.entrySet().iterator();

               while(var3.hasNext()) {
                  Map.Entry e = (Map.Entry)var3.next();
                  if (s.getName().equals(e.getValue()) && s.getBoolean()) {
                     this.enabledOres.add((Block)e.getKey());
                  }
               }
            }

            int x;
            int y;
            int z;
            switch (this.state) {
               case IDLE:
                  this.currentTarget = this.findNearestExposedOre();
                  if (this.currentTarget != null) {
                     x = this.currentTarget.m_123341_();
                     y = this.currentTarget.m_123342_();
                     z = this.currentTarget.m_123343_();
                     BaritoneBridge.executeCommand("sel p1 " + x + " " + y + " " + z);
                     this.state = XrayExposedAutoMineHack.State.SEL_P1;
                     this.waitTicks = 2;
                  }
                  break;
               case SEL_P1:
                  if (--this.waitTicks > 0) {
                     return;
                  }

                  x = this.currentTarget.m_123341_();
                  y = this.currentTarget.m_123342_();
                  z = this.currentTarget.m_123343_();
                  BaritoneBridge.executeCommand("sel p2 " + x + " " + y + " " + z);
                  this.state = XrayExposedAutoMineHack.State.SEL_P2;
                  this.waitTicks = 2;
                  break;
               case SEL_P2:
                  if (--this.waitTicks > 0) {
                     return;
                  }

                  BaritoneBridge.executeCommand("sel fill air");
                  this.state = XrayExposedAutoMineHack.State.FILL;
                  this.waitTicks = 3;
                  break;
               case FILL:
                  if (--this.waitTicks > 0) {
                     return;
                  }

                  Hack xray = findXrayHack();
                  if (xray instanceof XrayExposedHack && this.currentTarget != null) {
                     ((XrayExposedHack)xray).renderPositions.remove(this.currentTarget);
                  }

                  this.currentTarget = null;
                  this.state = XrayExposedAutoMineHack.State.IDLE;
            }

         }
      }
   }

   private BlockPos findNearestExposedOre() {
      Hack xray = findXrayHack();
      if (xray instanceof XrayExposedHack xh) {
         if (xh.isEnabled()) {
            BlockPos playerPos = mc.f_91074_.m_20183_();
            BlockPos nearest = null;
            double nearestDist = Double.MAX_VALUE;
            Iterator var7 = xh.renderPositions.entrySet().iterator();

            while(var7.hasNext()) {
               Map.Entry entry = (Map.Entry)var7.next();
               BlockPos pos = (BlockPos)entry.getKey();
               Block block = mc.f_91073_.m_8055_(pos).m_60734_();
               if (this.enabledOres.contains(block)) {
                  double dist = pos.m_123331_(playerPos);
                  if (dist < nearestDist && dist <= (double)this.range * (double)this.range) {
                     nearestDist = dist;
                     nearest = pos;
                  }
               }
            }

            return nearest;
         }
      }

      return null;
   }

   private static Hack findXrayHack() {
      Iterator var0 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var0.hasNext()) {
            return null;
         }

         hack = (Hack)var0.next();
      } while(!hack.getName().equals("Xray(露出版)"));

      return hack;
   }

   public void onClick() {
      this.toggle();
   }

   static {
      ORE_MAP.put(Blocks.f_50089_, "钻石矿");
      ORE_MAP.put(Blocks.f_152474_, "深层钻石矿");
      ORE_MAP.put(Blocks.f_49995_, "金矿");
      ORE_MAP.put(Blocks.f_152467_, "深层金矿");
      ORE_MAP.put(Blocks.f_49996_, "铁矿");
      ORE_MAP.put(Blocks.f_152468_, "深层铁矿");
      ORE_MAP.put(Blocks.f_50264_, "绿宝石矿");
      ORE_MAP.put(Blocks.f_152479_, "深层绿宝石矿");
      ORE_MAP.put(Blocks.f_49997_, "煤矿");
      ORE_MAP.put(Blocks.f_152469_, "深层煤矿");
      ORE_MAP.put(Blocks.f_152505_, "铜矿");
      ORE_MAP.put(Blocks.f_152506_, "深层铜矿");
      ORE_MAP.put(Blocks.f_50173_, "红石矿");
      ORE_MAP.put(Blocks.f_152473_, "深层红石矿");
      ORE_MAP.put(Blocks.f_50059_, "青金石矿");
      ORE_MAP.put(Blocks.f_152472_, "深层青金石矿");
      ORE_MAP.put(Blocks.f_49998_, "下界金矿");
      ORE_MAP.put(Blocks.f_50331_, "下界石英矿");
      ORE_MAP.put(Blocks.f_50722_, "远古残骸");
   }

   private static enum State {
      IDLE,
      SEL_P1,
      SEL_P2,
      FILL;

      // $FF: synthetic method
      private static State[] $values() {
         return new State[]{IDLE, SEL_P1, SEL_P2, FILL};
      }
   }
}
