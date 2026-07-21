package fku.org.example.fku.features.worldedit;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.worldedit.ClipboardManager;
import fku.org.example.fku.features.worldedit.HistoryManager;
import fku.org.example.fku.features.worldedit.SelectionManager;
import fku.org.example.fku.features.worldedit.ShapeGenerator;
import fku.org.example.fku.features.worldedit.TaskQueue;
import fku.org.example.fku.features.worldedit.ToolManager;
import fku.org.example.fku.features.worldedit.WorldEditConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class CommandRegistry {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final CommandRegistry INSTANCE = new CommandRegistry();
    private final Map<String, CommandHandler> commands = new HashMap<String, CommandHandler>();
    private String lastReplaceFrom = "";

    public static CommandRegistry getInstance() {
        return INSTANCE;
    }

    private CommandRegistry() {
        this.registerCommands();
    }

    private void registerCommands() {
        this.commands.put("wand", this::cmdWand);
        this.commands.put("set", this::cmdSet);
        this.commands.put("replace", this::cmdReplace);
        this.commands.put("sphere", this::cmdSphere);
        this.commands.put("cyl", this::cmdCylinder);
        this.commands.put("pyramid", this::cmdPyramid);
        this.commands.put("wall", this::cmdWall);
        this.commands.put("roof", this::cmdRoof);
        this.commands.put("copy", this::cmdCopy);
        this.commands.put("paste", this::cmdPaste);
        this.commands.put("undo", this::cmdUndo);
        this.commands.put("redo", this::cmdRedo);
        this.commands.put("expand", this::cmdExpand);
        this.commands.put("contract", this::cmdContract);
        this.commands.put("inset", this::cmdInset);
        this.commands.put("outset", this::cmdOutset);
        this.commands.put("schematic", this::cmdSchematic);
        this.commands.put("pos1", this::cmdPos1);
        this.commands.put("pos2", this::cmdPos2);
        this.commands.put("help", this::cmdHelp);
        this.commands.put("sel", this::cmdSel);
        this.commands.put("desel", this::cmdDesel);
        this.commands.put("tool", this::cmdTool);
        this.commands.put("cancel", this::cmdCancel);
        this.commands.put("pause", this::cmdPause);
        this.commands.put("resume", this::cmdResume);
    }

    public boolean execute(String input) {
        CommandHandler handler;
        if (!input.startsWith("//")) {
            return false;
        }
        String cmdLine = input.substring(2).trim();
        String[] parts = cmdLine.split("\\s+");
        if (parts.length == 0) {
            return false;
        }
        String cmdName = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        if (parts.length > 1) {
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
        }
        if ((handler = this.commands.get(cmdName)) == null) {
            this.sendMsg("\u00a7c\u672a\u77e5\u547d\u4ee4: //" + cmdName + " (\u8f93\u5165 //help \u67e5\u770b\u5e2e\u52a9)");
            return true;
        }
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        if (!(cfg.enabled || cmdName.equals("wand") || cmdName.equals("help") || cmdName.equals("sel") || cmdName.equals("desel"))) {
            this.sendMsg("\u00a7cWorldEdit \u672a\u6fc0\u6d3b\uff01\u8bf7\u5148\u6253\u5f00GUI\u5f00\u5173\u6216\u4f7f\u7528 //wand \u6fc0\u6d3b");
            return true;
        }
        try {
            handler.handle(args);
        }
        catch (Exception e) {
            this.sendMsg("\u00a7c\u6267\u884c\u547d\u4ee4\u65f6\u51fa\u9519: " + e.getMessage());
            Fku.LOGGER.error("[WorldEdit] \u547d\u4ee4\u6267\u884c\u5931\u8d25: //" + cmdName, (Throwable)e);
        }
        return true;
    }

    private void cmdWand(String[] args) {
        if (!WorldEditConfig.getInstance().enabled) {
            WorldEditConfig.getInstance().setEnabled(true);
        }
        ToolManager.getInstance().enableWand();
        this.giveToolItem();
        this.sendMsg("\u00a7a\u5de6\u952e\u9009\u62e9Pos1\uff0c\u53f3\u952e\u9009\u62e9Pos2");
    }

    private void giveToolItem() {
        int i;
        Item item;
        if (CommandRegistry.mc.player == null) {
            return;
        }
        String toolId = WorldEditConfig.getInstance().toolItem;
        Block toolBlock = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(toolId));
        if (toolBlock == null) {
            toolId = "minecraft:wooden_axe";
        }
        if ((item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(toolId))) == null) {
            return;
        }
        for (i = 0; i < 9; ++i) {
            if (CommandRegistry.mc.player.m_150109_().m_8020_(i).m_41720_() != item) continue;
            return;
        }
        for (i = 9; i < 36; ++i) {
            if (CommandRegistry.mc.player.m_150109_().m_8020_(i).m_41720_() != item) continue;
            ItemStack targetStack = CommandRegistry.mc.player.m_150109_().m_8020_(i).m_41777_();
            CommandRegistry.mc.player.m_150109_().f_35974_.set(CommandRegistry.mc.player.m_150109_().f_35977_, targetStack);
            CommandRegistry.mc.player.m_150109_().f_35974_.set(i, ItemStack.f_41583_);
            return;
        }
        if (CommandRegistry.mc.player.m_150110_().f_35937_) {
            CommandRegistry.mc.player.m_150109_().m_36054_(new ItemStack((ItemLike)item, 1));
        }
    }

    private void cmdSet(String[] args) {
        if (args.length < 1) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //set <\u65b9\u5757\u540d>");
            return;
        }
        BlockState target = this.parseBlockState(args[0]);
        if (target == null) {
            return;
        }
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMsg("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a");
            return;
        }
        List<BlockPos> positions = ShapeGenerator.cuboid(sel.getPos1(), sel.getPos2());
        TaskQueue.getInstance().submitSet(positions, target, "//set " + args[0]);
    }

    private void cmdReplace(String[] args) {
        BlockState targetState;
        if (args.length < 1) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //replace [from] <to>");
            return;
        }
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMsg("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a");
            return;
        }
        BlockState fromState = null;
        if (args.length >= 2) {
            fromState = this.parseBlockState(args[0]);
            targetState = this.parseBlockState(args[1]);
            this.lastReplaceFrom = args[0];
        } else {
            targetState = this.parseBlockState(args[0]);
        }
        if (targetState == null) {
            return;
        }
        List<BlockPos> positions = ShapeGenerator.cuboid(sel.getPos1(), sel.getPos2());
        TaskQueue.getInstance().submitReplace(positions, targetState, fromState, "//replace");
    }

    private void cmdSphere(String[] args) {
        if (args.length < 2) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //sphere <\u65b9\u5757> <\u534a\u5f84> [hollow]");
            return;
        }
        BlockState state = this.parseBlockState(args[0]);
        if (state == null) {
            return;
        }
        int radius = this.parseInt(args[1], 1);
        if (radius <= 0) {
            this.sendMsg("\u00a7c\u534a\u5f84\u5fc5\u987b\u5927\u4e8e0");
            return;
        }
        boolean hollow = args.length >= 3 && args[2].equalsIgnoreCase("hollow");
        BlockPos center = CommandRegistry.mc.player != null ? CommandRegistry.mc.player.m_20183_() : BlockPos.f_121853_;
        List<BlockPos> positions = ShapeGenerator.sphere(center, radius, hollow);
        TaskQueue.getInstance().submitSet(positions, state, hollow ? "//sphere hollow" : "//sphere");
    }

    private void cmdCylinder(String[] args) {
        if (args.length < 3) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //cyl <\u65b9\u5757> <\u534a\u5f84> <\u9ad8\u5ea6> [hollow]");
            return;
        }
        BlockState state = this.parseBlockState(args[0]);
        if (state == null) {
            return;
        }
        int radius = this.parseInt(args[1], 1);
        int height = this.parseInt(args[2], 1);
        if (radius <= 0 || height <= 0) {
            this.sendMsg("\u00a7c\u534a\u5f84\u548c\u9ad8\u5ea6\u5fc5\u987b\u5927\u4e8e0");
            return;
        }
        boolean hollow = args.length >= 4 && args[3].equalsIgnoreCase("hollow");
        BlockPos center = CommandRegistry.mc.player != null ? CommandRegistry.mc.player.m_20183_() : BlockPos.f_121853_;
        List<BlockPos> positions = ShapeGenerator.cylinder(center, radius, height, hollow);
        TaskQueue.getInstance().submitSet(positions, state, "//cyl");
    }

    private void cmdPyramid(String[] args) {
        if (args.length < 2) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //pyramid <\u65b9\u5757> <\u5927\u5c0f> [hollow]");
            return;
        }
        BlockState state = this.parseBlockState(args[0]);
        if (state == null) {
            return;
        }
        int size = this.parseInt(args[1], 1);
        if (size <= 0) {
            this.sendMsg("\u00a7c\u5927\u5c0f\u5fc5\u987b\u5927\u4e8e0");
            return;
        }
        boolean hollow = args.length >= 3 && args[2].equalsIgnoreCase("hollow");
        BlockPos baseCenter = CommandRegistry.mc.player != null ? CommandRegistry.mc.player.m_20183_() : BlockPos.f_121853_;
        List<BlockPos> positions = ShapeGenerator.pyramid(baseCenter, size, hollow);
        TaskQueue.getInstance().submitSet(positions, state, "//pyramid");
    }

    private void cmdWall(String[] args) {
        if (args.length < 1) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //wall <\u65b9\u5757>");
            return;
        }
        BlockState state = this.parseBlockState(args[0]);
        if (state == null) {
            return;
        }
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMsg("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a");
            return;
        }
        List<BlockPos> positions = ShapeGenerator.wall(sel.getPos1(), sel.getPos2());
        TaskQueue.getInstance().submitSet(positions, state, "//wall");
    }

    private void cmdRoof(String[] args) {
        if (args.length < 1) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //roof <\u65b9\u5757>");
            return;
        }
        BlockState state = this.parseBlockState(args[0]);
        if (state == null) {
            return;
        }
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMsg("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a");
            return;
        }
        List<BlockPos> positions = ShapeGenerator.roof(sel.getPos1(), sel.getPos2());
        TaskQueue.getInstance().submitSet(positions, state, "//roof");
    }

    private void cmdCopy(String[] args) {
        ClipboardManager.getInstance().copySelection();
    }

    private void cmdPaste(String[] args) {
        SelectionManager sel = SelectionManager.getInstance();
        BlockPos target = sel.hasPos1() ? sel.getPos1() : (CommandRegistry.mc.player != null ? CommandRegistry.mc.player.m_20183_() : BlockPos.f_121853_);
        ClipboardManager.getInstance().paste(target);
    }

    private void cmdUndo(String[] args) {
        HistoryManager.getInstance().undo();
    }

    private void cmdRedo(String[] args) {
        HistoryManager.getInstance().redo();
    }

    private void cmdExpand(String[] args) {
        if (args.length < 2) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //expand <\u6570\u91cf> <\u65b9\u5411(up/down/north/south/east/west)>");
            return;
        }
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMsg("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a");
            return;
        }
        int amount = this.parseInt(args[0], 1);
        Direction dir = this.parseDirection(args[1]);
        if (dir == null) {
            this.sendMsg("\u00a7c\u65e0\u6548\u65b9\u5411: " + args[1]);
            return;
        }
        BlockPos[] result = ShapeGenerator.expand(sel.getPos1(), sel.getPos2(), amount, dir);
        sel.setPos1(result[0]);
        sel.setPos2(result[1]);
    }

    private void cmdContract(String[] args) {
        if (args.length < 2) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //contract <\u6570\u91cf> <\u65b9\u5411>");
            return;
        }
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMsg("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a");
            return;
        }
        int amount = this.parseInt(args[0], 1);
        Direction dir = this.parseDirection(args[1]);
        if (dir == null) {
            this.sendMsg("\u00a7c\u65e0\u6548\u65b9\u5411: " + args[1]);
            return;
        }
        BlockPos[] result = ShapeGenerator.contract(sel.getPos1(), sel.getPos2(), amount, dir);
        if (result[0] == sel.getPos1() && result[1] == sel.getPos2()) {
            this.sendMsg("\u00a7c\u65e0\u6cd5\u6536\u7f29\uff08\u5df2\u8fbe\u6700\u5c0f\u5c3a\u5bf8\uff09");
            return;
        }
        sel.setPos1(result[0]);
        sel.setPos2(result[1]);
    }

    private void cmdInset(String[] args) {
        if (args.length < 1) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //inset <\u6570\u91cf>");
            return;
        }
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMsg("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a");
            return;
        }
        int amount = this.parseInt(args[0], 1);
        BlockPos min = sel.getMin();
        BlockPos max = sel.getMax();
        if (min == null || max == null) {
            return;
        }
        BlockPos newMin = min.m_7918_(amount, amount, amount);
        BlockPos newMax = max.m_7918_(-amount, -amount, -amount);
        if (newMin.m_123341_() > newMax.m_123341_() || newMin.m_123342_() > newMax.m_123342_() || newMin.m_123343_() > newMax.m_123343_()) {
            this.sendMsg("\u00a7c\u6536\u7f29\u540e\u9009\u533a\u4e3a\u7a7a");
            return;
        }
        sel.setPos1(newMin);
        sel.setPos2(newMax);
    }

    private void cmdOutset(String[] args) {
        if (args.length < 1) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //outset <\u6570\u91cf>");
            return;
        }
        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) {
            this.sendMsg("\u00a7c\u8bf7\u5148\u8bbe\u7f6e\u9009\u533a");
            return;
        }
        int amount = this.parseInt(args[0], 1);
        BlockPos min = sel.getMin();
        BlockPos max = sel.getMax();
        if (min == null || max == null) {
            return;
        }
        sel.setPos1(min.m_7918_(-amount, -amount, -amount));
        sel.setPos2(max.m_7918_(amount, amount, amount));
    }

    private void cmdSchematic(String[] args) {
        if (args.length < 2) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //schematic save/load <\u540d\u79f0>");
            return;
        }
        String name = args[1];
        if (args[0].equalsIgnoreCase("save")) {
            ClipboardManager.getInstance().saveSchematic(name);
        } else if (args[0].equalsIgnoreCase("load")) {
            ClipboardManager.getInstance().loadSchematic(name);
        } else {
            this.sendMsg("\u00a7c\u5b50\u547d\u4ee4: save \u6216 load");
        }
    }

    private void cmdPos1(String[] args) {
        if (CommandRegistry.mc.player == null) {
            return;
        }
        SelectionManager.getInstance().setPos1(CommandRegistry.mc.player.m_20183_());
    }

    private void cmdPos2(String[] args) {
        if (CommandRegistry.mc.player == null) {
            return;
        }
        SelectionManager.getInstance().setPos2(CommandRegistry.mc.player.m_20183_());
    }

    private void cmdHelp(String[] args) {
        this.sendMsg("\u00a76=== WorldEdit Lite \u5e2e\u52a9 ===");
        this.sendMsg("\u00a7e//wand \u00a77- \u83b7\u53d6\u9009\u533a\u5de5\u5177");
        this.sendMsg("\u00a7e//pos1 \u00a77- \u8bbe\u7f6e\u5f53\u524d\u4f4d\u7f6e\u4e3aPos1");
        this.sendMsg("\u00a7e//pos2 \u00a77- \u8bbe\u7f6e\u5f53\u524d\u4f4d\u7f6e\u4e3aPos2");
        this.sendMsg("\u00a7e//sel \u00a77- \u663e\u793a\u9009\u533a\u4fe1\u606f");
        this.sendMsg("\u00a7e//desel \u00a77- \u6e05\u9664\u9009\u533a");
        this.sendMsg("\u00a7e//set <\u65b9\u5757> \u00a77- \u586b\u5145\u9009\u533a");
        this.sendMsg("\u00a7e//replace [from] <to> \u00a77- \u66ff\u6362\u65b9\u5757");
        this.sendMsg("\u00a7e//sphere <\u65b9\u5757> <\u534a\u5f84> [hollow] \u00a77- \u7403\u4f53");
        this.sendMsg("\u00a7e//cyl <\u65b9\u5757> <\u534a\u5f84> <\u9ad8\u5ea6> [hollow] \u00a77- \u5706\u67f1");
        this.sendMsg("\u00a7e//pyramid <\u65b9\u5757> <\u5927\u5c0f> [hollow] \u00a77- \u91d1\u5b57\u5854");
        this.sendMsg("\u00a7e//wall <\u65b9\u5757> \u00a77- \u56f4\u5899");
        this.sendMsg("\u00a7e//roof <\u65b9\u5757> \u00a77- \u5c4b\u9876");
        this.sendMsg("\u00a7e//copy \u00a77- \u590d\u5236\u9009\u533a");
        this.sendMsg("\u00a7e//paste \u00a77- \u7c98\u8d34\u526a\u8d34\u677f");
        this.sendMsg("\u00a7e//undo \u00a77- \u64a4\u9500");
        this.sendMsg("\u00a7e//redo \u00a77- \u91cd\u505a");
        this.sendMsg("\u00a7e//expand <\u6570\u91cf> <\u65b9\u5411> \u00a77- \u6269\u5c55");
        this.sendMsg("\u00a7e//contract <\u6570\u91cf> <\u65b9\u5411> \u00a77- \u6536\u7f29");
        this.sendMsg("\u00a7e//inset <\u6570\u91cf> \u00a77- \u5411\u5185\u7f29\u8fdb");
        this.sendMsg("\u00a7e//outset <\u6570\u91cf> \u00a77- \u5411\u5916\u6269\u5c55");
        this.sendMsg("\u00a7e//schematic save/load <\u540d\u79f0> \u00a77- \u7ed3\u6784\u6587\u4ef6");
        this.sendMsg("\u00a7e//tool tree/remover/info \u00a77- \u5de5\u5177");
        this.sendMsg("\u00a7e//cancel \u00a77- \u53d6\u6d88\u5f53\u524d\u4efb\u52a1");
        this.sendMsg("\u00a7e//pause/resume \u00a77- \u6682\u505c/\u7ee7\u7eed");
    }

    private void cmdSel(String[] args) {
        SelectionManager sel = SelectionManager.getInstance();
        if (sel.hasSelection()) {
            BlockPos min = sel.getMin();
            BlockPos max = sel.getMax();
            if (min != null && max != null) {
                long vol = sel.getVolume();
                this.sendMsg("\u00a7e\u9009\u533a: " + min.m_123341_() + "," + min.m_123342_() + "," + min.m_123343_() + " \u2192 " + max.m_123341_() + "," + max.m_123342_() + "," + max.m_123343_());
                this.sendMsg("\u00a7e\u5927\u5c0f: " + (Math.abs(max.m_123341_() - min.m_123341_()) + 1) + "\u00d7" + (Math.abs(max.m_123342_() - min.m_123342_()) + 1) + "\u00d7" + (Math.abs(max.m_123343_() - min.m_123343_()) + 1) + " = " + vol + " \u65b9\u5757");
            }
        } else {
            this.sendMsg("\u00a7c\u672a\u8bbe\u7f6e\u9009\u533a");
        }
    }

    private void cmdDesel(String[] args) {
        SelectionManager.getInstance().clearSelection();
    }

    private void cmdTool(String[] args) {
        if (args.length < 1) {
            this.sendMsg("\u00a7c\u7528\u6cd5: //tool tree/remover/info");
            return;
        }
        if (!WorldEditConfig.getInstance().enabled) {
            WorldEditConfig.getInstance().setEnabled(true);
        }
        switch (args[0].toLowerCase()) {
            case "tree": {
                ToolManager.getInstance().setTool("tree");
                break;
            }
            case "remover": {
                ToolManager.getInstance().setTool("remover");
                break;
            }
            case "info": {
                ToolManager.getInstance().setTool("info");
                break;
            }
            default: {
                this.sendMsg("\u00a7c\u652f\u6301\u7684\u5de5\u5177: tree, remover, info");
            }
        }
    }

    private void cmdCancel(String[] args) {
        if (TaskQueue.getInstance().isRunning()) {
            TaskQueue.getInstance().cancel();
            this.sendMsg("\u00a7a\u4efb\u52a1\u5df2\u53d6\u6d88");
        } else {
            this.sendMsg("\u00a7e\u5f53\u524d\u6ca1\u6709\u8fd0\u884c\u4e2d\u7684\u4efb\u52a1");
        }
    }

    private void cmdPause(String[] args) {
        if (TaskQueue.getInstance().isRunning()) {
            TaskQueue.getInstance().setPaused(true);
            this.sendMsg("\u00a7a\u4efb\u52a1\u5df2\u6682\u505c");
        } else {
            this.sendMsg("\u00a7e\u5f53\u524d\u6ca1\u6709\u8fd0\u884c\u4e2d\u7684\u4efb\u52a1");
        }
    }

    private void cmdResume(String[] args) {
        TaskQueue.getInstance().setPaused(false);
        if (TaskQueue.getInstance().isRunning()) {
            this.sendMsg("\u00a7a\u4efb\u52a1\u5df2\u6062\u590d");
        }
    }

    private BlockState parseBlockState(String input) {
        Object blockId = input.contains(":") ? input : "minecraft:" + input;
        Block block = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation((String)blockId));
        if (block == null) {
            blockId = "minecraft:" + input;
            block = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation((String)blockId));
        }
        if (block == null) {
            this.sendMsg("\u00a7c\u672a\u77e5\u65b9\u5757: " + input);
            return null;
        }
        return block.m_49966_();
    }

    private int parseInt(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private Direction parseDirection(String s) {
        return switch (s.toLowerCase()) {
            case "up", "u" -> Direction.UP;
            case "down", "d" -> Direction.DOWN;
            case "north", "n" -> Direction.NORTH;
            case "south", "s" -> Direction.SOUTH;
            case "east", "e" -> Direction.EAST;
            case "west", "w" -> Direction.WEST;
            default -> null;
        };
    }

    private void sendMsg(String msg) {
        if (CommandRegistry.mc.player != null) {
            CommandRegistry.mc.player.m_5661_(Component.literal((String)("\u00a77[WorldEdit] " + msg)), true);
        }
    }

    @FunctionalInterface
    static interface CommandHandler {
        public void handle(String[] var1) throws Exception;
    }
}

