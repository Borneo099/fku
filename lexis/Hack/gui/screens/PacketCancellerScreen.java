package lexis.Hack.gui.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lexis.Hack.Hacks.Misc.PacketCancellerHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PacketCancellerScreen extends Screen {
   private final PacketCancellerHack hack;
   private final Screen parent;
   private final Tab currentTab;
   private List s2cPackets = new ArrayList();
   private List c2sPackets = new ArrayList();
   private EditBox searchBox;
   private String searchText = "";
   private int scrollOffset = 0;
   private static final int ITEM_HEIGHT = 22;
   private static final int VISIBLE_ITEMS = 15;
   private static final File CONFIG_DIR = new File("C:/karucn/Lexis/config/hack/");
   private static final File CONFIG_FILE;
   private static final Gson GSON;

   public PacketCancellerScreen(PacketCancellerHack hack, Screen parent, Tab initialTab) {
      super(Component.m_237113_("数据包取消器 - " + (initialTab == PacketCancellerScreen.Tab.S2C ? "S2C" : "C2S")));
      this.hack = hack;
      this.parent = parent;
      this.currentTab = initialTab;
      this.loadPackets();
      this.loadConfig();
   }

   private void loadPackets() {
      this.s2cPackets.add(new PacketEntry("S2C:Login", false, "§7登录包\n§f服务器确认你的登录", "§7Login packet\n§fServer confirms your login"));
      this.s2cPackets.add(new PacketEntry("S2C:Disconnect", false, "§7断开连接\n§f服务器踢出你时的消息", "§7Disconnect packet\n§fMessage when server kicks you"));
      this.s2cPackets.add(new PacketEntry("S2C:KeepAlive", false, "§7心跳包\n§f保持连接活跃，取消会被踢", "§7Keep alive packet\n§fMaintains connection, cancelling will get you kicked"));
      this.s2cPackets.add(new PacketEntry("S2C:PlayerInfo", false, "§7玩家信息\n§f玩家列表、皮肤、名称等", "§7Player info\n§fPlayer list, skins, names"));
      this.s2cPackets.add(new PacketEntry("S2C:PlayerPosLook", false, "§7强制移动\n§f服务器强制你移动到某位置（防作弊）", "§7Player position look\n§fServer forces you to move (anti-cheat)"));
      this.s2cPackets.add(new PacketEntry("S2C:ChatMessage", false, "§7聊天消息\n§f收到的所有聊天内容", "§7Chat message\n§fAll incoming chat messages"));
      this.s2cPackets.add(new PacketEntry("S2C:TimeUpdate", false, "§7时间更新\n§f游戏内时间变化", "§7Time update\n§fGame time changes"));
      this.s2cPackets.add(new PacketEntry("S2C:EntityStatus", false, "§7实体状态\n§f实体受伤、死亡、动作等", "§7Entity status\n§fEntity hurt, death, animations"));
      this.s2cPackets.add(new PacketEntry("S2C:EntityMetadata", false, "§7实体数据\n§f实体名称、状态效果等", "§7Entity metadata\n§fEntity name, potion effects"));
      this.s2cPackets.add(new PacketEntry("S2C:EntityVelocity", false, "§7实体速度\n§f击退、爆炸、弹射物速度", "§7Entity velocity\n§fKnockback, explosion, projectile speed"));
      this.s2cPackets.add(new PacketEntry("S2C:EntityTeleport", false, "§7实体传送\n§f实体瞬间移动", "§7Entity teleport\n§fEntity teleportation"));
      this.s2cPackets.add(new PacketEntry("S2C:EntityEquipment", false, "§7实体装备\n§f实体手持/穿戴的物品", "§7Entity equipment\n§fItems held/worn by entity"));
      this.s2cPackets.add(new PacketEntry("S2C:EntityEffect", false, "§7实体效果\n§f药水效果添加", "§7Entity effect\n§fPotion effect added"));
      this.s2cPackets.add(new PacketEntry("S2C:RemoveEntityEffect", false, "§7移除效果\n§f药水效果移除", "§7Remove entity effect\n§fPotion effect removed"));
      this.s2cPackets.add(new PacketEntry("S2C:SetHealth", false, "§7设置血量\n§f生命值、饥饿值、饱食度", "§7Set health\n§fHealth, hunger, saturation"));
      this.s2cPackets.add(new PacketEntry("S2C:Respawn", false, "§7重生\n§f死亡后重生，取消后就像极限模式", "§7Respawn\n§fRespawn after death, cancelling = hardcore mode"));
      this.s2cPackets.add(new PacketEntry("S2C:PlayerAbilities", false, "§7玩家能力\n§f飞行、创造模式等", "§7Player abilities\n§fFlying, creative mode etc"));
      this.s2cPackets.add(new PacketEntry("S2C:HeldItemChange", false, "§7手持物品\n§f其他玩家切换物品栏", "§7Held item change\n§fOther players switching items"));
      this.s2cPackets.add(new PacketEntry("S2C:WindowItems", false, "§7容器物品\n§f箱子、背包内所有物品", "§7Window items\n§fAll items in chest/inventory"));
      this.s2cPackets.add(new PacketEntry("S2C:WindowProperty", false, "§7容器属性\n§f熔炉进度、附魔等级等", "§7Window property\n§fFurnace progress, enchantment levels"));
      this.s2cPackets.add(new PacketEntry("S2C:SetSlot", false, "§7设置格子\n§f单个物品槽更新", "§7Set slot\n§fSingle slot update"));
      this.s2cPackets.add(new PacketEntry("S2C:OpenWindow", false, "§7打开容器\n§f打开箱子、工作台等界面", "§7Open window\n§fOpen chest, crafting table"));
      this.s2cPackets.add(new PacketEntry("S2C:CloseWindow", false, "§7关闭容器\n§f关闭界面", "§7Close window\n§fClose GUI"));
      this.s2cPackets.add(new PacketEntry("S2C:BlockUpdate", false, "§7方块更新\n§f单个方块变化", "§7Block update\n§fSingle block change"));
      this.s2cPackets.add(new PacketEntry("S2C:MultiBlockChange", false, "§7多方块更新\n§f多个方块同时变化", "§7Multi block change\n§fMultiple blocks changed"));
      this.s2cPackets.add(new PacketEntry("S2C:ChunkData", false, "§7区块数据\n§f加载地形", "§7Chunk data\n§fLoad terrain"));
      this.s2cPackets.add(new PacketEntry("S2C:UnloadChunk", false, "§7卸载区块\n§f卸载地形", "§7Unload chunk\n§fUnload terrain"));
      this.s2cPackets.add(new PacketEntry("S2C:SpawnEntity", false, "§7生成实体\n§f生成生物、物品等", "§7Spawn entity\n§fSpawn mobs, items"));
      this.s2cPackets.add(new PacketEntry("S2C:SpawnExperienceOrb", false, "§7生成经验球\n§f经验掉落", "§7Spawn experience orb\n§fXP orbs"));
      this.s2cPackets.add(new PacketEntry("S2C:SpawnLivingEntity", false, "§7生成生物\n§f动物、怪物", "§7Spawn living entity\n§fAnimals, monsters"));
      this.s2cPackets.add(new PacketEntry("S2C:SpawnPainting", false, "§7生成画\n§f放置画", "§7Spawn painting\n§fPlace painting"));
      this.s2cPackets.add(new PacketEntry("S2C:DestroyEntities", false, "§7销毁实体\n§f实体消失", "§7Destroy entities\n§fEntities removed"));
      this.s2cPackets.add(new PacketEntry("S2C:CollectItem", false, "§7拾取物品\n§f玩家捡起物品", "§7Collect item\n§fPlayer picks up item"));
      this.s2cPackets.add(new PacketEntry("S2C:Explosion", false, "§7爆炸\n§fTNT、苦力怕爆炸", "§7Explosion\n§fTNT, creeper explosion"));
      this.s2cPackets.add(new PacketEntry("S2C:SoundEffect", false, "§7音效\n§f游戏声音", "§7Sound effect\n§fGame sounds"));
      this.s2cPackets.add(new PacketEntry("S2C:Particle", false, "§7粒子效果\n§f破坏方块、药水等粒子", "§7Particle\n§fBlock break, potion particles"));
      this.s2cPackets.add(new PacketEntry("S2C:GameStateChange", false, "§7游戏状态\n§f天气、游戏模式等", "§7Game state change\n§fWeather, gamemode"));
      this.s2cPackets.add(new PacketEntry("S2C:UpdateScore", false, "§7更新分数\n§f计分板分数", "§7Update score\n§fScoreboard scores"));
      this.s2cPackets.add(new PacketEntry("S2C:UpdateObjective", false, "§7更新目标\n§f计分板目标", "§7Update objective\n§fScoreboard objectives"));
      this.s2cPackets.add(new PacketEntry("S2C:UpdateTeams", false, "§7更新队伍\n§f队伍颜色、成员", "§7Update teams\n§fTeam colors, members"));
      this.s2cPackets.add(new PacketEntry("S2C:Title", false, "§7标题\n§f屏幕大标题", "§7Title\n§fScreen title messages"));
      this.s2cPackets.add(new PacketEntry("S2C:TabList", false, "§7TAB列表\n§f玩家列表头尾", "§7Tab list\n§fPlayer list header/footer"));
      this.s2cPackets.add(new PacketEntry("S2C:WorldBorder", false, "§7世界边界\n§f边界大小", "§7World border\n§fBorder size"));
      this.s2cPackets.add(new PacketEntry("S2C:MapData", false, "§7地图数据\n§f地图内容", "§7Map data\n§fMap contents"));
      this.s2cPackets.add(new PacketEntry("S2C:Advancements", false, "§7进度\n§f成就/进度", "§7Advancements\n§fAchievements/progress"));
      this.s2cPackets.add(new PacketEntry("S2C:CommandTree", false, "§7命令树\n§f命令补全", "§7Command tree\n§fCommand suggestions"));
      this.s2cPackets.add(new PacketEntry("S2C:LookAt", false, "§7看向\n§f强制转头", "§7Look at\n§fForced look direction"));
      this.s2cPackets.add(new PacketEntry("S2C:SyncRecipeBook", false, "§7同步配方\n§f解锁配方", "§7Sync recipe book\n§fUnlock recipes"));
      this.s2cPackets.add(new PacketEntry("S2C:Tags", false, "§7标签\n§f游戏标签", "§7Tags\n§fGame tags"));
      this.c2sPackets.add(new PacketEntry("C2S:Login", false, "§7登录包\n§f告诉服务器你是谁，取消无法连接", "§7Login packet\n§fTell server who you are, cancelling = no connection"));
      this.c2sPackets.add(new PacketEntry("C2S:KeepAlive", false, "§7心跳包\n§f告诉服务器你还活着，取消会被踢", "§7Keep alive\n§fTell server you're alive, cancelling = kicked"));
      this.c2sPackets.add(new PacketEntry("C2S:ChatMessage", false, "§7聊天消息\n§f你发送的聊天", "§7Chat message\n§fMessages you send"));
      this.c2sPackets.add(new PacketEntry("C2S:PlayerAction", false, "§7玩家动作\n§f挖方块、放方块等", "§7Player action\n§fMining, placing blocks"));
      this.c2sPackets.add(new PacketEntry("C2S:PlayerInput", false, "§7玩家输入\n§f移动输入（WASD）", "§7Player input\n§fMovement input (WASD)"));
      this.c2sPackets.add(new PacketEntry("C2S:PlayerMovement", false, "§7玩家移动\n§f位置+旋转", "§7Player movement\n§fPosition + rotation"));
      this.c2sPackets.add(new PacketEntry("C2S:PlayerPosition", false, "§7玩家位置\n§f只发送位置", "§7Player position\n§fPosition only"));
      this.c2sPackets.add(new PacketEntry("C2S:PlayerRotation", false, "§7玩家旋转\n§f只发送视角", "§7Player rotation\n§fRotation only"));
      this.c2sPackets.add(new PacketEntry("C2S:PlayerPositionRotation", false, "§7位置+旋转\n§f同时发送", "§7Player position rotation\n§fBoth position and rotation"));
      this.c2sPackets.add(new PacketEntry("C2S:PlayerAbilities", false, "§7玩家能力\n§f飞行切换", "§7Player abilities\n§fToggle flying"));
      this.c2sPackets.add(new PacketEntry("C2S:HeldItemChange", false, "§7手持物品\n§f切换物品栏", "§7Held item change\n§fSwitch hotbar slot"));
      this.c2sPackets.add(new PacketEntry("C2S:Animation", false, "§7撸手动画\n§f挥动手臂", "§7Animation\n§fSwing arm"));
      this.c2sPackets.add(new PacketEntry("C2S:UseEntity", false, "§7使用实体\n§f攻击、交互实体", "§7Use entity\n§fAttack, interact with entity"));
      this.c2sPackets.add(new PacketEntry("C2S:UseItem", false, "§7使用物品\n§f右键物品（空气）", "§7Use item\n§fRight click item (air)"));
      this.c2sPackets.add(new PacketEntry("C2S:UseItemOn", false, "§7对块使用\n§f右键方块", "§7Use item on\n§fRight click block"));
      this.c2sPackets.add(new PacketEntry("C2S:ClickWindow", false, "§7点击容器\n§f移动物品", "§7Click window\n§fMove items in inventory"));
      this.c2sPackets.add(new PacketEntry("C2S:CloseWindow", false, "§7关闭容器\n§f关闭界面", "§7Close window\n§fClose GUI"));
      this.c2sPackets.add(new PacketEntry("C2S:CreativeInventoryAction", false, "§7创造模式物品\n§f创造模式拿东西", "§7Creative inventory action\n§fCreative mode item spawning"));
      this.c2sPackets.add(new PacketEntry("C2S:EnchantItem", false, "§7附魔\n§f附魔台点击", "§7Enchant item\n§fEnchantment table click"));
      this.c2sPackets.add(new PacketEntry("C2S:PickItem", false, "§7选取物品\n§f中键选取", "§7Pick item\n§fMiddle click pick block"));
      this.c2sPackets.add(new PacketEntry("C2S:SelectTrade", false, "§7选择交易\n§f村民交易", "§7Select trade\n§fVillager trading"));
      this.c2sPackets.add(new PacketEntry("C2S:RenameItem", false, "§7重命名物品\n§f铁砧改名", "§7Rename item\n§fAnvil renaming"));
      this.c2sPackets.add(new PacketEntry("C2S:UpdateCommandBlock", false, "§7更新命令块\n§f修改命令方块", "§7Update command block\n§fEdit command block"));
      this.c2sPackets.add(new PacketEntry("C2S:UpdateSign", false, "§7更新告示牌\n§f写牌子", "§7Update sign\n§fWrite on sign"));
      this.c2sPackets.add(new PacketEntry("C2S:UpdateStructureBlock", false, "§7更新结构块\n§f修改结构方块", "§7Update structure block\n§fEdit structure block"));
      this.c2sPackets.add(new PacketEntry("C2S:UpdateJigsawBlock", false, "§7更新拼图块\n§f修改拼图方块", "§7Update jigsaw block\n§fEdit jigsaw block"));
      this.c2sPackets.add(new PacketEntry("C2S:UpdateBeacon", false, "§7更新信标\n§f设置信标效果", "§7Update beacon\n§fSet beacon effects"));
      this.c2sPackets.add(new PacketEntry("C2S:TeleportConfirm", false, "§7确认传送\n§f接受传送", "§7Teleport confirm\n§fAccept teleport"));
      this.c2sPackets.add(new PacketEntry("C2S:QueryBlockNBT", false, "§7查询方块NBT\n§f获取方块数据", "§7Query block NBT\n§fGet block data"));
      this.c2sPackets.add(new PacketEntry("C2S:QueryEntityNBT", false, "§7查询实体NBT\n§f获取实体数据", "§7Query entity NBT\n§fGet entity data"));
      this.c2sPackets.add(new PacketEntry("C2S:EditBook", false, "§7编辑书\n§f写书", "§7Edit book\n§fWrite in book"));
      this.c2sPackets.add(new PacketEntry("C2S:RecipeBookData", false, "§7配方书数据\n§f解锁配方", "§7Recipe book data\n§fUnlock recipes"));
      this.c2sPackets.add(new PacketEntry("C2S:AdvancementTab", false, "§7进度标签\n§f打开进度", "§7Advancement tab\n§fOpen advancements"));
      this.c2sPackets.add(new PacketEntry("C2S:CommandSuggestion", false, "§7命令建议\n§fTab补全", "§7Command suggestion\n§fTab completion"));
      this.c2sPackets.add(new PacketEntry("C2S:ClientSettings", false, "§7客户端设置\n§f语言、视野等", "§7Client settings\n§fLanguage, render distance"));
      this.c2sPackets.add(new PacketEntry("C2S:ClientStatus", false, "§7客户端状态\n§f就绪、重生等", "§7Client status\n§fReady, respawn"));
      this.c2sPackets.add(new PacketEntry("C2S:ResourcePack", false, "§7资源包\n§f接受资源包", "§7Resource pack\n§fAccept resource pack"));
      this.c2sPackets.add(new PacketEntry("C2S:Pong", false, "§7Ping响应\n§f延迟响应", "§7Pong\n§fLatency response"));
      this.s2cPackets.sort(Comparator.comparing((a) -> {
         return a.name;
      }));
      this.c2sPackets.sort(Comparator.comparing((a) -> {
         return a.name;
      }));
   }

   private void loadConfig() {
      try {
         if (!CONFIG_FILE.exists()) {
            return;
         }

         FileReader reader = new FileReader(CONFIG_FILE);
         Map data = (Map)GSON.fromJson(reader, Map.class);
         reader.close();
         if (data == null) {
            return;
         }

         List enabledC2S;
         Iterator var4;
         PacketEntry entry;
         if (data.containsKey("s2c")) {
            enabledC2S = (List)data.get("s2c");

            for(var4 = this.s2cPackets.iterator(); var4.hasNext(); entry.enabled = enabledC2S.contains(entry.name)) {
               entry = (PacketEntry)var4.next();
            }
         }

         if (data.containsKey("c2s")) {
            enabledC2S = (List)data.get("c2s");

            for(var4 = this.c2sPackets.iterator(); var4.hasNext(); entry.enabled = enabledC2S.contains(entry.name)) {
               entry = (PacketEntry)var4.next();
            }
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   private void saveConfig() {
      try {
         CONFIG_DIR.mkdirs();
         Map data = new HashMap();
         List enabledS2C = new ArrayList();
         Iterator var3 = this.s2cPackets.iterator();

         while(var3.hasNext()) {
            PacketEntry entry = (PacketEntry)var3.next();
            if (entry.enabled) {
               enabledS2C.add(entry.name);
            }
         }

         data.put("s2c", enabledS2C);
         List enabledC2S = new ArrayList();
         Iterator var8 = this.c2sPackets.iterator();

         while(var8.hasNext()) {
            PacketEntry entry = (PacketEntry)var8.next();
            if (entry.enabled) {
               enabledC2S.add(entry.name);
            }
         }

         data.put("c2s", enabledC2S);
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(data, writer);
         writer.close();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   private List getCurrentList() {
      return this.currentTab == PacketCancellerScreen.Tab.S2C ? this.s2cPackets : this.c2sPackets;
   }

   private List getFilteredList() {
      List list = this.getCurrentList();
      return this.searchText.isEmpty() ? list : list.stream().filter((e) -> {
         return e.name.toLowerCase().contains(this.searchText);
      }).toList();
   }

   protected void m_7856_() {
      super.m_7856_();
      int centerX = this.f_96543_ / 2;
      int y = 20;
      this.searchBox = new EditBox(this.f_96547_, centerX - 150, y, 300, 20, Component.m_237113_(""));
      this.searchBox.m_94199_(50);
      this.searchBox.m_94151_((text) -> {
         this.searchText = text.toLowerCase();
         this.scrollOffset = 0;
      });
      this.m_142416_(this.searchBox);
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("全选"), (btn) -> {
         PacketEntry entry;
         for(Iterator var2 = this.getCurrentList().iterator(); var2.hasNext(); entry.enabled = true) {
            entry = (PacketEntry)var2.next();
         }

      }).m_252987_(centerX - 150, y, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("取消全选"), (btn) -> {
         PacketEntry entry;
         for(Iterator var2 = this.getCurrentList().iterator(); var2.hasNext(); entry.enabled = false) {
            entry = (PacketEntry)var2.next();
         }

      }).m_252987_(centerX - 60, y, 80, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("反选"), (btn) -> {
         PacketEntry entry;
         for(Iterator var2 = this.getCurrentList().iterator(); var2.hasNext(); entry.enabled = !entry.enabled) {
            entry = (PacketEntry)var2.next();
         }

      }).m_252987_(centerX + 30, y, 60, 20).m_253136_());
      y += 25;
      this.m_142416_(Button.m_253074_(Component.m_237113_("保存"), (btn) -> {
         this.saveConfig();
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
      String tabTitle = this.currentTab == PacketCancellerScreen.Tab.S2C ? "服务端→客户端数据包" : "客户端→服务端数据包";
      gui.m_280488_(this.f_96547_, tabTitle, centerX - 150, startY - 12, 16777130);
      List filtered = this.getFilteredList();
      gui.m_280509_(centerX - 152, startY - 2, centerX + 152, startY + 330 + 2, -1439485133);
      PacketEntry hoveredEntry = null;

      int scrollbarX;
      int y;
      int bgColor;
      String line;
      for(scrollbarX = 0; scrollbarX < 15; ++scrollbarX) {
         int index = this.scrollOffset + scrollbarX;
         if (index >= filtered.size()) {
            break;
         }

         PacketEntry entry = (PacketEntry)filtered.get(index);
         y = startY + scrollbarX * 22;
         boolean hovered = mouseX >= centerX - 150 && mouseX <= centerX + 150 && mouseY >= y && mouseY <= y + 22 - 2;
         if (entry.enabled) {
            bgColor = -1437814960;
         } else if (hovered) {
            bgColor = -1436129690;
         } else {
            bgColor = -1439485133;
         }

         gui.m_280509_(centerX - 150, y, centerX + 150, y + 22 - 2, bgColor);
         gui.m_280509_(centerX - 150, y, centerX - 149, y + 22 - 2, -7829368);
         gui.m_280509_(centerX + 149, y, centerX + 150, y + 22 - 2, -7829368);
         gui.m_280488_(this.f_96547_, entry.name, centerX - 140, y + 5, entry.enabled ? 16777215 : 13421772);
         line = entry.enabled ? "✓" : "✗";
         gui.m_280488_(this.f_96547_, line, centerX + 135, y + 5, entry.enabled ? 11206570 : 16755370);
         if (hovered) {
            hoveredEntry = entry;
         }
      }

      int sliderHeight;
      if (filtered.size() > 15) {
         scrollbarX = centerX + 155;
         int scrollbarHeight = 330;
         gui.m_280509_(scrollbarX, startY, scrollbarX + 4, startY + scrollbarHeight, -1436129690);
         float scrollPercent = (float)this.scrollOffset / (float)(filtered.size() - 15);
         sliderHeight = Math.max(20, (int)((float)(scrollbarHeight * 15) / (float)filtered.size()));
         bgColor = startY + (int)(scrollPercent * (float)(scrollbarHeight - sliderHeight));
         gui.m_280509_(scrollbarX, bgColor, scrollbarX + 4, bgColor + sliderHeight, -5592406);
      }

      if (hoveredEntry != null) {
         List tooltip = new ArrayList();
         String[] chineseLines = hoveredEntry.description.split("\n");
         String[] englishLines = chineseLines;
         y = chineseLines.length;

         for(sliderHeight = 0; sliderHeight < y; ++sliderHeight) {
            String line = englishLines[sliderHeight];
            tooltip.add(Component.m_237113_(line));
         }

         tooltip.add(Component.m_237113_(""));
         englishLines = hoveredEntry.englishDesc.split("\n");
         String[] var22 = englishLines;
         sliderHeight = englishLines.length;

         for(bgColor = 0; bgColor < sliderHeight; ++bgColor) {
            line = var22[bgColor];
            tooltip.add(Component.m_237113_(line));
         }

         gui.m_280666_(this.f_96547_, tooltip, mouseX, mouseY);
      }

   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (super.m_6375_(mouseX, mouseY, button)) {
         return true;
      } else {
         int centerX = this.f_96543_ / 2;
         int startY = 95;
         List filtered = this.getFilteredList();

         for(int i = 0; i < 15; ++i) {
            int index = this.scrollOffset + i;
            if (index >= filtered.size()) {
               break;
            }

            int y = startY + i * 22;
            if (mouseX >= (double)(centerX - 150) && mouseX <= (double)(centerX + 150) && mouseY >= (double)y && mouseY <= (double)(y + 22 - 2)) {
               ((PacketEntry)filtered.get(index)).enabled = !((PacketEntry)filtered.get(index)).enabled;
               return true;
            }
         }

         return false;
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      int centerX = this.f_96543_ / 2;
      int startY = 95;
      List filtered = this.getFilteredList();
      if (mouseX >= (double)(centerX - 152) && mouseX <= (double)(centerX + 152) && mouseY >= (double)(startY - 2) && mouseY <= (double)(startY + 330 + 2)) {
         int maxScroll = Math.max(0, filtered.size() - 15);
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
      if (this.parent != null) {
         Minecraft.m_91087_().m_91152_(this.parent);
      } else {
         Minecraft.m_91087_().m_91152_((Screen)null);
      }

   }

   public boolean m_7043_() {
      return false;
   }

   static {
      CONFIG_FILE = new File(CONFIG_DIR, "packet_canceller.json");
      GSON = (new GsonBuilder()).setPrettyPrinting().create();
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
      boolean enabled;
      String description;
      String englishDesc;

      PacketEntry(String name, boolean enabled, String description, String englishDesc) {
         this.name = name;
         this.enabled = enabled;
         this.description = description;
         this.englishDesc = englishDesc;
      }
   }
}
