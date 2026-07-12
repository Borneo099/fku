package lexis.Hack.Hacks.Render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;

public class NametagsHack extends Hack {
   private static boolean active = false;
   private boolean showHp = true;
   private boolean showDist = true;
   private boolean showItems = true;
   private boolean show3D = true;
   private boolean showEnchants = true;
   private boolean showGameMode = true;
   private double maxDistance = 64.0;
   private static final String CONFIG_KEY = "增强名牌";
   private final HackConfig config;
   private float entityRotation = 0.0F;

   public static boolean isActive() {
      return active;
   }

   public NametagsHack() {
      super("增强名牌", new String[]{"显示玩家血量/距离/装备/3D模型"}, Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("显示血量", "显示生命值", true));
      this.addSetting(new Hack.Setting("显示距离", "显示距离", true));
      this.addSetting(new Hack.Setting("显示装备", "显示装备+手持物品图标", true));
      this.addSetting(new Hack.Setting("显示3D模型", "左侧渲染旋转的3D玩家模型", true));
      this.addSetting(new Hack.Setting("显示附魔", "显示装备附魔ID和等级", true));
      this.addSetting(new Hack.Setting("显示模式", "显示游戏模式", true));
      this.addSetting(new Hack.Setting("最大距离", "显示范围", 64, 1, 256, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.showHp = this.config.getBooleanSetting("增强名牌", "显示血量", this.showHp);
      this.showDist = this.config.getBooleanSetting("增强名牌", "显示距离", this.showDist);
      this.showItems = this.config.getBooleanSetting("增强名牌", "显示装备", this.showItems);
      this.show3D = this.config.getBooleanSetting("增强名牌", "显示3D模型", this.show3D);
      this.showEnchants = this.config.getBooleanSetting("增强名牌", "显示附魔", this.showEnchants);
      this.showGameMode = this.config.getBooleanSetting("增强名牌", "显示模式", this.showGameMode);
      this.maxDistance = (double)this.config.getIntSetting("增强名牌", "最大距离", (int)this.maxDistance);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "显示血量":
               s.setValue(this.showHp);
               break;
            case "显示距离":
               s.setValue(this.showDist);
               break;
            case "显示装备":
               s.setValue(this.showItems);
               break;
            case "显示3D模型":
               s.setValue(this.show3D);
               break;
            case "显示附魔":
               s.setValue(this.showEnchants);
               break;
            case "显示模式":
               s.setValue(this.showGameMode);
               break;
            case "最大距离":
               s.setValue((int)this.maxDistance);
         }
      }

   }

   public void onEnable() {
      active = true;
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onDisable() {
      active = false;
      MinecraftForge.EVENT_BUS.unregister(this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         boolean v;
         switch (s.getName()) {
            case "显示血量":
               v = s.getBoolean();
               if (v != this.showHp) {
                  this.showHp = v;
                  needSave = true;
               }
               break;
            case "显示距离":
               v = s.getBoolean();
               if (v != this.showDist) {
                  this.showDist = v;
                  needSave = true;
               }
               break;
            case "显示装备":
               v = s.getBoolean();
               if (v != this.showItems) {
                  this.showItems = v;
                  needSave = true;
               }
               break;
            case "显示3D模型":
               v = s.getBoolean();
               if (v != this.show3D) {
                  this.show3D = v;
                  needSave = true;
               }
               break;
            case "显示附魔":
               v = s.getBoolean();
               if (v != this.showEnchants) {
                  this.showEnchants = v;
                  needSave = true;
               }
               break;
            case "显示模式":
               v = s.getBoolean();
               if (v != this.showGameMode) {
                  this.showGameMode = v;
                  needSave = true;
               }
               break;
            case "最大距离":
               double v = s.getDouble();
               if (v != this.maxDistance) {
                  this.maxDistance = v;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("增强名牌", this.getSettings());
      }

   }

   @SubscribeEvent
   public void onRenderGui(RenderGuiEvent.Post event) {
      if (this.isEnabled()) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null && mc.f_91073_ != null) {
            float pt = event.getPartialTick();
            this.entityRotation += pt * 2.6F;
            if (this.entityRotation > 360.0F) {
               this.entityRotation -= 360.0F;
            }

            GuiGraphics gfx = event.getGuiGraphics();
            Camera cam = mc.f_91063_.m_109153_();
            Vec3 camPos = cam.m_90583_();
            int sw = mc.m_91268_().m_85445_();
            int sh = mc.m_91268_().m_85446_();
            float camYaw = cam.m_90590_();
            float camPitch = cam.m_90589_();
            float cosYaw = Mth.m_14089_(-camYaw * 0.017453292F - 3.1415927F);
            float sinYaw = Mth.m_14031_(-camYaw * 0.017453292F - 3.1415927F);
            float cosPitch = -Mth.m_14089_(-camPitch * 0.017453292F);
            float sinPitch = Mth.m_14031_(-camPitch * 0.017453292F);
            float fx = sinYaw * cosPitch;
            float fy = sinPitch;
            float fz = cosYaw * cosPitch;
            float rx = cosYaw;
            float ry = 0.0F;
            float rz = -sinYaw;
            float ux = ry * fz - rz * sinPitch;
            float uy = rz * fx - cosYaw * fz;
            float uz = cosYaw * sinPitch - ry * fx;
            double fovTan = Math.tan(Math.toRadians((double)(Integer)mc.f_91066_.m_231837_().m_231551_() / 2.0));
            double fovScale = (double)sh / 2.0 / fovTan;
            Font font = mc.f_91062_;
            Iterator var29 = mc.f_91073_.m_6907_().iterator();

            while(var29.hasNext()) {
               Player player = (Player)var29.next();
               if (player != mc.f_91074_ && !player.m_5833_() && !player.m_20145_() && !((double)player.m_20270_(mc.f_91074_) > this.maxDistance)) {
                  Vec3 pos = player.m_20318_(pt).m_82520_(0.0, (double)player.m_20206_() + 0.5, 0.0);
                  Vec3 rel = pos.m_82546_(camPos);
                  float rlx = (float)rel.f_82479_;
                  float rly = (float)rel.f_82480_;
                  float rlz = (float)rel.f_82481_;
                  double dotFwd = (double)(rlx * fx + rly * fy + rlz * fz);
                  if (!(dotFwd < 0.2)) {
                     double dotRight = (double)(rlx * rx + rly * ry + rlz * rz);
                     double dotUpf = (double)(rlx * ux + rly * uy + rlz * uz);
                     double scale = fovScale / dotFwd;
                     float sx = (float)((double)sw / 2.0 + dotRight * scale);
                     float sy = (float)((double)sh / 2.0 - dotUpf * scale);
                     String name = player.m_36316_().getName();
                     int nameW = font.m_92895_(name);
                     float nameX = sx - (float)nameW / 2.0F;
                     if (this.show3D) {
                        nameX += 28.0F;
                     }

                     gfx.m_280056_(font, name, (int)nameX + 1, (int)sy + 1, Integer.MIN_VALUE, false);
                     gfx.m_280056_(font, name, (int)nameX, (int)sy, -1, false);
                     Objects.requireNonNull(font);
                     float lineY = sy + 9.0F + 3.0F;
                     float pct;
                     int w;
                     if (this.showHp) {
                        float hp = player.m_21223_();
                        float maxHp = player.m_21233_();
                        pct = Mth.m_14036_(hp / maxHp, 0.0F, 1.0F);
                        if (pct > 0.5F) {
                           w = -16711936;
                        } else if (pct > 0.25F) {
                           w = -256;
                        } else {
                           w = -65536;
                        }

                        String hpText = String.format("%.0f HP", hp);
                        int w = font.m_92895_(hpText);
                        float hx = nameX + (float)nameW / 2.0F - (float)w / 2.0F;
                        gfx.m_280056_(font, hpText, (int)hx + 1, (int)lineY + 1, Integer.MIN_VALUE, false);
                        gfx.m_280056_(font, hpText, (int)hx, (int)lineY, w, false);
                        Objects.requireNonNull(font);
                        lineY += (float)(9 + 2);
                     }

                     if (this.showDist) {
                        String dText = String.format("%.1fm", player.m_20270_(mc.f_91074_));
                        int w = font.m_92895_(dText);
                        pct = nameX + (float)nameW / 2.0F - (float)w / 2.0F;
                        gfx.m_280056_(font, dText, (int)pct + 1, (int)lineY + 1, Integer.MIN_VALUE, false);
                        gfx.m_280056_(font, dText, (int)pct, (int)lineY, -5592406, false);
                        Objects.requireNonNull(font);
                        lineY += (float)(9 + 2);
                     }

                     if (this.showGameMode) {
                        PlayerInfo info = mc.m_91403_().m_104949_(player.m_20148_());
                        if (info != null) {
                           GameType gm = info.m_105325_();
                           String modeText = gm.m_151500_().getString();
                           w = font.m_92895_(modeText);
                           float mx = nameX + (float)nameW / 2.0F - (float)w / 2.0F;
                           gfx.m_280056_(font, modeText, (int)mx + 1, (int)lineY + 1, Integer.MIN_VALUE, false);
                           gfx.m_280056_(font, modeText, (int)mx, (int)lineY, -11141121, false);
                           Objects.requireNonNull(font);
                           lineY += (float)(9 + 2);
                        }
                     }

                     if (this.showItems) {
                        this.renderPlayerItemsWithEnchants(gfx, player, sx, lineY);
                     } else if (this.showEnchants) {
                        this.renderPlayerEnchants(gfx, player, sx, lineY);
                     }

                     if (this.show3D) {
                        this.renderMiniPlayer(gfx, player, sx - 52.0F, sy - 18.0F, 28);
                     }
                  }
               }
            }

         }
      }
   }

   private float renderPlayerItemsWithEnchants(GuiGraphics gfx, Player player, float centerX, float y) {
      Minecraft mc = Minecraft.m_91087_();
      EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
      int iconSize = 14;
      int gap = 2;
      int totalW = slots.length * (iconSize + gap) - gap;
      float startX = centerX - (float)totalW / 2.0F;
      List slotEnchants = new ArrayList();
      int maxEnchLines = 0;

      int i;
      for(i = 0; i < slots.length; ++i) {
         List list = new ArrayList();
         ItemStack stack = player.m_6844_(slots[i]);
         if (!stack.m_41619_()) {
            Map enchants = stack.getAllEnchantments();
            Iterator var17 = enchants.entrySet().iterator();

            while(var17.hasNext()) {
               Map.Entry entry = (Map.Entry)var17.next();
               Enchantment ench = (Enchantment)entry.getKey();
               int level = (Integer)entry.getValue();
               String enchId = this.shortEnchName(ForgeRegistries.ENCHANTMENTS.getKey(ench).m_135815_());
               list.add(enchId + "." + level);
            }
         }

         slotEnchants.add(list);
         if (list.size() > maxEnchLines) {
            maxEnchLines = list.size();
         }
      }

      for(i = 0; i < slots.length; ++i) {
         ItemStack stack = player.m_6844_(slots[i]);
         float ix = startX + (float)(i * (iconSize + gap));
         if (!stack.m_41619_()) {
            gfx.m_280480_(stack, (int)ix, (int)y);
            gfx.m_280370_(mc.f_91062_, stack, (int)ix, (int)y);
         } else {
            gfx.m_280509_((int)ix, (int)y, (int)ix + iconSize, (int)y + iconSize, 1073741824);
         }
      }

      float enchY = y + (float)iconSize + 1.0F;
      if (this.showEnchants && maxEnchLines > 0) {
         for(int line = 0; line < maxEnchLines; ++line) {
            for(int i = 0; i < slots.length; ++i) {
               List list = (List)slotEnchants.get(i);
               if (line < list.size()) {
                  float ix = startX + (float)(i * (iconSize + gap));
                  gfx.m_280056_(mc.f_91062_, (String)list.get(line), (int)ix + 1, (int)enchY + 1, Integer.MIN_VALUE, false);
                  gfx.m_280056_(mc.f_91062_, (String)list.get(line), (int)ix, (int)enchY, -4474112, false);
               }
            }

            Objects.requireNonNull(mc.f_91062_);
            enchY += (float)(9 + 1);
         }

         return enchY + 2.0F;
      } else {
         return y + (float)iconSize + 4.0F;
      }
   }

   private String shortEnchName(String path) {
      String[] parts = path.split("_");
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < parts.length; ++i) {
         String p = parts[i];
         if (p.length() <= 3) {
            sb.append(p);
         } else {
            sb.append(p, 0, 3);
         }
      }

      return sb.toString();
   }

   private float renderPlayerEnchants(GuiGraphics gfx, Player player, float centerX, float y) {
      Minecraft mc = Minecraft.m_91087_();
      EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

      for(int i = 0; i < slots.length; ++i) {
         ItemStack stack = player.m_6844_(slots[i]);
         if (!stack.m_41619_()) {
            Map enchants = stack.getAllEnchantments();
            if (!enchants.isEmpty()) {
               StringBuilder sb = new StringBuilder();

               int level;
               String enchId;
               for(Iterator var11 = enchants.entrySet().iterator(); var11.hasNext(); sb.append(enchId).append(".").append(level)) {
                  Map.Entry entry = (Map.Entry)var11.next();
                  Enchantment ench = (Enchantment)entry.getKey();
                  level = (Integer)entry.getValue();
                  enchId = this.shortEnchName(ForgeRegistries.ENCHANTMENTS.getKey(ench).m_135815_());
                  if (!sb.isEmpty()) {
                     sb.append(' ');
                  }
               }

               String line = sb.toString();
               float tx = centerX - (float)mc.f_91062_.m_92895_(line) / 2.0F;
               gfx.m_280056_(mc.f_91062_, line, (int)tx + 1, (int)y + 1, Integer.MIN_VALUE, false);
               gfx.m_280056_(mc.f_91062_, line, (int)tx, (int)y, -4474112, false);
               Objects.requireNonNull(mc.f_91062_);
               y += (float)(9 + 2);
            }
         }
      }

      return y;
   }

   private void renderMiniPlayer(GuiGraphics gfx, Player player, float x, float y, int size) {
      float savedYBodyRot = player.f_20883_;
      float savedYRot = player.m_146908_();
      float savedXRot = player.m_146909_();
      float savedYHeadRot = player.f_20885_;
      float savedYHeadRotO = player.f_20886_;
      float savedAttackAnim = player.f_20921_;
      int savedSwingTime = player.f_20913_;
      Pose savedPose = player.m_20089_();
      float savedWalkSpeed = player.f_267362_.m_267731_();
      player.f_267362_.m_267771_(0.0F);
      player.f_20921_ = 0.0F;
      player.f_20913_ = 0;
      player.m_20124_(Pose.STANDING);
      player.f_20883_ = 180.0F;
      player.m_146922_(180.0F);
      player.m_146926_(0.0F);
      player.f_20885_ = 180.0F;
      player.f_20886_ = 180.0F;
      Quaternionf pose = (new Quaternionf()).rotateZ(3.1415927F);
      Quaternionf yaw = (new Quaternionf()).rotateY((float)Math.toRadians((double)this.entityRotation));
      pose.mul(yaw);
      int cx = (int)x;
      int cy = (int)y;
      int scale = size * 2;
      InventoryScreen.m_280432_(gfx, cx, cy, scale, pose, (Quaternionf)null, player);
      player.f_20883_ = savedYBodyRot;
      player.m_146922_(savedYRot);
      player.m_146926_(savedXRot);
      player.f_20885_ = savedYHeadRot;
      player.f_20886_ = savedYHeadRotO;
      player.f_20921_ = savedAttackAnim;
      player.f_20913_ = savedSwingTime;
      player.m_20124_(savedPose);
      player.f_267362_.m_267771_(savedWalkSpeed);
   }

   public void onClick() {
      this.toggle();
   }
}
