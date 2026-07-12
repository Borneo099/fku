package lexis.Client.OOCCommand.CommandBlockOOC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lexis.item.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CommandBlockOOCScreen extends Screen {
   private final Screen parent;
   private List commandTexts;
   private List editBoxes;
   private List deleteButtons;
   private int scrollOffset = 0;
   private static final int MAX_COMMANDS = 512;
   private static final int INPUT_HEIGHT = 25;
   private static final int VISIBLE_INPUTS = 12;
   private Button generateButton;
   private Button generateTildeButton;
   private Button generateEggButton;
   private Button generateEggTildeButton;
   private Button addButton;
   private Button stealButton;
   private Button helpButton;
   private boolean useTildeMode = false;
   private boolean isUpdating = false;
   private CommandBlockOOCConfig config;

   public CommandBlockOOCScreen(Screen parent) {
      super(Component.m_237113_("命令方块OOC生成器"));
      this.parent = parent;
      this.config = CommandBlockOOCConfig.getInstance();
      if (this.config == null) {
         this.config = new CommandBlockOOCConfig();
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
      this.generateButton = Button.m_253074_(Component.m_237113_("生成(标准版,定版)"), (btn) -> {
         this.generateOOC(false, false);
      }).m_252987_(centerX - 150, buttonY + 25, 90, 20).m_253136_();
      this.m_142416_(this.generateButton);
      this.generateTildeButton = Button.m_253074_(Component.m_237113_("生成(~版)"), (btn) -> {
         this.generateOOC(true, false);
      }).m_252987_(centerX - 50, buttonY + 25, 80, 20).m_253136_();
      this.m_142416_(this.generateTildeButton);
      this.generateEggButton = Button.m_253074_(Component.m_237113_("生成(蛋版,定版)"), (btn) -> {
         this.generateOOC(false, true);
      }).m_252987_(centerX + 40, buttonY + 25, 100, 20).m_253136_();
      this.m_142416_(this.generateEggButton);
      this.generateEggTildeButton = Button.m_253074_(Component.m_237113_("生成(蛋版,~版)"), (btn) -> {
         this.generateOOC(true, true);
      }).m_252987_(centerX + 150, buttonY + 25, 100, 20).m_253136_();
      this.m_142416_(this.generateEggTildeButton);
      this.stealButton = Button.m_253074_(Component.m_237113_("一键读取方块导入"), (btn) -> {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null) {
            if (!mc.f_91074_.m_7500_()) {
               mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f需要创造模式才能使用功能！"), false);
            } else {
               Minecraft.m_91087_().m_91152_((Screen)null);
               ItemStack bone = new ItemStack(Items.f_42500_);
               bone.m_41714_(Component.m_237113_("Lexis I Steal"));
               CompoundTag tag = bone.m_41784_();
               tag.m_128344_("LexisXCherryStealBlocks", (byte)1);
               bone.m_41751_(tag);
               Utils.addItem(bone);
               StealBlockHandler.activate();
               mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f在手上骨头 右键对方块 可以添加/移除方块，按键：H导入 C清空 V退出"), false);
            }
         }
      }).m_252987_(centerX - 150, buttonY + 50, 120, 20).m_253136_();
      this.m_142416_(this.stealButton);
      this.helpButton = Button.m_253074_(Component.m_237113_("?"), (btn) -> {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null) {
            mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f注意：这功能需要创造模式能用，可能方块有数据 确定客户端读取不到 你要是房主/管理员就行能读取方块数据"), false);
         }
      }).m_252987_(centerX - 20, buttonY + 50, 20, 20).m_253136_();
      this.m_142416_(this.helpButton);
      this.refreshInputFields();
   }

   public void addCommand(String command) {
      if (this.commandTexts.size() < 512) {
         this.commandTexts.add(command);
         this.refreshInputFields();
         this.saveToConfig();
      }

   }

   private void addNewCommand() {
      if (this.commandTexts.size() < 512) {
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
      gui.m_280137_(this.f_96547_, "=== 命令方块OOC生成器 ===", this.f_96543_ / 2, 15, 16766720);
      gui.m_280137_(this.f_96547_, "可添加最多 512 条命令 I 当前: " + this.commandTexts.size(), this.f_96543_ / 2, 30, 11184810);
      gui.m_280509_(this.f_96543_ / 2 - 200, 45, this.f_96543_ / 2 + 200, 46, -1);
      super.m_88315_(gui, mouseX, mouseY, delta);
      int listHeight = 300;
      int maxScroll = Math.max(0, this.commandTexts.size() - 12);
      if (maxScroll > 0) {
         int scrollbarX = this.f_96543_ / 2 + 140;
         int scrollbarY = 50;
         gui.m_280509_(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + listHeight, 1140850688);
         float scrollPercent = (float)this.scrollOffset / (float)maxScroll;
         int sliderHeight = Math.max(20, listHeight * 12 / this.commandTexts.size());
         int sliderY = scrollbarY + (int)(scrollPercent * (float)(listHeight - sliderHeight));
         gui.m_280509_(scrollbarX, sliderY, scrollbarX + 6, sliderY + sliderHeight, -5592406);
      }

      gui.m_280056_(this.f_96547_, "注意：哪 一键读取方块导入 可能中率读取数据nbt失败！数据成为 空 Command:\"\"了 是命令方块问题其地方块什么没问题", 10, this.f_96544_ - 135, 16776960, false);
      gui.m_280056_(this.f_96547_, "使用：手动使用F3+I 对方块就行复制到这gui界面 不会读取失败！", 10, this.f_96544_ - 120, 16776960, false);
      gui.m_280056_(this.f_96547_, "警告：生成ooc的nbt数据太长了会被踢出服务器！要对服务器可以设置最大21亿数据(内存,比如是Paper+Mohist端)", 10, this.f_96544_ - 100, 65280, false);
      gui.m_280056_(this.f_96547_, "注意：在蛋版可能名称是\"一键生成91\"防止怀疑误会,2：在~版,要你位置对坐标能成功,就是创世神一样位置这复制", 10, this.f_96544_ - 85, 16776960, false);
   }

   private void generateOOC(boolean tildeMode, boolean eggMode) {
      this.useTildeMode = tildeMode;
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null) {
         try {
            double playerX = mc.f_91074_.m_20185_();
            double playerY = mc.f_91074_.m_20186_();
            double playerZ = mc.f_91074_.m_20189_();
            List commandCarts = new ArrayList();

            LinkedHashMap cart;
            for(int i = 0; i < this.commandTexts.size(); ++i) {
               String cmdText = ((String)this.commandTexts.get(i)).trim();
               if (!cmdText.isEmpty()) {
                  String processedCommand;
                  if (tildeMode) {
                     processedCommand = this.convertToRelativeCoordinates(cmdText, playerX, playerY, playerZ);
                  } else {
                     processedCommand = cmdText;
                  }

                  cart = new LinkedHashMap();
                  cart.put("id", "minecraft:command_block_minecart");
                  cart.put("Command", processedCommand);
                  commandCarts.add(cart);
               }
            }

            List allPassengers = new ArrayList();
            Map firstFalling = new LinkedHashMap();
            firstFalling.put("id", "minecraft:falling_block");
            firstFalling.put("Time", 1);
            Map railState = new LinkedHashMap();
            railState.put("Name", "minecraft:activator_rail");
            firstFalling.put("BlockState", railState);
            allPassengers.add(firstFalling);
            allPassengers.addAll(commandCarts);
            cart = new LinkedHashMap();
            cart.put("id", "minecraft:command_block_minecart");
            cart.put("Command", "setblock ~ ~1 ~ minecraft:command_block{Command:\"fill ~ ~ ~ ~ ~-3 ~ minecraft:air\",auto:1b}");
            allPassengers.add(cart);
            Map killCart = new LinkedHashMap();
            killCart.put("id", "minecraft:command_block_minecart");
            killCart.put("Command", "kill @e[type=minecraft:command_block_minecart,distance=..1]");
            allPassengers.add(killCart);
            Map summon = new LinkedHashMap();
            summon.put("Time", 1);
            summon.put("Motion", new double[]{0.0, -10.0, 0.0});
            Map stoneState = new LinkedHashMap();
            stoneState.put("Name", "minecraft:redstone_block");
            summon.put("BlockState", stoneState);
            summon.put("Passengers", allPassengers);
            String var10000 = this.mapToNBT(summon);
            String fullCommand = "summon minecraft:falling_block ~ ~1.5 ~ " + var10000;
            if (eggMode) {
               Map root = new LinkedHashMap();
               root.put("Count", 1);
               root.put("id", "minecraft:axolotl_spawn_egg");
               Map tag = new LinkedHashMap();
               List enchantments = new ArrayList();
               Map enchant = new LinkedHashMap();
               enchant.put("id", "");
               enchant.put("lvl", 0);
               enchantments.add(enchant);
               tag.put("Enchantments", enchantments);
               Map entityTag = new LinkedHashMap();
               entityTag.put("id", "minecraft:falling_block");
               entityTag.put("Time", 1);
               Map blockState = new LinkedHashMap();
               blockState.put("Name", "minecraft:command_block");
               entityTag.put("BlockState", blockState);
               entityTag.put("Motion", new double[]{0.0, -1.0, 0.0});
               entityTag.put("Tags", new String[]{"CDCmd_eeg"});
               Map tileEntityData = new LinkedHashMap();
               tileEntityData.put("Command", fullCommand);
               tileEntityData.put("auto", 1);
               entityTag.put("TileEntityData", tileEntityData);
               tag.put("EntityTag", entityTag);
               Map display = new LinkedHashMap();
               List lore = new ArrayList();
               lore.add("{\"italic\":false,\"color\":\"white\",\"extra\":[{\"text\":\"\"},{\"color\":\"blue\",\"text\":\"By Lexis X Karucn\"}],\"text\":\"\"}");
               display.put("Lore", lore);
               display.put("Name", "{\"italic\":false,\"extra\":[{\"text\":\"\"},{\"bold\":true,\"color\":\"gold\",\"text\":\"一键生成91\"}],\"text\":\"\"}");
               tag.put("display", display);
               root.put("tag", tag);
               String eggNbt = this.mapToNBT(root);
               CompoundTag nbtTag = TagParser.m_129359_(eggNbt);
               ItemStack eggItem = ItemStack.m_41712_(nbtTag);
               eggItem.m_41764_(1);
               eggItem = Utils.fixGhostItem(eggItem);
               Utils.addItem(eggItem);
               mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已生成" + (tildeMode ? "~版" : "定版") + "蛋版OOC物品！"), false);
            } else {
               ItemStack commandBlock = new ItemStack(Items.f_42116_);
               CompoundTag tag = new CompoundTag();
               CompoundTag blockEntityTag = new CompoundTag();
               blockEntityTag.m_128344_("auto", (byte)1);
               blockEntityTag.m_128359_("Command", fullCommand);
               tag.m_128365_("BlockEntityTag", blockEntityTag);
               commandBlock.m_41751_(tag);
               commandBlock = Utils.fixGhostItem(commandBlock);
               Utils.addItem(commandBlock);
               mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已生成" + (tildeMode ? "~版" : "标准版") + "OOC命令方块！"), false);
            }
         } catch (Exception var31) {
            mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f生成失败: " + var31.getMessage()), false);
         }

      }
   }

   private String mapToNBT(Object obj) {
      StringBuilder sb;
      Iterator var5;
      boolean first;
      if (obj instanceof Map map) {
         sb = new StringBuilder("{");
         first = true;
         var5 = map.entrySet().iterator();

         while(var5.hasNext()) {
            Map.Entry entry = (Map.Entry)var5.next();
            if (!first) {
               sb.append(",");
            }

            first = false;
            sb.append(entry.getKey()).append(":").append(this.mapToNBT(entry.getValue()));
         }

         sb.append("}");
         return sb.toString();
      } else if (obj instanceof List list) {
         sb = new StringBuilder("[");
         first = true;
         var5 = list.iterator();

         while(var5.hasNext()) {
            Object item = var5.next();
            if (!first) {
               sb.append(",");
            }

            first = false;
            sb.append(this.mapToNBT(item));
         }

         sb.append("]");
         return sb.toString();
      } else if (obj instanceof String str) {
         if (!str.contains("'") && !str.contains("\\")) {
            return "'" + str + "'";
         } else {
            str = str.replace("\\", "\\\\").replace("'", "\\'");
            return "'" + str + "'";
         }
      } else if (obj instanceof Number) {
         return obj.toString();
      } else if (obj instanceof Boolean) {
         return obj.toString();
      } else {
         int i;
         if (obj instanceof double[]) {
            double[] arr = (double[])obj;
            sb = new StringBuilder("[");

            for(i = 0; i < arr.length; ++i) {
               if (i > 0) {
                  sb.append(",");
               }

               sb.append(arr[i]).append("d");
            }

            sb.append("]");
            return sb.toString();
         } else if (obj instanceof String[]) {
            String[] arr = (String[])obj;
            sb = new StringBuilder("[");

            for(i = 0; i < arr.length; ++i) {
               if (i > 0) {
                  sb.append(",");
               }

               sb.append(this.mapToNBT(arr[i]));
            }

            sb.append("]");
            return sb.toString();
         } else {
            return "null";
         }
      }
   }

   private String convertToRelativeCoordinates(String command, double playerX, double playerY, double playerZ) {
      if (!command.startsWith("say ") && !command.startsWith("/say ")) {
         String[] parts = command.split(" ");
         StringBuilder result = new StringBuilder();
         int coordCount = 0;
         boolean isCoordCommand = command.startsWith("setblock") || command.startsWith("/setblock") || command.startsWith("fill") || command.startsWith("/fill") || command.startsWith("clone") || command.startsWith("/clone");

         for(int i = 0; i < parts.length; ++i) {
            String part = parts[i];
            if (i == 0) {
               result.append(part);
               if (i < parts.length - 1) {
                  result.append(" ");
               }
            } else {
               if (part.contains("[") && part.contains("]")) {
                  result.append(part);
               } else if (part.startsWith("{") && part.endsWith("}")) {
                  result.append(part);
               } else if (isCoordCommand && this.isCoordinate(part)) {
                  try {
                     double coord = Double.parseDouble(part);
                     double relative;
                     if (coordCount % 3 == 0) {
                        relative = coord - playerX;
                     } else if (coordCount % 3 == 1) {
                        relative = coord - playerY;
                     } else {
                        relative = coord - playerZ;
                     }

                     if (Math.abs(relative) < 0.01) {
                        result.append("~");
                     } else {
                        result.append(String.format("~%.2f", relative));
                     }

                     ++coordCount;
                  } catch (NumberFormatException var18) {
                     result.append(part);
                  }
               } else {
                  result.append(part);
               }

               if (i < parts.length - 1) {
                  result.append(" ");
               }
            }
         }

         return result.toString();
      } else {
         return command;
      }
   }

   private boolean isCoordinate(String str) {
      if (str != null && !str.isEmpty()) {
         String pattern = "^-?\\d+(\\.\\d+)?$";
         return str.matches(pattern);
      } else {
         return false;
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
