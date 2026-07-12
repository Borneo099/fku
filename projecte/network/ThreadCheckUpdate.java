package moze_intel.projecte.network;

import moze_intel.projecte.PECore;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLConfig.ConfigValue;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ComparableVersion;

@EventBusSubscriber(
   modid = "projecte",
   value = {Dist.CLIENT}
)
public class ThreadCheckUpdate extends Thread {
   private static final String curseURL = "https://www.curseforge.com/minecraft/mc-mods/projecte/files";
   private static volatile ComparableVersion target = null;
   private static volatile boolean hasSentMessage = false;

   public ThreadCheckUpdate() {
      this.setName("ProjectE Update Checker Notifier");
   }

   public void run() {
      if (FMLConfig.getBoolConfigValue(ConfigValue.VERSION_CHECK)) {
         IModInfo info = PECore.MOD_CONTAINER.getModInfo();
         VersionChecker.CheckResult result = null;
         int tries = 0;

         do {
            VersionChecker.CheckResult res = VersionChecker.getResult(info);
            if (res.status() != Status.PENDING) {
               result = res;
            }

            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var6) {
            }

            ++tries;
         } while(result == null && tries < 10);

         if (result == null) {
            PECore.LOGGER.info("Update check failed.");
         } else {
            if (result.status() == Status.OUTDATED) {
               target = result.target();
            }

         }
      }
   }

   @SubscribeEvent
   public static void worldLoad(EntityJoinLevelEvent evt) {
      Entity var2 = evt.getEntity();
      if (var2 instanceof LocalPlayer player) {
         if (target != null && !hasSentMessage) {
            hasSentMessage = true;
            player.m_213846_(PELang.UPDATE_AVAILABLE.translate(new Object[]{target}));
            player.m_213846_(PELang.UPDATE_GET_IT.translate(new Object[0]));
            player.m_213846_(TextComponentUtil.build(new ClickEvent(Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/projecte/files"), "https://www.curseforge.com/minecraft/mc-mods/projecte/files"));
         }
      }

   }
}
