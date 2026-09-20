package fku.org.example.fku.features.clientop;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 客户端OP（Client OP）主控逻辑
 *
 * 纯客户端模拟：在本地为“需 OP 权限”的指令提供补全与预览便利。
 * 所有效果仅在本地生效，不向服务端发送任何权限请求或伪造身份。
 *
 * 数据流：
 *  - MixinClientOPCommandSuggestions 实时写入 {@link #currentInput} / {@link #inputBox}
 *  - {@link #mergeSuggestions} 在补全合并点注入本地 OP 指令
 *  - {@link #buildPreview} 生成“如果我是OP会执行什么”的本地预览文本
 */
@OnlyIn(Dist.CLIENT)
public class ClientOPFeature {

    /** 当前聊天输入文本（由 Mixin 实时写入），用于补全前缀匹配与预览 */
    public static String currentInput = "";
    /** 当前聊天输入框（用于预览定位），由 Mixin 写入 */
    public static EditBox inputBox = null;

    public static void init() {
        // 静默加载配置
        ClientOPConfig.getInstance();
    }

    public static boolean isEnabled() {
        return ClientOPConfig.getInstance().enabled;
    }

    // ═══════════════ 本地参数补全（无权限时接管） ═══════════════

    /**
     * 构建本地补全（指令名 + 常见 OP 指令参数：实体ID/物品ID/玩家名/选择器等）。
     * 仅在客户端指令树（服务端下发，无权限时不含 OP 指令）无法给出补全时由 Mixin 调用。
     * 返回 null 表示本地无可用补全，交还原版流程。
     */
    public static Suggestions buildLocalSuggestions(String text) {
        ClientOPConfig cfg = ClientOPConfig.getInstance();
        if (!cfg.enableLocalSuggestions) return null;
        try {
            if (text == null || !text.startsWith("/")) return null;

            int wordStart = text.lastIndexOf(' ') + 1;
            StringRange range = StringRange.between(wordStart, text.length());
            String prefix = text.substring(wordStart).toLowerCase(Locale.ROOT);
            String[] tokens = text.substring(1).split("\\s");
            String cmd = tokens[0].toLowerCase(Locale.ROOT);
            int argIndex = tokens.length - 1; // 0=正在补全指令名

            List<Suggestion> list = new ArrayList<>();
            if (argIndex == 0) {
                for (OpCommandDB.OpCommand c : OpCommandDB.COMMANDS) {
                    if (c.op && c.name.startsWith(prefix)) addLocal(list, range, c.name, c.desc, cfg);
                }
            } else {
                OpCommandDB.OpCommand c = OpCommandDB.find(cmd);
                if (c != null && c.op) {
                    for (String s : argSuggestions(cmd, argIndex - 1)) {
                        String lower = s.toLowerCase(Locale.ROOT);
                        if (lower.startsWith(prefix) || (s.startsWith("minecraft:") && s.substring(10).startsWith(prefix))) {
                            addLocal(list, range, s, "OP参数补全(本地)", cfg);
                        }
                    }
                }
            }
            if (list.isEmpty()) return null;
            if (list.size() > cfg.maxSuggestions) list = new ArrayList<>(list.subList(0, cfg.maxSuggestions));
            return new Suggestions(range, list);
        } catch (Exception e) {
            return null;
        }
    }

    private static void addLocal(List<Suggestion> list, StringRange range, String text, String tip, ClientOPConfig cfg) {
        for (Suggestion s : list) {
            if (s.getText().equals(text)) return;
        }
        list.add(new Suggestion(range, text, Component.literal("§7[OP] " + tip)));
    }

    /** 按指令与参数位置返回本地候选（idx 为指令名后的参数序号，从 0 开始） */
    private static List<String> argSuggestions(String cmd, int idx) {
        switch (cmd) {
            case "gamemode":
            case "defaultgamemode":
                return idx == 0 ? List.of("survival", "creative", "adventure", "spectator") : List.of();
            case "weather":
                return idx == 0 ? List.of("clear", "rain", "thunder") : List.of();
            case "difficulty":
                return idx == 0 ? List.of("peaceful", "easy", "normal", "hard") : List.of();
            case "time":
                if (idx == 0) return List.of("set", "add", "query");
                if (idx == 1) return List.of("day", "night", "noon", "midnight");
                return List.of();
            case "summon":
                return idx == 0 ? registryIds(BuiltInRegistries.ENTITY_TYPE) : List.of();
            case "give":
                if (idx == 0) return targetSuggestions();
                if (idx == 1) return registryIds(BuiltInRegistries.ITEM);
                return List.of();
            case "tp":
            case "teleport":
                if (idx == 0) return targetSuggestions();
                return idx <= 2 ? List.of("~") : List.of();
            case "kill":
            case "spawnpoint":
            case "setworldspawn":
                return idx == 0 ? targetSuggestions() : List.of();
            case "clear":
                if (idx == 0) return targetSuggestions();
                if (idx == 1) return registryIds(BuiltInRegistries.ITEM);
                return List.of();
            case "effect":
                if (idx == 0) return targetSuggestions();
                if (idx == 1) {
                    List<String> l = new ArrayList<>(registryIds(BuiltInRegistries.MOB_EFFECT));
                    l.add("clear");
                    return l;
                }
                return List.of();
            case "enchant":
                if (idx == 0) return targetSuggestions();
                if (idx == 1) return registryIds(BuiltInRegistries.ENCHANTMENT);
                return List.of();
            case "op":
            case "deop":
            case "kick":
            case "ban":
            case "pardon":
                return idx == 0 ? playerNames() : List.of();
            case "gamerule":
                if (idx == 0) return List.of("keepInventory", "doDaylightCycle", "doMobSpawning", "doFireTick",
                        "mobGriefing", "randomTickSpeed", "commandBlockOutput", "doTileDrops", "doEntityDrops",
                        "pvp", "announceAdvancements", "doInsomnia", "doImmediateRespawn", "fallDamage",
                        "spectatorsGenerateChunks", "showDeathMessages");
                if (idx == 1) return List.of("true", "false");
                return List.of();
            case "setblock":
                if (idx <= 1) return List.of("~");
                if (idx == 2) return registryIds(BuiltInRegistries.BLOCK);
                return List.of();
            case "fill":
                if (idx <= 1) return List.of("~");
                if (idx == 3) return registryIds(BuiltInRegistries.BLOCK);
                return List.of();
            case "locate":
                return idx == 0 ? List.of("village", "stronghold", "mansion", "monument", "mineshaft", "fortress",
                        "endcity", "ancient_city", "bastion_remnant", "buried_treasure", "desert_pyramid",
                        "shipwreck", "swamp_hut", "pillager_outpost", "ruined_portal", "trail_ruins",
                        "woodland_mansion", "nether_fossil", "ocean_ruin") : List.of();
            case "xp":
            case "experience":
                return idx == 0 ? List.of("add", "set", "query") : List.of();
            case "particle":
                return idx == 0 ? List.of("minecraft:heart", "minecraft:flame", "minecraft:cloud", "minecraft:crit",
                        "minecraft:happy_villager", "minecraft:end_rod", "minecraft:firework",
                        "minecraft:soul_fire_flame", "minecraft:electric_spark", "minecraft:totem_of_undying") : List.of();
            default:
                return List.of();
        }
    }

    /** 目标选择器 + 在线玩家名 */
    private static List<String> targetSuggestions() {
        List<String> list = new ArrayList<>(List.of("@s", "@p", "@a", "@e", "@r", "@n"));
        list.addAll(playerNames());
        return list;
    }

    private static List<String> playerNames() {
        try {
            var conn = Minecraft.getInstance().getConnection();
            if (conn == null) return List.of();
            List<String> names = new ArrayList<>();
            for (var info : conn.getOnlinePlayers()) {
                names.add(info.getProfile().getName());
            }
            return names;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static List<String> registryIds(Registry<?> registry) {
        try {
            List<String> list = new ArrayList<>();
            for (net.minecraft.resources.ResourceLocation rl : registry.keySet()) {
                list.add(rl.toString());
            }
            return list;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 生成指令本地预览文本（仅本地模拟，不实际执行）。
     * 返回 null 表示无需显示预览。
     */
    public static String buildPreview(String cmd) {
        ClientOPConfig cfg = ClientOPConfig.getInstance();
        if (cmd == null || !cmd.startsWith("/")) return null;
        String raw = cmd.substring(1).trim();
        if (raw.isEmpty()) return null;
        String[] parts = raw.split("\\s+");
        OpCommandDB.OpCommand c = OpCommandDB.find(parts[0]);

        if (c == null) {
            if (cfg.showPreviewForOpOnly) return null;
            return "§7指令预览: /" + raw + "  §o(本地模拟, 非真实权限)";
        }
        if (cfg.showPreviewForOpOnly && !c.op) return null;

        String detail = describe(c, parts);
        String opTag = c.op ? "§c[OP指令] " : "";
        return opTag + "§a" + detail + "  §7§o(本地模拟, 非真实权限)";
    }

    private static String describe(OpCommandDB.OpCommand c, String[] parts) {
        String args = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        return switch (c.name) {
            case "give" -> "给予 " + p(parts, 1, "<目标>") + " 物品 " + (args.isEmpty() ? "<物品>" : args);
            case "tp", "teleport" -> "传送 " + p(parts, 1, "<目标>") + " → " + p(parts, 2, "<目的地>");
            case "gamemode", "defaultgamemode" -> "切换游戏模式为 " + p(parts, 1, "<模式>");
            case "weather" -> "设置天气为 " + p(parts, 1, "<类型>");
            case "time" -> "设置时间为 " + p(parts, 1, "<值>");
            case "op" -> "赋予 " + p(parts, 1, "<玩家>") + " 管理员权限";
            case "deop" -> "撤销 " + p(parts, 1, "<玩家>") + " 的管理员权限";
            case "kick" -> "踢出 " + p(parts, 1, "<玩家>");
            case "kill" -> "清除 " + p(parts, 1, "所有实体");
            case "effect" -> "操作状态效果 " + (args.isEmpty() ? "<目标> <类型>" : args);
            case "summon" -> "生成实体 " + p(parts, 1, "<实体>");
            case "enchant" -> "附魔 " + (args.isEmpty() ? "<类型>" : args);
            case "spawnpoint" -> "设置 " + p(parts, 1, "自己") + " 的出生点";
            case "gamerule" -> "设置规则 " + (args.isEmpty() ? "<规则> <值>" : args);
            case "fill" -> "从 " + p(parts, 1, "<坐标1>") + " 到 " + p(parts, 2, "<坐标2>") + " 填充 " + p(parts, 4, "<方块>");
            case "setblock" -> "在 " + p(parts, 1, "<坐标>") + " 放置 " + p(parts, 4, "<方块>");
            case "locate" -> "定位 " + p(parts, 1, "<结构>");
            default -> c.desc + (args.isEmpty() ? "" : "：" + args);
        };
    }

    private static String p(String[] parts, int idx, String fallback) {
        return parts.length > idx ? parts[idx] : fallback;
    }
}
