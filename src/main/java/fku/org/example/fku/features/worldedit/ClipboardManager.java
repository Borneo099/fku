package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 剪贴板管理器 — 复制/粘贴/NBT保存
 *
 * 设计思想：
 * - 复制时保存 BlockState + BlockEntity NBT
 * - .schematic 文件使用 NBT 格式保存（简化版 Sponge 格式）
 * - 粘贴支持相对于原点的位置偏移
 */
public class ClipboardManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ClipboardManager INSTANCE = new ClipboardManager();

    private List<BlockPos> copiedPositions = new ArrayList<>();
    private List<BlockState> copiedStates = new ArrayList<>();
    private List<CompoundTag> copiedBlockEntityData = new ArrayList<>();
    private BlockPos origin; // 复制时的原点（pos1）
    private boolean hasClipboard = false;

    public static ClipboardManager getInstance() { return INSTANCE; }

    private ClipboardManager() {}

    /**
     * 复制选区内的方块
     */
    public boolean copySelection() {
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            sendMessage("§c请先设置选区 (//wand 左键Pos1 右键Pos2)");
            return false;
        }
        if (!WorldEditConfig.getInstance().enableClipboard) {
            sendMessage("§c剪贴板功能已禁用");
            return false;
        }

        BlockPos min = sel.getMin();
        BlockPos max = sel.getMax();
        if (min == null || max == null) return false;

        copiedPositions.clear();
        copiedStates.clear();
        copiedBlockEntityData.clear();
        origin = min;

        int count = 0;
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (mc.level == null) continue;
                    BlockState state = mc.level.getBlockState(pos);
                    if (state.isAir()) continue; // 跳过空气

                    copiedPositions.add(pos);
                    copiedStates.add(state);

                    // 保存 BlockEntity NBT
                    BlockEntity be = mc.level.getBlockEntity(pos);
                    if (be != null) {
                        CompoundTag tag = be.saveWithFullMetadata();
                        copiedBlockEntityData.add(tag);
                    } else {
                        copiedBlockEntityData.add(new CompoundTag());
                    }
                    count++;
                }
            }
        }

        hasClipboard = true;
        sendMessage("§a已复制 §7" + count + " 个方块到剪贴板");
        return true;
    }

    /**
     * 粘贴剪贴板内容到指定位置
     */
    public boolean paste(BlockPos targetOrigin) {
        if (!hasClipboard || copiedPositions.isEmpty()) {
            sendMessage("§c剪贴板为空，请先 //copy");
            return false;
        }

        List<BlockPos> pastePositions = new ArrayList<>();
        List<BlockState> pasteStates = new ArrayList<>();
        List<Object> pasteData = new ArrayList<>();

        int dx = targetOrigin.getX() - origin.getX();
        int dy = targetOrigin.getY() - origin.getY();
        int dz = targetOrigin.getZ() - origin.getZ();

        for (int i = 0; i < copiedPositions.size(); i++) {
            BlockPos originalPos = copiedPositions.get(i);
            BlockPos newPos = originalPos.offset(dx, dy, dz);
            pastePositions.add(newPos);
            pasteStates.add(copiedStates.get(i));
            pasteData.add(i < copiedBlockEntityData.size() ? copiedBlockEntityData.get(i) : new CompoundTag());
        }

        if (mc.player != null) {
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7[WorldEdit] §a开始粘贴 (" + pastePositions.size() + " 个方块)"),
                    true);
        }

        TaskQueue.getInstance().submitPaste(pastePositions, pasteStates, pasteData, "粘贴");
        return true;
    }

    /**
     * 保存选区为 .schematic 文件
     */
    public boolean saveSchematic(String name) {
        if (!copySelection()) return false;

        WorldEditConfig cfg = WorldEditConfig.getInstance();
        File schematicsDir;
        try {
            schematicsDir = new File(mc.gameDirectory, cfg.schematicsFolder);
        } catch (Exception e) {
            schematicsDir = new File("config/fku/schematics");
        }
        if (!schematicsDir.exists()) schematicsDir.mkdirs();

        // 构建 NBT
        CompoundTag root = new CompoundTag();
        root.putInt("Version", 2);
        root.putShort("Width", (short)(copiedPositions.isEmpty() ? 0 :
                Math.abs(copiedPositions.get(copiedPositions.size()-1).getX() - origin.getX()) + 1));
        root.putShort("Height", (short)(copiedPositions.isEmpty() ? 0 :
                Math.abs(copiedPositions.get(copiedPositions.size()-1).getY() - origin.getY()) + 1));
        root.putShort("Length", (short)(copiedPositions.isEmpty() ? 0 :
                Math.abs(copiedPositions.get(copiedPositions.size()-1).getZ() - origin.getZ()) + 1));

        // 保存方块ID和BlockEntity数据（简化版）
        int[] blockIds = new int[copiedPositions.size()];
        List<CompoundTag> tileEntities = new ArrayList<>();

        for (int i = 0; i < copiedPositions.size(); i++) {
            BlockPos pos = copiedPositions.get(i);
            BlockState state = copiedStates.get(i);
            // 计算本地坐标
            int lx = pos.getX() - origin.getX();
            int ly = pos.getY() - origin.getY();
            int lz = pos.getZ() - origin.getZ();
            int index = ly * (root.getShort("Width") * root.getShort("Length"))
                    + lz * root.getShort("Width") + lx;
            if (index < blockIds.length) {
                blockIds[index] = Block.getId(state);
            }

            if (i < copiedBlockEntityData.size() && !copiedBlockEntityData.get(i).isEmpty()) {
                CompoundTag te = copiedBlockEntityData.get(i).copy();
                te.putInt("x", pos.getX());
                te.putInt("y", pos.getY());
                te.putInt("z", pos.getZ());
                tileEntities.add(te);
            }
        }

        root.putIntArray("BlockData", blockIds);
        root.put("TileEntities", new net.minecraft.nbt.ListTag() {{
            for (CompoundTag te : tileEntities) {
                add(te);
            }
        }});

        File file = new File(schematicsDir, name.endsWith(".schematic") ? name : name + ".schematic");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            NbtIo.writeCompressed(root, fos);
            sendMessage("§a已保存: " + file.getName());
            return true;
        } catch (IOException e) {
            sendMessage("§c保存失败: " + e.getMessage());
            Fku.LOGGER.error("[WorldEdit] 保存schematic失败", e);
            return false;
        }
    }

    /**
     * 加载 .schematic 文件到剪贴板
     */
    public boolean loadSchematic(String name) {
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        File schematicsDir;
        try {
            schematicsDir = new File(mc.gameDirectory, cfg.schematicsFolder);
        } catch (Exception e) {
            schematicsDir = new File("config/fku/schematics");
        }

        File file = new File(schematicsDir, name.endsWith(".schematic") ? name : name + ".schematic");
        if (!file.exists()) {
            sendMessage("§c文件不存在: " + file.getName());
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            CompoundTag root = NbtIo.readCompressed(fis);
            short width = root.getShort("Width");
            short height = root.getShort("Height");
            short length = root.getShort("Length");
            int[] blockIds = root.getIntArray("BlockData");

            copiedPositions.clear();
            copiedStates.clear();
            copiedBlockEntityData.clear();

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int index = y * width * length + z * width + x;
                        if (index >= blockIds.length) continue;
                        BlockState state = Block.stateById(blockIds[index]);
                        if (state == null || state.isAir()) continue;

                        BlockPos pos = new BlockPos(x, y, z);
                        copiedPositions.add(pos);
                        copiedStates.add(state);

                        // 查找对应的 BlockEntity
                        CompoundTag teTag = findTileEntity(root.getList("TileEntities", 10), x, y, z);
                        copiedBlockEntityData.add(teTag != null ? teTag : new CompoundTag());
                    }
                }
            }

            origin = BlockPos.ZERO;
            hasClipboard = true;
            sendMessage("§a已加载: " + file.getName() + " (" + copiedPositions.size() + " 方块)");
            return true;
        } catch (IOException e) {
            sendMessage("§c加载失败: " + e.getMessage());
            Fku.LOGGER.error("[WorldEdit] 加载schematic失败", e);
            return false;
        }
    }

    private CompoundTag findTileEntity(net.minecraft.nbt.ListTag list, int x, int y, int z) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            if (tag.getInt("x") == x && tag.getInt("y") == y && tag.getInt("z") == z) {
                return tag;
            }
        }
        return null;
    }

    private void sendMessage(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7[WorldEdit] " + msg), true);
        }
    }

    public boolean hasClipboard() { return hasClipboard; }
    public int getClipboardSize() { return copiedPositions.size(); }
    public void clearClipboard() {
        hasClipboard = false;
        copiedPositions.clear();
        copiedStates.clear();
        copiedBlockEntityData.clear();
        origin = null;
    }
}
