package lexis.Client.Commands.Lexishelp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class LexisHelpCommand {
   private static final String PREFIX = "§d[§6Lexis§d] §f";
   private static final int MAX_LINES_PER_PAGE = 10;
   private static Map commandMap = new LinkedHashMap();
   private static List commandList = new ArrayList();
   private static final SuggestionProvider COMMAND_SUGGESTIONS = (context, builder) -> {
      return SharedSuggestionProvider.m_82970_(commandList, builder);
   };

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("help_lexis").then(Commands.m_82127_("Command").then(Commands.m_82129_("指令", StringArgumentType.word()).suggests(COMMAND_SUGGESTIONS).executes(LexisHelpCommand::showCommandDetail)))).then(Commands.m_82129_("页", IntegerArgumentType.integer(1)).executes(LexisHelpCommand::showPage))).executes((context) -> {
         return showPage(context, 1);
      })));
   }

   private static int showCommandDetail(CommandContext context) {
      String command = StringArgumentType.getString(context, "指令");
      if (commandMap.containsKey(command)) {
         CommandInfo info = (CommandInfo)commandMap.get(command);
         String commandPrefix = info.isClient ? "/lexis client " : "/lexis server ";
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§6===== 指令说明 =====");
         }, false);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§e" + commandPrefix + command);
         }, false);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§7不同类端: " + (info.isClient ? "§a客户端指令" : "§c服务端指令"));
         }, false);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§7说明: " + info.description);
         }, false);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§7用法: " + commandPrefix + command);
         }, false);
      } else {
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§d[§6Lexis§d] §f§c未找到指令: " + command);
         }, false);
      }

      return 1;
   }

   private static int showPage(CommandContext context) {
      int page = IntegerArgumentType.getInteger(context, "页");
      return showPage(context, page);
   }

   private static int showPage(CommandContext context, int page) {
      int totalItems = commandList.size();
      int totalPages = (int)Math.ceil((double)totalItems / 10.0);
      if (totalItems == 0) {
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§d[§6Lexis§d] §f§c没有可用的指令");
         }, false);
         return 0;
      } else if (page >= 1 && page <= totalPages) {
         int startIndex = (page - 1) * 10;
         int endIndex = Math.min(startIndex + 10, totalItems);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§6===== Lexis 指令列表 (第 " + page + "/" + totalPages + " 页) =====");
         }, false);

         for(int i = startIndex; i < endIndex; ++i) {
            String command = (String)commandList.get(i);
            CommandInfo info = (CommandInfo)commandMap.get(command);
            int number = i + 1;
            String commandPrefix = info.isClient ? "/lexis client " : "/lexis server ";
            String typeColor = info.isClient ? "§a" : "§c";
            String typeText = info.isClient ? "客户端" : "服务端";
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§e" + number + ". " + typeColor + "[" + typeText + "]§r " + commandPrefix + command + " §7- " + info.description);
            }, false);
         }

         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§7--------------------------------");
         }, false);
         if (page > 1) {
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§a上一页: /lexis help_lexis " + (page - 1));
            }, false);
         }

         if (page < totalPages) {
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§a下一页: /lexis help_lexis " + (page + 1));
            }, false);
         }

         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§7输入 /lexis help_lexis Command <指令> 查看详细说明");
         }, false);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§7§o客户端指令直接使用 /lexis client <指令>，服务端指令需使用 /lexis server <指令>");
         }, false);
         return 1;
      } else {
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§d[§6Lexis§d] §f§c页码错误！最大有效: 1 - " + totalPages);
         }, false);
         return 0;
      }
   }

   static {
      commandMap.put("CommandBlock_OOC", new CommandInfo("这个CommandBlock_OOC 最强一键生成OOC命令方块！打开gui 你学学就行了", true));
      commandMap.put("SignOOC", new CommandInfo("告示牌ooc生成，这要你手上有ooc告示牌带有数据nbt 右键告示牌能生成效果！你要来生成告示牌ooc！", true));
      commandMap.put("goto", new CommandInfo("这是 找路goto，指令 lexis goto Player <玩家名> + Pos <坐标> + stop 的 停止移动找路", true));
      commandMap.put("keybind", new CommandInfo("这里设置按键-在原版按键绑定界面中会找到发现这新按键了(要指令过搞) 使用指令说明:\"/lexis client keybind set <在按键绑定界面不同格1~32> <按键绑定界面显示名称中> \"say <>(这里加<最后on/off会触发加切换模式就是聊天消息> [Lexis-Dev] on > off)\" \"!on-off\"\" 这里某工具MOD一样", true));
      commandMap.put("playermagnet", new CommandInfo("这是磁吸玩家，在其地玩家试图进入你服务器(关闭的功能off)，可能玩家会禁止进入这服务器，如果开启功能了 玩家可以进入你服务器了 公开模式", false));
      commandMap.put("maxNbtSize", new CommandInfo("这是最大NBT上限，玩家拿过多nbt 会自动清空背包 可能你自己也会清空！", false));
      commandMap.put("illegalEntity", new CommandInfo("这是检测异常实体 会处理强制清除实体+禁止生成新实体 异常所有:血量NaN/Infinite异常 + 死亡时间异常 + 加速度崩服异常 + 史莱姆过大 + 物品名称过长 + text解析崩服异常", false));
      commandMap.put("dogslayer", new CommandInfo("这是词汇神剑，最强第一名神剑 可击杀 异常实体nan血量，这仅自己你能用 其地玩家用不了这武器，潜行+右键切换技能就行", false));
      commandMap.put("aichat", new CommandInfo("这是ai聊天，在聊天输入 @Lexis <内容>，在指令 可以 清空记忆+重新开聊天 和 开关，其地玩家输入这可能有效会问", false));
      commandMap.put("packetlimit", new CommandInfo("这是发包限制，玩家试图发包太多了 会被踢出玩家！防止卡你服务器，默认是开启功能", false));
      commandMap.put("Lexis", new CommandInfo("这是大写Lexis 意思是 玩家 和 管理员op 可以使用这指令，小写 lexis 等于是你仅可用 其地无法", false));
      commandMap.put("coins", new CommandInfo("修改 金币数量，可以在商店买东西 最大2147483647", false));
      commandMap.put("NoMenu", new CommandInfo("这是禁止菜单功能，如果你要是 生存服 + 其地服，你可以禁止这菜单 其地玩家 用不了这，无法上架商店+无法其地吧", false));
      commandMap.put("jump", new CommandInfo("执行一次跳跃", true));
      commandMap.put("invsee", new CommandInfo("查看其他玩家的背包", true));
      commandMap.put("enchant", new CommandInfo("附魔指令，只有创造能用 用法: /lexis client enchant give <附魔id> <等级> 给手上加一个附魔giveall <等级> 给手上塞满所有非诅咒附魔giveallnoCurse 同上allitemsgive <id> <等级> 给背包所有能附魔的物品都加上这个allitemsgivenoCurse <等级> 给背包所有物品塞满能塞的附魔（小心卡服）附魔id例子：minecraft:sharpness 生存别想了，去玩铁砧吧", true));
      commandMap.put("drop", new CommandInfo("扔光背包所有物品 也会副手 除了旁观模式 其地模式能用吧", true));
      commandMap.put("tp", new CommandInfo("传送指令 仅客户端 传送到坐标(距离11米) ：@p 传送到最近玩家：@r 传送到随机玩家 距离限制11米 防止回弹", true));
      commandMap.put("ModifyCount", new CommandInfo("修改物品数量 仅创造模式可用 命令：setitemCount <数量> 修改当前主手(空的副手)物品数量 setallitemCount <数量> 修改背包+副手所有物品数量", true));
      commandMap.put("ModifyDamagedurability", new CommandInfo("改物品耐久 setdurability <值>改手上物品耐久，正数破坏负数修复；setalldurability <值>改背包+副手所有物品耐久 值最大-/+21亿。仅创造模式", true));
      commandMap.put("getitemnbt", new CommandInfo("读取手上物品的 NBT 数据，显示在聊天栏，点击消息可复制", true));
      commandMap.put("Serverinfo", new CommandInfo("查看服务器信息：地址+延迟+TPS+版本+难度+天数+权限等级 仅客户端指令", true));
      commandMap.put("disconnect", new CommandInfo("主动断开与服务器的连接 被踢出一样，可自定义断开踢出文本", true));
      commandMap.put("tpgoto", new CommandInfo("客户端传送找路玩家位置", true));
      commandMap.put("tpgotoPos", new CommandInfo("客户端传送找路位置坐标", true));
      commandMap.put("friends", new CommandInfo("添加玩家的好友，在hack功能 不会对操作触发交互/攻击/放置的好友", true));
      commandMap.put("tpPlayerEgg", new CommandInfo("传送玩家蛋", true));
      commandMap.put("ReplayPacket", new CommandInfo("重新修复发包的指令，如果发包出现问题卡住了 可以过这修复发包！", true));
      commandMap.put("ServerSwitch", new CommandInfo("切换服务器的指令，如果你是网易玩家 使用这指令，最后你的ip还是显示 127.0.0.1 在服务器看到你也是ip，防止盗取你IP", true));
      commandList.addAll(commandMap.keySet());
   }

   private static class CommandInfo {
      String description;
      boolean isClient;

      CommandInfo(String description, boolean isClient) {
         this.description = description;
         this.isClient = isClient;
      }
   }
}
