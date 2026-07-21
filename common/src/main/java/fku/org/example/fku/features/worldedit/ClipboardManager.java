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
        this.selWidth = max.m_123341_() - min.m_123341_() + 1;
        this.selHeight = max.m_123342_() - min.m_123342_() + 1;
        this.selLength = max.m_123343_() - min.m_123343_() + 1;
        int count = 0;
        for (int y = min.m_123342_(); y <= max.m_123342_(); ++y) {
            for (int x = min.m_123341_(); x <= max.m_123341_(); ++x) {
                for (int z = min.m_123343_(); z <= max.m_123343_(); ++z) {
                    BlockPos pos;
                    BlockState state;
                    if (ClipboardManager.mc.f_91073_ == null || (state = ClipboardManager.mc.f_91073_.m_8055_(pos = new BlockPos(x, y, z))).m_60795_()) continue;
                    this.copiedPositions.add(pos);
                    this.copiedStates.add(state);
                    BlockEntity be = ClipboardManager.mc.f_91073_.m_7702_(pos);
                    if (be != null) {
                        this.copiedBlockEntityData.add(be.m_187480_());
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
        int dx = targetOrigin.m_123341_() - this.origin.m_123341_();
        int dy = targetOrigin.m_123342_() - this.origin.m_123342_();
        int dz = targetOrigin.m_123343_() - this.origin.m_123343_();
        for (int i = 0; i < this.copiedPositions.size(); ++i) {
            BlockPos originalPos = this.copiedPositions.get(i);
            BlockPos newPos = originalPos.m_7918_(dx, dy, dz);
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
        int w = max.m_123341_() - min.m_123341_() + 1;
        int h = max.m_123342_() - min.m_123342_() + 1;
        int l = max.m_123343_() - min.m_123343_() + 1;
        HashMap<String, Integer> palette = new HashMap<String, Integer>();
        ArrayList<BlockState> paletteList = new ArrayList<BlockState>();
        for (int y = min.m_123342_(); y <= max.m_123342_(); ++y) {
            for (int x = min.m_123341_(); x <= max.m_123341_(); ++x) {
                for (int z = min.m_123343_(); z <= max.m_123343_(); ++z) {
                    String key;
                    BlockState state;
                    if (ClipboardManager.mc.f_91073_ == null || (state = ClipboardManager.mc.f_91073_.m_8055_(new BlockPos(x, y, z))).m_60795_() || palette.containsKey(key = this.stateToString(state))) continue;
                    palette.put(key, palette.size());
                    paletteList.add(state);
                }
            }
        }
        if (!palette.containsKey("minecraft:air")) {
            palette.put("minecraft:air", palette.size());
            paletteList.add(Blocks.f_50016_.m_49966_());
        }
        int[] blockData = new int[w * h * l];
        ArrayList<CompoundTag> tileEntitiesList = new ArrayList<CompoundTag>();
        for (int y = 0; y < h; ++y) {
            for (int z = 0; z < l; ++z) {
                for (int x = 0; x < w; ++x) {
                    int index = y * w * l + z * w + x;
                    BlockPos pos = min.m_7918_(x, y, z);
                    if (ClipboardManager.mc.f_91073_ == null) continue;
                    BlockState state = ClipboardManager.mc.f_91073_.m_8055_(pos);
                    String key = this.stateToString(state);
                    blockData[index] = palette.getOrDefault(key, 0);
                    BlockEntity be = ClipboardManager.mc.f_91073_.m_7702_(pos);
                    if (be == null) continue;
                    CompoundTag te = be.m_187480_();
                    te.m_128405_("x", x);
                    te.m_128405_("y", y);
                    te.m_128405_("z", z);
                    tileEntitiesList.add(te);
                }
            }
        }
        CompoundTag root = new CompoundTag();
        root.m_128405_("Version", 2);
        root.m_128405_("DataVersion", SharedConstants.m_183709_().m_183476_().m_193006_());
        root.m_128376_("Width", w);
        root.m_128376_("Height", h);
        root.m_128376_("Length", l);
        CompoundTag paletteTag = new CompoundTag();
        for (Map.Entry entry : palette.entrySet()) {
            paletteTag.m_128405_((String)entry.getKey(), ((Integer)entry.getValue()).intValue());
        }
        root.m_128365_("Palette", (Tag)paletteTag);
        root.m_128385_("BlockData", blockData);
        ListTag teList = new ListTag();
        for (CompoundTag te : tileEntitiesList) {
            teList.add(te);
        }
        root.m_128365_("BlockEntities", (Tag)teList);
        File file = new File(schematicsDir, (String)(name.endsWith(".schematic") ? name : name + ".schematic"));
        FileOutputStream fos = new FileOutputStream(file);
        try {
            NbtIo.m_128947_((CompoundTag)root, (OutputStream)fos);
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
        File file = new File(schematicsDir, (String)(name.endsWith(".schematic") ? name : name + ".schematic"));
        if (!file.exists()) {
            this.sendMessage("\u00a7c\u6587\u4ef6\u4e0d\u5b58\u5728: " + file.getName());
            return false;
        }
        FileInputStream fis = new FileInputStream(file);
        try {
            CompoundTag root = NbtIo.m_128939_((InputStream)fis);
            int w = root.m_128448_("Width");
            int h = root.m_128448_("Height");
            int l = root.m_128448_("Length");
            int[] blockData = root.m_128465_("BlockData");
            CompoundTag paletteTag = root.m_128469_("Palette");
            HashMap<Integer, String> reversePalette = new HashMap<Integer, String>();
            for (String key : paletteTag.m_128431_()) {
                reversePalette.put(paletteTag.m_128451_(key), key);
            }
            HashMap<BlockPos, CompoundTag> teMap = new HashMap<BlockPos, CompoundTag>();
            if (root.m_128425_("BlockEntities", 9)) {
                ListTag teList = root.m_128437_("BlockEntities", 10);
                for (int i = 0; i < teList.size(); ++i) {
                    CompoundTag te = teList.m_128728_(i);
                    BlockPos tePos = new BlockPos(te.m_128451_("x"), te.m_128451_("y"), te.m_128451_("z"));
                    te.m_128473_("x");
                    te.m_128473_("y");
                    te.m_128473_("z");
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
                        if (index >= blockData.length || (state = this.stringToState(stateStr = reversePalette.getOrDefault(paletteIndex = blockData[index], "minecraft:air"))) == null || state.m_60795_()) continue;
                        BlockPos pos = new BlockPos(x, y, z);
                        this.copiedPositions.add(pos);
                        this.copiedStates.add(state);
                        BlockPos teKey = new BlockPos(x, y, z);
                        if (teMap.containsKey(teKey)) {
                            this.copiedBlockEntityData.add(((CompoundTag)teMap.get(teKey)).m_6426_());
                            continue;
                        }
                        this.copiedBlockEntityData.add(new CompoundTag());
                    }
                }
            }
            this.origin = BlockPos.f_121853_;
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
        sb.append(ForgeRegistries.BLOCKS.getKey(state.m_60734_()));
        ImmutableMap values = state.m_61148_();
        if (!values.isEmpty()) {
            sb.append("[");
            boolean first = true;
            for (Map.Entry entry : values.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(((Property)entry.getKey()).m_61708_()).append("=").append(((Comparable)entry.getValue()).toString());
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
            if ((block = (Block)ForgeRegistries.BLOCKS.getValue(ResourceLocation.m_135820_((String)blockId))) == null) {
                return null;
            }
            BlockState state = block.m_49966_();
            if (bracket >= 0 && !(propertiesPart = str.substring(bracket + 1, str.length() - 1)).isEmpty()) {
                for (String prop : propertiesPart.split(",")) {
                    Optional opt;
                    Property propDef;
                    String[] kv = prop.split("=", 2);
                    if (kv.length != 2 || (propDef = block.m_49965_().m_61081_(kv[0])) == null || !(opt = propDef.m_6215_(kv[1])).isPresent()) continue;
                    state = (BlockState)state.m_61124_(propDef, (Comparable)opt.get());
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
        if (state.m_61138_((Property)BlockStateProperties.f_61372_)) {
            facing = (Direction)state.m_61143_((Property)BlockStateProperties.f_61372_);
        } else if (state.m_61138_((Property)BlockStateProperties.f_61374_)) {
            facing = (Direction)state.m_61143_((Property)BlockStateProperties.f_61374_);
        } else if (state.m_61138_((Property)BlockStateProperties.f_61373_)) {
            facing = (Direction)state.m_61143_((Property)BlockStateProperties.f_61373_);
        }
        if (facing == null) {
            return new float[]{Float.NaN, Float.NaN};
        }
        float yaw = switch (facing) {
            case Direction.SOUTH -> 0.0f;
            case Direction.WEST -> 90.0f;
            case Direction.NORTH -> 180.0f;
            case Direction.EAST -> -90.0f;
            case Direction.DOWN -> Float.NaN;
            case Direction.UP -> Float.NaN;
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
        if (state.m_61138_((Property)BlockStateProperties.f_61372_)) {
            return ((Direction)state.m_61143_((Property)BlockStateProperties.f_61372_)).m_122424_();
        }
        if (state.m_61138_((Property)BlockStateProperties.f_61374_)) {
            return (Direction)state.m_61143_((Property)BlockStateProperties.f_61374_);
        }
        return Direction.UP;
    }

    private void sendMessage(String msg) {
        if (ClipboardManager.mc.player != null) {
            ClipboardManager.mc.player.m_5661_(Component.literal((String)("\u00a77[WorldEdit] " + msg)), true);
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

