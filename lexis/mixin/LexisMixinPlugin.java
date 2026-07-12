package lexis.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class LexisMixinPlugin implements IMixinConfigPlugin {
   private static Boolean taczLoaded = null;
   private static Boolean baritoneLoaded = null;

   private static boolean isTaczLoaded() {
      if (taczLoaded == null) {
         try {
            Class.forName("com.tacz.guns.api.TimelessAPI", false, LexisMixinPlugin.class.getClassLoader());
            taczLoaded = true;
         } catch (Throwable var1) {
            taczLoaded = false;
         }
      }

      return taczLoaded;
   }

   private static boolean isBaritoneLoaded() {
      if (baritoneLoaded == null) {
         try {
            Class.forName("baritone.api.BaritoneAPI", false, LexisMixinPlugin.class.getClassLoader());
            baritoneLoaded = true;
         } catch (Throwable var1) {
            baritoneLoaded = false;
         }
      }

      return baritoneLoaded;
   }

   public void onLoad(String mixinPackage) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      if (mixinClassName.contains(".tacz.")) {
         return isTaczLoaded();
      } else {
         return mixinClassName.contains(".baritone.") ? isBaritoneLoaded() : true;
      }
   }

   public void acceptTargets(Set myTargets, Set otherTargets) {
   }

   public List getMixins() {
      return null;
   }

   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
