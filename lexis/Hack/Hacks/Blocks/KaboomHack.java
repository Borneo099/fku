package lexis.Hack.Hacks.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Collectors;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.BlockBreaker;
import lexis.Hack.Utils.BlockUtils;
import lexis.Hack.Utils.RotationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;

public class KaboomHack extends Hack {
   private int power = 128;
   private HackConfig config;

   public KaboomHack() {
      super("核爆", new String[]{"试试看效果", "§c§l警告：如果你是房主，使用这功能 可能其地客户端玩家会崩了就是连接超时 自己的没事吧", "如果自己是客户端 可能会卡连接超时一样"}, Hack.Category.BLOCKS, false);
      this.addSetting(new Hack.Setting("威力", "爆炸数量", 128.0, 32.0, 2048.0, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.power = (int)this.config.getDoubleSetting("核爆", "威力", 128.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("威力")) {
            setting.setValue((double)this.power);
            break;
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            if (setting.getName().equals("威力")) {
               this.power = (int)setting.getDouble();
               break;
            }
         }

         if (!mc.f_91074_.m_150110_().f_35937_ && !mc.f_91074_.m_21255_()) {
            NotificationManager.error("核爆", "你需要是创造模式！");
         } else {
            mc.f_91073_.m_255391_(mc.f_91074_, mc.f_91074_.m_20185_(), mc.f_91074_.m_20186_(), mc.f_91074_.m_20189_(), 6.0F, false, ExplosionInteraction.TNT);
            ArrayList blocks = this.getBlocksByDistanceReversed(6.0);

            for(int i = 0; i < this.power; ++i) {
               BlockBreaker.breakBlocksWithPacketSpam(blocks);
            }

         }
      }
   }

   private ArrayList getBlocksByDistanceReversed(double range) {
      Vec3 eyesVec = RotationUtils.getEyesPos().m_82520_(0.5, 0.5, 0.5);
      double rangeSq = Math.pow(range + 0.5, 2.0);
      int rangeI = (int)Math.ceil(range);
      BlockPos center = BlockPos.m_274446_(RotationUtils.getEyesPos());
      BlockPos min = center.m_7918_(-rangeI, -rangeI, -rangeI);
      BlockPos max = center.m_7918_(rangeI, rangeI, rangeI);
      return (ArrayList)BlockUtils.getAllInBox(min, max).stream().filter((pos) -> {
         return eyesVec.m_82557_(Vec3.m_82512_(pos)) <= rangeSq;
      }).sorted(Comparator.comparingDouble((pos) -> {
         return -eyesVec.m_82557_(Vec3.m_82512_(pos));
      })).collect(Collectors.toCollection(ArrayList::new));
   }
}
