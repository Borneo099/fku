package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * 选区管理器 — 管理Pos1/Pos2坐标与渲染
 *
 * 设计思想：
 * - 使用两个 BlockPos 定义选区
 * - 提供选区边框渲染（RenderLevelLastEvent）
 * - 支持选区面积/体积计算
 */
public class SelectionManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final SelectionManager INSTANCE = new SelectionManager();

    private BlockPos pos1;
    private BlockPos pos2;
    private boolean hasPos1 = false;
    private boolean hasPos2 = false;

    public static SelectionManager getInstance() { return INSTANCE; }

    private SelectionManager() {}

    // ════════════ 选区设置 ════════════

    public void setPos1(BlockPos pos) {
        this.pos1 = pos;
        this.hasPos1 = true;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§7[WorldEdit] §aPos1 已设置: " + formatPos(pos)), true);
            showSelectionInfo();
        }
    }

    public void setPos2(BlockPos pos) {
        this.pos2 = pos;
        this.hasPos2 = true;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§7[WorldEdit] §aPos2 已设置: " + formatPos(pos)), true);
            showSelectionInfo();
        }
    }

    public void clearSelection() {
        pos1 = null;
        pos2 = null;
        hasPos1 = false;
        hasPos2 = false;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§7[WorldEdit] §e选区已清除"), true);
        }
    }

    /**
     * 显示选区信息
     */
    private void showSelectionInfo() {
        if (!hasPos1 || !hasPos2) return;
        int dx = Math.abs(pos1.getX() - pos2.getX()) + 1;
        int dy = Math.abs(pos1.getY() - pos2.getY()) + 1;
        int dz = Math.abs(pos1.getZ() - pos2.getZ()) + 1;
        long volume = (long) dx * dy * dz;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§7[WorldEdit] §e选区: " + dx + "×" + dy + "×" + dz + " = " + volume + " 方块"),
                    true);
        }
    }

    // ════════════ Getters ════════════

    public BlockPos getPos1() { return pos1; }
    public BlockPos getPos2() { return pos2; }
    public boolean hasPos1() { return hasPos1; }
    public boolean hasPos2() { return hasPos2; }
    public boolean hasSelection() { return hasPos1 && hasPos2 && pos1 != null && pos2 != null; }

    public BlockPos getMin() {
        if (!hasSelection()) return null;
        return new BlockPos(
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ()));
    }

    public BlockPos getMax() {
        if (!hasSelection()) return null;
        return new BlockPos(
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ()));
    }

    /**
     * 获取选区体积
     */
    public long getVolume() {
        if (!hasSelection()) return 0;
        int dx = Math.abs(pos1.getX() - pos2.getX()) + 1;
        int dy = Math.abs(pos1.getY() - pos2.getY()) + 1;
        int dz = Math.abs(pos1.getZ() - pos2.getZ()) + 1;
        return (long) dx * dy * dz;
    }

    // ════════════ 渲染 ════════════

    /**
     * 在 RenderLevelLastEvent 中调用 — 渲染选区边框
     *
     * 使用相机相对坐标 + PoseStack 矩阵渲染。
     * 坐标 = 世界坐标 - 相机位置 → 相机相对坐标 → PoseStack 渲染
     */
    public void renderSelection(PoseStack poseStack, float partialTick) {
        if (!hasSelection() || !WorldEditConfig.getInstance().renderSelection) return;
        if (mc.level == null || mc.player == null) return;

        BlockPos min = getMin();
        BlockPos max = getMax();
        if (min == null || max == null) return;

        // 相机位置
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        // 计算相机相对坐标
        float x1 = (float)(min.getX() - camPos.x);
        float y1 = (float)(min.getY() - camPos.y);
        float z1 = (float)(min.getZ() - camPos.z);
        float x2 = (float)(max.getX() + 1 - camPos.x);
        float y2 = (float)(max.getY() + 1 - camPos.y);
        float z2 = (float)(max.getZ() + 1 - camPos.z);

        int color = parseColor(WorldEditConfig.getInstance().selectionColor);

        // 获取 PoseStack 的矩阵
        Matrix4f matrix = poseStack.last().pose();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = 0.8f;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.LINES);

        // 底部矩形
        addLine(consumer, matrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
        addLine(consumer, matrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
        addLine(consumer, matrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
        addLine(consumer, matrix, x1, y1, z2, x1, y1, z1, r, g, b, a);
        // 顶部矩形
        addLine(consumer, matrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
        addLine(consumer, matrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
        addLine(consumer, matrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
        addLine(consumer, matrix, x1, y2, z2, x1, y2, z1, r, g, b, a);
        // 垂直线
        addLine(consumer, matrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
        addLine(consumer, matrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
        addLine(consumer, matrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
        addLine(consumer, matrix, x1, y1, z2, x1, y2, z2, r, g, b, a);

        bufferSource.endBatch();
    }

    private void addLine(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float z1,
                         float x2, float y2, float z2, float r, float g, float b, float a) {
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
    }

    /**
     * 解析颜色字符串 (#RRGGBB) 为整数
     */
    private int parseColor(String color) {
        try {
            if (color.startsWith("#")) {
                return Integer.parseInt(color.substring(1), 16) | 0xFF000000;
            }
            return 0xFF00FF00; // 默认绿色
        } catch (Exception e) {
            return 0xFF00FF00;
        }
    }

    private String formatPos(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
