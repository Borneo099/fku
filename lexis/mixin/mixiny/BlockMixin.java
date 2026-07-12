package lexis.mixin.mixiny;

import com.mojang.blaze3d.platform.GlUtil;
import lexis.Hack.Utils.pathfinding.Faker;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({DebugScreenOverlay.class})
public abstract class BlockMixin {
   @Redirect(
      method = {"getSystemInformation"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/platform/GlUtil;getRenderer()Ljava/lang/String;"
)
   )
   private String lexis$fakeGpu() {
      return Faker.isEnabled() ? "NVIDIA GeForce RTX 5070 Ti/PCIe/SSE2" : GlUtil.m_84820_();
   }

   @Redirect(
      method = {"getSystemInformation"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/platform/GlUtil;getVendor()Ljava/lang/String;"
)
   )
   private String lexis$fakeVendor() {
      return Faker.isEnabled() ? "NVIDIA Corporation" : GlUtil.m_84818_();
   }

   @Redirect(
      method = {"getSystemInformation"},
      at = @At(
   value = "INVOKE",
   target = "Lcom/mojang/blaze3d/platform/GlUtil;getOpenGLVersion()Ljava/lang/String;"
)
   )
   private String lexis$fakeGlVersion() {
      return Faker.isEnabled() ? "3.2.0 NVIDIA 566.36" : GlUtil.m_84821_();
   }
}
