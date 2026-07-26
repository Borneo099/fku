package fku.org.example.fku.features.tpgoto; /* water */

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import fku.org.example.fku.Fku;
import fku.org.example.fku.utils.pathfinding.AStarPathFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * 传送前往坐标 — 通过 A* 寻路 + 真传送包逐步移动到目标坐标
 * 默认开启，无需开关
 * 改编自 Lexis 客户端 TpGotoPosCommand
 * 该方法由赛博教员实现
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TpGotoPosFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger("TpGotoPos");
    private static final Minecraft mc = Minecraft.getInstance();

    private static boolean active = false;
    private static boolean moving = false;
    private static List<Vec3> path = null;
    private static int pathIndex = 0;
    private static Vec3 targetPos = null;
    private static long lastPacketTime = 0L;
    private static boolean waitingForChunk = false;
    private static long chunkWaitStartTime = 0L;

    // ── 渲染相关 ──
    private static List<Vec3> renderPathNodes = null;

    // ── 位置校验相关（卡住检测） ──
    private static Vec3 stuckCheckPos = null;
    private static int stuckCount = 0;
    private static long lastVerifyTime = 0L;
    private static Vec3 lastTeleportPos = null;

    // ──────── API ────────

    public static boolean isActive() { return active; }

    /** 开始传送到指定坐标 */
    public static void startTeleport(double x, double y, double z) {
        if (mc.player == null || mc.level == null) return;
        if (active) { sendMsg("§e已有正在进行的传送，请先停止"); return; }

        BlockPos targetBlock = BlockPos.containing(x, y, z);
        targetPos = new Vec3(targetBlock.getX() + 0.5, targetBlock.getY() + 0.01, targetBlock.getZ() + 0.5);
        active = true;
        sendMsg("§7正在计算前往坐标 " + targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ() + " 的路径");

        CompletableFuture.supplyAsync(() -> computePath(mc, targetBlock))
                .thenAccept(p -> handlePath(p, "目标坐标"));
    }

    /** 停止传送 */
    public static void stopWalking(String reason) {
        active = false;
        moving = false;
        path = null;
        renderPathNodes = null;
        targetPos = null;
        pathIndex = 0;
        waitingForChunk = false;
        chunkWaitStartTime = 0L;
        stuckCheckPos = null;
        stuckCount = 0;
        lastVerifyTime = 0L;
        lastTeleportPos = null;
        if (reason != null) sendMsg(reason);
    }

    // ──────── 路径计算 ────────

    private static List<Vec3> computePath(Minecraft mc, BlockPos targetBlock) {
        if (mc.player == null) return null;
        Vec3 start = mc.player.position();
        Vec3 end = new Vec3(targetBlock.getX() + 0.5, targetBlock.getY() + 0.01, targetBlock.getZ() + 0.5);
        TpGotoConfig cfg = TpGotoConfig.getInstance();
        try {
            AStarPathFinder finder = new AStarPathFinder(mc.level);
            finder.setAirPath(cfg.airPath);
            finder.setAttackRange(3.0);
            return finder.findPath(start, end, cfg.maxStep);
        } catch (Exception e) {
            LOGGER.error("路径计算失败", e);
            return null;
        }
    }

    private static void handlePath(List<Vec3> p, String targetDesc) {
        if (p != null && p.size() >= 2) {
            mc.execute(() -> {
                path = p;
                renderPathNodes = new ArrayList<>(p);
                pathIndex = 1;
                mc.execute(() -> {
                    try { Thread.sleep(1000L); } catch (InterruptedException ignored) {}
                    moving = true;
                    waitingForChunk = false;
                    chunkWaitStartTime = 0L;
                    sendMsg("§a路径计算完成");
                });
            });
        } else {
            mc.execute(() -> {
                sendMsg("§c无法找到路径");
                active = false;
            });
        }
    }

    // ──────── Tick 事件 ────────

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.player == null || mc.level == null) return;
        if (!active || !moving) return;

        if (targetPos == null) { stopWalking("§c目标位置无效"); return; }

        TpGotoConfig cfg = TpGotoConfig.getInstance();

        // ── 飞行 ──
        if (cfg.tpFlightEnabled) {
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().mayfly = true;
        }

        // ── 到达判定 ──
        if (mc.player.position().distanceTo(targetPos) <= cfg.stopDistance) {
            stopWalking("§a已经到位置了");
            return;
        }

        // ── 路径行走 + 发包 ──
        if (path != null && pathIndex < path.size()) {
            BlockPos destBlock = BlockPos.containing(path.get(pathIndex));

            // 区块加载检查（根据配置决定是否跳过）
            if (cfg.waitForChunk && !mc.level.isLoaded(destBlock)) {
                if (!waitingForChunk) {
                    waitingForChunk = true;
                    chunkWaitStartTime = System.currentTimeMillis();
                }
                // 检查是否超时，超时则强制继续发包
                if (System.currentTimeMillis() - chunkWaitStartTime < cfg.chunkWaitTimeout) {
                    return;
                }
                // 超时后 fallthrough 继续发包
            }
            if (waitingForChunk) waitingForChunk = false;

            long now = System.currentTimeMillis();
            if (now - lastPacketTime >= cfg.packetInterval) {
                Vec3 dest = path.get(pathIndex);
                boolean onGround = mc.player.onGround();
                mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(dest.x, dest.y, dest.z, onGround));
                mc.player.setPos(dest.x, dest.y, dest.z);
                lastPacketTime = now;
                lastTeleportPos = dest;
                pathIndex++;
            }
        } else {
            stopWalking("§a已经到位置了");
            return;
        }

        // ── 位置校验（在发包之后） ──
        if (cfg.positionCheck) {
            long now = System.currentTimeMillis();
            if (now - lastVerifyTime >= cfg.positionCheckInterval) {
                lastVerifyTime = now;
                Vec3 currentPos = mc.player.position();
                if (stuckCheckPos != null) {
                    if (currentPos.distanceTo(stuckCheckPos) < 0.5) {
                        stuckCount++;
                        if (stuckCount >= 3) {
                            stopWalking("§c检测到玩家被卡住，已停止传送");
                            return;
                        }
                    } else {
                        stuckCount = 0;
                    }
                }
                stuckCheckPos = currentPos;
            }
        }
    }

    // ──────── 3D 渲染（路径连线） ────────

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (mc.player == null || mc.level == null) return;
        if (!active) return;

        TpGotoConfig cfg = TpGotoConfig.getInstance();
        if (!cfg.renderPath || renderPathNodes == null || renderPathNodes.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);

        // 路径颜色 — 绿色半透明
        int pathColor = 0x5500FF00;
        float pr = ((pathColor >> 16) & 0xFF) / 255f;
        float pg = ((pathColor >> 8) & 0xFF) / 255f;
        float pb = (pathColor & 0xFF) / 255f;
        float pa = ((pathColor >> 24) & 0xFF) / 255f;

        for (int i = 0; i < renderPathNodes.size(); i++) {
            Vec3 n = renderPathNodes.get(i);

            // 渲染节点小框
            renderBox(poseStack, consumer,
                    new AABB(n.x - 0.2, n.y, n.z - 0.2, n.x + 0.2, n.y + 2, n.z + 0.2),
                    pathColor);

            // 渲染节点间连线
            if (i < renderPathNodes.size() - 1) {
                Vec3 next = renderPathNodes.get(i + 1);
                Matrix4f mat = poseStack.last().pose();
                consumer.vertex(mat, (float) n.x, (float) (n.y + 1), (float) n.z).color(pr, pg, pb, pa).normal(0f, 1f, 0f).endVertex();
                consumer.vertex(mat, (float) next.x, (float) (next.y + 1), (float) next.z).color(pr, pg, pb, pa).normal(0f, 1f, 0f).endVertex();
            }
        }

        poseStack.popPose();
    }

    // ──────── 渲染辅助 ────────

    /** 渲染 AABB 边框 */
    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, AABB box, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        Matrix4f mat = poseStack.last().pose();
        double minX = box.minX, minY = box.minY, minZ = box.minZ;
        double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;

        // 底部矩形（4条边线）
        consumer.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, -1f, 0f).endVertex();

        // 顶部矩形（4条边线）
        consumer.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();

        // 竖线（4条垂直边）
        consumer.vertex(mat, (float) minX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) minZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) maxX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) minY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
        consumer.vertex(mat, (float) minX, (float) maxY, (float) maxZ).color(r, g, b, a).normal(0f, 1f, 0f).endVertex();
    }

    // ──────── 工具 ────────

    private static void sendMsg(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§7[传送前往坐标] " + msg), false);
        }
    }
}