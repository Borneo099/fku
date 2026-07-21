package fku.org.example.fku.features.displaymodel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.phys.Vec3;

public class ModelParser {
    private static final int MAX_RECURSION_DEPTH = 50;
    private static final Pattern NBT_PATTERN = Pattern.compile("\\{(?:[^{}]|\\{[^{}]*\\})*\\}");
    private static final Pattern SUMMON_PATTERN = Pattern.compile("^/summon\\s+(\\S+)\\s+(~-?\\d*\\.?\\d*)\\s+(~-?\\d*\\.?\\d*)\\s+(~-?\\d*\\.?\\d*)");
    private static final Pattern TILDE_COORD = Pattern.compile("(~)(-?\\d*\\.?\\d*)");

    public static List<CompoundTag> extractPassengers(String command) throws Exception {
        String nbtString = ModelParser.extractNbtString(command);
        if (nbtString == null || nbtString.isEmpty()) {
            throw new IllegalArgumentException("\u65e0\u6cd5\u4ece\u6307\u4ee4\u4e2d\u63d0\u53d6NBT\u6570\u636e\uff0c\u8bf7\u786e\u8ba4\u6307\u4ee4\u683c\u5f0f\u6b63\u786e");
        }
        nbtString = ModelParser.cleanNbtString(nbtString);
        CompoundTag rootTag = TagParser.m_129359_((String)nbtString);
        ArrayList<CompoundTag> passengers = new ArrayList<CompoundTag>();
        if (rootTag.m_128425_("Passengers", 9)) {
            ListTag passengerList = rootTag.m_128437_("Passengers", 10);
            for (int i = 0; i < passengerList.size(); ++i) {
                CompoundTag passenger = passengerList.m_128728_(i);
                ModelParser.extractPassengerRecursive(passenger, passengers, 0);
            }
        }
        if (passengers.isEmpty()) {
            throw new IllegalArgumentException("\u6307\u4ee4\u4e2d\u672a\u5305\u542bPassengers\u4e58\u5ba2\u6570\u636e");
        }
        return passengers;
    }

    private static void extractPassengerRecursive(CompoundTag tag, List<CompoundTag> result, int depth) {
        if (depth > 50) {
            return;
        }
        result.add(tag.m_6426_());
        if (tag.m_128425_("Passengers", 9)) {
            ListTag passengers = tag.m_128437_("Passengers", 10);
            for (int i = 0; i < passengers.size(); ++i) {
                ModelParser.extractPassengerRecursive(passengers.m_128728_(i), result, depth + 1);
            }
        }
    }

    public static Vec3 extractOffset(String command) {
        Matcher matcher = SUMMON_PATTERN.matcher(command);
        if (matcher.find()) {
            double dx = ModelParser.parseTildeCoord(matcher.group(2));
            double dy = ModelParser.parseTildeCoord(matcher.group(3));
            double dz = ModelParser.parseTildeCoord(matcher.group(4));
            return new Vec3(dx, dy, dz);
        }
        return Vec3.f_82478_;
    }

    public static String extractEntityId(String command) {
        String[] parts = command.trim().split("\\s+");
        if (parts.length >= 2 && parts[0].equalsIgnoreCase("/summon")) {
            return parts[1];
        }
        return "minecraft:block_display";
    }

    private static double parseTildeCoord(String coord) {
        if ((coord = coord.trim()).startsWith("~")) {
            String num = coord.substring(1);
            if (num.isEmpty() || num.equals("0")) {
                return 0.0;
            }
            return Double.parseDouble(num);
        }
        return Double.parseDouble(coord);
    }

    private static String extractNbtString(String command) {
        int start = command.indexOf(123);
        int end = command.lastIndexOf(125);
        if (start >= 0 && end > start) {
            return command.substring(start, end + 1);
        }
        return null;
    }

    private static String cleanNbtString(String nbt) {
        nbt = ModelParser.fixFloatListValues(nbt);
        nbt = ModelParser.fixTrailingCommas(nbt);
        return nbt;
    }

    private static String fixFloatListValues(String nbt) {
        Pattern listPattern = Pattern.compile("(\\w+):\\[([^\\]]+)\\]");
        Matcher matcher = listPattern.matcher(nbt);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String listContent = matcher.group(2);
            String fixedContent = ModelParser.fixNumberSuffixes(listContent);
            matcher.appendReplacement(sb, matcher.group(1) + ":[" + fixedContent + "]");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String fixNumberSuffixes(String content) {
        String[] parts = content.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; ++i) {
            String part = parts[i].trim();
            if (part.matches(".*[fFdDsSbBlL]")) {
                sb.append(part);
            } else if (part.matches("-?\\d+\\.\\d+")) {
                sb.append(part).append("f");
            } else {
                sb.append(part);
            }
            if (i >= parts.length - 1) continue;
            sb.append(",");
        }
        return sb.toString();
    }

    private static String fixTrailingCommas(String nbt) {
        nbt = nbt.replaceAll(",\\s*\\]", "]");
        nbt = nbt.replaceAll(",\\s*\\}", "}");
        return nbt;
    }
}

