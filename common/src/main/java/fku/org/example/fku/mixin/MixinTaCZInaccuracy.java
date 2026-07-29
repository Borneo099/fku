package fku.org.example.fku.mixin; /* water */

import com.google.common.collect.Maps;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.resource.modifier.custom.InaccuracyModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import fku.org.example.fku.features.tacz.TaCZConfig;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * MixinTaCZInaccuracy — 无扩散功能
 * 参考自 NoSpread 02 的 MixinInaccuracyModifier
 * 当 TaCZConfig.noSpreadEnabled 开启时，将扩散值归零
 * 该 Mixin 由赛博教员实现
 */
@Mixin(value = InaccuracyModifier.class, remap = false)
public class MixinTaCZInaccuracy {

    @Inject(method = "initCache", at = @At("RETURN"), cancellable = true)
    private void onInitCache(ItemStack gunItem, GunData gunData, CallbackInfoReturnable<CacheValue<Map<InaccuracyType, Float>>> cir) {
        if (TaCZConfig.getInstance().noSpreadEnabled) {
            Map<InaccuracyType, Float> zeroMap = Maps.newHashMap();
            zeroMap.put(InaccuracyType.STAND, 0.0f);
            zeroMap.put(InaccuracyType.MOVE, 0.0f);
            zeroMap.put(InaccuracyType.SNEAK, 0.0f);
            zeroMap.put(InaccuracyType.LIE, 0.0f);
            zeroMap.put(InaccuracyType.AIM, 0.0f);
            cir.setReturnValue(new CacheValue<>(zeroMap));
        }
    }
}