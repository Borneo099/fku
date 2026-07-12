package lexis.Hack.gui.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lexis.Hack.Hacks.Misc.PacketLoggerHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PacketLoggerScreen extends Screen {
   private final PacketLoggerHack hack;
   private final Screen parent;
   private final Tab currentTab;
   private List allPackets = new ArrayList();
   private List filteredPackets = new ArrayList();
   private Set selectedPackets = new HashSet();
   private EditBox searchBox;
   private String searchText = "";
   private int scrollOffset = 0;
   private static final int ITEM_HEIGHT = 22;
   private static final int VISIBLE_ITEMS = 15;
   private static final Map PACKET_DESCRIPTIONS = new HashMap();
   private static final List S2C_PACKETS;
   private static final List C2S_PACKETS;

   private static void putDesc(String name, String chinese, String english) {
      PACKET_DESCRIPTIONS.put(name, new String[]{chinese, english});
   }

   public PacketLoggerScreen(PacketLoggerHack hack, Screen parent, Tab tab) {
      super(Component.m_237113_("数据包记录器 - " + (tab == PacketLoggerScreen.Tab.S2C ? "S2C" : "C2S")));
      this.hack = hack;
      this.parent = parent;
      this.currentTab = tab;
      this.loadSelected();
      this.initPackets();
   }

   private void loadSelected() {
      if (this.currentTab == PacketLoggerScreen.Tab.S2C) {
         this.selectedPackets.addAll(this.hack.getMonitoredS2C());
      } else {
         this.selectedPackets.addAll(this.hack.getMonitoredC2S());
      }

   }

   private void initPackets() {
      List packetList = this.currentTab == PacketLoggerScreen.Tab.S2C ? S2C_PACKETS : C2S_PACKETS;
      this.allPackets.clear();
      Iterator var2 = packetList.iterator();

      while(var2.hasNext()) {
         String name = (String)var2.next();
         String[] desc = (String[])PACKET_DESCRIPTIONS.getOrDefault(name, new String[]{"未知数据包", "Unknown packet"});
         this.allPackets.add(new PacketEntry(name, this.selectedPackets.contains(name), desc[0], desc[1]));
      }

      this.allPackets.sort(Comparator.comparing((e) -> {
         return e.name;
      }));
      this.filteredPackets = new ArrayList(this.allPackets);
   }

   private void filterPackets(String search) {
      this.searchText = search.toLowerCase();
      if (this.searchText.isEmpty()) {
         this.filteredPackets = new ArrayList(this.allPackets);
      } else {
         this.filteredPackets = this.allPackets.stream().filter((e) -> {
            return e.name.toLowerCase().contains(this.searchText);
         }).toList();
      }

      this.scrollOffset = 0;
   }

   private void saveSelected() {
      Set selected = new HashSet();
      Iterator var2 = this.allPackets.iterator();

      while(var2.hasNext()) {
         PacketEntry entry = (PacketEntry)var2.next();
         if (entry.selected) {
            selected.add(entry.name);
         }
      }

      if (this.currentTab == PacketLoggerScreen.Tab.S2C) {
         this.hack.setMonitoredS2C(selected);
      } else {
         this.hack.setMonitoredC2S(selected);
      }

   }

   protected void m_7856_() {
      int centerX = this.f_96543_ / 2;
      int y = 20;
      this.searchBox = new EditBox(this.f_96547_, centerX - 150, y, 300, 20, Component.m_237113_(""));
      this.searchBox.m_94199_(50);
      this.searchBox.m_94151_(this::filterPackets);
      this.m_142416_(this.searchBox);
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("全选"), (btn) -> {
         PacketEntry entry;
         for(Iterator var2 = this.filteredPackets.iterator(); var2.hasNext(); entry.selected = true) {
            entry = (PacketEntry)var2.next();
         }

      }).m_252987_(centerX - 150, y, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("取消全选"), (btn) -> {
         PacketEntry entry;
         for(Iterator var2 = this.filteredPackets.iterator(); var2.hasNext(); entry.selected = false) {
            entry = (PacketEntry)var2.next();
         }

      }).m_252987_(centerX - 60, y, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("反选"), (btn) -> {
         PacketEntry entry;
         for(Iterator var2 = this.filteredPackets.iterator(); var2.hasNext(); entry.selected = !entry.selected) {
            entry = (PacketEntry)var2.next();
         }

      }).m_252987_(centerX + 30, y, 60, 20).m_253136_());
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("保存"), (btn) -> {
         this.saveSelected();
         this.m_7379_();
      }).m_252987_(centerX - 110, this.f_96544_ - 30, 100, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("取消"), (btn) -> {
         this.m_7379_();
      }).m_252987_(centerX + 10, this.f_96544_ - 30, 100, 20).m_253136_());
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      this.m_280273_(gui);
      super.m_88315_(gui, mouseX, mouseY, delta);
      int centerX = this.f_96543_ / 2;
      int startY = 95;
      gui.m_280488_(this.f_96547_, "选择要记录的数据包", centerX - 150, startY - 12, 16777130);
      List list = this.filteredPackets;
      int maxScroll = Math.max(0, list.size() - 15);
      gui.m_280509_(centerX - 152, startY - 2, centerX + 152, startY + 330 + 2, -1439485133);
      PacketEntry hoveredEntry = null;

      int scrollbarX;
      int bgColor;
      for(scrollbarX = 0; scrollbarX < 15; ++scrollbarX) {
         int index = this.scrollOffset + scrollbarX;
         if (index >= list.size()) {
            break;
         }

         PacketEntry entry = (PacketEntry)list.get(index);
         int y = startY + scrollbarX * 22;
         boolean hovered = mouseX >= centerX - 150 && mouseX <= centerX + 150 && mouseY >= y && mouseY <= y + 22 - 2;
         if (entry.selected) {
            bgColor = -1437814960;
         } else if (hovered) {
            bgColor = -1436129690;
         } else {
            bgColor = -1439485133;
         }

         gui.m_280509_(centerX - 150, y, centerX + 150, y + 22 - 2, bgColor);
         gui.m_280488_(this.f_96547_, entry.name, centerX - 140, y + 5, entry.selected ? 16777215 : 13421772);
         String status = entry.selected ? "✓" : "✗";
         gui.m_280488_(this.f_96547_, status, centerX + 135, y + 5, entry.selected ? 11206570 : 16755370);
         if (hovered) {
            hoveredEntry = entry;
         }
      }

      if (list.size() > 15) {
         scrollbarX = centerX + 155;
         int scrollbarHeight = 330;
         gui.m_280509_(scrollbarX, startY, scrollbarX + 4, startY + scrollbarHeight, -1436129690);
         float percent = (float)this.scrollOffset / (float)maxScroll;
         int sliderHeight = Math.max(20, (int)((float)(scrollbarHeight * 15) / (float)list.size()));
         bgColor = startY + (int)(percent * (float)(scrollbarHeight - sliderHeight));
         gui.m_280509_(scrollbarX, bgColor, scrollbarX + 4, bgColor + sliderHeight, -5592406);
      }

      if (hoveredEntry != null) {
         List tooltip = new ArrayList();
         tooltip.add(Component.m_237113_(hoveredEntry.description));
         tooltip.add(Component.m_237113_(""));
         tooltip.add(Component.m_237113_(hoveredEntry.englishDesc));
         gui.m_280666_(this.f_96547_, tooltip, mouseX, mouseY);
      }

   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (super.m_6375_(mouseX, mouseY, button)) {
         return true;
      } else {
         int centerX = this.f_96543_ / 2;
         int startY = 95;
         List list = this.filteredPackets;

         for(int i = 0; i < 15; ++i) {
            int index = this.scrollOffset + i;
            if (index >= list.size()) {
               break;
            }

            int y = startY + i * 22;
            if (mouseX >= (double)(centerX - 150) && mouseX <= (double)(centerX + 150) && mouseY >= (double)y && mouseY <= (double)(y + 22 - 2)) {
               ((PacketEntry)list.get(index)).selected = !((PacketEntry)list.get(index)).selected;
               return true;
            }
         }

         return false;
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      int centerX = this.f_96543_ / 2;
      int startY = 95;
      if (mouseX >= (double)(centerX - 152) && mouseX <= (double)(centerX + 152) && mouseY >= (double)(startY - 2) && mouseY <= (double)(startY + 330 + 2)) {
         int maxScroll = Math.max(0, this.filteredPackets.size() - 15);
         this.scrollOffset = (int)Math.max(0.0, Math.min((double)maxScroll, (double)this.scrollOffset - delta * 3.0));
         return true;
      } else {
         return false;
      }
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.m_7379_();
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   public void m_7379_() {
      Minecraft.m_91087_().m_91152_(this.parent);
   }

   public boolean m_7043_() {
      return false;
   }

   static {
      putDesc("S2C:Login", "登录包 - 服务器确认你的登录", "Login packet - Server confirms your login");
      putDesc("S2C:Disconnect", "断开连接 - 服务器踢出你时的消息", "Disconnect packet - Message when server kicks you");
      putDesc("S2C:KeepAlive", "心跳包 - 保持连接活跃，取消会被踢", "Keep alive packet - Maintains connection, cancelling will get you kicked");
      putDesc("S2C:PlayerInfo", "玩家信息 - 玩家列表、皮肤、名称等", "Player info - Player list, skins, names");
      putDesc("S2C:PlayerPosLook", "强制移动 - 服务器强制你移动到某位置（防作弊）", "Player position look - Server forces you to move (anti-cheat)");
      putDesc("S2C:ChatMessage", "聊天消息 - 收到的所有聊天内容", "Chat message - All incoming chat messages");
      putDesc("S2C:TimeUpdate", "时间更新 - 游戏内时间变化", "Time update - Game time changes");
      putDesc("S2C:EntityStatus", "实体状态 - 实体受伤、死亡、动作等", "Entity status - Entity hurt, death, animations");
      putDesc("S2C:EntityMetadata", "实体数据 - 实体名称、状态效果等", "Entity metadata - Entity name, potion effects");
      putDesc("S2C:EntityVelocity", "实体速度 - 击退、爆炸、弹射物速度", "Entity velocity - Knockback, explosion, projectile speed");
      putDesc("S2C:EntityTeleport", "实体传送 - 实体瞬间移动", "Entity teleport - Entity teleportation");
      putDesc("S2C:EntityEquipment", "实体装备 - 实体手持/穿戴的物品", "Entity equipment - Items held/worn by entity");
      putDesc("S2C:EntityEffect", "实体效果 - 药水效果添加", "Entity effect - Potion effect added");
      putDesc("S2C:RemoveEntityEffect", "移除效果 - 药水效果移除", "Remove entity effect - Potion effect removed");
      putDesc("S2C:SetHealth", "设置血量 - 生命值、饥饿值、饱食度", "Set health - Health, hunger, saturation");
      putDesc("S2C:Respawn", "重生 - 死亡后重生，取消后就像极限模式", "Respawn - Respawn after death, cancelling = hardcore mode");
      putDesc("S2C:PlayerAbilities", "玩家能力 - 飞行、创造模式等", "Player abilities - Flying, creative mode etc");
      putDesc("S2C:HeldItemChange", "手持物品 - 其他玩家切换物品栏", "Held item change - Other players switching items");
      putDesc("S2C:WindowItems", "容器物品 - 箱子、背包内所有物品", "Window items - All items in chest/inventory");
      putDesc("S2C:WindowProperty", "容器属性 - 熔炉进度、附魔等级等", "Window property - Furnace progress, enchantment levels");
      putDesc("S2C:SetSlot", "设置格子 - 单个物品槽更新", "Set slot - Single slot update");
      putDesc("S2C:OpenWindow", "打开容器 - 打开箱子、工作台等界面", "Open window - Open chest, crafting table");
      putDesc("S2C:CloseWindow", "关闭容器 - 关闭界面", "Close window - Close GUI");
      putDesc("S2C:BlockUpdate", "方块更新 - 单个方块变化", "Block update - Single block change");
      putDesc("S2C:MultiBlockChange", "多方块更新 - 多个方块同时变化", "Multi block change - Multiple blocks changed");
      putDesc("S2C:ChunkData", "区块数据 - 加载地形", "Chunk data - Load terrain");
      putDesc("S2C:UnloadChunk", "卸载区块 - 卸载地形", "Unload chunk - Unload terrain");
      putDesc("S2C:SpawnEntity", "生成实体 - 生成生物、物品等", "Spawn entity - Spawn mobs, items");
      putDesc("S2C:SpawnExperienceOrb", "生成经验球 - 经验掉落", "Spawn experience orb - XP orbs");
      putDesc("S2C:SpawnLivingEntity", "生成生物 - 动物、怪物", "Spawn living entity - Animals, monsters");
      putDesc("S2C:SpawnPainting", "生成画 - 放置画", "Spawn painting - Place painting");
      putDesc("S2C:DestroyEntities", "销毁实体 - 实体消失", "Destroy entities - Entities removed");
      putDesc("S2C:CollectItem", "拾取物品 - 玩家捡起物品", "Collect item - Player picks up item");
      putDesc("S2C:Explosion", "爆炸 - TNT、苦力怕爆炸", "Explosion - TNT, creeper explosion");
      putDesc("S2C:SoundEffect", "音效 - 游戏声音", "Sound effect - Game sounds");
      putDesc("S2C:Particle", "粒子效果 - 破坏方块、药水等粒子", "Particle - Block break, potion particles");
      putDesc("S2C:GameStateChange", "游戏状态 - 天气、游戏模式等", "Game state change - Weather, gamemode");
      putDesc("S2C:UpdateScore", "更新分数 - 计分板分数", "Update score - Scoreboard scores");
      putDesc("S2C:UpdateObjective", "更新目标 - 计分板目标", "Update objective - Scoreboard objectives");
      putDesc("S2C:UpdateTeams", "更新队伍 - 队伍颜色、成员", "Update teams - Team colors, members");
      putDesc("S2C:Title", "标题 - 屏幕大标题", "Title - Screen title messages");
      putDesc("S2C:TabList", "TAB列表 - 玩家列表头尾", "Tab list - Player list header/footer");
      putDesc("S2C:WorldBorder", "世界边界 - 边界大小", "World border - Border size");
      putDesc("S2C:MapData", "地图数据 - 地图内容", "Map data - Map contents");
      putDesc("S2C:Advancements", "进度 - 成就/进度", "Advancements - Achievements/progress");
      putDesc("S2C:CommandTree", "命令树 - 命令补全", "Command tree - Command suggestions");
      putDesc("S2C:LookAt", "看向 - 强制转头", "Look at - Forced look direction");
      putDesc("S2C:SyncRecipeBook", "同步配方 - 解锁配方", "Sync recipe book - Unlock recipes");
      putDesc("S2C:Tags", "标签 - 游戏标签", "Tags - Game tags");
      putDesc("C2S:Login", "登录包 - 告诉服务器你是谁，取消无法连接", "Login packet - Tell server who you are, cancelling = no connection");
      putDesc("C2S:KeepAlive", "心跳包 - 告诉服务器你还活着，取消会被踢", "Keep alive - Tell server you're alive, cancelling = kicked");
      putDesc("C2S:ChatMessage", "聊天消息 - 你发送的聊天", "Chat message - Messages you send");
      putDesc("C2S:PlayerAction", "玩家动作 - 挖方块、放方块等", "Player action - Mining, placing blocks");
      putDesc("C2S:PlayerInput", "玩家输入 - 移动输入（WASD）", "Player input - Movement input (WASD)");
      putDesc("C2S:PlayerMovement", "玩家移动 - 位置+旋转", "Player movement - Position + rotation");
      putDesc("C2S:PlayerPosition", "玩家位置 - 只发送位置", "Player position - Position only");
      putDesc("C2S:PlayerRotation", "玩家旋转 - 只发送视角", "Player rotation - Rotation only");
      putDesc("C2S:PlayerPositionRotation", "位置+旋转 - 同时发送", "Player position rotation - Both position and rotation");
      putDesc("C2S:PlayerAbilities", "玩家能力 - 飞行切换", "Player abilities - Toggle flying");
      putDesc("C2S:HeldItemChange", "手持物品 - 切换物品栏", "Held item change - Switch hotbar slot");
      putDesc("C2S:Animation", "撸手动画 - 挥动手臂", "Animation - Swing arm");
      putDesc("C2S:UseEntity", "使用实体 - 攻击、交互实体", "Use entity - Attack, interact with entity");
      putDesc("C2S:UseItem", "使用物品 - 右键物品（空气）", "Use item - Right click item (air)");
      putDesc("C2S:UseItemOn", "对块使用 - 右键方块", "Use item on - Right click block");
      putDesc("C2S:ClickWindow", "点击容器 - 移动物品", "Click window - Move items in inventory");
      putDesc("C2S:CloseWindow", "关闭容器 - 关闭界面", "Close window - Close GUI");
      putDesc("C2S:CreativeInventoryAction", "创造模式物品 - 创造模式拿东西", "Creative inventory action - Creative mode item spawning");
      putDesc("C2S:EnchantItem", "附魔 - 附魔台点击", "Enchant item - Enchantment table click");
      putDesc("C2S:PickItem", "选取物品 - 中键选取", "Pick item - Middle click pick block");
      putDesc("C2S:SelectTrade", "选择交易 - 村民交易", "Select trade - Villager trading");
      putDesc("C2S:RenameItem", "重命名物品 - 铁砧改名", "Rename item - Anvil renaming");
      putDesc("C2S:UpdateCommandBlock", "更新命令块 - 修改命令方块", "Update command block - Edit command block");
      putDesc("C2S:UpdateSign", "更新告示牌 - 写牌子", "Update sign - Write on sign");
      putDesc("C2S:UpdateStructureBlock", "更新结构块 - 修改结构方块", "Update structure block - Edit structure block");
      putDesc("C2S:UpdateJigsawBlock", "更新拼图块 - 修改拼图方块", "Update jigsaw block - Edit jigsaw block");
      putDesc("C2S:UpdateBeacon", "更新信标 - 设置信标效果", "Update beacon - Set beacon effects");
      putDesc("C2S:TeleportConfirm", "确认传送 - 接受传送", "Teleport confirm - Accept teleport");
      putDesc("C2S:QueryBlockNBT", "查询方块NBT - 获取方块数据", "Query block NBT - Get block data");
      putDesc("C2S:QueryEntityNBT", "查询实体NBT - 获取实体数据", "Query entity NBT - Get entity data");
      putDesc("C2S:EditBook", "编辑书 - 写书", "Edit book - Write in book");
      putDesc("C2S:RecipeBookData", "配方书数据 - 解锁配方", "Recipe book data - Unlock recipes");
      putDesc("C2S:AdvancementTab", "进度标签 - 打开进度", "Advancement tab - Open advancements");
      putDesc("C2S:CommandSuggestion", "命令建议 - Tab补全", "Command suggestion - Tab completion");
      putDesc("C2S:ClientSettings", "客户端设置 - 语言、视野等", "Client settings - Language, render distance");
      putDesc("C2S:ClientStatus", "客户端状态 - 就绪、重生等", "Client status - Ready, respawn");
      putDesc("C2S:ResourcePack", "资源包 - 接受资源包", "Resource pack - Accept resource pack");
      putDesc("C2S:Pong", "Ping响应 - 延迟响应", "Pong - Latency response");
      S2C_PACKETS = Arrays.asList("S2C:Login", "S2C:Disconnect", "S2C:KeepAlive", "S2C:PlayerInfo", "S2C:PlayerPosLook", "S2C:ChatMessage", "S2C:TimeUpdate", "S2C:EntityStatus", "S2C:EntityMetadata", "S2C:EntityVelocity", "S2C:EntityTeleport", "S2C:EntityEquipment", "S2C:EntityEffect", "S2C:RemoveEntityEffect", "S2C:SetHealth", "S2C:Respawn", "S2C:PlayerAbilities", "S2C:HeldItemChange", "S2C:WindowItems", "S2C:WindowProperty", "S2C:SetSlot", "S2C:OpenWindow", "S2C:CloseWindow", "S2C:BlockUpdate", "S2C:MultiBlockChange", "S2C:ChunkData", "S2C:UnloadChunk", "S2C:SpawnEntity", "S2C:SpawnExperienceOrb", "S2C:SpawnLivingEntity", "S2C:SpawnPainting", "S2C:DestroyEntities", "S2C:CollectItem", "S2C:Explosion", "S2C:SoundEffect", "S2C:Particle", "S2C:GameStateChange", "S2C:UpdateScore", "S2C:UpdateObjective", "S2C:UpdateTeams", "S2C:Title", "S2C:TabList", "S2C:WorldBorder", "S2C:MapData", "S2C:Advancements", "S2C:CommandTree", "S2C:LookAt", "S2C:SyncRecipeBook", "S2C:Tags");
      C2S_PACKETS = Arrays.asList("C2S:Login", "C2S:KeepAlive", "C2S:ChatMessage", "C2S:PlayerAction", "C2S:PlayerInput", "C2S:PlayerMovement", "C2S:PlayerPosition", "C2S:PlayerRotation", "C2S:PlayerPositionRotation", "C2S:PlayerAbilities", "C2S:HeldItemChange", "C2S:Animation", "C2S:UseEntity", "C2S:UseItem", "C2S:UseItemOn", "C2S:ClickWindow", "C2S:CloseWindow", "C2S:CreativeInventoryAction", "C2S:EnchantItem", "C2S:PickItem", "C2S:SelectTrade", "C2S:RenameItem", "C2S:UpdateCommandBlock", "C2S:UpdateSign", "C2S:UpdateStructureBlock", "C2S:UpdateJigsawBlock", "C2S:UpdateBeacon", "C2S:TeleportConfirm", "C2S:QueryBlockNBT", "C2S:QueryEntityNBT", "C2S:EditBook", "C2S:RecipeBookData", "C2S:AdvancementTab", "C2S:CommandSuggestion", "C2S:ClientSettings", "C2S:ClientStatus", "C2S:ResourcePack", "C2S:Pong");
   }

   public static enum Tab {
      S2C,
      C2S;

      // $FF: synthetic method
      private static Tab[] $values() {
         return new Tab[]{S2C, C2S};
      }
   }

   private static class PacketEntry {
      String name;
      boolean selected;
      String description;
      String englishDesc;

      PacketEntry(String name, boolean selected, String description, String englishDesc) {
         this.name = name;
         this.selected = selected;
         this.description = description;
         this.englishDesc = englishDesc;
      }
   }
}
