package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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
 * 使用 Sponge Schematic v2 格式保存结构文件
 * 复制时保存完整 BlockState + BlockEntity NBT
 * 粘贴时根据 BlockState 属性计算正确的放置朝向
 */
public class ClipboardManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ClipboardManager INSTANCE = new ClipboardManager();

    private List<BlockPos> copiedPositions = new ArrayList<>();
    private List<BlockState> copiedStates = new ArrayList<>();
    private List<CompoundTag> copiedBlockEntityData = new ArrayList<>();
    private BlockPos origin;
    private int selWidth, selHeight, selLength;
    private boolean hasClipboard = false;

    public static ClipboardManager getInstance() { return INSTANCE; }

    private ClipboardManager() {}

    /**
     * 复制选区内的方块 — 保存完整 BlockState + NBT
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
        selWidth = max.getX() - min.getX() + 1;
        selHeight = max.getY() - min.getY() + 1;
        selLength = max.getZ() - min.getZ() + 1;

        int count = 0;
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    if (mc.level == null) continue;
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.level.getBlockState(pos);
                    if (state.isAir()) continue;

                    copiedPositions.add(pos);
                    copiedStates.add(state);

                    BlockEntity be = mc.level.getBlockEntity(pos);
                    if (be != null) {
                        copiedBlockEntityData.add(be.saveWithFullMetadata(mc.level.registryAccess()));
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
     * 粘贴 — 保存完整 BlockState 供 TaskQueue 按朝向放置
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

        sendMessage("§a开始粘贴 (" + pastePositions.size() + " 个方块)");
        TaskQueue.getInstance().submitPaste(pastePositions, pasteStates, pasteData, "粘贴");
        return true;
    }

    // ═══════════════════ Sponge Schematic v2 ═══════════════════

    /**
     * 保存选区为 .schematic 文件（Sponge v2 格式）
     */
    public boolean saveSchematic(String name) {
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            sendMessage("§c请先设置选区");
            return false;
        }
        BlockPos min = sel.getMin();
        BlockPos max = sel.getMax();
        if (min == null || max == null) return false;

        WorldEditConfig cfg = WorldEditConfig.getInstance();
        File schematicsDir;
        try {
            schematicsDir = new File(mc.gameDirectory, cfg.schematicsFolder);
        } catch (Exception e) {
            schematicsDir = new File("config/fku/schematics");
        }
        if (!schematicsDir.exists()) schematicsDir.mkdirs();

        short w = (short)(max.getX() - min.getX() + 1);
        short h = (short)(max.getY() - min.getY() + 1);
        short l = (short)(max.getZ() - min.getZ() + 1);

        // 构建调色板：BlockState → 索引
        Map<String, Integer> palette = new HashMap<>();
        List<BlockState> paletteList = new ArrayList<>();

        // 第一遍：收集所有 unique block state
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    if (mc.level == null) continue;
                    BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
                    if (state.isAir()) continue;
                    String key = stateToString(state);
                    if (!palette.containsKey(key)) {
                        palette.put(key, palette.size());
                        paletteList.add(state);
                    }
                }
            }
        }
        // 确保空气在调色板中（索引0）
        if (!palette.containsKey("minecraft:air")) {
            palette.put("minecraft:air", palette.size());
            paletteList.add(Blocks.AIR.defaultBlockState());
        }

        // 第二遍：写入 BlockData（XYZ 顺序）
        int[] blockData = new int[w * h * l];
        List<CompoundTag> tileEntitiesList = new ArrayList<>();

        for (int y = 0; y < h; y++) {
            for (int z = 0; z < l; z++) {
                for (int x = 0; x < w; x++) {
                    int index = y * w * l + z * w + x;
                    BlockPos pos = min.offset(x, y, z);
                    if (mc.level == null) continue;
                    BlockState state = mc.level.getBlockState(pos);
                    String key = stateToString(state);
                    blockData[index] = palette.getOrDefault(key, 0);

                    // 保存 BlockEntity
                    BlockEntity be = mc.level.getBlockEntity(pos);
                    if (be != null) {
                        CompoundTag te = be.saveWithFullMetadata(mc.level.registryAccess());
                        te.putInt("x", x);
                        te.putInt("y", y);
                        te.putInt("z", z);
                        tileEntitiesList.add(te);
                    }
                }
            }
        }

        // 构建 NBT
        CompoundTag root = new CompoundTag();
        root.putInt("Version", 2);
        root.putInt("DataVersion", net.minecraft.SharedConstants.getCurrentVersion().dataVersion().version());
        root.putShort("Width", w);
        root.putShort("Height", h);
        root.putShort("Length", l);

        // 调色板
        CompoundTag paletteTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : palette.entrySet()) {
            paletteTag.putInt(entry.getKey(), entry.getValue());
        }
        root.put("Palette", paletteTag);
        root.putIntArray("BlockData", blockData);

        // BlockEntities
        ListTag teList = new ListTag();
        for (CompoundTag te : tileEntitiesList) {
            teList.add(te);
        }
        root.put("BlockEntities", teList);

        // 写入文件
        File file = new File(schematicsDir, name.endsWith(".schematic") ? name : name + ".schematic");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            NbtIo.writeCompressed(root, fos);
            sendMessage("§a已保存: " + file.getName() + " (" + blockData.length + " 方块)");
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
            CompoundTag root = NbtIo.readCompressed(fis, NbtAccounter.unlimitedHeap());
            short w = root.getShortOr("Width", (short)0);
            short h = root.getShortOr("Height", (short)0);
            short l = root.getShortOr("Length", (short)0);
            int[] blockData = root.getIntArray("BlockData").orElse(new int[0]);

            // 读取调色板
            CompoundTag paletteTag = root.getCompoundOrEmpty("Palette");
            Map<Integer, String> reversePalette = new HashMap<>();
            for (String key : paletteTag.keySet()) {
                reversePalette.put(paletteTag.getIntOr(key, 0), key);
            }

            // 解析 BlockEntities
            Map<BlockPos, CompoundTag> teMap = new HashMap<>();
            if (root.contains("BlockEntities")) {
                ListTag teList = root.getListOrEmpty("BlockEntities");
                for (int i = 0; i < teList.size(); i++) {
                    CompoundTag te = teList.getCompoundOrEmpty(i);
                    BlockPos tePos = new BlockPos(te.getIntOr("x", 0), te.getIntOr("y", 0), te.getIntOr("z", 0));
                    te.remove("x"); te.remove("y"); te.remove("z");
                    teMap.put(tePos, te);
                }
            }

            copiedPositions.clear();
            copiedStates.clear();
            copiedBlockEntityData.clear();

            for (int y = 0; y < h; y++) {
                for (int z = 0; z < l; z++) {
                    for (int x = 0; x < w; x++) {
                        int index = y * w * l + z * w + x;
                        if (index >= blockData.length) continue;
                        int paletteIndex = blockData[index];
                        String stateStr = reversePalette.getOrDefault(paletteIndex, "minecraft:air");
                        BlockState state = stringToState(stateStr);
                        if (state == null || state.isAir()) continue;

                        BlockPos pos = new BlockPos(x, y, z);
                        copiedPositions.add(pos);
                        copiedStates.add(state);

                        BlockPos teKey = new BlockPos(x, y, z);
                        if (teMap.containsKey(teKey)) {
                            copiedBlockEntityData.add(teMap.get(teKey).copy());
                        } else {
                            copiedBlockEntityData.add(new CompoundTag());
                        }
                    }
                }
            }

            origin = BlockPos.ZERO;
            selWidth = w;
            selHeight = h;
            selLength = l;
            hasClipboard = true;
            sendMessage("§a已加载: " + file.getName() + " (" + copiedPositions.size() + " 方块)");
            return true;
        } catch (IOException e) {
            sendMessage("§c加载失败: " + e.getMessage());
            Fku.LOGGER.error("[WorldEdit] 加载schematic失败", e);
            return false;
        }
    }

    // ═══════════════════ BlockState 序列化 ═══════════════════

    /**
     * BlockState → 字符串 (如 "minecraft:oak_stairs[facing=east,half=bottom]")
     */
    private String stateToString(BlockState state) {
        StringBuilder sb = new StringBuilder();
        sb.append(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        var values = state.getValues();
        if (!values.isEmpty()) {
            sb.append("[");
            boolean first = true;
            for (var entry : values.entrySet()) {
                if (!first) sb.append(",");
                sb.append(entry.getKey().getName()).append("=").append(entry.getValue().toString());
                first = false;
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * 字符串 → BlockState
     * 使用 Minecraft 内置的 BlockState 解析
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private BlockState stringToState(String str) {
        try {
            String blockId = str;
            int bracket = str.indexOf('[');
            if (bracket >= 0) {
                blockId = str.substring(0, bracket);
            }
            Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(
                    net.minecraft.resources.ResourceLocation.tryParse(blockId));
            if (block == null) return null;
            
            var state = block.defaultBlockState();
            if (bracket >= 0) {
                String propertiesPart = str.substring(bracket + 1, str.length() - 1);
                if (!propertiesPart.isEmpty()) {
                    for (String prop : propertiesPart.split(",")) {
                        String[] kv = prop.split("=", 2);
                        if (kv.length != 2) continue;
                        var propDef = block.getStateDefinition().getProperty(kv[0]);
                        if (propDef != null) {
                            var opt = propDef.getValue(kv[1]);
                            if (opt.isPresent()) {
                                state = state.setValue((net.minecraft.world.level.block.state.properties.Property) propDef, 
                                    (java.lang.Comparable) opt.get());
                            }
                        }
                    }
                }
            }
            return state;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据 BlockState 计算放置时的朝向（yaw偏转角、点击面）
     * 用于 TaskQueue 的粘贴操作
     */
    public static float[] getPlacementYawPitch(BlockState state) {
        // 优先使用 FACING 属性
        Direction facing = null;
        if (state.hasProperty(BlockStateProperties.FACING)) {
            facing = state.getValue(BlockStateProperties.FACING);
        } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        } else if (state.hasProperty(BlockStateProperties.FACING_HOPPER)) {
            facing = state.getValue(BlockStateProperties.FACING_HOPPER);
        }

        if (facing == null) {
            return new float[]{Float.NaN, Float.NaN}; // 不需要特殊朝向
        }

        float yaw;
        switch (facing) {
            case SOUTH: yaw = 0; break;
            case WEST:  yaw = 90; break;
            case NORTH: yaw = 180; break;
            case EAST:  yaw = -90; break;
            case DOWN:  yaw = Float.NaN; break; // 竖直方向由 pitch 控制
            case UP:    yaw = Float.NaN; break;
            default:    yaw = Float.NaN; break;
        }

        float pitch = 0;
        if (facing == Direction.UP) pitch = -90;
        else if (facing == Direction.DOWN) pitch = 90;

        return new float[]{yaw, pitch};
    }

    /**
     * 获取放置时应该点击的方块面
     */
    public static Direction getPlacementFace(BlockState state) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING).getOpposite();
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.UP;
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
