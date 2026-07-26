package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;

/**
 * 自动换弹 — 移植自 Lexis AutoReloadHack
 * 弹匣打空就自动换弹（通过反射调用 TaCZ API，无依赖时静默失效）
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class AutoReloadFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;
    private static int reloadCooldown = 0;

    // 反射缓存
    private static Class<?> iGunClass;
    private static Method iGunGetIGunOrNull;
    private static Method getCurrentAmmoCount;
    private static Method hasBulletInBarrel;
    private static Class<?> clientPlayerGunOperator;
    private static Method fromLocalPlayer;
    private static Method reload;
    private static boolean reflectionFailed = false;

    private static void initReflection() {
        if (reflectionFailed) return;
        try {
            iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
            iGunGetIGunOrNull = iGunClass.getMethod("getIGunOrNull", ItemStack.class);
            getCurrentAmmoCount = iGunClass.getMethod("getCurrentAmmoCount", ItemStack.class);
            hasBulletInBarrel = iGunClass.getMethod("hasBulletInBarrel", ItemStack.class);
            clientPlayerGunOperator = Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
            fromLocalPlayer = clientPlayerGunOperator.getMethod("fromLocalPlayer", net.minecraft.client.player.LocalPlayer.class);
            reload = clientPlayerGunOperator.getMethod("reload");
        } catch (Exception e) {
            reflectionFailed = true;
            Fku.LOGGER.warn("[AutoReload] TaCZ 枪械模组未安装，自动换弹功能不可用");
        }
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        initReflection();
        MinecraftForge.EVENT_BUS.register(AutoReloadFeature.class);
        Fku.LOGGER.info("[AutoReloadFeature] 自动换弹已初始化");
    }

    public static boolean isEnabled() { 
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.autoReloadEnabled; 
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        TaCZConfig cfg = TaCZConfig.getInstance();
        if (!cfg.masterEnabled || !cfg.autoReloadEnabled || mc.player == null || reflectionFailed) return;

        if (reloadCooldown > 0) { reloadCooldown--; return; }
        ItemStack stack = mc.player.getMainHandItem();
        try {
            Object gun = iGunGetIGunOrNull.invoke(null, stack);
            if (gun == null) return;
            int ammo = (int) getCurrentAmmoCount.invoke(gun, stack);
            boolean barrelLoaded = (boolean) hasBulletInBarrel.invoke(gun, stack);
            if (ammo <= 1 && (ammo != 1 || !barrelLoaded)) {
                Object operator = fromLocalPlayer.invoke(null, mc.player);
                if (operator != null) { reload.invoke(operator); reloadCooldown = 4; }
            }
        } catch (Exception ignored) {}
    }
}