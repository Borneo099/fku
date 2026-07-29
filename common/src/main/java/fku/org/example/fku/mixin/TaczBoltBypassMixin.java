package fku.org.example.fku.mixin; /* water */

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import fku.org.example.fku.features.tacz.TaCZConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * 全狙自动 — 跳过栓动步枪的栓动动作，直接使子弹上膛
 * 参考自 Lexis TaczBoltBypassMixin
 * 该 Mixin 由赛博教员实现
 */
@Mixin(targets = {"com.tacz.guns.entity.shooter.LivingEntityBolt"}, remap = false)
public class TaczBoltBypassMixin {

    @Shadow @Final private ShooterDataHolder data;
    @Shadow @Final private LivingEntity shooter;

    @Inject(method = {"bolt"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void onBolt(CallbackInfo ci) {
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.sniperFullAutoEnabled) return;
        if (shooter == null) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (!shooter.equals(mc.player)) return;

            Supplier<ItemStack> gunSupplier = data.currentGunItem;
            if (gunSupplier == null) return;
            ItemStack stack = gunSupplier.get();
            if (stack == null || stack.isEmpty()) return;

            Item item = stack.getItem();
            if (item instanceof IGun gun) {
                gun.setBulletInBarrel(stack, true);
                data.isBolting = false;
                ci.cancel();
            }
        } catch (Exception ignored) {}
    }
}