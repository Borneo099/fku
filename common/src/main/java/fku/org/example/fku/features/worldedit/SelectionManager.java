package fku.org.example.fku.features.worldedit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fku.org.example.fku.features.worldedit.WorldEditConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class SelectionManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final SelectionManager INSTANCE = new SelectionManager();
    private BlockPos pos1;
    private BlockPos pos2;
    private boolean hasPos1 = false;
    private boolean hasPos2 = false;

    public static SelectionManager getInstance() {
        return INSTANCE;
    }

    private SelectionManager() {
    }

    public void setPos1(BlockPos pos) {
        this.pos1 = pos;
        this.hasPos1 = true;
        if (SelectionManager.mc.player != null) {
            SelectionManager.mc.player.m_5661_(Component.literal((String)("\u00a77[WorldEdit] \u00a7aPos1 \u5df2\u8bbe\u7f6e: " + this.formatPos(pos))), true);
            this.showSelectionInfo();
        }
    }

    public void setPos2(BlockPos pos) {
        this.pos2 = pos;
        this.hasPos2 = true;
        if (SelectionManager.mc.player != null) {
            SelectionManager.mc.player.m_5661_(Component.literal((String)("\u00a77[WorldEdit] \u00a7aPos2 \u5df2\u8bbe\u7f6e: " + this.formatPos(pos))), true);
            this.showSelectionInfo();
        }
    }

    public void clearSelection() {
        this.pos1 = null;
        this.pos2 = null;
        this.hasPos1 = false;
        this.hasPos2 = false;
        if (SelectionManager.mc.player != null) {
            SelectionManager.mc.player.m_5661_(Component.literal((String)"\u00a77[WorldEdit] \u00a7e\u9009\u533a\u5df2\u6e05\u9664"), true);
        }
    }

    private void showSelectionInfo() {
        if (!this.hasPos1 || !this.hasPos2) {
            return;
        }
        int dx = Math.abs(this.pos1.m_123341_() - this.pos2.m_123341_()) + 1;
        int dy = Math.abs(this.pos1.m_123342_() - this.pos2.m_123342_()) + 1;
        int dz = Math.abs(this.pos1.m_123343_() - this.pos2.m_123343_()) + 1;
        long volume = dx * dy * dz;
        if (SelectionManager.mc.player != null) {
            SelectionManager.mc.player.m_5661_(Component.literal((String)("\u00a77[WorldEdit] \u00a7e\u9009\u533a: " + dx + "\u00d7" + dy + "\u00d7" + dz + " = " + volume + " \u65b9\u5757")), true);
        }
    }

    public BlockPos getPos1() {
        return this.pos1;
    }

    public BlockPos getPos2() {
        return this.pos2;
    }

    public boolean hasPos1() {
        return this.hasPos1;
    }

    public boolean hasPos2() {
        return this.hasPos2;
    }

    public boolean hasSelection() {
        return this.hasPos1 && this.hasPos2 && this.pos1 != null && this.pos2 != null;
    }

    public BlockPos getMin() {
        if (!this.hasSelection()) {
            return null;
        }
        return new BlockPos(Math.min(this.pos1.m_123341_(), this.pos2.m_123341_()), Math.min(this.pos1.m_123342_(), this.pos2.m_123342_()), Math.min(this.pos1.m_123343_(), this.pos2.m_123343_()));
    }

    public BlockPos getMax() {
        if (!this.hasSelection()) {
            return null;
        }
        return new BlockPos(Math.max(this.pos1.m_123341_(), this.pos2.m_123341_()), Math.max(this.pos1.m_123342_(), this.pos2.m_123342_()), Math.max(this.pos1.m_123343_(), this.pos2.m_123343_()));
    }

    public long getVolume() {
        if (!this.hasSelection()) {
            return 0L;
        }
        int dx = Math.abs(this.pos1.m_123341_() - this.pos2.m_123341_()) + 1;
        int dy = Math.abs(this.pos1.m_123342_() - this.pos2.m_123342_()) + 1;
        int dz = Math.abs(this.pos1.m_123343_() - this.pos2.m_123343_()) + 1;
        return dx * dy * dz;
    }

    public void renderSelection(PoseStack poseStack, float partialTick) {
        if (!this.hasSelection() || !WorldEditConfig.getInstance().renderSelection) {
            return;
        }
        if (SelectionManager.mc.f_91073_ == null || SelectionManager.mc.player == null) {
            return;
        }
        BlockPos min = this.getMin();
        BlockPos max = this.getMax();
        if (min == null || max == null) {
            return;
        }
        Vec3 camPos = SelectionManager.mc.f_91063_.m_109153_().getPosition();
        float x1 = (min.m_123341_() - camPos.x);
        float y1 = (min.m_123342_() - camPos.y);
        float z1 = (min.m_123343_() - camPos.z);
        float x2 = ((max.m_123341_() + 1) - camPos.x);
        float y2 = ((max.m_123342_() + 1) - camPos.y);
        float z2 = ((max.m_123343_() + 1) - camPos.z);
        int color = this.parseColor(WorldEditConfig.getInstance().selectionColor);
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = 0.8f;
        this.addLine(matrix, buffer, x1, y1, z1, x2, y1, z1, r, g, b, a);
        this.addLine(matrix, buffer, x2, y1, z1, x2, y1, z2, r, g, b, a);
        this.addLine(matrix, buffer, x2, y1, z2, x1, y1, z2, r, g, b, a);
        this.addLine(matrix, buffer, x1, y1, z2, x1, y1, z1, r, g, b, a);
        this.addLine(matrix, buffer, x1, y2, z1, x2, y2, z1, r, g, b, a);
        this.addLine(matrix, buffer, x2, y2, z1, x2, y2, z2, r, g, b, a);
        this.addLine(matrix, buffer, x2, y2, z2, x1, y2, z2, r, g, b, a);
        this.addLine(matrix, buffer, x1, y2, z2, x1, y2, z1, r, g, b, a);
        this.addLine(matrix, buffer, x1, y1, z1, x1, y2, z1, r, g, b, a);
        this.addLine(matrix, buffer, x2, y1, z1, x2, y2, z1, r, g, b, a);
        this.addLine(matrix, buffer, x2, y1, z2, x2, y2, z2, r, g, b, a);
        this.addLine(matrix, buffer, x1, y1, z2, x1, y2, z2, r, g, b, a);
        tesselator.m_85914_();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void addLine(Matrix4f matrix, BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        buffer.vertex(matrix, x1, y1, z1).m_85950_(r, g, b, a).endVertex();
        buffer.vertex(matrix, x2, y2, z2).m_85950_(r, g, b, a).endVertex();
    }

    private int parseColor(String color) {
        try {
            if (color.startsWith("#")) {
                return Integer.parseInt(color.substring(1), 16) | 0xFF000000;
            }
            return -16711936;
        }
        catch (Exception e) {
            return -16711936;
        }
    }

    private String formatPos(BlockPos pos) {
        return pos.m_123341_() + ", " + pos.m_123342_() + ", " + pos.m_123343_();
    }
}

