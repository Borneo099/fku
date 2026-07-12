package moze_intel.projecte.config;

import java.nio.file.Path;
import moze_intel.projecte.PECore;
import net.minecraftforge.fml.loading.FMLPaths;

public class ProjectEConfig {
   public static final Path CONFIG_DIR;
   public static final ServerConfig server = new ServerConfig();
   public static final CommonConfig common = new CommonConfig();
   public static final ClientConfig client = new ClientConfig();

   public static void register() {
      registerConfig(server);
      registerConfig(common);
      registerConfig(client);
   }

   public static void registerConfig(IPEConfig config) {
      PEModConfig peModConfig = new PEModConfig(PECore.MOD_CONTAINER, config);
      if (config.addToContainer()) {
         PECore.MOD_CONTAINER.addConfig(peModConfig);
      }

   }

   static {
      CONFIG_DIR = FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve("ProjectE"));
   }
}
