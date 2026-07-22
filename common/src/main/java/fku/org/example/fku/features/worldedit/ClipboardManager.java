package fku.org.example.fku.features.worldedit;

import com.google.common.collect.ImmutableMap;
import fku.org.example.fku.Fku;
import fku.org.example.fku.features.worldedit.SelectionManager;
import fku.org.example.fku.features.worldedit.TaskQueue;
import fku.org.example.fku.features.worldedit.WorldEditConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

public class ClipboardManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ClipboardManager INSTANCE = new ClipboardManager();
    private List<BlockPos> copiedPositions = new ArrayList<BlockPos>();
    private List<BlockState> copiedStates = new ArrayList<BlockState>();
    private List<CompoundTag> copiedBlockEntityData = new ArrayList<CompoundTag>();
    private BlockPos origin;
    private int selWidth;
    private int selHeight;
    private int selLength;
    private boolean hasClipboard = false;

    public static ClipboardManager getInstance() {
        return INSTANCE;
    }

    private ClipboardManager() {
    }

    public boolean copySelection() {
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMessage("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a (//wand \u5de6\u952ePos1 \u53f3\u952ePos2)");
            return false;
        }
        if (!WorldEditConfig.getInstance().enableClipboard) {
            this.sendMessage("\u00a7c\u526a\u8d34\u677f\u529f\u80fd\u5df2\u7981\u7528");
            return false;
        }
        BlockPos min = sel.getMin();
        BlockPos max = sel.getMax();
        if (min == null || max == null) {
            return false;
        }
        this.copiedPositions.clear();
        this.copiedStates.clear();
        this.copiedBlockEntityData.clear();
        this.origin = min;
        this.selWidth = max.getX() - min.getX() + 1;
        this.selHeight = max.getY() - min.getY() + 1;
        this.selLength = max.getZ() - min.getZ() + 1;
        int count = 0;
        for (int y = min.getY(); y <= max.getY(); ++y) {
            for (int x = min.getX(); x <= max.getX(); ++x) {
                for (int z = min.getZ(); z <= max.getZ(); ++z) {
                    BlockPos pos;
                    BlockState state;
                    if (ClipboardManager.mc.level == null || (state = ClipboardManager.mc.level.getBlockState(pos = new BlockPos(x, y, z))).isAir()) continue;
                    this.copiedPositions.add(pos);
                    this.copiedStates.add(state);
                    BlockEntity be = ClipboardManager.mc.level.getBlockEntity(pos);
                    if (be != null) {
                        this.copiedBlockEntityData.add(be.saveWithFullMetadata());
                    } else {
                        this.copiedBlockEntityData.add(new CompoundTag());
                    }
                    ++count;
                }
            }
        }
        this.hasClipboard = true;
        this.sendMessage("\u00a7a\u5df2\u590d\u5236 \u00a77" + count + " \u4e2a\u65b9\u5757\u5230\u526a\u8d34\u677f");
        return true;
    }

    public boolean paste(BlockPos targetOrigin) {
        if (!this.hasClipboard || this.copiedPositions.isEmpty()) {
            this.sendMessage("\u00a7c\u526a\u8d34\u677f\u4e3a\u7a7a\uff0c\u8bf7\u5148 //copy");
            return false;
        }
        ArrayList<BlockPos> pastePositions = new ArrayList<BlockPos>();
        ArrayList<BlockState> pasteStates = new ArrayList<BlockState>();
        ArrayList<Object> pasteData = new ArrayList<Object>();
        int dx = targetOrigin.getX() - this.origin.getX();
        int dy = targetOrigin.getY() - this.origin.getY();
        int dz = targetOrigin.getZ() - this.origin.getZ();
        for (int i = 0; i < this.copiedPositions.size(); ++i) {
            BlockPos originalPos = this.copiedPositions.get(i);
            BlockPos newPos = originalPos.offset(dx, dy, dz);
            pastePositions.add(newPos);
            pasteStates.add(this.copiedStates.get(i));
            pasteData.add(i < this.copiedBlockEntityData.size() ? this.copiedBlockEntityData.get(i) : new CompoundTag());
        }
        this.sendMessage("\u00a7a\u5f00\u59cb\u7c98\u8d34 (" + pastePositions.size() + " \u4e2a\u65b9\u5757)");
        TaskQueue.getInstance().submitPaste(pastePositions, pasteStates, pasteData, "\u7c98\u8d34");
        return true;
    }

    public boolean saveSchematic(String name) {
        boolean bl;
        File schematicsDir;
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMessage("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a");
            return false;
        }
        BlockPos min = sel.getMin();
        BlockPos max = sel.getMax();
        if (min == null || max == null) {
            return false;
        }
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        try {
            schematicsDir = new File(ClipboardManager.mc.gameDirectory, cfg.schematicsFolder);
        }
        catch (Exception e) {
            schematicsDir = new File("config/fku/schematics");
        }
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
        }
        int w = max.getX() - min.getX() + 1;
        int h = max.getY() - min.getY() + 1;
        int l = max.getZ() - min.getZ() + 1;
        HashMap<String, Integer> palette = new HashMap<String, Integer>();
        ArrayList<BlockState> paletteList = new ArrayList<BlockState>();
        for (int y = min.getY(); y <= max.getY(); ++y) {
            for (int x = min.getX(); x <= max.getX(); ++x) {
                for (int z = min.getZ(); z <= max.getZ(); ++z) {
                    String key;
                    BlockState state;
                    if (ClipboardManager.mc.level == null || (state = ClipboardManager.mc.level.getBlockState(new BlockPos(x, y, z))).isAir() || palette.containsKey(key = this.stateToString(state))) continue;
                    palette.put(key, palette.size());
                    paletteList.add(state);
                }
            }
        }
        if (!palette.containsKey("minecraft:air")) {
            palette.put("minecraft:air", palette.size());
            paletteList.add(Blocks.AIR.defaultBlockState());
        }
        int[] blockData = new int[w * h * l];
        ArrayList<CompoundTag> tileEntitiesList = new ArrayList<CompoundTag>();
        for (int y = 0; y < h; ++y) {
            for (int z = 0; z < l; ++z) {
                for (int x = 0; x < w; ++x) {
                    int index = y * w * l + z * w + x;
                    BlockPos pos = min.offset(x, y, z);
                    if (ClipboardManager.mc.level == null) continue;
                    BlockState state = ClipboardManager.mc.level.getBlockState(pos);
                    String key = this.stateToString(state);
                    blockData[index] = palette.getOrDefault(key, 0);
                    BlockEntity be = ClipboardManager.mc.level.getBlockEntity(pos);
                    if (be == null) continue;
                    CompoundTag te = be.saveWithFullMetadata();
                    te.putInt("x", x);
                    te.putInt("y", y);
                    te.putInt("z", z);
                    tileEntitiesList.add(te);
                }
            }
        }
        CompoundTag root = new CompoundTag();
        root.putInt("Version", 2);
        root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        root.putShort("Width", (short)w);
        root.putShort("Height", (short)h);
        root.putShort("Length", (short)l);
        CompoundTag paletteTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : palette.entrySet()) {
            paletteTag.putInt(entry.getKey(), entry.getValue());
        }
        root.put("Palette", paletteTag);
        root.putIntArray("BlockData", blockData);
        ListTag teList = new ListTag();
        for (CompoundTag te : tileEntitiesList) {
            teList.add(te);
        }
        root.put("BlockEntities", teList);
        File file = new File(schematicsDir, name.endsWith(".schematic") ? name : name + ".schematic");
        FileOutputStream fos = new FileOutputStream(file);
        try {
            NbtIo.writeCompressed((CompoundTag)root, (OutputStream)fos);
            this.sendMessage("\u00a7a\u5df2\u4fdd\u5b58: " + file.getName() + " (" + blockData.length + " \u65b9\u5757)");
            bl = true;
        }
        catch (Throwable throwable) {
            try {
                try {
                    fos.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                this.sendMessage("\u00a7c\u4fdd\u5b58\u5931\u8d25: " + e.getMessage());
                Fku.LOGGER.error("[WorldEdit] \u4fdd\u5b58schematic\u5931\u8d25", (Throwable)e);
                return false;
            }
        }
        fos.close();
        return bl;
    }

    public boolean loadSchematic(String name) {
        boolean bl;
        File schematicsDir;
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        try {
            schematicsDir = new File(ClipboardManager.mc.gameDirectory, cfg.schematicsFolder);
        }
        catch (Exception e) {
            schematicsDir = new File("config/fku/schematics");
        }
        File file = new File(schematicsDir, name.endsWith(".schematic") ? name : name + ".schematic");
        if (!file.exists()) {
            this.sendMessage("\u00a7c\u6587\u4ef6\u4e0d\u5b58\u5728: " + file.getName());
            return false;
        }
        FileInputStream fis = new FileInputStream(file);
        try {
            CompoundTag root = NbtIo.readCompressed((InputStream)fis);
            int w = root.getShort("Width");
            int h = root.getShort("Height");
            int l = root.getShort("Length");
            int[] blockData = root.getIntArray("BlockData");
            CompoundTag paletteTag = root.getCompound("Palette");
            HashMap<Integer, String> reversePalette = new HashMap<Integer, String>();
            for (String key : paletteTag.getAllKeys()) {
                reversePalette.put(paletteTag.getInt(key), key);
            }
            HashMap<BlockPos, CompoundTag> teMap = new HashMap<BlockPos, CompoundTag>();
            if (root.contains("BlockEntities", 9)) {
                ListTag teList = root.getList("BlockEntities", 10);
                for (int i = 0; i < teList.size(); ++i) {
                    CompoundTag te = teList.getCompound(i);
                    BlockPos tePos = new BlockPos(te.getInt("x"), te.getInt("y"), te.getInt("z"));
                    te.remove("x");
                    te.remove("y");
                    te.remove("z");
                    teMap.put(tePos, te);
                }
            }
            this.copiedPositions.clear();
            this.copiedStates.clear();
            this.copiedBlockEntityData.clear();
            for (int y = 0; y < h; ++y) {
                for (int z = 0; z < l; ++z) {
                    for (int x = 0; x < w; ++x) {
                        int paletteIndex;
                        String stateStr;
                        BlockState state;
                        int index = y * w * l + z * w + x;
                        if (index >= blockData.length || (state = this.stringToState(stateStr = reversePalette.getOrDefault(paletteIndex = blockData[index], "minecraft:air"))) == null || state.isAir()) continue;
                        BlockPos pos = new BlockPos(x, y, z);
                        this.copiedPositions.add(pos);
                        this.copiedStates.add(state);
                        BlockPos teKey = new BlockPos(x, y, z);
                        if (teMap.containsKey(teKey)) {
                            this.copiedBlockEntityData.add(((CompoundTag)teMap.get(teKey)).copy());
                            continue;
                        }
                        this.copiedBlockEntityData.add(new CompoundTag());
                    }
                }
            }
            this.origin = BlockPos.ZERO;
            this.selWidth = w;
            this.selHeight = h;
            this.selLength = l;
            this.hasClipboard = true;
            this.sendMessage("\u00a7a\u5df2\u52a0\u8f7d: " + file.getName() + " (" + this.copiedPositions.size() + " \u65b9\u5757)");
            bl = true;
        }
        catch (Throwable throwable) {
            try {
                try {
                    fis.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                this.sendMessage("\u00a7c\u52a0\u8f7d\u5931\u8d25: " + e.getMessage());
                Fku.LOGGER.error("[WorldEdit] \u52a0\u8f7dschematic\u5931\u8d25", (Throwable)e);
                return false;
            }
        }
        fis.close();
        return bl;
    }

    private String stateToString(BlockState state) {
        StringBuilder sb = new StringBuilder();
        sb.append(ForgeRegistries.BLOCKS.getKey(state.getBlock()));
        ImmutableMap<Property<?>, Comparable<?>> values = state.getValues();
        if (!values.isEmpty()) {
            sb.append("[");
            boolean first = true;
            for (Map.Entry<Property<?>, Comparable<?>> entry : values.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(entry.getKey().getName()).append("=").append(entry.getValue().toString());
                first = false;
            }
            sb.append("]");
        }
        return sb.toString();
    }

    private BlockState stringToState(String str) {
        try {
            String propertiesPart;
            Block block;
            String blockId = str;
            int bracket = str.indexOf(91);
            if (bracket >= 0) {
                blockId = str.substring(0, bracket);
            }
            if ((block = (Block)ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse((String)blockId))) == null) {
                return null;
            }
            BlockState state = block.defaultBlockState();
            if (bracket >= 0 && !(propertiesPart = str.substring(bracket + 1, str.length() - 1)).isEmpty()) {
                for (String prop : propertiesPart.split(",")) {
                    Optional opt;
                    Property propDef;
                    String[] kv = prop.split("=", 2);
                    if (kv.length != 2 || (propDef = block.getStateDefinition().getProperty(kv[0])) == null || !(opt = propDef.getValue(kv[1])).isPresent()) continue;
                    state = (BlockState)state.setValue(propDef, (Comparable)opt.get());
                }
            }
            return state;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static float[] getPlacementYawPitch(BlockState state) {
        Direction facing = null;
        if (state.hasProperty((Property)BlockStateProperties.FACING)) {
            facing = (Direction)state.getValue((Property)BlockStateProperties.FACING);
        } else if (state.hasProperty((Property)BlockStateProperties.HORIZONTAL_FACING)) {
            facing = (Direction)state.getValue((Property)BlockStateProperties.HORIZONTAL_FACING);
        } else if (state.hasProperty((Property)BlockStateProperties.FACING_HOPPER)) {
            facing = (Direction)state.getValue((Property)BlockStateProperties.FACING_HOPPER);
        }
        if (facing == null) {
            return new float[]{Float.NaN, Float.NaN};
        }
        float yaw = switch (facing) {
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            case NORTH -> 180.0f;
            case EAST -> -90.0f;
            case DOWN -> Float.NaN;
            case UP -> Float.NaN;
            default -> Float.NaN;
        };
        float pitch = 0.0f;
        if (facing == Direction.UP) {
            pitch = -90.0f;
        } else if (facing == Direction.DOWN) {
            pitch = 90.0f;
        }
        return new float[]{yaw, pitch};
    }

    public static Direction getPlacementFace(BlockState state) {
        if (state.hasProperty((Property)BlockStateProperties.FACING)) {
            return ((Direction)state.getValue((Property)BlockStateProperties.FACING)).getOpposite();
        }
        if (state.hasProperty((Property)BlockStateProperties.HORIZONTAL_FACING)) {
            return (Direction)state.getValue((Property)BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.UP;
    }

    private void sendMessage(String msg) {
        if (ClipboardManager.mc.player != null) {
            ClipboardManager.mc.player.displayClientMessage(Component.literal((String)("\u00a77[WorldEdit] " + msg)), true);
        }
    }

    public boolean hasClipboard() {
        return this.hasClipboard;
    }

    public int getClipboardSize() {
        return this.copiedPositions.size();
    }

    public void clearClipboard() {
        this.hasClipboard = false;
        this.copiedPositions.clear();
        this.copiedStates.clear();
        this.copiedBlockEntityData.clear();
        this.origin = null;
    }
}

