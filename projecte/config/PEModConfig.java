package moze_intel.projecte.config;

import java.nio.file.Path;
import java.util.function.Function;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

public class PEModConfig extends ModConfig {
   private static final PEConfigFileTypeHandler PE_TOML = new PEConfigFileTypeHandler();
   private final IPEConfig peConfig;

   public PEModConfig(ModContainer container, IPEConfig config) {
      super(config.getConfigType(), config.getConfigSpec(), container, "ProjectE/" + config.getFileName() + ".toml");
      this.peConfig = config;
   }

   public ConfigFileTypeHandler getHandler() {
      return PE_TOML;
   }

   public void clearCache(ModConfigEvent event) {
      this.peConfig.clearCache(event instanceof ModConfigEvent.Unloading);
   }

   private static class PEConfigFileTypeHandler extends ConfigFileTypeHandler {
      private static Path getPath(Path configBasePath) {
         return configBasePath.endsWith("serverconfig") ? FMLPaths.CONFIGDIR.get() : configBasePath;
      }

      public Function reader(Path configBasePath) {
         return super.reader(getPath(configBasePath));
      }

      public void unload(Path configBasePath, ModConfig config) {
         super.unload(getPath(configBasePath), config);
      }
   }
}
