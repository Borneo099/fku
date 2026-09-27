package fku.org.example.fku.mixin;

import fku.org.example.fku.features.dynamicisland.DynamicIslandConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 取消原版 Tab 玩家列表渲染：当灵动岛「Tab键替换玩家列表」开启时，
 * 按下 Tab 只显示灵动岛的玩家列表 HUD，避免与原版重叠显示两个列表。
 *
 * 为什么用 Mixin 而非事件取消：原版 Tab 列表在 neoforge 下不通过
 * VanillaGuiOverlay 事件总线渲染，事件方式取消无效；直接 hook 原版
 * PlayerTabOverlay.render 可在 Forge / NeoForge 两端都生效。
 *
 * 按方法名注入（@At HEAD 取消），不依赖 render 的具体参数描述符，
 * 避免不同小版本间签名差异导致 mixin 应用失败。
 */
@OnlyIn(Dist.CLIENT)
@Mixin(PlayerTabOverlay.class)
public class MixinGui {
   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void fku$cancelVanillaTabList(CallbackInfo ci) {
      DynamicIslandConfig cfg = DynamicIslandConfig.getInstance();
      if (cfg.enabled && cfg.tabShowEnabled && Minecraft.getInstance().options.keyPlayerList.isDown()) {
         ci.cancel();
      }
   }
}
