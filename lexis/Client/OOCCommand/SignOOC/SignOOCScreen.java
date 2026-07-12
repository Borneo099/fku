package lexis.Client.OOCCommand.SignOOC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.item.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SignOOCScreen extends Screen {
   private final Screen parent;
   private List commandTexts;
   private List editBoxes;
   private List deleteButtons;
   private int scrollOffset = 0;
   private static final int MAX_COMMANDS = 32768;
   private static final int INPUT_HEIGHT = 25;
   private static final int VISIBLE_INPUTS = 12;
   private Button generateButton;
   private Button generateTildeButton;
   private Button addButton;
   private Button stealButton;
   private Button helpButton;
   private boolean isUpdating = false;
   private SignOOCConfig config;

   public SignOOCScreen(Screen parent) {
      super(Component.m_237113_("告示牌OOC生成器"));
      this.parent = parent;
      this.config = SignOOCConfig.getInstance();
      if (this.config == null) {
         this.config = new SignOOCConfig();
      }

      this.commandTexts = new ArrayList(this.config.getCommands());
      this.editBoxes = new ArrayList();
      this.deleteButtons = new ArrayList();

      while(this.commandTexts.size() < 3) {
         this.commandTexts.add("");
      }

   }

   protected void m_7856_() {
      super.m_7856_();
      int centerX = this.f_96543_ / 2;
      int buttonY = this.f_96544_ - 80;
      this.addButton = Button.m_253074_(Component.m_237113_("添加新指令"), (btn) -> {
         this.addNewCommand();
      }).m_252987_(centerX - 150, buttonY, 100, 20).m_253136_();
      this.m_142416_(this.addButton);
      this.generateButton = Button.m_253074_(Component.m_237113_("生成(定版)"), (btn) -> {
         this.generateSign(false);
      }).m_252987_(centerX - 150, buttonY + 25, 90, 20).m_253136_();
      this.m_142416_(this.generateButton);
      this.generateTildeButton = Button.m_253074_(Component.m_237113_("生成(~版)"), (btn) -> {
         this.generateSign(true);
      }).m_252987_(centerX - 50, buttonY + 25, 80, 20).m_253136_();
      this.m_142416_(this.generateTildeButton);
      this.stealButton = Button.m_253074_(Component.m_237113_("一键读取方块导入"), (btn) -> {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null) {
            Minecraft.m_91087_().m_91152_((Screen)null);
            SignOOCRegionHandler.activate();
            mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §6区域选择：左键设pos1 右键设pos2 ，按键：H导入 C清空 V退出"), false);
         }
      }).m_252987_(centerX - 150, buttonY + 50, 120, 20).m_253136_();
      this.m_142416_(this.stealButton);
      this.helpButton = Button.m_253074_(Component.m_237113_("?"), (btn) -> {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null) {
            mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §d关闭GUI后 左键pos1 右键pos2 框选区域，按H导入方块到 NBT"), false);
         }
      }).m_252987_(centerX - 20, buttonY + 50, 20, 20).m_253136_();
      this.m_142416_(this.helpButton);
      this.refreshInputFields();
   }

   public void addCommand(String command) {
      if (this.commandTexts.size() < 32768) {
         this.commandTexts.add(command);
         this.refreshInputFields();
         this.saveToConfig();
      }

   }

   private void addNewCommand() {
      if (this.commandTexts.size() < 32768) {
         this.commandTexts.add("");
         this.refreshInputFields();
         this.saveToConfig();
      }

   }

   private void removeCommand(int index) {
      if (this.commandTexts.size() > 1) {
         this.commandTexts.remove(index);
         this.refreshInputFields();
         this.saveToConfig();
      }

   }

   private void saveToConfig() {
      this.config.setCommands(new ArrayList(this.commandTexts));
   }

   private void refreshInputFields() {
      if (!this.isUpdating) {
         this.isUpdating = true;
         Iterator var1 = this.editBoxes.iterator();

         while(var1.hasNext()) {
            EditBox box = (EditBox)var1.next();
            this.m_169411_(box);
         }

         var1 = this.deleteButtons.iterator();

         while(var1.hasNext()) {
            Button btn = (Button)var1.next();
            this.m_169411_(btn);
         }

         this.editBoxes.clear();
         this.deleteButtons.clear();
         int startY = 50;
         int centerX = this.f_96543_ / 2;

         for(int i = 0; i < this.commandTexts.size(); ++i) {
            int yPos = startY + i * 25 - this.scrollOffset * 25;
            if (yPos >= 40 && yPos <= this.f_96544_ - 100) {
               EditBox editBox = new EditBox(this.f_96547_, centerX - 150, yPos, 250, 20, Component.m_237113_("命令 " + (i + 1)));
               editBox.m_94199_(Integer.MAX_VALUE);
               editBox.m_94144_((String)this.commandTexts.get(i));
               editBox.m_94151_((text) -> {
                  this.commandTexts.set(i, text);
                  this.saveToConfig();
               });
               editBox.m_94190_(true);
               this.m_142416_(editBox);
               this.editBoxes.add(editBox);
               Button deleteBtn = Button.m_253074_(Component.m_237113_("✕"), (btnx) -> {
                  this.removeCommand(i);
               }).m_252987_(centerX + 110, yPos, 20, 20).m_253136_();
               this.m_142416_(deleteBtn);
               this.deleteButtons.add(deleteBtn);
            }
         }

         this.isUpdating = false;
      }
   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      this.m_280273_(gui);
      gui.m_280137_(this.f_96547_, "=== 告示牌OOC生成器 ===", this.f_96543_ / 2, 15, 16766720);
      gui.m_280137_(this.f_96547_, "可添加最多 32768 条命令 I 当前: " + this.commandTexts.size(), this.f_96543_ / 2, 30, 11184810);
      gui.m_280509_(this.f_96543_ / 2 - 200, 45, this.f_96543_ / 2 + 200, 46, -1);
      super.m_88315_(gui, mouseX, mouseY, delta);
      gui.m_280056_(this.f_96547_, "一键读取方块导入：关GUI后 左键pos1 右键pos2 框选(有渲染)，按H导入", 10, this.f_96544_ - 135, 16776960, false);
      gui.m_280056_(this.f_96547_, "生成(定版)=绝对坐标重建 I 生成(~版)=相对玩家坐标 (创世神位置对齐)", 10, this.f_96544_ - 120, 16776960, false);
      gui.m_280056_(this.f_96547_, "告示牌：放下后点四行 部署自闭链执行器，自带前置2条装配命令", 10, this.f_96544_ - 100, 65280, false);
      gui.m_280056_(this.f_96547_, "注意：nbt数据太长会被踢出服务器！导入方块上限32768个", 10, this.f_96544_ - 85, 16776960, false);
   }

   private void generateSign(boolean tildeMode) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         try {
            double playerX = mc.f_91074_.m_20185_();
            double playerY = mc.f_91074_.m_20186_();
            double playerZ = mc.f_91074_.m_20189_();
            String l1Cmd = "tag @s add krooc_ooc";
            String l2Cmd = "execute if entity @s[tag=krooc_ooc] run data modify storage karucn:krooc krooc set from entity @s SelectedItem.tag.krooc";
            String l3Cmd = "execute if entity @s[tag=krooc_ooc] run setblock ~ ~1 ~1 minecraft:command_block[facing=north]" + this.buildClockBlock();
            String l4Cmd = "execute if entity @s[tag=krooc_ooc] run setblock ~ ~1 ~ minecraft:chain_command_block[facing=east]" + this.buildChainRing();
            ListTag frontMessages = new ListTag();
            frontMessages.add(StringTag.m_129297_(this.signMessageJson("SignOOC模板", "#FF66CC", l1Cmd)));
            frontMessages.add(StringTag.m_129297_(this.signMessageJson("1.20.1+", "#33CCFF", l2Cmd)));
            frontMessages.add(StringTag.m_129297_(this.signMessageJson("ㅤ", "gold", l3Cmd)));
            frontMessages.add(StringTag.m_129297_(this.signMessageJson("ㅤ", "gold", l4Cmd)));
            ListTag backMessages = new ListTag();

            for(int i = 0; i < 4; ++i) {
               backMessages.add(StringTag.m_129297_("{\"text\":\"\"}"));
            }

            CompoundTag frontText = new CompoundTag();
            frontText.m_128365_("messages", frontMessages);
            frontText.m_128359_("color", "black");
            frontText.m_128344_("has_glowing_text", (byte)0);
            CompoundTag backText = new CompoundTag();
            backText.m_128365_("messages", backMessages);
            backText.m_128359_("color", "black");
            backText.m_128344_("has_glowing_text", (byte)0);
            CompoundTag blockEntityTag = new CompoundTag();
            blockEntityTag.m_128365_("front_text", frontText);
            blockEntityTag.m_128365_("back_text", backText);
            blockEntityTag.m_128359_("id", "minecraft:sign");
            blockEntityTag.m_128344_("is_waxed", (byte)1);
            ListTag krooc = new ListTag();
            krooc.add(StringTag.m_129297_("data modify block ~1 ~ ~ Command set value 'data remove storage karucn:krooc krooc[0]'"));
            krooc.add(StringTag.m_129297_("data modify block ~1 ~ ~1 Command set value 'execute unless data storage karucn:krooc krooc[0] run fill ~ ~ ~ ~-2 ~ ~-1 air'"));
            Iterator var19 = this.commandTexts.iterator();

            while(var19.hasNext()) {
               String cmdText = (String)var19.next();
               String t = cmdText.trim();
               if (!t.isEmpty()) {
                  String processed = tildeMode ? this.convertToRelativeCoordinates(t, playerX, playerY, playerZ) : t;
                  krooc.add(StringTag.m_129297_(processed));
               }
            }

            CompoundTag rootTag = new CompoundTag();
            rootTag.m_128365_("BlockEntityTag", blockEntityTag);
            rootTag.m_128365_("krooc", krooc);
            ListTag enchantments = new ListTag();
            CompoundTag ench = new CompoundTag();
            ench.m_128359_("id", "minecraft:bane_of_arthropods");
            ench.m_128376_("lvl", (short)1);
            enchantments.add(ench);
            rootTag.m_128365_("Enchantments", enchantments);
            rootTag.m_128405_("HideFlags", 1);
            CompoundTag displayTag = new CompoundTag();
            displayTag.m_128359_("Name", "{\"italic\":false,\"text\":\"\",\"extra\":[{\"text\":\"Sign \",\"bold\":true,\"italic\":false,\"color\":\"#46D7C6\"},{\"text\":\"OOC \",\"bold\":true,\"italic\":false,\"color\":\"#46D768\"},{\"text\":\"Template\",\"bold\":true,\"italic\":false,\"color\":\"#B36D22\"}]}");
            ListTag loreList = new ListTag();
            loreList.add(StringTag.m_129297_("{\"italic\":false,\"text\":\"\",\"extra\":[{\"text\":\"by \",\"color\":\"#55FFFF\"},{\"text\":\"Karucn\",\"color\":\"#9EFF58\"}]}"));
            displayTag.m_128365_("Lore", loreList);
            rootTag.m_128365_("display", displayTag);
            ItemStack sign = new ItemStack(Items.f_42445_);
            sign.m_41751_(rootTag);
            sign.m_41714_(Component.m_237113_("Sign OOC Template"));
            sign = Utils.fixGhostItem(sign);
            Utils.addItem(sign);
            mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已生成" + (tildeMode ? "~版" : "定版") + "告示牌OOC！"), false);
         } catch (Exception var25) {
            mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f生成失败: " + var25.getMessage()), false);
         }

      }
   }

   private String signMessageJson(String text, String color, String command) {
      return "[{\"text\":\"" + this.jsonEscape(text) + "\",\"color\":\"" + color + "\",\"bold\":true,\"italic\":false,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + this.jsonEscape(command) + "\"}}]";
   }

   private String jsonEscape(String s) {
      return s.replace("\\", "\\\\").replace("\"", "\\\"");
   }

   private String buildClockBlock() {
      CompoundTag t = new CompoundTag();
      t.m_128344_("auto", (byte)1);
      t.m_128344_("TrackOutput", (byte)0);
      t.m_128359_("Command", "gamerule commandBlockOutput false");
      return t.toString();
   }

   private String buildChainRing() {
      String writeCmd = "data modify block ~ ~ ~-1 Command set from storage karucn:krooc krooc[0]";
      String var10000 = this.chainBlock(writeCmd);
      String c3 = "setblock ~-1 ~ ~ minecraft:chain_command_block[facing=north]" + var10000;
      var10000 = this.chainBlock(c3);
      String c2 = "setblock ~ ~ ~1 minecraft:chain_command_block[facing=west]" + var10000;
      var10000 = this.chainBlock(c2);
      String c1 = "setblock ~1 ~ ~ minecraft:chain_command_block[facing=south]" + var10000;
      return this.chainBlock(c1);
   }

   private String chainBlock(String command) {
      CompoundTag t = new CompoundTag();
      t.m_128344_("auto", (byte)1);
      t.m_128344_("UpdateLastExecution", (byte)0);
      t.m_128344_("TrackOutput", (byte)0);
      t.m_128359_("Command", command);
      return t.toString();
   }

   private String convertToRelativeCoordinates(String command, double playerX, double playerY, double playerZ) {
      boolean slash = command.startsWith("/");
      String body = slash ? command.substring(1) : command;
      String lower = body.toLowerCase();
      byte coordCount;
      if (lower.startsWith("setblock ")) {
         coordCount = 3;
      } else if (lower.startsWith("fill ")) {
         coordCount = 6;
      } else {
         if (!lower.startsWith("clone ")) {
            return command;
         }

         coordCount = 9;
      }

      String[] tok = body.split(" ", coordCount + 2);
      if (tok.length < coordCount + 1) {
         return command;
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append(slash ? "/" : "").append(tok[0]);

         for(int i = 1; i <= coordCount; ++i) {
            sb.append(" ").append(this.toRelative(tok[i], i, playerX, playerY, playerZ));
         }

         if (tok.length == coordCount + 2) {
            sb.append(" ").append(tok[coordCount + 1]);
         }

         return sb.toString();
      }
   }

   private String toRelative(String token, int index, double playerX, double playerY, double playerZ) {
      if (!token.startsWith("~") && !token.startsWith("^")) {
         int axis = (index - 1) % 3;
         double base;
         if (axis == 0) {
            base = playerX;
         } else if (axis == 1) {
            base = playerY;
         } else {
            base = playerZ;
         }

         try {
            double v = Double.parseDouble(token);
            double rel = v - base;
            return Math.abs(rel) < 0.01 ? "~" : String.format("~%.2f", rel);
         } catch (NumberFormatException var16) {
            return token;
         }
      } else {
         return token;
      }
   }

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      int maxScroll = Math.max(0, this.commandTexts.size() - 12);
      int newScroll = (int)Math.max(0.0, Math.min((double)maxScroll, (double)this.scrollOffset - delta));
      if (newScroll != this.scrollOffset) {
         this.scrollOffset = newScroll;
         this.refreshInputFields();
      }

      return true;
   }

   public boolean m_7043_() {
      return false;
   }
}
