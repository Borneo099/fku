package fku.org.example.fku.features.dynamicisland;

import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.state.BlockState;

public class ProgressManager {
   private static final ProgressProvider[] ORDER = new ProgressProvider[]{new MiningProvider(), new EatingProvider(), new BowChargeProvider(), new ShieldProvider(), new FishingProvider(), new XpProvider(), new MusicDiscProvider()};
   private static final Field PROG_FIELD;
   private static final Field POS_FIELD;

   public static ProgressProvider getActive(DynamicIslandConfig cfg) {
      if (!cfg.showProgressBars) {
         return null;
      } else {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer p = mc.player;
         if (p == null) {
            return null;
         } else {
            ProgressProvider[] var3 = ORDER;
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               ProgressProvider pp = var3[var5];
               if (pp.enabled(cfg) && pp.isActive(p, mc)) {
                  return pp;
               }
            }

            return null;
         }
      }
   }

   public static void tickMusic(Minecraft mc) {
      if (mc != null) {
         RecordMusicDetector.tick();
      }

   }

   private static Field findField(Class start, String name, Class preferType, String... hints) {
      Field f = findByName(start, name);
      return f != null ? f : findByType(start, preferType, hints);
   }

   private static Field findByName(Class start, String name) {
      Class c = start;

      while(c != null && c != Object.class) {
         try {
            Field f = c.getDeclaredField(name);
            f.setAccessible(true);
            return f;
         } catch (NoSuchFieldException var4) {
            c = c.getSuperclass();
         }
      }

      return null;
   }

   private static Field findByType(Class start, Class type, String[] hints) {
      Field fallback = null;

      for(Class c = start; c != null && c != Object.class; c = c.getSuperclass()) {
         Field[] var5 = c.getDeclaredFields();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Field f = var5[var7];
            if (f.getType() == type) {
               f.setAccessible(true);
               String ln = f.getName().toLowerCase();
               String[] var10 = hints;
               int var11 = hints.length;

               for(int var12 = 0; var12 < var11; ++var12) {
                  String h = var10[var12];
                  if (ln.contains(h)) {
                     return f;
                  }
               }

               if (fallback == null) {
                  fallback = f;
               }
            }
         }
      }

      return fallback;
   }

   private static float getDestroyProgress(Minecraft mc) {
      if (PROG_FIELD != null && mc.gameMode != null) {
         try {
            return (Float)PROG_FIELD.get(mc.gameMode);
         } catch (Exception var2) {
            return -1.0F;
         }
      } else {
         return -1.0F;
      }
   }

   private static BlockPos getDestroyBlockPos(Minecraft mc) {
      if (POS_FIELD != null && mc.gameMode != null) {
         try {
            return (BlockPos)POS_FIELD.get(mc.gameMode);
         } catch (Exception var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static float clamp(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   static {
      PROG_FIELD = findField(MultiPlayerGameMode.class, "destroyProgress", Float.TYPE, "destroy", "progress");
      POS_FIELD = findField(MultiPlayerGameMode.class, "destroyBlockPos", BlockPos.class, "destroy", "blockpos");
   }

   static class MiningProvider implements ProgressProvider {
      public boolean isActive(LocalPlayer p, Minecraft mc) {
         return mc.gameMode != null && ProgressManager.getDestroyProgress(mc) > 0.001F;
      }

      public float getRatio() {
         Minecraft mc = Minecraft.getInstance();
         return mc.gameMode != null ? ProgressManager.clamp(ProgressManager.getDestroyProgress(mc)) : 0.0F;
      }

      public ItemStack getIcon() {
         LocalPlayer p = Minecraft.getInstance().player;
         return p != null ? p.getMainHandItem() : ItemStack.EMPTY;
      }

      public String getTitle() {
         return "挖掘";
      }

      public String getSubtitle() {
         Minecraft mc = Minecraft.getInstance();
         int cap = this.capability(mc);
         BlockPos bp = ProgressManager.getDestroyBlockPos(mc);
         if (bp != null && mc.level != null) {
            BlockState bs = mc.level.getBlockState(bp);
            String name = bs.getBlock().getName().getString();
            String tag = cap == 0 ? "徒手可挖" : (cap == 1 ? "可挖掘" : "工具不足");
            return name + " · " + tag;
         } else {
            return "方块";
         }
      }

      public int getColor() {
         int cap = this.capability(Minecraft.getInstance());
         return cap == 0 ? 6732650 : (cap == 1 ? 16758605 : 15684432);
      }

      public boolean enabled(DynamicIslandConfig cfg) {
         return cfg.showProgMining;
      }

      private int capability(Minecraft mc) {
         BlockPos bp = ProgressManager.getDestroyBlockPos(mc);
         LocalPlayer p = mc.player;
         if (bp != null && mc.level != null && p != null) {
            BlockState bs = mc.level.getBlockState(bp);
            if (!bs.requiresCorrectToolForDrops()) {
               return 0;
            } else {
               return p.getMainHandItem().isCorrectToolForDrops(bs) ? 1 : 2;
            }
         } else {
            return 1;
         }
      }
   }

   static class EatingProvider implements ProgressProvider {
      public boolean isActive(LocalPlayer p, Minecraft mc) {
         UseAnim a = p.getUseItem().getUseAnimation();
         return a == UseAnim.EAT || a == UseAnim.DRINK;
      }

      public float getRatio() {
         LocalPlayer p = Minecraft.getInstance().player;
         ItemStack use = p.getUseItem();
         int max = Math.max(1, use.getItem().getUseDuration(use));
         int remaining = p.getUseItemRemainingTicks();
         return ProgressManager.clamp((float)(max - remaining) / (float)max);
      }

      public ItemStack getIcon() {
         return Minecraft.getInstance().player.getUseItem();
      }

      public String getTitle() {
         return Minecraft.getInstance().player.getUseItem().getUseAnimation() == UseAnim.DRINK ? "饮用" : "进食";
      }

      public String getSubtitle() {
         return Minecraft.getInstance().player.getUseItem().getHoverName().getString();
      }

      public int getColor() {
         return 6732650;
      }

      public boolean enabled(DynamicIslandConfig cfg) {
         return cfg.showProgEating;
      }
   }

   static class BowChargeProvider implements ProgressProvider {
      public boolean isActive(LocalPlayer p, Minecraft mc) {
         ItemStack use = p.getUseItem();
         return use.getItem() instanceof ProjectileWeaponItem && p.getUseItem().getUseAnimation() == UseAnim.BOW;
      }

      public float getRatio() {
         LocalPlayer p = Minecraft.getInstance().player;
         ItemStack use = p.getUseItem();
         int max = Math.max(1, use.getItem().getUseDuration(use));
         int remaining = p.getUseItemRemainingTicks();
         int denom = use.getItem() instanceof CrossbowItem ? 25 : 20;
         return ProgressManager.clamp((float)(max - remaining) / (float)denom);
      }

      public ItemStack getIcon() {
         return Minecraft.getInstance().player.getUseItem();
      }

      public String getTitle() {
         return "蓄力";
      }

      public String getSubtitle() {
         ItemStack use = Minecraft.getInstance().player.getUseItem();
         return use.getHoverName().getString();
      }

      public int getColor() {
         return 11225020;
      }

      public boolean enabled(DynamicIslandConfig cfg) {
         return cfg.showProgBow;
      }
   }

   static class ShieldProvider implements ProgressProvider {
      public boolean isActive(LocalPlayer p, Minecraft mc) {
         return p.isBlocking();
      }

      public float getRatio() {
         ItemStack s = Minecraft.getInstance().player.getUseItem();
         int max = s.getMaxDamage();
         return max <= 0 ? 1.0F : ProgressManager.clamp(1.0F - (float)s.getDamageValue() / (float)max);
      }

      public ItemStack getIcon() {
         return Minecraft.getInstance().player.getUseItem();
      }

      public String getTitle() {
         return "格挡";
      }

      public String getSubtitle() {
         ItemStack s = Minecraft.getInstance().player.getUseItem();
         int max = s.getMaxDamage();
         int left = max - s.getDamageValue();
         return "耐久 " + left + "/" + max;
      }

      public int getColor() {
         float r = this.getRatio();
         return r > 0.5F ? 6732650 : (r > 0.2F ? 16758605 : 15684432);
      }

      public boolean enabled(DynamicIslandConfig cfg) {
         return cfg.showProgShield;
      }
   }

   static class FishingProvider implements ProgressProvider {
      private long lastBiteMs = 0L;

      public boolean isActive(LocalPlayer p, Minecraft mc) {
         FishingHook hook = this.findHook(p, mc);
         if (hook == null) {
            return false;
         } else {
            long now = System.currentTimeMillis();
            if (hook.getHookedIn() != null) {
               this.lastBiteMs = now;
            } else if (hook.isInWater() && hook.getDeltaMovement().y < -0.25) {
               this.lastBiteMs = now;
            }

            return true;
         }
      }

      public float getRatio() {
         return 0.0F;
      }

      public boolean hasBar() {
         return false;
      }

      public ItemStack getIcon() {
         LocalPlayer p = Minecraft.getInstance().player;
         return p != null ? p.getMainHandItem() : ItemStack.EMPTY;
      }

      public String getTitle() {
         return "钓鱼";
      }

      public String getSubtitle() {
         boolean biting = System.currentTimeMillis() - this.lastBiteMs < 2000L;
         return biting ? "上钩了！！快拉竿！" : "等待中...";
      }

      public int getColor() {
         return System.currentTimeMillis() - this.lastBiteMs < 2000L ? 16732754 : 2541274;
      }

      public boolean enabled(DynamicIslandConfig cfg) {
         return cfg.showProgFishing;
      }

      private FishingHook findHook(LocalPlayer p, Minecraft mc) {
         if (mc.level == null) {
            return null;
         } else {
            List hooks = mc.level.getEntitiesOfClass(FishingHook.class, p.getBoundingBox().inflate(64.0), (h) -> {
               return h.getPlayerOwner() == p;
            });
            return hooks.isEmpty() ? null : (FishingHook)hooks.get(0);
         }
      }
   }

   static class XpProvider implements ProgressProvider {
      public boolean isActive(LocalPlayer p, Minecraft mc) {
         return p.experienceProgress > 0.001F;
      }

      public float getRatio() {
         LocalPlayer p = Minecraft.getInstance().player;
         return ProgressManager.clamp(p.experienceProgress);
      }

      public ItemStack getIcon() {
         return new ItemStack(Items.EXPERIENCE_BOTTLE);
      }

      public String getTitle() {
         return "经验";
      }

      public String getSubtitle() {
         return "Lv." + Minecraft.getInstance().player.experienceLevel;
      }

      public int getColor() {
         return 58879;
      }

      public boolean enabled(DynamicIslandConfig cfg) {
         return cfg.showProgXp;
      }
   }

   static class MusicDiscProvider implements ProgressProvider {
      public boolean isActive(LocalPlayer p, Minecraft mc) {
         return RecordMusicDetector.isPlaying();
      }

      public float getRatio() {
         return RecordMusicDetector.getProgress();
      }

      public ItemStack getIcon() {
         RecordItem r = RecordMusicDetector.getCurrentRecord();
         return r != null ? new ItemStack(r) : ItemStack.EMPTY;
      }

      public String getTitle() {
         RecordItem r = RecordMusicDetector.getCurrentRecord();
         return r != null ? (new ItemStack(r)).getHoverName().getString() : "唱片机";
      }

      public String getSubtitle() {
         return !RecordMusicDetector.isPlaying() ? "播放中" : "播放中 · 余 " + fmtTime(RecordMusicDetector.getRemainingSeconds());
      }

      public int getColor() {
         return 14696699;
      }

      public boolean enabled(DynamicIslandConfig cfg) {
         return cfg.showProgMusic;
      }

      public boolean isMusicProvider() {
         return true;
      }

      public float getBeatFrequency() {
         return RecordMusicDetector.getBeatFrequency();
      }

      private static String fmtTime(long sec) {
         sec = Math.max(0L, sec);
         long m = sec / 60L;
         long s = sec % 60L;
         return "" + m + ":" + (s < 10L ? "0" : "") + s;
      }
   }
}
