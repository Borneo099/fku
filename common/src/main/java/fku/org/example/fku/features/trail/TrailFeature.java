package fku.org.example.fku.features.trail; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 拖尾特效核心功能 — 在玩家移动时生成动态轨迹特效
 * 支持多种拖尾模式：残影、粒子流、流光轨迹、能量涟漪、虚空裂隙、元素足迹、星尘轨迹
 * 支持多种触发条件：始终、疾跑、飞行、跳跃、战斗
 * 该功能由赛博教员实现
 *
 * ★ 设计思想（矛盾论）：
 *   主要矛盾：性能与视觉密度的矛盾
 *   - 解决：粒子上限控制 + 距离分级渲染 + 低帧率自动禁用
 *   次要矛盾：路径点存储与平滑度的矛盾
 *   - 解决：环形缓冲区固定大小 + Catmull-Rom曲线插值
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class TrailFeature {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;
    private static final TrailManager trailManager = new TrailManager();

    /** 初始化拖尾特效 — 加载配置、注册事件 */
    public static void init() {
        if (initialized) return;
        initialized = true;
        TrailConfig.load();
        MinecraftForge.EVENT_BUS.register(TrailFeature.class);
        Fku.LOGGER.info("[Trail] 拖尾特效已初始化");
    }

    public static TrailManager getTrailManager() { return trailManager; }

    /**
     * 客户端Tick事件 — 每Tick更新路径点、发射粒子、管理残影
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.player == null || mc.level == null) return;

        TrailConfig cfg = TrailConfig.getInstance();
        if (!cfg.enabled) return;

        trailManager.tick(cfg);
    }

    /**
     * 世界渲染 — 绘制光轨、残影、能量涟漪
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (mc.player == null) return;

        TrailConfig cfg = TrailConfig.getInstance();
        if (!cfg.enabled) return;

        // 仅渲染非粒子模式的拖尾效果（粒子模式由原版粒子系统自动渲染）
        String mode = cfg.trailMode;
        if ("PARTICLE".equals(mode) || "STARDUST".equals(mode)
            || "ELEMENTAL_FOOTPRINT".equals(mode) || "VOID_FISSURE".equals(mode)) {
            return; // 粒子模式由Minecraft原版粒子系统自动渲染
        }

        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.getPosition();
        trailManager.render(event.getPoseStack(), cfg, cameraPos);
    }
}