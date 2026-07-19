package fku.org.example.fku.features.waterwalk; /* water */

import net.minecraft.client.Minecraft;

/**
 * WaterWalk（水上行走 / Jesus）—— 把水/岩浆当实体方块
 *
 * 真正的「液体变固体」实现在 MixinLiquidBlockJesus：
 *   当 enabled 且碰撞上下文是（非潜行的）玩家时，让 LiquidBlock 返回完整方块碰撞箱，
 *   玩家便像站在固体顶上 —— 不沉、不烧、不减速、如履平地。
 *
 * 本类只负责加载配置 + 暴露 isActive() 供 mixin 调用。
 *
 * 来源：lexis1.20.1/lexis/mixin/mixina/LiquidBlockMixin.java + JesusHack.java
 */
public class WaterWalkFeature {

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    /** 从配置文件静默恢复开关状态（在 FMLClientSetupEvent 中调用） */
    public static void init() {
        WaterWalkConfig.getInstance();
    }

    /** 供 MixinLiquidBlockJesus 判断功能是否激活（含潜行放行） */
    public static boolean isActive() {
        WaterWalkConfig cfg = WaterWalkConfig.getInstance();
        if (!cfg.enabled) return false;
        Minecraft mc = getMc();
        if (mc == null || mc.player == null) return false;
        // ★ 潜行时关闭：让玩家可正常下潜
        return !mc.options.keyShift.isDown();
    }
}
