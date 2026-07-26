package fku.org.example.fku.features.playeresp; /* water */

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家ESP功能 — 在玩家周围绘制方框/连线
 * 移植自 Lexis PlayerEspHack
 * 使用 BufferBuilder + Tesselator 直接渲染，确保正确显示
 * 该功能由赛博教员实现
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class PlayerEspFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        PlayerEspConfig.load();
        MinecraftForge.EVENT_BUS.register(PlayerEspFeature.class);
        Fku.LOGGER.info("[PlayerESP] 玩家ESP已初始化");
    }

    public static boolean isEnabled() { return PlayerEspConfig.getInstance().enabled; }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        PlayerEspConfig cfg = PlayerEspConfig.getInstance();
        if (!cfg.enabled) return;
        if (mc.player == null || mc.level == null) return;

        // 收集视野内的玩家
        List<Player> players = new ArrayList<>();
        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;
            if (p.distanceTo(mc.player) > cfg.maxDistance) continue;
            players.add(p);
        }
        if (players.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        float partialTick = event.getPartialTick();

        boolean showBox = cfg.mode.contains("BOX") || cfg.mode.equals("ALL");
        boolean showLines = cfg.mode.contains("LINES") || cfg.mode.equals("ALL");
        boolean showSides = cfg.mode.contains("SIDES") || cfg.mode.equals("ALL");

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // ── 单次渲染所有内容（方框+连线+六面），避免 Tesselator 状态冲突 ──
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buf = t.getBuilder();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = poseStack.last().pose();

        for (Player p : players) {
            AABB bb = p.getBoundingBox().inflate(0.1);
            double minX = bb.minX, minY = bb.minY, minZ = bb.minZ;
            double maxX = bb.maxX, maxY = bb.maxY, maxZ = bb.maxZ;

            // 六面填充
            if (showSides) {
                int sidesArgb = cfg.sidesColor;
                float sr = ((sidesArgb >> 16) & 0xFF) / 255f;
                float sg = ((sidesArgb >> 8) & 0xFF) / 255f;
                float sb = (sidesArgb & 0xFF) / 255f;
                float sa = ((sidesArgb >> 24) & 0xFF) / 255f;
                buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                // 底面
                buf.vertex(mat, (float)minX, (float)minY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)minX, (float)minY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                // 顶面
                buf.vertex(mat, (float)minX, (float)maxY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                // 前面
                buf.vertex(mat, (float)minX, (float)minY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                // 后面
                buf.vertex(mat, (float)minX, (float)minY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                // 左面
                buf.vertex(mat, (float)minX, (float)minY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)minX, (float)minY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                // 右面
                buf.vertex(mat, (float)maxX, (float)minY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)minZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)maxZ).color(sr, sg, sb, sa).endVertex();
                t.end();
            }

            // 方框边框 — 12条线
            if (showBox) {
                int boxArgb = cfg.boxColor;
                float br = ((boxArgb >> 16) & 0xFF) / 255f;
                float bg = ((boxArgb >> 8) & 0xFF) / 255f;
                float bb2 = (boxArgb & 0xFF) / 255f;
                float ba = ((boxArgb >> 24) & 0xFF) / 255f;
                buf.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                // 底面
                buf.vertex(mat, (float)minX, (float)minY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)minX, (float)minY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)minX, (float)minY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)minX, (float)minY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                // 顶面
                buf.vertex(mat, (float)minX, (float)maxY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                // 竖线
                buf.vertex(mat, (float)minX, (float)minY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)minZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)minY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)maxX, (float)maxY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)minX, (float)minY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                buf.vertex(mat, (float)minX, (float)maxY, (float)maxZ).color(br, bg, bb2, ba).endVertex();
                t.end();
            }

            // 连线（准星到玩家中心）
            if (showLines) {
                int linesArgb = cfg.linesColor;
                float lr = ((linesArgb >> 16) & 0xFF) / 255f;
                float lg = ((linesArgb >> 8) & 0xFF) / 255f;
                float lb = (linesArgb & 0xFF) / 255f;
                float la = ((linesArgb >> 24) & 0xFF) / 255f;
                Vec3 center = p.getBoundingBox().getCenter();
                buf.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                // 从相机到玩家中心（相机在 translate(-cam) 后位于原点）
                buf.vertex(mat, 0.0f, 0.0f, 0.0f).color(lr, lg, lb, la).endVertex();
                buf.vertex(mat, (float)center.x, (float)center.y, (float)center.z).color(lr, lg, lb, la).endVertex();
                t.end();
            }
        }
        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}