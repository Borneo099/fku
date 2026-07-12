package lexis.Hack.Hacks.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoFixGroundHack extends Hack {
   private double range = 5.0;
   private int speed = 10;
   private int tickCounter = 0;
   private HackConfig config;
   private BlockPos currentTarget = null;
   private boolean isBreaking = false;
   private long breakStartTime = 0L;
   private boolean superFastMode = false;
   private FixStage currentStage;
   private List blocksToFix;
   private int currentBlockIndex;
   private long lastRepairTime;
   private static final long REPAIR_COOLDOWN = 100L;

   public AutoFixGroundHack() {
      super("自动修地", new String[]{"自动修复超平坦地上", "§c§l警告：如果服务器有发包限制 会被踢！"}, Hack.Category.BLOCKS, true);
      this.currentStage = AutoFixGroundHack.FixStage.BEDROCK;
      this.blocksToFix = new ArrayList();
      this.currentBlockIndex = 0;
      this.lastRepairTime = 0L;
      this.addSetting(new Hack.Setting("修复距离", "检测距离", 5.0, 1.0, 64.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("修复速度", "每tick放置数量", 10.0, 1.0, 512.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("超速模式", "放置更快，五倍速度 要修改\"修复速度\"", false));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.range = this.config.getDoubleSetting("自动修地", "修复距离", 5.0);
      this.speed = (int)this.config.getDoubleSetting("自动修地", "修复速度", 10.0);
      this.superFastMode = this.config.getBooleanSetting("自动修地", "超速模式", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "修复距离":
               setting.setValue(this.range);
               break;
            case "修复速度":
               setting.setValue((double)this.speed);
               break;
            case "超速模式":
               setting.setValue(this.superFastMode);
         }
      }

   }

   public void onEnable() {
      this.currentTarget = null;
      this.currentStage = AutoFixGroundHack.FixStage.BEDROCK;
      this.blocksToFix.clear();
      this.currentBlockIndex = 0;
      this.isBreaking = false;
   }

   public void onDisable() {
      this.currentTarget = null;
      this.isBreaking = false;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "修复距离":
                  this.range = setting.getDouble();
                  break;
               case "修复速度":
                  this.speed = (int)setting.getDouble();
                  break;
               case "超速模式":
                  this.superFastMode = setting.getBoolean();
            }
         }

         BlockPos targetPos;
         BlockPos targetPos;
         if (this.superFastMode) {
            for(int placed = 0; placed < this.speed && this.currentBlockIndex < this.blocksToFix.size(); ++this.currentBlockIndex) {
               targetPos = (BlockPos)this.blocksToFix.get(this.currentBlockIndex);
               if (!this.repairBlock(targetPos)) {
                  break;
               }

               ++placed;
            }
         } else {
            ++this.tickCounter;
            if (this.tickCounter < 20 / this.speed) {
               return;
            }

            this.tickCounter = 0;
            if (this.currentBlockIndex < this.blocksToFix.size()) {
               targetPos = (BlockPos)this.blocksToFix.get(this.currentBlockIndex);
               if (this.repairBlock(targetPos)) {
                  ++this.currentBlockIndex;
               }
            }
         }

         if (!mc.f_91074_.m_150110_().f_35937_) {
            NotificationManager.error("自动修地", "你需要是创造模式！");
            this.setEnabled(false);
         } else {
            targetPos = mc.f_91074_.m_20183_().m_7495_();
            this.updateBlocksToFix(targetPos);
            if (this.blocksToFix.isEmpty()) {
               this.advanceStage();
               this.currentTarget = null;
            } else {
               if (this.currentBlockIndex >= this.blocksToFix.size()) {
                  this.currentBlockIndex = 0;
               }

               targetPos = (BlockPos)this.blocksToFix.get(this.currentBlockIndex);
               if (this.currentTarget == null || !this.currentTarget.equals(targetPos)) {
                  this.currentTarget = targetPos;
                  HeadOnlyLook.startLookingAt(this.currentTarget);
               }

               long currentTime = System.currentTimeMillis();
               if (currentTime - this.lastRepairTime >= 100L / (long)this.speed) {
                  if (this.repairBlock(targetPos)) {
                     this.lastRepairTime = currentTime;
                     ++this.currentBlockIndex;
                  }

               }
            }
         }
      }
   }

   private void updateBlocksToFix(BlockPos center) {
      this.blocksToFix.clear();
      int rangeInt = (int)this.range;

      for(int x = -rangeInt; x <= rangeInt; ++x) {
         for(int z = -rangeInt; z <= rangeInt; ++z) {
            for(int y = -64; y <= -61; ++y) {
               BlockPos pos = new BlockPos(center.m_123341_() + x, y, center.m_123343_() + z);
               if (this.needsFix(pos)) {
                  this.blocksToFix.add(pos.m_7949_());
               }
            }
         }
      }

      this.blocksToFix.sort(Comparator.comparingDouble((p) -> {
         return p.m_123331_(mc.f_91074_.m_20183_());
      }));
   }

   private boolean needsFix(BlockPos pos) {
      BlockState state = mc.f_91073_.m_8055_(pos);
      switch (pos.m_123342_()) {
         case -64:
            return state.m_60734_() != Blocks.f_50752_;
         case -63:
         case -62:
            return state.m_60734_() != Blocks.f_50493_;
         case -61:
            return state.m_60734_() != Blocks.f_50440_;
         default:
            return false;
      }
   }

   private void advanceStage() {
      switch (this.currentStage) {
         case BEDROCK:
            this.currentStage = AutoFixGroundHack.FixStage.DIRT;
            break;
         case DIRT:
            this.currentStage = AutoFixGroundHack.FixStage.GRASS;
            break;
         case GRASS:
            this.currentStage = AutoFixGroundHack.FixStage.BEDROCK;
      }

      this.currentBlockIndex = 0;
   }

   private ItemStack getRequiredBlock(int y) {
      switch (y) {
         case -64:
            return new ItemStack(Items.f_41829_, 64);
         case -63:
         case -62:
            return new ItemStack(Items.f_42329_, 64);
         case -61:
            return new ItemStack(Items.f_42276_, 64);
         default:
            return ItemStack.f_41583_;
      }
   }

   private boolean repairBlock(BlockPos pos) {
      BlockState currentState = mc.f_91073_.m_8055_(pos);
      ItemStack required = this.getRequiredBlock(pos.m_123342_());
      if (required.m_41619_()) {
         return false;
      } else if (!currentState.m_60795_() && this.needsFix(pos)) {
         this.destroyBlock(pos);
         return false;
      } else {
         this.placeBlock(pos, required);
         return true;
      }
   }

   private void destroyBlock(BlockPos pos) {
      if (mc.f_91072_ != null && mc.f_91074_ != null) {
         mc.m_91403_().m_104955_(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, pos, Direction.UP));
         mc.m_91403_().m_104955_(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
      }
   }

   private void placeBlock(BlockPos pos, ItemStack stack) {
      if (mc.f_91072_ != null && mc.f_91074_ != null) {
         int currentSlot = mc.f_91074_.m_150109_().f_35977_;
         mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(currentSlot + 36, stack.m_41777_()));
         mc.f_91074_.m_150109_().m_6836_(currentSlot, stack.m_41777_());
         mc.f_91072_.m_233732_(mc.f_91074_, mc.f_91074_.m_7655_(), new BlockHitResult(Vec3.m_82512_(pos), Direction.UP, pos, false));
      }
   }

   public List getBlocksToFix() {
      return this.blocksToFix;
   }

   public int getCurrentBlockIndex() {
      return this.currentBlockIndex;
   }

   public void onClick() {
      this.toggle();
   }

   private static enum FixStage {
      BEDROCK,
      DIRT,
      GRASS;

      // $FF: synthetic method
      private static FixStage[] $values() {
         return new FixStage[]{BEDROCK, DIRT, GRASS};
      }
   }
}
