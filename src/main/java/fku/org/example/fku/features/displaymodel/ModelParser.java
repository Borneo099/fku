package fku.org.example.fku.features.displaymodel; /* water */

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实体模型指令解析器
 * 职责：解析/summon指令，提取Passengers乘客列表和坐标偏移
 * 
 * 设计思想（矛盾论）：
 * - 主要矛盾：Passengers嵌套的递归提取 vs 主实体排除
 * - 方案：仅提取Passengers列表中的乘客，丢弃主实体本身
 * 
 * 参考：
 * - Minecraft Wiki SNBT格式说明（https://minecraft.wiki/w/NBT_format）
 * - vanilla /summon命令的解析逻辑
 */
public class ModelParser {
    /** 递归深度限制（防止栈溢出） */
    private static final int MAX_RECURSION_DEPTH = 50;
    
    /** NBT数据提取正则 */
    private static final Pattern NBT_PATTERN = Pattern.compile("\\{(?:[^{}]|\\{[^{}]*\\})*\\}");
    
    /** /summon指令格式：/summon <entity> [<x> <y> <z>] [<nbt>] */
    private static final Pattern SUMMON_PATTERN = 
        Pattern.compile("^/summon\\s+(\\S+)\\s+(~-?\\d*\\.?\\d*)\\s+(~-?\\d*\\.?\\d*)\\s+(~-?\\d*\\.?\\d*)");
    
    /** 波浪坐标正则 */
    private static final Pattern TILDE_COORD = Pattern.compile("(~)(-?\\d*\\.?\\d*)");

    /**
     * 提取/summon指令中的Passengers乘客列表
     * 仅返回乘客实体（不含主实体本身），每个乘客包含完整NBT
     *
     * @param command /summon指令字符串
     * @return 乘客CompoundTag列表
     * @throws Exception 解析失败时抛出
     */
    public static List<CompoundTag> extractPassengers(String command) throws Exception {
        // 提取NBT部分
        String nbtString = extractNbtString(command);
        if (nbtString == null || nbtString.isEmpty()) {
            throw new IllegalArgumentException("无法从指令中提取NBT数据，请确认指令格式正确");
        }
        
        // 清理和修复NBT字符串（处理block-display.com生成的特殊格式）
        nbtString = cleanNbtString(nbtString);
        
        // 解析为CompoundTag
        CompoundTag rootTag = TagParser.parseTag(nbtString);
        
        // 仅提取Passengers列表中的内容，不包含主实体
        List<CompoundTag> passengers = new ArrayList<>();
        if (rootTag.contains("Passengers", Tag.TAG_LIST)) {
            ListTag passengerList = rootTag.getList("Passengers", Tag.TAG_COMPOUND);
            for (int i = 0; i < passengerList.size(); i++) {
                CompoundTag passenger = passengerList.getCompound(i);
                // 递归提取嵌套Passengers（避免遗漏多级嵌套）
                extractPassengerRecursive(passenger, passengers, 0);
            }
        }
        
        if (passengers.isEmpty()) {
            throw new IllegalArgumentException("指令中未包含Passengers乘客数据");
        }
        
        return passengers;
    }

    /**
     * 递归提取乘客实体（含嵌套Passengers）
     */
    private static void extractPassengerRecursive(CompoundTag tag, List<CompoundTag> result, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return; // 超过递归深度限制，安全截断
        }
        
        // 将当前乘客加入结果列表
        result.add(tag.copy());
        
        // 递归处理该乘客的嵌套Passengers
        if (tag.contains("Passengers", Tag.TAG_LIST)) {
            ListTag passengers = tag.getList("Passengers", Tag.TAG_COMPOUND);
            for (int i = 0; i < passengers.size(); i++) {
                extractPassengerRecursive(passengers.getCompound(i), result, depth + 1);
            }
        }
    }

    /**
     * 提取/summon指令中的相对坐标偏移
     * 例如 /summon minecraft:block_display ~-0.5 ~0.5 ~-0.5 {...}
     * 返回 (dx=-0.5, dy=0.5, dz=-0.5)
     *
     * @param command /summon指令字符串
     * @return 坐标偏移Vec3
     */
    public static Vec3 extractOffset(String command) {
        Matcher matcher = SUMMON_PATTERN.matcher(command);
        if (matcher.find()) {
            double dx = parseTildeCoord(matcher.group(2));
            double dy = parseTildeCoord(matcher.group(3));
            double dz = parseTildeCoord(matcher.group(4));
            return new Vec3(dx, dy, dz);
        }
        return Vec3.ZERO;
    }

    /**
     * 提取/summon指令中的实体ID
     */
    public static String extractEntityId(String command) {
        String[] parts = command.trim().split("\\s+");
        if (parts.length >= 2 && parts[0].equalsIgnoreCase("/summon")) {
            return parts[1]; // 第二个部分就是实体ID
        }
        return "minecraft:block_display";
    }

    /**
     * 解析波浪坐标
     * ~-0.5 → -0.5, ~ → 0, ~0 → 0
     */
    private static double parseTildeCoord(String coord) {
        coord = coord.trim();
        if (coord.startsWith("~")) {
            String num = coord.substring(1);
            if (num.isEmpty() || num.equals("0")) {
                return 0.0;
            }
            return Double.parseDouble(num);
        }
        return Double.parseDouble(coord);
    }

    /**
     * 从指令中提取NBT字符串（以{开头的内容）
     */
    private static String extractNbtString(String command) {
        // 查找第一个{到最后一个}之间的内容
        int start = command.indexOf('{');
        int end = command.lastIndexOf('}');
        
        if (start >= 0 && end > start) {
            return command.substring(start, end + 1);
        }
        
        return null;
    }

    /**
     * 清理和修复NBT字符串，处理block-display.com等工具生成的特殊格式
     */
    private static String cleanNbtString(String nbt) {
        nbt = fixFloatListValues(nbt);
        nbt = fixTrailingCommas(nbt);
        return nbt;
    }

    /**
     * 修复Float列表中的Double值问题
     * block-display.com可能生成: transformation:[0.25f,0,0,0,...0.2]
     * 这里0.2是double，需要转换为0.2f
     */
    private static String fixFloatListValues(String nbt) {
        Pattern listPattern = Pattern.compile("(\\w+):\\[([^\\]]+)\\]");
        Matcher matcher = listPattern.matcher(nbt);
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String listContent = matcher.group(2);
            String fixedContent = fixNumberSuffixes(listContent);
            matcher.appendReplacement(sb, matcher.group(1) + ":[" + fixedContent + "]");
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    /**
     * 修复列表中的数字后缀
     */
    private static String fixNumberSuffixes(String content) {
        String[] parts = content.split(",");
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            
            if (part.matches(".*[fFdDsSbBlL]")) {
                sb.append(part); // 已有后缀，保留
            } else if (part.matches("-?\\d+\\.\\d+")) {
                // 浮点数无后缀 → 添加f
                sb.append(part).append("f");
            } else {
                sb.append(part); // 整数或其他，保持原样
            }
            
            if (i < parts.length - 1) {
                sb.append(",");
            }
        }
        
        return sb.toString();
    }

    /**
     * 修复尾部逗号问题
     */
    private static String fixTrailingCommas(String nbt) {
        nbt = nbt.replaceAll(",\\s*\\]", "]");
        nbt = nbt.replaceAll(",\\s*\\}", "}");
        return nbt;
    }
}