package fku.org.example.fku.features.clientop;

import java.util.ArrayList;
import java.util.List;

/**
 * OP 指令数据库 — 客户端本地维护的常见“需权限”指令列表
 *
 * 仅用于本地补全与预览（模拟便利），不向服务端发送任何权限请求或伪造身份。
 * op=true 表示通常需要服务端 OP 权限；op=false 为常见但无需权限的指令（仅用于预览识别）。
 */
public class OpCommandDB {

    public static class OpCommand {
        public final String name;
        public final String desc;
        public final boolean op;
        public OpCommand(String name, String desc, boolean op) {
            this.name = name;
            this.desc = desc;
            this.op = op;
        }
    }

    public static final List<OpCommand> COMMANDS = new ArrayList<>();
    static {
        // ===== 需 OP 权限的指令 =====
        COMMANDS.add(new OpCommand("give", "给予玩家物品", true));
        COMMANDS.add(new OpCommand("gamemode", "切换游戏模式", true));
        COMMANDS.add(new OpCommand("defaultgamemode", "设置默认游戏模式", true));
        COMMANDS.add(new OpCommand("gamerule", "设置游戏规则", true));
        COMMANDS.add(new OpCommand("tp", "传送实体", true));
        COMMANDS.add(new OpCommand("teleport", "传送实体", true));
        COMMANDS.add(new OpCommand("weather", "设置天气", true));
        COMMANDS.add(new OpCommand("time", "设置时间", true));
        COMMANDS.add(new OpCommand("summon", "生成实体", true));
        COMMANDS.add(new OpCommand("fill", "填充方块", true));
        COMMANDS.add(new OpCommand("setblock", "放置方块", true));
        COMMANDS.add(new OpCommand("kill", "清除实体", true));
        COMMANDS.add(new OpCommand("op", "赋予玩家管理员权限", true));
        COMMANDS.add(new OpCommand("deop", "撤销玩家管理员权限", true));
        COMMANDS.add(new OpCommand("effect", "给予/清除状态效果", true));
        COMMANDS.add(new OpCommand("enchant", "附魔", true));
        COMMANDS.add(new OpCommand("xp", "给予经验", true));
        COMMANDS.add(new OpCommand("experience", "给予经验", true));
        COMMANDS.add(new OpCommand("spawnpoint", "设置出生点", true));
        COMMANDS.add(new OpCommand("setworldspawn", "设置世界出生点", true));
        COMMANDS.add(new OpCommand("locate", "定位结构", true));
        COMMANDS.add(new OpCommand("clear", "清空物品栏", true));
        COMMANDS.add(new OpCommand("difficulty", "设置难度", true));
        COMMANDS.add(new OpCommand("forceload", "强制加载区块", true));
        COMMANDS.add(new OpCommand("ban", "封禁玩家", true));
        COMMANDS.add(new OpCommand("ban-ip", "封禁IP", true));
        COMMANDS.add(new OpCommand("kick", "踢出玩家", true));
        COMMANDS.add(new OpCommand("pardon", "解封玩家", true));
        COMMANDS.add(new OpCommand("pardon-ip", "解封IP", true));
        COMMANDS.add(new OpCommand("whitelist", "白名单管理", true));
        COMMANDS.add(new OpCommand("publish", "开放局域网", true));
        COMMANDS.add(new OpCommand("save-all", "保存存档", true));
        COMMANDS.add(new OpCommand("save-on", "开启自动保存", true));
        COMMANDS.add(new OpCommand("save-off", "关闭自动保存", true));
        COMMANDS.add(new OpCommand("stop", "关闭服务器", true));
        COMMANDS.add(new OpCommand("title", "发送标题", true));
        COMMANDS.add(new OpCommand("particle", "生成粒子", true));
        COMMANDS.add(new OpCommand("playsound", "播放音效", true));
        COMMANDS.add(new OpCommand("seed", "查看世界种子", true));
        COMMANDS.add(new OpCommand("spreadplayers", "随机散布玩家", true));
        COMMANDS.add(new OpCommand("worldborder", "设置世界边界", true));
        COMMANDS.add(new OpCommand("bossbar", "Boss血条管理", true));
        COMMANDS.add(new OpCommand("team", "队伍管理", true));
        COMMANDS.add(new OpCommand("trigger", "触发计分板", true));
        COMMANDS.add(new OpCommand("item", "物品栏操作", true));
        COMMANDS.add(new OpCommand("loot", "战利品", true));
        COMMANDS.add(new OpCommand("recipe", "配方管理", true));
        COMMANDS.add(new OpCommand("datapack", "数据包管理", true));
        COMMANDS.add(new OpCommand("ride", "骑乘控制", true));
        COMMANDS.add(new OpCommand("damage", "造成伤害", true));
        COMMANDS.add(new OpCommand("jfr", "性能记录(JFR)", true));
        COMMANDS.add(new OpCommand("perf", "性能分析", true));

        // ===== 无需 OP（仅预览识别用）=====
        COMMANDS.add(new OpCommand("msg", "私聊玩家", false));
        COMMANDS.add(new OpCommand("tell", "私聊玩家", false));
        COMMANDS.add(new OpCommand("w", "私聊玩家", false));
        COMMANDS.add(new OpCommand("me", "动作聊天", false));
        COMMANDS.add(new OpCommand("say", "广播消息", false));
        COMMANDS.add(new OpCommand("help", "查看帮助", false));
    }

    public static OpCommand find(String name) {
        if (name == null) return null;
        for (OpCommand c : COMMANDS) {
            if (c.name.equalsIgnoreCase(name)) return c;
        }
        return null;
    }
}
