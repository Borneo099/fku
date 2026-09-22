package fku.org.example.fku.features.dynamicisland;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimationController {
   private State state;
   private float curW;
   private float curH;
   private float velW;
   private float velH;
   private float holdTimer;
   private long lastTime;
   private float targetW;
   private float targetH;
   private int idleIndex;
   private float idleSwapTimer;
   private ProgressProvider activeProgress;

   public AnimationController(DynamicIslandConfig cfg) {
      this.state = AnimationController.State.IDLE;
      this.velW = 0.0F;
      this.velH = 0.0F;
      this.holdTimer = 0.0F;
      this.lastTime = 0L;
      this.idleIndex = 0;
      this.idleSwapTimer = 0.0F;
      this.activeProgress = null;
      this.curW = (float)cfg.capsuleWidth;
      this.targetW = this.curW;
      this.targetH = (float)cfg.capsuleHeight;
   }

   public void update(DynamicIslandConfig cfg) {
      long now = System.currentTimeMillis();
      float dt = this.lastTime == 0L ? 0.016F : (float)(now - this.lastTime) / 1000.0F;
      if (dt > 0.05F) {
         dt = 0.05F;
      }

      if (dt < 0.0F) {
         dt = 0.0F;
      }

      this.lastTime = now;
      ProgressManager.tickMusic(Minecraft.getInstance());
      if (DynamicIslandDamageHandler.isActive()) {
         this.activeProgress = null;
         this.targetW = this.computeDamageWidth(cfg, DynamicIslandDamageHandler.getDisplayName(), DynamicIslandDamageHandler.getMeasuredDamage());
         this.targetH = (float)(cfg.expandedHeight + 14);
         this.state = AnimationController.State.PROGRESS;
         this.springStep(dt, this.targetW, this.targetH, cfg);
      } else {
         IslandNotification note = NotificationCenter.activeNotification();
         if (note == null) {
            note = NotificationCenter.takeNext();
            if (note != null) {
               NotificationCenter.setActive(note);
            }
         }

         if (note != null) {
            this.activeProgress = null;
            if (note.targetEntity != null) {
               this.targetW = this.computeDamageWidth(cfg, note.displayName, note.damageAmount);
               this.targetH = (float)(cfg.expandedHeight + 14);
            } else {
               this.targetW = this.computeNotifyWidth(cfg, note);
               this.targetH = (float)cfg.expandedHeight;
            }

            if (this.state == AnimationController.State.EXPANDING) {
               this.springStep(dt, this.targetW, this.targetH, cfg);
               if (this.settled(this.targetW, this.targetH)) {
                  this.state = AnimationController.State.HOLDING;
                  this.holdTimer = 0.0F;
               }
            } else if (this.state == AnimationController.State.HOLDING) {
               this.springStep(dt, this.targetW, this.targetH, cfg);
               this.holdTimer += dt;
               if (this.holdTimer >= (float)cfg.notificationDuration / 1000.0F) {
                  NotificationCenter.setActive((IslandNotification)null);
                  this.state = AnimationController.State.IDLE;
               }
            } else {
               this.state = AnimationController.State.EXPANDING;
               this.springStep(dt, this.targetW, this.targetH, cfg);
            }

         } else if (TabListRenderer.isActive(cfg)) {
            this.activeProgress = null;
            int[] sz = TabListRenderer.measure(cfg);
            this.targetW = (float)sz[0];
            this.targetH = (float)sz[1];
            this.state = AnimationController.State.PLAYER_LIST;
            this.springStep(dt, this.targetW, this.targetH, cfg);
         } else {
            ProgressProvider p = ProgressManager.getActive(cfg);
            if (p != null) {
               this.activeProgress = p;
               this.targetW = this.computeProgressWidth(cfg, p);
               this.targetH = p.isMusicProvider() ? (float)(cfg.expandedHeight + 10) : (float)cfg.expandedHeight;
               this.state = AnimationController.State.PROGRESS;
               this.springStep(dt, this.targetW, this.targetH, cfg);
            } else {
               this.activeProgress = null;
               this.targetH = (float)cfg.capsuleHeight;
               this.targetW = this.computeIdleWidth(cfg);
               this.idleSwapTimer += dt;
               if (this.idleSwapTimer >= (float)Math.max(1, cfg.idleInfoRotateSeconds)) {
                  this.idleSwapTimer = 0.0F;
                  ++this.idleIndex;
               }

               this.state = AnimationController.State.IDLE;
               this.springStep(dt, this.targetW, this.targetH, cfg);
            }
         }
      }
   }

   private boolean settled(float tw, float th) {
      if (!(Math.abs(this.curW - tw) >= 1.0F) && !(Math.abs(this.curH - th) >= 1.0F)) {
         if (!(Math.abs(this.velW) >= 20.0F) && !(Math.abs(this.velH) >= 20.0F)) {
            this.curW = tw;
            this.curH = th;
            this.velW = 0.0F;
            this.velH = 0.0F;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void springStep(float dt, float targetW, float targetH, DynamicIslandConfig cfg) {
      if (!cfg.animationEnabled) {
         this.curW = targetW;
         this.curH = targetH;
         this.velW = 0.0F;
         this.velH = 0.0F;
      } else {
         float k = cfg.stiffness;
         float c = cfg.damping;
         this.velW += (-k * (this.curW - targetW) - c * this.velW) * dt;
         this.curW += this.velW * dt;
         this.velH += (-k * (this.curH - targetH) - c * this.velH) * dt;
         this.curH += this.velH * dt;
      }
   }

   private float computeNotifyWidth(DynamicIslandConfig cfg, IslandNotification n) {
      Font font = Minecraft.getInstance().font;
      if (font != null && n != null) {
         int iconSize = Math.min(cfg.expandedHeight - 14, 22);
         float titleScale = 1.2F;
         int textW = (int)Math.max((float)font.width(n.displayName) * titleScale, (float)font.width(n.text));
         int rightPad = cfg.showTimestamp ? 36 : 14;
         int w = 12 + iconSize + 10 + textW + rightPad;
         return (float)Math.max(cfg.capsuleWidth + 30, Math.min(w, cfg.expandedWidth));
      } else {
         return (float)cfg.expandedWidth;
      }
   }

   private float computeDamageWidth(DynamicIslandConfig cfg, String name, float damage) {
      Font font = Minecraft.getInstance().font;
      if (font == null) {
         return (float)Math.max(cfg.expandedWidth, 300);
      } else {
         String safeName = name == null ? "" : name;
         String dmgText = "-" + trim1(damage);
         int avatar = 48;
         int midW = (int)Math.max((float)font.width(safeName) * 1.1F + 10.0F, 96.0F);
         int dmgW = Math.max((int)((float)font.width(dmgText) * 1.6F) + 18, 70);
         int w = 12 + avatar + 10 + midW + 10 + dmgW + 14;
         return (float)Math.max(cfg.capsuleWidth + 40, Math.min(w, Math.max(cfg.expandedWidth, 340)));
      }
   }

   private float computeProgressWidth(DynamicIslandConfig cfg, ProgressProvider p) {
      Font font = Minecraft.getInstance().font;
      if (font != null && p != null) {
         int slot = 22;
         int textBlockW;
         int barMinW;
         if (!p.hasBar()) {
            textBlockW = (int)Math.max((float)(font.width(p.getTitle()) + 8), (float)font.width(p.getSubtitle()) * 1.15F + 10.0F);
            barMinW = 12 + slot + 10 + textBlockW + 14;
            return (float)Math.max(cfg.capsuleWidth + 30, Math.min(barMinW, cfg.expandedWidth));
         } else {
            textBlockW = Math.max(110, font.width(p.getTitle()) + 8);
            textBlockW = Math.max(textBlockW, font.width(p.getSubtitle()) + 8);
            barMinW = 110;
            int pctW = 38;
            int w = 12 + slot + 10 + Math.max(textBlockW, barMinW) + 10 + pctW + 12;
            return (float)Math.max(cfg.capsuleWidth + 40, Math.min(w, cfg.expandedWidth));
         }
      } else {
         return (float)cfg.expandedWidth;
      }
   }

   private static String trim1(float v) {
      return v == (float)((long)v) ? String.valueOf((long)v) : String.format("%.1f", v);
   }

   private float computeIdleWidth(DynamicIslandConfig cfg) {
      String info = this.currentIdleInfo(cfg);
      if (info == null) {
         return (float)cfg.capsuleWidth;
      } else {
         Font font = Minecraft.getInstance().font;
         int w = 25 + font.width(info) + 12;
         return (float)Math.max(cfg.capsuleWidth, Math.min(w, 400));
      }
   }

   public String currentIdleInfo(DynamicIslandConfig cfg) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return null;
      } else {
         List items = this.collectIdleItems(mc, cfg);
         if (items.isEmpty()) {
            return null;
         } else {
            return "ALL".equals(cfg.idleDisplayMode) ? String.join(" · ", items) : (String)items.get(Math.floorMod(this.idleIndex, items.size()));
         }
      }
   }

   private List collectIdleItems(Minecraft mc, DynamicIslandConfig cfg) {
      List list = new ArrayList();

      try {
         if (cfg.idleShowPlayer) {
            list.add("玩家 " + mc.getUser().getName());
         }

         if (mc.level != null) {
            long t = mc.level.getDayTime() % 24000L;
            if (t < 0L) {
               t += 24000L;
            }

            int hour = (int)((t / 1000L + 6L) % 24L);
            int minute = (int)(t % 1000L * 60L / 1000L);
            if (cfg.idleShowTime) {
               list.add(String.format("时间 %02d:%02d", hour, minute));
            }

            if (cfg.idleShowWeather) {
               list.add(mc.level.isThundering() ? "天气 雷暴" : (mc.level.isRaining() ? "天气 雨天" : "天气 晴天"));
            }
         }

         if (cfg.idleShowCoords) {
            BlockPos p = mc.player.blockPosition();
            int var10001 = p.getX();
            list.add("坐标 " + var10001 + " " + p.getY() + " " + p.getZ());
         }

         if (cfg.idleShowFps) {
            list.add("FPS " + mc.getFps());
         }

         if (cfg.idleShowDimension && mc.level != null) {
            String dim = mc.level.dimension().location().getPath();
            String name = dim.contains("nether") ? "下界" : (dim.contains("end") ? "末地" : "主世界");
            list.add("维度 " + name);
         }

         if (cfg.idleShowBiome && mc.level != null && mc.player != null) {
            Holder<Biome> holder = mc.level.getBiome(mc.player.blockPosition());
            holder.unwrapKey().ifPresent((ResourceKey<Biome> k) -> {
               list.add("群系 " + k.location().getPath());
            });
         }
      } catch (Exception var8) {
      }

      return list;
   }

   public float getW() {
      return this.curW;
   }

   public float getH() {
      return this.curH;
   }

   public float getTargetW() {
      return this.targetW;
   }

   public State getState() {
      return this.state;
   }

   public ProgressProvider getActiveProgress() {
      return this.activeProgress;
   }

   public boolean isPlayerList() {
      return this.state == State.PLAYER_LIST;
   }

   public static enum State {
      IDLE,
      EXPANDING,
      HOLDING,
      PROGRESS,
      PLAYER_LIST;

      // $FF: synthetic method
      private static State[] $values() {
         return new State[]{IDLE, EXPANDING, HOLDING, PROGRESS};
      }
   }
}
