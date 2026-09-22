package fku.org.example.fku.features.dynamicisland;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(
   modid = "fku",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class RecordMusicDetector {
   private static final Map RECORD_BY_SOUND = new HashMap();
   private static RecordItem currentRecord;
   private static long startTick;
   private static SoundInstance soundInst;
   private static final double MAX_RANGE = 24.0;

   @SubscribeEvent
   public static void onPlaySound(PlaySoundEvent e) {
      handle(e.getSound());
   }

   @SubscribeEvent
   public static void onStreamingSound(PlayStreamingSourceEvent e) {
      handle(e.getSound());
   }

   private static void handle(SoundInstance si) {
      if (si != null) {
         ResourceLocation loc = si.getLocation();
         if (loc != null) {
            RecordItem rec = (RecordItem)RECORD_BY_SOUND.get(loc);
            if (rec != null) {
               currentRecord = rec;
               startTick = currentTick();
               soundInst = si;
            }

         }
      }
   }

   public static void tick() {
      if (currentRecord != null) {
         Minecraft mc = Minecraft.getInstance();
         long gt = currentTick();
         if (mc.level != null && mc.player != null) {
            if (soundInst != null && gt - startTick > 15L) {
               SoundManager sm = mc.getSoundManager();
               if (!sm.isActive(soundInst)) {
                  reset();
                  return;
               }

               LocalPlayer player = mc.player;
               double sx = soundInst.getX();
               double sy = soundInst.getY();
               double sz = soundInst.getZ();
               double ddx = player.getX() - sx;
               double ddy = player.getY() - sy;
               double ddz = player.getZ() - sz;
               double d2 = ddx * ddx + ddy * ddy + ddz * ddz;
               if (d2 > 576.0) {
                  reset();
                  return;
               }
            }

            long len = (long)currentRecord.getLengthInTicks();
            if (startTick < 0L || len > 0L && gt - startTick > len + 80L) {
               reset();
            }

         } else {
            reset();
         }
      }
   }

   private static void reset() {
      currentRecord = null;
      startTick = -1L;
      soundInst = null;
   }

   private static long currentTick() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level != null ? mc.level.getGameTime() : 0L;
   }

   public static boolean isPlaying() {
      return currentRecord != null;
   }

   public static RecordItem getCurrentRecord() {
      return currentRecord;
   }

   public static float getProgress() {
      if (currentRecord == null) {
         return 0.0F;
      } else {
         long len = (long)currentRecord.getLengthInTicks();
         if (len <= 0L) {
            return 0.0F;
         } else {
            float r = (float)(currentTick() - startTick) / (float)len;
            return Math.max(0.0F, Math.min(1.0F, r));
         }
      }
   }

   public static long getRemainingSeconds() {
      if (currentRecord == null) {
         return 0L;
      } else {
         long len = (long)currentRecord.getLengthInTicks();
         if (len <= 0L) {
            return 0L;
         } else {
            long elapsed = startTick < 0L ? 0L : currentTick() - startTick;
            return Math.max(0L, (len - elapsed) / 20L);
         }
      }
   }

   public static float getBeatFrequency() {
      if (currentRecord == null) {
         return 2.0F;
      } else {
         int h = currentRecord.getDescriptionId().hashCode();
         return 1.6F + (float)(Math.abs(h) % 11) * 0.09F;
      }
   }

   static {
      try {
         Iterator var0 = BuiltInRegistries.ITEM.iterator();

         while(var0.hasNext()) {
            Item item = (Item)var0.next();
            if (item instanceof RecordItem ri) {
               RECORD_BY_SOUND.put(ri.getSound().getLocation(), ri);
            }
         }
      } catch (Exception var3) {
      }

      currentRecord = null;
      startTick = -1L;
      soundInst = null;
   }
}
