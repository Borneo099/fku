package lexis;

import java.io.IOException;
import java.net.ServerSocket;
import lexis.Hack.Utils.ModDependencyChecker;
import lexis.czco.CreativeTabHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("lexis")
public class LexisMod {
   public static final String MOD_ID = "lexis";
   private static ServerSocket lockSocket;

   public LexisMod() {
      try {
         lockSocket = new ServerSocket(59761);
      } catch (IOException var2) {
         throw new RuntimeException("\n\n========== Lexis Critical Error ==========\nLexis refuses your multiple game openings!\n\nAnother game with Lexis mod is already running.\nPlease close the other game and try again.\n==========================================");
      }

      CreativeTabHandler.startColorAnimation();
      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
      ModDependencyChecker.check();
   }

   public static void registerShaders() {
   }

   private void commonSetup(FMLCommonSetupEvent event) {
   }
}
