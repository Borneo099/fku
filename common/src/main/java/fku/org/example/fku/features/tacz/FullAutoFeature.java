package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;

/**
 * 全枪自动 — 所有枪械保持连续射击（通过反射调用 TaCZ API）
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class FullAutoFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;

    private static Class<?> clientPlayerGunOperatorClass;
    private static Method fromLocalPlayerMethod;
    private static Method shootMethod;
    private static boolean reflectionFailed = false;
    private static boolean wasShooting = false;
    private static int shootCooldown = 0;

    private static void initReflection() {
        if (reflectionFailed) return;
        try {
            clientPlayerGunOperatorClass = Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
            fromLocalPlayerMethod = clientPlayerGunOperatorClass.getMethod("fromLocalPlayer", LocalPlayer.class);
            shootMethod = clientPlayerGunOperatorClass.getMethod("shoot");
        } catch (Exception e) {
            reflectionFailed = true;
            Fku.LOGGER.warn("[FullAuto] TaCZ 枪械模组未安装，全枪自动功能不可用");
        }
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        initReflection();
        MinecraftForge.EVENT_BUS.register(FullAutoFeature.class);
        Fku.LOGGER.info("[FullAutoFeature] 全枪自动已初始化");
    }

    public static boolean isEnabled() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.fullAutoEnabled;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled() || mc.player == null || reflectionFailed) return;

        if (shootCooldown > 0) { shootCooldown--; return; }

        boolean leftClickDown = mc.options.keyAttack.isDown();
        // 检测是否手持 TaCZ 枪械（通过反射判断）
        ItemStack stack = mc.player.getMainHandItem();
        boolean isGun = false;
        try {
            Class<?> iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
            Method getIGunOrNull = iGunClass.getMethod("getIGunOrNull", ItemStack.class);
            Object gun = getIGunOrNull.invoke(null, stack);
            isGun = (gun != null);
        } catch (Exception ignored) {}

        if (leftClickDown && isGun) {
            try {
                Object operator = fromLocalPlayerMethod.invoke(null, mc.player);
                if (operator != null) {
                    shootMethod.invoke(operator);
                    shootCooldown = 2; // 每3 tick 射一次（约 40ms）
                    wasShooting = true;
                }
            } catch (Exception ignored) {}
        } else {
            wasShooting = false;
        }
    }
}