package lexis.czco;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.RegisterEvent;

@EventBusSubscriber(
   bus = Bus.MOD
)
public class CreativeTabHandler {
   public static final ResourceKey LEXIS_TAB;
   private static CreativeModeTab lexisTabInstance;

   @SubscribeEvent
   public static void registerCreativeTabs(RegisterEvent event) {
      event.register(Registries.f_279569_, (helper) -> {
         lexisTabInstance = CreativeModeTab.builder().m_257941_(Component.m_237113_("XF Lexis FX Plus")).m_257737_(() -> {
            return createKarucnHead();
         }).m_257501_((params, output) -> {
            output.m_246342_(createHead("§bLexis Mod By Karucn", new String[]{"§7欢迎使用 Lexis Client", "§7这里记录了每个版本的更新内容 —— 新功能、修复、优化", "§7点击下方的纸页查看详细日志"}));
            output.m_246342_(createPaper("1.§b更新日志", new String[]{"§6=== 1.3.4 版本 ===", "§71. 新增创造模式物品栏专属页 —— 就是你现在看到的这个页面！", "§72. 优化绕过 OpMod 的 ModID 检测逻辑", "§73. 修复：游戏加载过程中有小概率没绕过反作弊导致直接崩溃", "", "§6=== 1.3.5 版本 ===", "§74. 修复：LexisHack GUI 里「更改GUI打开」不显示功能名称后缀 [xxx] 的 Bug", "§75. 更新：按钮美化功能 —— 鼠标悬停时按钮背景会变成半透明白色", "§76. 新增：自动无头活塞功能，在物品窗口里可以找到", "", "§6=== 1.3.6 版本 ===", "§78. 更新：玩家 ESP 现在支持三种自定义颜色 —— 连线、六面体、方框各调各的", "§79. 更新：玩家 ESP 新增发光模式", "§710. 更新：自动冲刺加入「忽略移动限制」选项", "§711. 调整：所有功能的「看向」延时统一改为 800ms 再停止", "§712. 重新设计右下角通知器，现在更简洁好看了", "§713. 新增：开发者皮肤 —— 名字带 Dev 的玩家会自动换皮肤（仅本地可见）", "§714. 更新：颜色选择器新增 RGB 数值输入框和复制/导入按钮", "§715. 更新：时间修改器加入循环流动模式，可自定义流速最高 51200 倍", "§716. 新增：数据包记录器，方便排查各种奇怪问题", "§717. 新增：聊天不清理文本 —— 误按 Esc 关掉聊天框再打开，字还在；被 kill 了也不会丢", "§718. 新增：阻止关闭游戏 —— Alt+F4 和窗口 X 都无效，防手滑退出", "§719. 新增：更好聊天消息，带翻译按钮和快捷操作", "§720. 新增：保留合成格功能", "§721. 新增：自动告示牌功能", "§722. 新增：上帝视角功能", "§723. 更新：创造页里的 Lexis 物品禁止拿取", "§724. 更新：更好聊天消息设置里加入美化、时间戳、高亮自己、自定义按钮颜色", "", "§6=== 1.3.7 版本 ===", "§725. 新增指令：lexis client jump", "§726. 新增指令：lexis client invsee", "§727. 新增指令：lexis client enchant", "§728. 新增指令：lexis client drop", "§729. 新增指令：lexis client tp", "§730. 新增指令：lexis client ModifyCount", "§731. 新增指令：lexis client ModifyDamagedurability", "§732. 新增指令：lexis client getitemnbt", "§733. 新增指令：lexis client Serverinfo", "§734. 新增指令：lexis client disconnect", "", "§6=== 1.3.8 版本 ===", "§735. 调整 OppMod 的 ModID 检测策略", "", "§6=== 1.3.9 版本 ===", "§736. Lexis Client GUI 全面升级为透明风格", "§737. 修复：保留合成格一直没效果的陈年 Bug", "§738. 新增：反刷屏功能", "§739. 新增指令：lexis client tpgoto", "§740. 新增指令：lexis client tpgotoPos", "§741. 新增指令：lexis clickgui"}));
            output.m_246342_(createPaper("2.§b更新日志", new String[]{"§6=== 1.4.0 版本 ===", "§742. 加强：自动点爆水晶 —— 新增两项子设置，更灵活的控制逻辑", "§743. 新增：自动反击功能", "§744. 新增：重生锚光环", "§745. 修复：自动暴击一直不生效的 Bug", "§746. 加强：玩家 ESP 发光模式大幅改进，六面体 / 方框 / 连线更清晰", "§747. 加强：实体透视现在支持自定义发光颜色了", "§748. 加强：透视隐身实体支持发光模式，六面 / 方框 / 连线三种颜色各调各的", "§749. 更新：Lexis Logo 换了新字体，更好看", "§750. 新增：没敌机关枪功能", "§751. 移除：船飞功能（没什么人用就删了）", "§752. 新增：实体控制功能", "§753. 现在强制要求安装 mafglib 作为前置 Mod，防止功能崩溃", "", "§6=== 1.4.1 版本 ===", "§754. 修复：跟 Wurst 一起安装偶尔会崩游戏的兼容问题", "", "§6=== 1.4.2 版本 ===", "§755. 修复 & 更新：tpgotoPos / tpgoto stop 卡住不走的 Bug，路径线改成粒子，未加载区块自动暂停等待", "", "§6=== 1.4.3 版本 ===", "§756. 调整：绕过 OpMod 的 ModID 检测改为返回空白，更隐蔽", "§757. 新增：搜索实体功能", "§758. 调整：自动暴击改成自适应高度，防止卡墙卡地", "§759. 更新：自动暴击新增发包模式和跳跃模式，加「仅地面」选项", "§760. 新增：多重光环", "§761. 新增指令：lexis client friends —— 加好友，所有 Hack 自动跳过好友", "§762. 新增指令：lexis client tpPlayerEgg", "", "§6=== 1.4.4 版本 ===", "§763. 调整：功能设置 GUI 的滑块和边框改成圆角线条了", "§764. 新增：游戏窗口异环主题（对，就是那个异环——好看就完了）", "§765. 更新：自动暴击右上角 HUD 显示当前模式 [发包/跳跃]", "§766. 优化：自动暴击跳跃模式绕过反作弊检测", "§767. 移除：主题设置功能", "§768. 更新：AI 聊天（服务端指令 lexis server aichat）模型升级为 Claude 4.7 Opus", "§769. 更新：聊天 AI 功能全面改进", "§770. 修复：聊天 AI 回复发送消息后被踢出服务器的 Bug", "§771. 修复：玩家 ESP 跟其他发光功能互相冲突导致发光消失", "§772. 修复：自动攻击跟 Wurst 的 Killaura 同时开会崩溃"}));
            output.m_246342_(createPaper("3.§b更新日志", new String[]{"§6=== 1.4.5 版本 ===", "§773. 美化：Lexis GUI 窗口整体重新设计，更现代了", "§774. 新增：瞄准实体发光功能", "§775. 调整：功能开关的切换提示格式更清晰", "§776. 更换左上角 Lexis Logo 为新版", "§777. Lexis Plus 版整体加强！", "", "§6=== 1.4.6 版本 ===", "§778. 优化：绕过反作弊 ModID 检测更稳定", "§779. 新增：直升机鞘翅飞行", "§780. 新增：旋转功能", "§781. 调整：看向工具改进，绕过反作弊检测效果更好", "§782. 加强：传送光环（远杀模式）", "§783. 新增：[Lexis] 聊天前缀变成彩虹渐变效果", "§784. 新增：数据包挖掘功能", "§785. 新增指令：lexis client ReplayPacket", "§786. 更新：按 F3 查看 Debug 信息时，Lexis Logo 和 HUD 会自动隐藏，松开 F3 恢复显示", "§787. 更新：游戏窗口标题栏颜色改成梦幻紫粉", "§788. 新增指令：lexis client ServerSwitch", "", "§6=== 1.4.7 版本 ===", "§789. 新增：网易云音乐功能", "§790. 优化：反刷屏逻辑", "§791. 调整：服务端 AI 聊天和聊天消息翻译模型切换为 gpt-oss-120b", "§792. 新增：事件通知器", "§793. 修复：加载 Lexis Logo GIF 偶发导致系统蓝屏的严重 Bug", "§794. 修复：游戏窗口标题栏颜色偶尔不生效", "§795. 新增：X-Ray 透视功能", "§796. 新增：自定义水晶旋转动画", "", "§6=== 1.4.8 版本 ===", "§797. 新增：传送门无敌模式", "§798. 新增：无服务器旋转", "§799. 新增：无跳跃延迟", "§7100. 新增：幽灵模式", "§7101. 修复：自动告示牌一直没效果的 Bug", "§7102. 更新：自动告示牌加入日期格式设置", "§7103. 新增：超远投掷", "§7104. 优化：Blink 功能更流畅"}));
            output.m_246342_(createPaper("4.§b更新日志", new String[]{"§6=== 1.4.9 版本 ===", "§7105. 新增：实体选择 GUI 支持鼠标拖拽 3D 旋转预览 —— 选实体更直观", "§7106. 新增：方块选择 GUI 同样支持 3D 旋转预览", "§7107. 新增：单方块选择 GUI", "§7108. 新增：字体选择 GUI，支持预览系统所有字体", "§7109. 加强：杀圈光环改用新的多实体选择 GUI，加入绕过命名 / 绕过被动设置", "§7110. 加强：自动点爆水晶改用新 GUI + 绕过命名 / 绕过被动", "§7111. 加强：传送光环改用新 GUI + 绕过命名 / 绕过被动", "§7112. 加强：重生锚光环改用新 GUI + 绕过命名 / 绕过被动", "§7113. 加强：传送光环（远杀）改用新 GUI + 绕过命名 / 绕过被动", "§7114. 加强：多重光环改用新 GUI + 绕过命名 / 绕过被动", "§7115. 更新：右上角功能 HUD 名称现在能动态显示当前 [模式/参数] 了", "§7116. 更新：颜色选择器彩虹模式支持后台持续运行，切窗口也不会停"}));
            output.m_246342_(createPaper("5.§b更新日志", new String[]{"§6=== 1.5.0 版本 ===", "§7117. 灵魂出窍全面重写！平滑相机移动 + 方块可交互 + Baritone 联动 goto", "§7118. 灵魂出窍新增：十字准星显示选项", "§7119. 灵魂出窍新增：手部渲染开关（注意：开了跟光影不兼容）", "§7120. 灵魂出窍新增：方向键 ↑↓←→ 直接操控真实玩家移动", "§7121. 灵魂出窍新增：点击方块自动触发 Baritone goto 寻路", "§7122. 灵魂出窍新增：单击 / 双击触发模式切换", "§7123. 灵魂出窍优化：开着 Baritone 时打开灵魂出窍不会中断寻路", "§7124. 修复：灵魂出窍相机穿方块时会误破坏方块的 Bug", "§7125. 新增：三叉戟循环复制", "§7126. 新增：Baritone 加速模式 —— 寻路中加速 + 无惯性精准移动", "§7127. 新增：Baritone 跑酷模式 —— 支持跑酷 / 疾跑 / 放置方块", "§7128. 新增：Baritone 任意维度鞘翅 —— 强制所有维度可用，解除 Y 轴限制", "", "§6=== 1.5.1 版本 ===", "§7129. 新增：TaCZ 无后坐力 —— 开枪枪口纹丝不动", "§7130. 新增：TaCZ 瞬镜 —— 右键瞬间完成瞄准，没拉栓动画", "§7131. 新增：TaCZ 疾跑不打断开枪 —— 边跑边打", "§7132. 修复：在TaCZ 开枪 对假人会崩溃", "", "§6=== 1.5.2 版本 ===", "§7133. 新增：TaCZ 子弹透视 —— 子弹飞行轨迹可视化，颜色 / 线宽 / 距离全可调", "§7134. 新增：TaCZ 子弹自瞄 —— 屏幕圆圈 FOV 显示，实体进圈自动锁定", "§7135. 自瞄支持：自定义圆圈大小 / 颜色、锁定颜色、旋转速度（过反作弊）", "§7136. 自瞄支持：绕过好友 / 绕过命名实体 / 绕过被动动物 / 可配置实体白名单", "§7137. 新增：TaCZ 自动换弹 —— 弹匣打空自动换弹，不用手动按 R 了", "§7138. 新增：TaCZ 全狙击自动 —— 长按左键连续射击，狙击枪也能按住连发", "", "§6=== 1.5.3 版本 ===", "§7139. 修复：TaCZ 功能 加载时序问题全部失效（瞬镜/无后座/疾跑不断/全狙击自动/子弹透视）", "§7140. 修复：数据包拦截 字段类型错误导致崩溃", "§7141. 修复：与 高清修复 会冲突 崩溃问题，现在允许了", "", "§6=== 1.5.4 版本 ===", "§7142. 更新：在左 上Lexis logo 加效果 乱码", "§7143. 更新：增强名牌", "§7144. 修复： 开启 实体所有者 与 高清修复 发生崩溃 OpenGL无效渲染问题", "§6=== 1.5.5 版本 ===", "§7145. 修复：在房间 有 大量模组 加载中，lexis的加载配置 中概率 加载配置失败 变成 默认配置BUG了", "§7146. 更改：lexis logo 乱码效果时长"}));
            output.m_246342_(createPaper("6.§b更新日志", new String[]{"§6=== 1.5.6 版本 ===", "§7147. 更改：四个GUI 方块选择 和 实体选择 和 字体选择 等 更改 字体 和 允许鼠标移动对窗口了，高度一点了", "§7148. 更新：更好的原版发光", "§7149. 修复：增强名牌 和 实体所有者 投影世界坐标错误位置", "§7150. 更新：物品描边", "§7151. 更新：显示自己名称", "§6=== 1.5.7 版本 ===", "§7152. 加强：传送光环(远杀) 加两个 设置功能了，使用一tick传送能打到实体 无少回弹一点", "§7153. 更新：TaCZ 的 子弹自瞄 的 锁定部位 设置功能", "§7154. 更新：灾变的窗口 -》 灾变的快速找到Boss位置", "§7155. 更新：baritone窗口 -》 原版结构定位前往", "§7156. 更新：TaCZ 窗口 -》 无尽自瞄", "§7157. 更新：TaCZ(服务端) 窗口", "§7158. 更新：TaCZ(服务端) -》 无限子弹", "§7159. 更新：TaCZ(服务端) -》 无散布", "§7160. 允许：TaCZ(服务端) -》 所有武器 允许连发", "§7161. 更新：TaCZ(服务端) -》 满级射速", "§6=== 1.5.8 版本 ===", "§7162. 修复：数据包挖 功能 NEP一个问题崩溃", "§7163. 修改：在所有功能hack 不允许出现 NEP崩溃了", "§6=== 1.5.9 版本 ===", "§7164. 更新：发包飞行", "§7165. 更新：虚空透视", "§6=== 1.6.0 版本 ===", "§7166. 更新：灾变 -》 无屏幕震动", "§7167. 更改：在hack的窗口 改 方框版", "§7168. 更新：色差效果", "§7169. 更新：虚拟光影", "§7170. 更新：防砍动画", "§7171. 更新：动态模糊"}));
            output.m_246342_(createPaper("6.§b更新日志", new String[]{"§6=== 1.6.1 版本 ===", "§7172. 更新：任务中允许自己第三人称看见", "§7173. 修复：问题在 联动模组的窗口(如 tacz baritone 等) 加载配置 自动开启功能 当前功能不生效问题", "§7174. 更改：看向中 身体 和 头 看向一样 之前是 头看向", "§7175. 更新：聊天记录不清除", "§6=== 1.6.2 版本 ===", "§7176. 更新：Xray(露出版)", "§7177. 更新：Xray露出的自动挖矿", "§7178. 修复：聊天记录不清除 的功能 启动失败游戏 导致崩溃了问题注入失败"}));
         }).m_257652_();
         helper.register(LEXIS_TAB, lexisTabInstance);
      });
   }

   public static void startColorAnimation() {
      (new Thread(() -> {
         long startTime = System.currentTimeMillis();

         while(!Thread.currentThread().isInterrupted()) {
            try {
               float hue = (float)((System.currentTimeMillis() - startTime) % 5000L) / 5000.0F;
               float targetHue = 0.75F + (float)Math.sin((double)hue * Math.PI * 2.0) * 0.15F;
               int rgb = Color.HSBtoRGB(targetHue, 0.9F, 0.9F);
               int color = rgb & 16777215;
               if (lexisTabInstance != null) {
                  Minecraft.m_91087_().execute(() -> {
                     MutableComponent newTitle = Component.m_237113_("XF Lexis FX Plus").m_130938_((style) -> {
                        return style.m_178520_(color);
                     });
                     lexisTabInstance.f_40764_ = newTitle;
                  });
               }

               Thread.sleep(50L);
            } catch (InterruptedException var6) {
               Thread.currentThread().interrupt();
            }
         }

      })).start();
   }

   private static ItemStack createKarucnHead() {
      ItemStack head = new ItemStack(Items.f_42680_);
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("SkullOwner", "Karucn");
      head.m_41751_(tag);
      return head;
   }

   private static ItemStack createHead(String displayName, String[] lore) {
      ItemStack head = new ItemStack(Items.f_42680_);
      CompoundTag tag = head.m_41784_();
      tag.m_128379_("NoLexisItems", true);
      tag.m_128359_("SkullOwner", "Karucn");
      CompoundTag display = new CompoundTag();
      display.m_128359_("Name", Serializer.m_130703_(Component.m_237113_(displayName).m_130938_((style) -> {
         return style.m_131155_(false);
      })));
      ListTag loreList = new ListTag();
      String[] var6 = lore;
      int var7 = lore.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         String line = var6[var8];
         loreList.add(StringTag.m_129297_(Serializer.m_130703_(Component.m_237113_(line))));
      }

      display.m_128365_("Lore", loreList);
      tag.m_128365_("display", display);
      head.m_41751_(tag);
      return head;
   }

   private static ItemStack createPaper(String displayName, String[] lore) {
      ItemStack paper = new ItemStack(Items.f_42516_);
      CompoundTag tag = paper.m_41784_();
      tag.m_128379_("NoLexisItems", true);
      CompoundTag display = new CompoundTag();
      display.m_128359_("Name", Serializer.m_130703_(Component.m_237113_(displayName).m_130938_((style) -> {
         return style.m_131155_(false);
      })));
      ListTag loreList = new ListTag();
      String[] var6 = lore;
      int var7 = lore.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         String line = var6[var8];
         loreList.add(StringTag.m_129297_(Serializer.m_130703_(Component.m_237113_(line))));
      }

      display.m_128365_("Lore", loreList);
      tag.m_128365_("display", display);
      paper.m_41663_(Enchantments.f_44986_, 1);
      tag.m_128405_("HideFlags", 1);
      paper.m_41751_(tag);
      return paper;
   }

   static {
      LEXIS_TAB = ResourceKey.m_135785_(Registries.f_279569_, new ResourceLocation("lexis", "lexis_tab"));
      lexisTabInstance = null;
   }
}
