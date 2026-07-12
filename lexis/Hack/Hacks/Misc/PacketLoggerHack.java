package lexis.Hack.Hacks.Misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketLoggerEvent;
import lexis.Hack.events.PacketLoggerListener;
import lexis.Hack.gui.screens.PacketLoggerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketLoggerHack extends Hack implements PacketLoggerListener {
   private static final Logger LOGGER = LogManager.getLogger("PacketLogger");
   private static final File CONFIG_DIR = new File("C:/karucn/Lexis/config/hack/");
   private static final File CONFIG_FILE;
   private static final Gson GSON;
   private static final String CONFIG_KEY = "数据包记录器";
   private HackConfig config;
   private boolean logToChat = true;
   private boolean logToFile = false;
   private Set monitoredS2C = new HashSet();
   private Set monitoredC2S = new HashSet();
   private static final Map PACKET_DESCRIPTIONS;

   private static void initDescriptions() {
      putDesc("S2C:Login", "登录包 - 服务器确认你的登录");
      putDesc("S2C:Disconnect", "断开连接 - 服务器踢出你时的消息");
      putDesc("S2C:KeepAlive", "心跳包 - 保持连接活跃，取消会被踢");
      putDesc("S2C:PlayerInfo", "玩家信息 - 玩家列表、皮肤、名称等");
      putDesc("S2C:PlayerPosLook", "强制移动 - 服务器强制你移动到某位置（防作弊）");
      putDesc("S2C:ChatMessage", "聊天消息 - 收到的所有聊天内容");
      putDesc("S2C:TimeUpdate", "时间更新 - 游戏内时间变化");
      putDesc("S2C:EntityStatus", "实体状态 - 实体受伤、死亡、动作等");
      putDesc("S2C:EntityMetadata", "实体数据 - 实体名称、状态效果等");
      putDesc("S2C:EntityVelocity", "实体速度 - 击退、爆炸、弹射物速度");
      putDesc("S2C:EntityTeleport", "实体传送 - 实体瞬间移动");
      putDesc("S2C:EntityEquipment", "实体装备 - 实体手持/穿戴的物品");
      putDesc("S2C:EntityEffect", "实体效果 - 药水效果添加");
      putDesc("S2C:RemoveEntityEffect", "移除效果 - 药水效果移除");
      putDesc("S2C:SetHealth", "设置血量 - 生命值、饥饿值、饱食度");
      putDesc("S2C:Respawn", "重生 - 死亡后重生，取消后就像极限模式");
      putDesc("S2C:PlayerAbilities", "玩家能力 - 飞行、创造模式等");
      putDesc("S2C:HeldItemChange", "手持物品 - 其他玩家切换物品栏");
      putDesc("S2C:WindowItems", "容器物品 - 箱子、背包内所有物品");
      putDesc("S2C:WindowProperty", "容器属性 - 熔炉进度、附魔等级等");
      putDesc("S2C:SetSlot", "设置格子 - 单个物品槽更新");
      putDesc("S2C:OpenWindow", "打开容器 - 打开箱子、工作台等界面");
      putDesc("S2C:CloseWindow", "关闭容器 - 关闭界面");
      putDesc("S2C:BlockUpdate", "方块更新 - 单个方块变化");
      putDesc("S2C:MultiBlockChange", "多方块更新 - 多个方块同时变化");
      putDesc("S2C:ChunkData", "区块数据 - 加载地形");
      putDesc("S2C:UnloadChunk", "卸载区块 - 卸载地形");
      putDesc("S2C:SpawnEntity", "生成实体 - 生成生物、物品等");
      putDesc("S2C:SpawnExperienceOrb", "生成经验球 - 经验掉落");
      putDesc("S2C:SpawnLivingEntity", "生成生物 - 动物、怪物");
      putDesc("S2C:SpawnPainting", "生成画 - 放置画");
      putDesc("S2C:DestroyEntities", "销毁实体 - 实体消失");
      putDesc("S2C:CollectItem", "拾取物品 - 玩家捡起物品");
      putDesc("S2C:Explosion", "爆炸 - TNT、苦力怕爆炸");
      putDesc("S2C:SoundEffect", "音效 - 游戏声音");
      putDesc("S2C:Particle", "粒子效果 - 破坏方块、药水等粒子");
      putDesc("S2C:GameStateChange", "游戏状态 - 天气、游戏模式等");
      putDesc("S2C:UpdateScore", "更新分数 - 计分板分数");
      putDesc("S2C:UpdateObjective", "更新目标 - 计分板目标");
      putDesc("S2C:UpdateTeams", "更新队伍 - 队伍颜色、成员");
      putDesc("S2C:Title", "标题 - 屏幕大标题");
      putDesc("S2C:TabList", "TAB列表 - 玩家列表头尾");
      putDesc("S2C:WorldBorder", "世界边界 - 边界大小");
      putDesc("S2C:MapData", "地图数据 - 地图内容");
      putDesc("S2C:Advancements", "进度 - 成就/进度");
      putDesc("S2C:CommandTree", "命令树 - 命令补全");
      putDesc("S2C:LookAt", "看向 - 强制转头");
      putDesc("S2C:SyncRecipeBook", "同步配方 - 解锁配方");
      putDesc("S2C:Tags", "标签 - 游戏标签");
      putDesc("C2S:Login", "登录包 - 告诉服务器你是谁，取消无法连接");
      putDesc("C2S:KeepAlive", "心跳包 - 告诉服务器你还活着，取消会被踢");
      putDesc("C2S:ChatMessage", "聊天消息 - 你发送的聊天");
      putDesc("C2S:PlayerAction", "玩家动作 - 挖方块、放方块等");
      putDesc("C2S:PlayerInput", "玩家输入 - 移动输入（WASD）");
      putDesc("C2S:PlayerMovement", "玩家移动 - 位置+旋转");
      putDesc("C2S:PlayerPosition", "玩家位置 - 只发送位置");
      putDesc("C2S:PlayerRotation", "玩家旋转 - 只发送视角");
      putDesc("C2S:PlayerPositionRotation", "位置+旋转 - 同时发送");
      putDesc("C2S:PlayerAbilities", "玩家能力 - 飞行切换");
      putDesc("C2S:HeldItemChange", "手持物品 - 切换物品栏");
      putDesc("C2S:Animation", "撸手动画 - 挥动手臂");
      putDesc("C2S:UseEntity", "使用实体 - 攻击、交互实体");
      putDesc("C2S:UseItem", "使用物品 - 右键物品（空气）");
      putDesc("C2S:UseItemOn", "对块使用 - 右键方块");
      putDesc("C2S:ClickWindow", "点击容器 - 移动物品");
      putDesc("C2S:CloseWindow", "关闭容器 - 关闭界面");
      putDesc("C2S:CreativeInventoryAction", "创造模式物品 - 创造模式拿东西");
      putDesc("C2S:EnchantItem", "附魔 - 附魔台点击");
      putDesc("C2S:PickItem", "选取物品 - 中键选取");
      putDesc("C2S:SelectTrade", "选择交易 - 村民交易");
      putDesc("C2S:RenameItem", "重命名物品 - 铁砧改名");
      putDesc("C2S:UpdateCommandBlock", "更新命令块 - 修改命令方块");
      putDesc("C2S:UpdateSign", "更新告示牌 - 写牌子");
      putDesc("C2S:UpdateStructureBlock", "更新结构块 - 修改结构方块");
      putDesc("C2S:UpdateJigsawBlock", "更新拼图块 - 修改拼图方块");
      putDesc("C2S:UpdateBeacon", "更新信标 - 设置信标效果");
      putDesc("C2S:TeleportConfirm", "确认传送 - 接受传送");
      putDesc("C2S:QueryBlockNBT", "查询方块NBT - 获取方块数据");
      putDesc("C2S:QueryEntityNBT", "查询实体NBT - 获取实体数据");
      putDesc("C2S:EditBook", "编辑书 - 写书");
      putDesc("C2S:RecipeBookData", "配方书数据 - 解锁配方");
      putDesc("C2S:AdvancementTab", "进度标签 - 打开进度");
      putDesc("C2S:CommandSuggestion", "命令建议 - Tab补全");
      putDesc("C2S:ClientSettings", "客户端设置 - 语言、视野等");
      putDesc("C2S:ClientStatus", "客户端状态 - 就绪、重生等");
      putDesc("C2S:ResourcePack", "资源包 - 接受资源包");
      putDesc("C2S:Pong", "Ping响应 - 延迟响应");
   }

   private static void putDesc(String name, String desc) {
      PACKET_DESCRIPTIONS.put(name, desc);
   }

   public PacketLoggerHack() {
      super("数据包记录器", "记录指定数据包的发送/接收", Hack.Category.MISC, true);
      this.addSetting(new Hack.Setting("记录到聊天", "将数据包信息输出到聊天窗口", true));
      this.addSetting(new Hack.Setting("记录到日志", "将数据包信息写入原版日志文件(latest.log)", false));
      this.addSetting(new Hack.Setting("打开S2C设置", "选择要记录的服务端→客户端数据包", "打开S2C", () -> {
         if (mc != null) {
            mc.m_91152_(new PacketLoggerScreen(this, mc.f_91080_, PacketLoggerScreen.Tab.S2C));
         }

      }));
      this.addSetting(new Hack.Setting("打开C2S设置", "选择要记录的客户端→服务端数据包", "打开C2S", () -> {
         if (mc != null) {
            mc.m_91152_(new PacketLoggerScreen(this, mc.f_91080_, PacketLoggerScreen.Tab.C2S));
         }

      }));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      this.loadMonitoredPackets();
   }

   private void loadConfig() {
      this.logToChat = this.config.getBooleanSetting("数据包记录器", "记录到聊天", true);
      this.logToFile = this.config.getBooleanSetting("数据包记录器", "记录到日志", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "记录到聊天":
               setting.setValue(this.logToChat);
               break;
            case "记录到日志":
               setting.setValue(this.logToFile);
         }
      }

   }

   private void loadMonitoredPackets() {
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

         if (data.containsKey("s2c")) {
            this.monitoredS2C.clear();
            this.monitoredS2C.addAll((List)data.get("s2c"));
         }

         if (data.containsKey("c2s")) {
            this.monitoredC2S.clear();
            this.monitoredC2S.addAll((List)data.get("c2s"));
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   private void saveMonitoredPackets() {
      try {
         CONFIG_DIR.mkdirs();
         Map data = new HashMap();
         data.put("s2c", new ArrayList(this.monitoredS2C));
         data.put("c2s", new ArrayList(this.monitoredC2S));
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(data, writer);
         writer.close();
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   public Set getMonitoredS2C() {
      return this.monitoredS2C;
   }

   public Set getMonitoredC2S() {
      return this.monitoredC2S;
   }

   public void setMonitoredS2C(Set set) {
      this.monitoredS2C = set;
      this.saveMonitoredPackets();
   }

   public void setMonitoredC2S(Set set) {
      this.monitoredC2S = set;
      this.saveMonitoredPackets();
   }

   public void onEnable() {
      EventManager.add(PacketLoggerListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(PacketLoggerListener.class, this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "记录到聊天":
               if (setting.getBoolean() != this.logToChat) {
                  this.logToChat = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "记录到日志":
               if (setting.getBoolean() != this.logToFile) {
                  this.logToFile = setting.getBoolean();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("数据包记录器", this.getSettings());
      }

   }

   public void onPacketSend(PacketLoggerEvent.Send event) {
      if (this.isEnabled()) {
         String packetName = this.getPacketName(event.packet);
         if (this.monitoredC2S.contains(packetName)) {
            this.logPacket("发送", packetName);
         }
      }
   }

   public void onPacketReceive(PacketLoggerEvent.Receive event) {
      if (this.isEnabled()) {
         String packetName = this.getPacketName(event.packet);
         if (this.monitoredS2C.contains(packetName)) {
            this.logPacket("收到", packetName);
         }
      }
   }

   private void logPacket(String direction, String packetName) {
      String description = (String)PACKET_DESCRIPTIONS.getOrDefault(packetName, "未知数据包");
      String message = String.format("§7[§dLexis§7] §b[数据包记录器] %s了：§e%s §7翻译：§f%s §7已记录！", direction, packetName, description);
      if (this.logToChat && mc.f_91074_ != null) {
         mc.f_91074_.m_5661_(Component.m_237113_(message), false);
      }

      if (this.logToFile) {
         LOGGER.info("[数据包记录器] {}了：{} - {}", direction, packetName, description);
      }

   }

   private String getPacketName(Packet packet) {
      if (packet instanceof ClientboundLoginPacket) {
         return "S2C:Login";
      } else if (packet instanceof ClientboundDisconnectPacket) {
         return "S2C:Disconnect";
      } else if (packet instanceof ClientboundKeepAlivePacket) {
         return "S2C:KeepAlive";
      } else if (packet instanceof ClientboundPlayerInfoUpdatePacket) {
         return "S2C:PlayerInfo";
      } else if (packet instanceof ClientboundPlayerPositionPacket) {
         return "S2C:PlayerPosLook";
      } else if (packet instanceof ClientboundSystemChatPacket) {
         return "S2C:ChatMessage";
      } else if (packet instanceof ClientboundSetTimePacket) {
         return "S2C:TimeUpdate";
      } else if (packet instanceof ClientboundEntityEventPacket) {
         return "S2C:EntityStatus";
      } else if (packet instanceof ClientboundSetEntityDataPacket) {
         return "S2C:EntityMetadata";
      } else if (packet instanceof ClientboundSetEntityMotionPacket) {
         return "S2C:EntityVelocity";
      } else if (packet instanceof ClientboundTeleportEntityPacket) {
         return "S2C:EntityTeleport";
      } else if (packet instanceof ClientboundSetEquipmentPacket) {
         return "S2C:EntityEquipment";
      } else if (packet instanceof ClientboundUpdateMobEffectPacket) {
         return "S2C:EntityEffect";
      } else if (packet instanceof ClientboundRemoveMobEffectPacket) {
         return "S2C:RemoveEntityEffect";
      } else if (packet instanceof ClientboundSetHealthPacket) {
         return "S2C:SetHealth";
      } else if (packet instanceof ClientboundRespawnPacket) {
         return "S2C:Respawn";
      } else if (packet instanceof ClientboundPlayerAbilitiesPacket) {
         return "S2C:PlayerAbilities";
      } else if (packet instanceof ClientboundSetCarriedItemPacket) {
         return "S2C:HeldItemChange";
      } else if (packet instanceof ClientboundContainerSetContentPacket) {
         return "S2C:WindowItems";
      } else if (packet instanceof ClientboundContainerSetDataPacket) {
         return "S2C:WindowProperty";
      } else if (packet instanceof ClientboundContainerSetSlotPacket) {
         return "S2C:SetSlot";
      } else if (packet instanceof ClientboundOpenScreenPacket) {
         return "S2C:OpenWindow";
      } else if (packet instanceof ClientboundContainerClosePacket) {
         return "S2C:CloseWindow";
      } else if (packet instanceof ClientboundBlockUpdatePacket) {
         return "S2C:BlockUpdate";
      } else if (packet instanceof ClientboundSectionBlocksUpdatePacket) {
         return "S2C:MultiBlockChange";
      } else if (packet instanceof ClientboundLevelChunkWithLightPacket) {
         return "S2C:ChunkData";
      } else if (packet instanceof ClientboundForgetLevelChunkPacket) {
         return "S2C:UnloadChunk";
      } else if (packet instanceof ClientboundAddEntityPacket) {
         return "S2C:SpawnEntity";
      } else if (packet instanceof ClientboundAddExperienceOrbPacket) {
         return "S2C:SpawnExperienceOrb";
      } else if (packet instanceof ClientboundRemoveEntitiesPacket) {
         return "S2C:DestroyEntities";
      } else if (packet instanceof ClientboundTakeItemEntityPacket) {
         return "S2C:CollectItem";
      } else if (packet instanceof ClientboundExplodePacket) {
         return "S2C:Explosion";
      } else if (packet instanceof ClientboundSoundPacket) {
         return "S2C:SoundEffect";
      } else if (packet instanceof ClientboundLevelParticlesPacket) {
         return "S2C:Particle";
      } else if (packet instanceof ClientboundGameEventPacket) {
         return "S2C:GameStateChange";
      } else if (packet instanceof ClientboundSetScorePacket) {
         return "S2C:UpdateScore";
      } else if (packet instanceof ClientboundSetObjectivePacket) {
         return "S2C:UpdateObjective";
      } else if (packet instanceof ClientboundSetDisplayObjectivePacket) {
         return "S2C:UpdateObjective";
      } else if (packet instanceof ClientboundSetPlayerTeamPacket) {
         return "S2C:UpdateTeams";
      } else if (packet instanceof ClientboundSetTitleTextPacket) {
         return "S2C:Title";
      } else if (packet instanceof ClientboundTabListPacket) {
         return "S2C:TabList";
      } else if (packet instanceof ClientboundInitializeBorderPacket) {
         return "S2C:WorldBorder";
      } else if (packet instanceof ClientboundMapItemDataPacket) {
         return "S2C:MapData";
      } else if (packet instanceof ClientboundUpdateAdvancementsPacket) {
         return "S2C:Advancements";
      } else if (packet instanceof ClientboundCommandsPacket) {
         return "S2C:CommandTree";
      } else if (packet instanceof ClientboundPlayerLookAtPacket) {
         return "S2C:LookAt";
      } else if (packet instanceof ClientboundRecipePacket) {
         return "S2C:SyncRecipeBook";
      } else if (packet instanceof ClientboundUpdateTagsPacket) {
         return "S2C:Tags";
      } else if (packet instanceof ServerboundHelloPacket) {
         return "C2S:Login";
      } else if (packet instanceof ServerboundKeepAlivePacket) {
         return "C2S:KeepAlive";
      } else if (packet instanceof ServerboundChatPacket) {
         return "C2S:ChatMessage";
      } else if (packet instanceof ServerboundPlayerActionPacket) {
         return "C2S:PlayerAction";
      } else if (packet instanceof ServerboundPlayerInputPacket) {
         return "C2S:PlayerInput";
      } else if (packet instanceof ServerboundMovePlayerPacket) {
         if (packet instanceof ServerboundMovePlayerPacket.Pos) {
            return "C2S:PlayerPosition";
         } else if (packet instanceof ServerboundMovePlayerPacket.Rot) {
            return "C2S:PlayerRotation";
         } else {
            return packet instanceof ServerboundMovePlayerPacket.PosRot ? "C2S:PlayerPositionRotation" : "C2S:PlayerMovement";
         }
      } else if (packet instanceof ServerboundPlayerAbilitiesPacket) {
         return "C2S:PlayerAbilities";
      } else if (packet instanceof ServerboundSetCarriedItemPacket) {
         return "C2S:HeldItemChange";
      } else if (packet instanceof ServerboundSwingPacket) {
         return "C2S:Animation";
      } else if (packet instanceof ServerboundInteractPacket) {
         return "C2S:UseEntity";
      } else if (packet instanceof ServerboundUseItemPacket) {
         return "C2S:UseItem";
      } else if (packet instanceof ServerboundUseItemOnPacket) {
         return "C2S:UseItemOn";
      } else if (packet instanceof ServerboundContainerClickPacket) {
         return "C2S:ClickWindow";
      } else if (packet instanceof ServerboundContainerClosePacket) {
         return "C2S:CloseWindow";
      } else if (packet instanceof ServerboundSetCreativeModeSlotPacket) {
         return "C2S:CreativeInventoryAction";
      } else if (packet instanceof ServerboundContainerButtonClickPacket) {
         return "C2S:EnchantItem";
      } else if (packet instanceof ServerboundPickItemPacket) {
         return "C2S:PickItem";
      } else if (packet instanceof ServerboundSelectTradePacket) {
         return "C2S:SelectTrade";
      } else if (packet instanceof ServerboundRenameItemPacket) {
         return "C2S:RenameItem";
      } else if (packet instanceof ServerboundSetCommandBlockPacket) {
         return "C2S:UpdateCommandBlock";
      } else if (packet instanceof ServerboundSignUpdatePacket) {
         return "C2S:UpdateSign";
      } else if (packet instanceof ServerboundSetStructureBlockPacket) {
         return "C2S:UpdateStructureBlock";
      } else if (packet instanceof ServerboundSetJigsawBlockPacket) {
         return "C2S:UpdateJigsawBlock";
      } else if (packet instanceof ServerboundSetBeaconPacket) {
         return "C2S:UpdateBeacon";
      } else if (packet instanceof ServerboundAcceptTeleportationPacket) {
         return "C2S:TeleportConfirm";
      } else if (packet instanceof ServerboundBlockEntityTagQuery) {
         return "C2S:QueryBlockNBT";
      } else if (packet instanceof ServerboundEntityTagQuery) {
         return "C2S:QueryEntityNBT";
      } else if (packet instanceof ServerboundEditBookPacket) {
         return "C2S:EditBook";
      } else if (packet instanceof ServerboundRecipeBookSeenRecipePacket) {
         return "C2S:RecipeBookData";
      } else if (packet instanceof ServerboundRecipeBookChangeSettingsPacket) {
         return "C2S:RecipeBookData";
      } else if (packet instanceof ServerboundSeenAdvancementsPacket) {
         return "C2S:AdvancementTab";
      } else if (packet instanceof ServerboundCommandSuggestionPacket) {
         return "C2S:CommandSuggestion";
      } else if (packet instanceof ServerboundClientInformationPacket) {
         return "C2S:ClientSettings";
      } else if (packet instanceof ServerboundClientCommandPacket) {
         return "C2S:ClientStatus";
      } else if (packet instanceof ServerboundResourcePackPacket) {
         return "C2S:ResourcePack";
      } else {
         return packet instanceof ServerboundPongPacket ? "C2S:Pong" : packet.getClass().getSimpleName();
      }
   }

   public void onClick() {
      this.toggle();
   }

   static {
      CONFIG_FILE = new File(CONFIG_DIR, "packet_logger.json");
      GSON = (new GsonBuilder()).setPrettyPrinting().create();
      PACKET_DESCRIPTIONS = new HashMap();
      initDescriptions();
   }
}
