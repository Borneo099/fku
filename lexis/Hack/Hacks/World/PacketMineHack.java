package lexis.Hack.Hacks.World;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.Utils.Render.RenderUtils;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import lexis.Hack.events.StartBreakingBlockEvent;
import lexis.Hack.events.StartBreakingBlockListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PacketMineHack extends Hack implements StartBreakingBlockListener, RenderListener {
   private int delay = 1;
   private boolean rotate = true;
   private boolean autoSwitch = false;
   private boolean notOnUse = true;
   private boolean render = true;
   private String shapeMode = "BOTH";
   private SettingColor readySideColor = new SettingColor(0, 204, 0, 10);
   private SettingColor readyLineColor = new SettingColor(0, 204, 0, 255);
   private SettingColor sideColor = new SettingColor(204, 0, 0, 10);
   private SettingColor lineColor = new SettingColor(204, 0, 0, 255);
   private final List blocks = new ArrayList();
   private boolean swapped = false;
   private boolean shouldUpdateSlot = false;
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "数据包挖掘";

   public PacketMineHack() {
      super("数据包挖掘", new String[]{"发包挖掘。", "§4§l源码功能来自：Meteor1.20.1Fabric"}, Hack.Category.WORLD, true);
      this.addSetting(new Hack.Setting("延迟(ticks)", "挖掘延迟", 1, 0, 5, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("旋转", "挖掘时看向方块", true));
      this.addSetting(new Hack.Setting("自动切换工具", "自动切换最佳工具", false));
      this.addSetting(new Hack.Setting("物品使用时切换", "使用物品时不自动切换", true));
      this.addSetting(new Hack.Setting("渲染", "显示挖掘方块轮廓", true));
      this.addSetting(new Hack.Setting("形状模式", "渲染形状", "BOTH", new String[]{"BOX", "LINES", "BOTH"}));
      this.addSetting(new Hack.Setting("就绪六面颜色", "方块可破坏时的半透明填充颜色", this.readySideColor.getPacked()));
      this.addSetting(new Hack.Setting("就绪线条颜色", "方块可破坏时的线条颜色", this.readyLineColor.getPacked()));
      this.addSetting(new Hack.Setting("普通六面颜色", "挖掘中六面颜色", this.sideColor.getPacked()));
      this.addSetting(new Hack.Setting("普通线条颜色", "挖掘中线条颜色", this.lineColor.getPacked()));
      this.loadConfig();
   }

   private void loadConfig() {
      this.delay = (int)this.config.getDoubleSetting("数据包挖掘", "延迟(ticks)", 1.0);
      this.rotate = this.config.getBooleanSetting("数据包挖掘", "旋转", true);
      this.autoSwitch = this.config.getBooleanSetting("数据包挖掘", "自动切换工具", false);
      this.notOnUse = this.config.getBooleanSetting("数据包挖掘", "物品使用时切换", true);
      this.render = this.config.getBooleanSetting("数据包挖掘", "渲染", true);
      this.shapeMode = this.config.getStringSetting("数据包挖掘", "形状模式", "BOTH");
      this.readySideColor = new SettingColor(this.config.getIntSetting("数据包挖掘", "就绪六面颜色", this.readySideColor.getPacked()));
      this.readyLineColor = new SettingColor(this.config.getIntSetting("数据包挖掘", "就绪线条颜色", this.readyLineColor.getPacked()));
      this.sideColor = new SettingColor(this.config.getIntSetting("数据包挖掘", "普通六面颜色", this.sideColor.getPacked()));
      this.lineColor = new SettingColor(this.config.getIntSetting("数据包挖掘", "普通线条颜色", this.lineColor.getPacked()));
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "延迟(ticks)":
               s.setValue((double)this.delay);
               break;
            case "旋转":
               s.setValue(this.rotate);
               break;
            case "自动切换工具":
               s.setValue(this.autoSwitch);
               break;
            case "物品使用时切换":
               s.setValue(this.notOnUse);
               break;
            case "渲染":
               s.setValue(this.render);
               break;
            case "形状模式":
               s.setValue(this.shapeMode);
               break;
            case "就绪六面颜色":
               s.setValue(this.readySideColor.getPacked());
               break;
            case "就绪线条颜色":
               s.setValue(this.readyLineColor.getPacked());
               break;
            case "普通六面颜色":
               s.setValue(this.sideColor.getPacked());
               break;
            case "普通线条颜色":
               s.setValue(this.lineColor.getPacked());
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("数据包挖掘", this.getSettings());
   }

   public void onEnable() {
      EventManager.add(StartBreakingBlockListener.class, this);
      EventManager.add(RenderListener.class, this);
      this.swapped = false;
      this.shouldUpdateSlot = false;
   }

   public void onDisable() {
      EventManager.remove(StartBreakingBlockListener.class, this);
      EventManager.remove(RenderListener.class, this);
      Iterator var1 = this.blocks.iterator();

      while(var1.hasNext()) {
         MyBlock block = (MyBlock)var1.next();
         block.clear();
      }

      this.blocks.clear();
      if (this.shouldUpdateSlot) {
         mc.f_91074_.f_108617_.m_104955_(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
         this.shouldUpdateSlot = false;
      }

      HeadOnlyLook.stopLooking();
   }

   public void onStartBreakingBlock(StartBreakingBlockEvent event) {
      if (this.canBreak(event.blockPos)) {
         event.cancel();
         this.swapped = false;
         if (!this.isMiningBlock(event.blockPos)) {
            this.blocks.add(new MyBlock(event.blockPos, event.direction));
         }

      }
   }

   private boolean canBreak(BlockPos pos) {
      return !mc.f_91073_.m_8055_(pos).m_60795_();
   }

   private boolean isMiningBlock(BlockPos pos) {
      Iterator var2 = this.blocks.iterator();

      MyBlock block;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         block = (MyBlock)var2.next();
      } while(!block.blockPos.equals(pos));

      return true;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            switch (s.getName()) {
               case "延迟(ticks)":
                  int d = (int)s.getDouble();
                  if (d != this.delay) {
                     this.delay = d;
                     needSave = true;
                  }
                  break;
               case "旋转":
                  boolean r = s.getBoolean();
                  if (r != this.rotate) {
                     this.rotate = r;
                     needSave = true;
                  }
                  break;
               case "自动切换工具":
                  boolean a = s.getBoolean();
                  if (a != this.autoSwitch) {
                     this.autoSwitch = a;
                     needSave = true;
                  }
                  break;
               case "物品使用时切换":
                  boolean nu = s.getBoolean();
                  if (nu != this.notOnUse) {
                     this.notOnUse = nu;
                     needSave = true;
                  }
                  break;
               case "渲染":
                  boolean ren = s.getBoolean();
                  if (ren != this.render) {
                     this.render = ren;
                     needSave = true;
                  }
                  break;
               case "形状模式":
                  String sm = s.getString();
                  if (!sm.equals(this.shapeMode)) {
                     this.shapeMode = sm;
                     needSave = true;
                  }
                  break;
               case "就绪六面颜色":
                  int rsc = (Integer)s.getValue();
                  if (rsc != this.readySideColor.getPacked()) {
                     this.readySideColor = new SettingColor(rsc);
                     needSave = true;
                  }
                  break;
               case "就绪线条颜色":
                  int rlc = (Integer)s.getValue();
                  if (rlc != this.readyLineColor.getPacked()) {
                     this.readyLineColor = new SettingColor(rlc);
                     needSave = true;
                  }
                  break;
               case "普通六面颜色":
                  int sc = (Integer)s.getValue();
                  if (sc != this.sideColor.getPacked()) {
                     this.sideColor = new SettingColor(sc);
                     needSave = true;
                  }
                  break;
               case "普通线条颜色":
                  int lc = (Integer)s.getValue();
                  if (lc != this.lineColor.getPacked()) {
                     this.lineColor = new SettingColor(lc);
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         this.blocks.removeIf(MyBlock::shouldRemove);
         if (this.shouldUpdateSlot) {
            mc.f_91074_.f_108617_.m_104955_(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            this.shouldUpdateSlot = false;
         }

         if (!this.blocks.isEmpty()) {
            ((MyBlock)this.blocks.get(0)).mine();
         }

         if (this.autoSwitch && (!mc.f_91074_.m_6117_() || !this.notOnUse)) {
            var2 = this.blocks.iterator();

            while(var2.hasNext()) {
               MyBlock block = (MyBlock)var2.next();
               if (block.isReady()) {
                  int bestSlot = this.findBestToolSlot(block.blockState);
                  if (bestSlot != -1 && bestSlot != mc.f_91074_.m_150109_().f_35977_) {
                     mc.f_91074_.m_150109_().f_35977_ = bestSlot;
                     this.swapped = true;
                     this.shouldUpdateSlot = true;
                     break;
                  }
               }
            }
         }

      }
   }

   private int findBestToolSlot(BlockState state) {
      int bestSlot = -1;
      double bestSpeed = 1.0;

      for(int i = 0; i < 9; ++i) {
         ItemStack stack = mc.f_91074_.m_150109_().m_8020_(i);
         if (!stack.m_41619_()) {
            double speed = (double)stack.m_41691_(state);
            if (speed > bestSpeed) {
               bestSpeed = speed;
               bestSlot = i;
            }
         }
      }

      return bestSlot;
   }

   private float getBreakDelta(int slot, BlockState state) {
      ItemStack stack = mc.f_91074_.m_150109_().m_8020_(slot);
      float hardness = state.m_60800_(mc.f_91073_, (BlockPos)null);
      if (hardness == -1.0F) {
         return 0.0F;
      } else {
         float speed = stack.m_41691_(state);
         if (speed <= 0.0F) {
            return 0.0F;
         } else {
            int efficiency = EnchantmentHelper.m_44843_(Enchantments.f_44984_, stack);
            speed += (float)(efficiency * efficiency + 1);
            return speed / hardness / 30.0F;
         }
      }
   }

   public void onRender(PoseStack poseStack, float partialTick) {
      if (this.render) {
         Iterator var3 = this.blocks.iterator();

         while(var3.hasNext()) {
            MyBlock block = (MyBlock)var3.next();
            block.render(poseStack);
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   private class MyBlock {
      BlockPos blockPos;
      Direction direction;
      BlockState blockState;
      Block block;
      int timer;
      boolean mining;
      double progress;

      MyBlock(BlockPos pos, Direction dir) {
         this.blockPos = pos;
         this.direction = dir;
         this.blockState = Hack.mc.f_91073_.m_8055_(pos);
         this.block = this.blockState.m_60734_();
         this.timer = PacketMineHack.this.delay;
         this.mining = false;
         this.progress = 0.0;
      }

      boolean shouldRemove() {
         boolean remove = Hack.mc.f_91073_.m_8055_(this.blockPos).m_60734_() != this.block || Hack.mc.f_91074_.m_20275_((double)this.blockPos.m_123341_() + 0.5, (double)this.blockPos.m_123342_() + 0.5, (double)this.blockPos.m_123343_() + 0.5) > (double)(Hack.mc.f_91072_.m_105286_() * Hack.mc.f_91072_.m_105286_());
         if (remove) {
            Hack.mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, this.blockPos, this.direction));
            Hack.mc.f_91074_.f_108617_.m_104955_(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
         }

         return remove;
      }

      boolean isReady() {
         return this.progress >= 1.0;
      }

      void mine() {
         if (PacketMineHack.this.rotate) {
            HeadOnlyLook.startLookingAt(this.blockPos);
         }

         this.sendMinePackets();
         int bestSlot = PacketMineHack.this.findBestToolSlot(this.blockState);
         if (bestSlot == -1) {
            bestSlot = Hack.mc.f_91074_.m_150109_().f_35977_;
         }

         this.progress += (double)PacketMineHack.this.getBreakDelta(bestSlot, this.blockState);
      }

      void sendMinePackets() {
         if (this.timer <= 0) {
            if (!this.mining) {
               Hack.mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
               Hack.mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
               this.mining = true;
            }
         } else {
            --this.timer;
         }

      }

      void render(PoseStack poseStack) {
         VoxelShape shape = Hack.mc.f_91073_.m_8055_(this.blockPos).m_60808_(Hack.mc.f_91073_, this.blockPos);
         double x1 = (double)this.blockPos.m_123341_();
         double y1 = (double)this.blockPos.m_123342_();
         double z1 = (double)this.blockPos.m_123343_();
         double x2 = (double)(this.blockPos.m_123341_() + 1);
         double y2 = (double)(this.blockPos.m_123342_() + 1);
         double z2 = (double)(this.blockPos.m_123343_() + 1);
         if (!shape.m_83281_()) {
            x1 += shape.m_83288_(Axis.X);
            y1 += shape.m_83288_(Axis.Y);
            z1 += shape.m_83288_(Axis.Z);
            x2 = (double)this.blockPos.m_123341_() + shape.m_83297_(Axis.X);
            y2 = (double)this.blockPos.m_123342_() + shape.m_83297_(Axis.Y);
            z2 = (double)this.blockPos.m_123343_() + shape.m_83297_(Axis.Z);
         }

         AABB box = new AABB(x1, y1, z1, x2, y2, z2);
         if (this.isReady()) {
            this.renderBox(poseStack, box, PacketMineHack.this.readySideColor, PacketMineHack.this.readyLineColor);
         } else {
            this.renderBox(poseStack, box, PacketMineHack.this.sideColor, PacketMineHack.this.lineColor);
         }

      }

      private void renderBox(PoseStack poseStack, AABB box, SettingColor fillColor, SettingColor lineColor) {
         switch (PacketMineHack.this.shapeMode) {
            case "BOX":
               RenderUtils.drawOutlinedBoxes(poseStack, List.of(box), lineColor.getPacked(), false);
               break;
            case "LINES":
               RenderUtils.drawSolidBoxes(poseStack, List.of(box), fillColor.getPacked(), false);
               break;
            default:
               RenderUtils.drawSolidBoxes(poseStack, List.of(box), fillColor.getPacked(), false);
               RenderUtils.drawOutlinedBoxes(poseStack, List.of(box), lineColor.getPacked(), false);
         }

      }

      void clear() {
         Hack.mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, this.blockPos, this.direction));
      }
   }
}
