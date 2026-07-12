package lexis.Hack.Hacks.Blocks;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InstantRebreakHack extends Hack {
   private static final Minecraft mc = Minecraft.m_91087_();
   private HackConfig config;
   private static final String CONFIG_KEY = "瞬间重新破坏";
   private boolean enabled = false;
   private int delay = 0;
   private boolean onlyPick = false;
   private boolean rotate = true;
   private int ticks = 0;
   private int airTicks = 0;
   private static final int MAX_AIR_TICKS = 20;
   private BlockPos currentBlockPos = null;
   private Direction currentDirection;
   private boolean render;
   private SettingColor sideColor;
   private SettingColor lineColor;

   public InstantRebreakHack() {
      super("瞬间重新破坏", "立即在同一位置重新破坏方块", Hack.Category.BLOCKS, true);
      this.currentDirection = Direction.UP;
      this.render = true;
      this.sideColor = new SettingColor(204, 0, 0, 10);
      this.lineColor = new SettingColor(204, 0, 0, 255);
      this.addSetting(new Hack.Setting("延迟", "破坏尝试之间的延迟(tick)", 0.0, 0.0, 20.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("仅镐子", "只有手持镐子时才尝试挖掘", false));
      this.addSetting(new Hack.Setting("看向方块", "面向正在挖掘的方块", true));
      this.addSetting(new Hack.Setting("渲染", "在正在破坏的方块上渲染覆盖层", true));
      this.addSetting(new Hack.Setting("侧面颜色", "方块侧面颜色", this.sideColor.getPacked()));
      this.addSetting(new Hack.Setting("线条颜色", "方块线条颜色", this.lineColor.getPacked()));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.delay = (int)this.config.getDoubleSetting("瞬间重新破坏", "延迟", 0.0);
      this.onlyPick = this.config.getBooleanSetting("瞬间重新破坏", "仅镐子", false);
      this.rotate = this.config.getBooleanSetting("瞬间重新破坏", "看向方块", true);
      this.render = this.config.getBooleanSetting("瞬间重新破坏", "渲染", true);
      int sideColorPacked = this.config.getIntSetting("瞬间重新破坏", "侧面颜色", (new SettingColor(204, 0, 0, 10)).getPacked());
      int lineColorPacked = this.config.getIntSetting("瞬间重新破坏", "线条颜色", (new SettingColor(204, 0, 0, 255)).getPacked());
      this.sideColor = new SettingColor(sideColorPacked);
      this.lineColor = new SettingColor(lineColorPacked);
      Iterator var3 = this.getSettings().iterator();

      while(var3.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var3.next();
         switch (setting.getName()) {
            case "延迟":
               setting.setValue((double)this.delay);
               break;
            case "仅镐子":
               setting.setValue(this.onlyPick);
               break;
            case "看向方块":
               setting.setValue(this.rotate);
               break;
            case "渲染":
               setting.setValue(this.render);
               break;
            case "侧面颜色":
               setting.setValue(this.sideColor.getPacked());
               break;
            case "线条颜色":
               setting.setValue(this.lineColor.getPacked());
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("瞬间重新破坏", this.getSettings());
   }

   public void onEnable() {
      this.enabled = true;
      this.ticks = 0;
      this.currentBlockPos = null;
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onDisable() {
      this.enabled = false;
      this.currentBlockPos = null;
      MinecraftForge.EVENT_BUS.unregister(this);
      if (this.rotate) {
         HeadOnlyLook.stopLooking();
      }

   }

   @SubscribeEvent
   public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
      if (this.enabled) {
         if (event.getEntity() == mc.f_91074_) {
            this.currentDirection = event.getFace();
            this.currentBlockPos = event.getPos();
            this.airTicks = 0;
         }
      }
   }

   @SubscribeEvent
   public void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END && this.enabled) {
         if (mc.f_91074_ != null && mc.m_91403_() != null) {
            if (this.currentBlockPos != null) {
               double distance = mc.f_91074_.m_20238_(Vec3.m_82512_(this.currentBlockPos));
               if (distance > 36.0) {
                  if (HeadOnlyLook.isLooking()) {
                     HeadOnlyLook.stopLooking();
                  }

                  return;
               }
            }

            if (this.currentBlockPos != null) {
               BlockState state = mc.f_91073_.m_8055_(this.currentBlockPos);
               if (state.m_60795_()) {
                  ++this.airTicks;
                  if (this.airTicks > 20 && HeadOnlyLook.isLooking()) {
                     HeadOnlyLook.stopLooking();
                  }
               } else {
                  this.airTicks = 0;
                  if (this.rotate) {
                     this.lookAtBlock(this.currentBlockPos);
                  } else if (HeadOnlyLook.isLooking()) {
                     HeadOnlyLook.stopLooking();
                  }
               }
            }

            if (this.currentBlockPos != null) {
               if (this.ticks >= this.delay) {
                  this.ticks = 0;
                  if (this.shouldMine()) {
                     this.sendPacket();
                     mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
                     mc.m_91403_().m_104955_(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                  }
               } else {
                  ++this.ticks;
               }

            }
         }
      }
   }

   private void stopLookingAndReset() {
      if (HeadOnlyLook.isLooking()) {
         HeadOnlyLook.stopLooking();
      }

      this.currentBlockPos = null;
      this.airTicks = 0;
   }

   private float normalizeAngle(float angle) {
      angle %= 360.0F;
      if (angle > 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   private void sendPacket() {
      if (this.currentBlockPos != null && mc.m_91403_() != null) {
         mc.m_91403_().m_104955_(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, this.currentBlockPos, this.currentDirection));
      }
   }

   private boolean shouldMine() {
      if (this.currentBlockPos == null) {
         return false;
      } else if (mc.f_91073_.m_151570_(this.currentBlockPos)) {
         return false;
      } else {
         BlockState state = mc.f_91073_.m_8055_(this.currentBlockPos);
         if (state.m_60795_()) {
            return false;
         } else {
            return !this.onlyPick ? true : mc.f_91074_.m_21205_().m_204117_(ItemTags.f_271360_);
         }
      }
   }

   public BlockPos getCurrentBlockPos() {
      return this.currentBlockPos;
   }

   public SettingColor getSideColor() {
      return this.sideColor;
   }

   public SettingColor getLineColor() {
      return this.lineColor;
   }

   public boolean shouldRender() {
      return this.enabled && this.render && this.currentBlockPos != null && this.shouldMine();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "延迟":
               int newDelay = (int)setting.getDouble();
               if (newDelay != this.delay) {
                  this.delay = newDelay;
                  needSave = true;
               }
               break;
            case "仅镐子":
               boolean newPick = setting.getBoolean();
               if (newPick != this.onlyPick) {
                  this.onlyPick = newPick;
                  needSave = true;
               }
               break;
            case "看向方块":
               boolean newRotate = setting.getBoolean();
               if (newRotate != this.rotate) {
                  this.rotate = newRotate;
                  if (!this.rotate && HeadOnlyLook.isLooking()) {
                     HeadOnlyLook.stopLooking();
                  }

                  needSave = true;
               }
               break;
            case "渲染":
               boolean newRender = setting.getBoolean();
               if (newRender != this.render) {
                  this.render = newRender;
                  needSave = true;
               }
               break;
            case "侧面颜色":
               int newSide = (Integer)setting.getValue();
               if (newSide != this.sideColor.getPacked()) {
                  this.sideColor = new SettingColor(newSide);
                  needSave = true;
               }
               break;
            case "线条颜色":
               int newLine = (Integer)setting.getValue();
               if (newLine != this.lineColor.getPacked()) {
                  this.lineColor = new SettingColor(newLine);
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

   }

   private void lookAtBlock(BlockPos pos) {
      if (this.rotate && pos != null && mc.f_91074_ != null) {
         Vec3 eyePos = mc.f_91074_.m_146892_();
         Vec3 blockCenter = Vec3.m_82512_(pos);
         Vec3 lookVec = blockCenter.m_82546_(eyePos);
         double distanceXZ = Math.sqrt(lookVec.f_82479_ * lookVec.f_82479_ + lookVec.f_82481_ * lookVec.f_82481_);
         float yaw = (float)Math.toDegrees(Math.atan2(lookVec.f_82481_, lookVec.f_82479_)) - 90.0F;
         float pitch = (float)(-Math.toDegrees(Math.atan2(lookVec.f_82480_, distanceXZ)));
         this.normalizeAngle(yaw);
         pitch = Math.max(-90.0F, Math.min(90.0F, pitch));
         HeadOnlyLook.startLookingAt(pos);
      }
   }

   public void onClick() {
      this.toggle();
   }
}
