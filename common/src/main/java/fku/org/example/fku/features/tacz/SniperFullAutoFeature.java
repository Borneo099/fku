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
 * 全狙击自动 — 所有狙击枪自动连发（通过反射调用 TaCZ API）
 * 移植自 Lexis SniperFullAutoHack
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class SniperFullAutoFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;

    private static Class<?> clientPlayerGunOperatorClass;
    private static Method fromLocalPlayerMethod;
    private static Method shootMethod;
    private static boolean reflectionFailed = false;
    private static int shootCooldown = 0;

    private static void initReflection() {
        if (reflectionFailed) return;
        try {
            clientPlayerGunOperatorClass = Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
            fromLocalPlayerMethod = clientPlayerGunOperatorClass.getMethod("fromLocalPlayer", LocalPlayer.class);
            shootMethod = clientPlayerGunOperatorClass.getMethod("shoot");
        } catch (Exception e) {
            reflectionFailed = true;
            Fku.LOGGER.warn("[SniperFullAuto] TaCZ 枪械模组未安装，全狙自动功能不可用");
        }
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        initReflection();
        MinecraftForge.EVENT_BUS.register(SniperFullAutoFeature.class);
        Fku.LOGGER.info("[SniperFullAutoFeature] 全狙自动已初始化");
    }

    public static boolean isEnabled() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.sniperFullAutoEnabled;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled() || mc.player == null || reflectionFailed) return;

        if (shootCooldown > 0) { shootCooldown--; return; }

        boolean leftClickDown = mc.options.keyAttack.isDown();
        ItemStack stack = mc.player.getMainHandItem();
        // 检测是否为狙击枪（通过 GetGunData 反射判断）
        boolean isSniper = false;
        try {
            Class<?> iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
            Method getIGunOrNull = iGunClass.getMethod("getIGunOrNull", ItemStack.class);
            Object gun = getIGunOrNull.invoke(null, stack);
            if (gun != null) {
                Method getGunData = iGunClass.getMethod("getGunData", ItemStack.class);
                Object gunData = getGunData.invoke(gun, stack);
                if (gunData != null) {
                    Class<?> gunDataClass = gunData.getClass();
                    // 尝试获取射击模式
                    try {
                        Method getRoundsPerMinute = gunDataClass.getMethod("getRoundsPerMinute");
                        int rpm = (int) getRoundsPerMinute.invoke(gunData);
                        // 低射速 = 狙击枪
                        isSniper = rpm < 120;
                    } catch (Exception e) {
                        // 尝试获取 type 字段
                        try {
                            Method getType = gunDataClass.getMethod("getType");
                            Object type = getType.invoke(gunData);
                            String typeStr = type.toString().toLowerCase();
                            isSniper = typeStr.contains("sniper") || typeStr.contains("bolt") || typeStr.contains("marksman");
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}

        if (leftClickDown && isSniper) {
            try {
                Object operator = fromLocalPlayerMethod.invoke(null, mc.player);
                if (operator != null) {
                    shootMethod.invoke(operator);
                    shootCooldown = 8; // 狙击枪慢一点
                }
            } catch (Exception ignored) {}
        }
    }
}