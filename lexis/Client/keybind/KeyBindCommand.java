package lexis.Client.keybind;

import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class KeyBindCommand {
   private static final String PREFIX = "§d[§6Lexis§d] §f";
   private static final int MAX_KEYS = 32;
   private static Map toggleStates = new HashMap();
   public static KeyMapping[] LEXIS_KEYS = new KeyMapping[32];
   private static KeyBindConfig config = KeyBindConfig.getInstance();
   private static Map keyBinds;
   private static boolean isInitialized;

   public static void initKeyMappings() {
      if (!isInitialized) {
         for(int i = 1; i <= 32; ++i) {
            KeyBindConfig.KeyBindData data = (KeyBindConfig.KeyBindData)keyBinds.getOrDefault(i, new KeyBindConfig.KeyBindData());
            String displayName = data.isSet() ? "lexis " + data.getName() : "key.lexis." + i;
            LEXIS_KEYS[i - 1] = new KeyMapping(displayName, Type.KEYSYM, -1, "key.categories.lexis");
            if (!keyBinds.containsKey(i)) {
               keyBinds.put(i, data);
            }

            toggleStates.put(i, false);
         }

         isInitialized = true;
      }
   }

   @SubscribeEvent
   public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
      initKeyMappings();

      for(int i = 0; i < 32; ++i) {
         if (LEXIS_KEYS[i] != null) {
            event.register(LEXIS_KEYS[i]);
         }
      }

   }

   public static KeyMapping getKey(int id) {
      return id >= 1 && id <= 32 ? LEXIS_KEYS[id - 1] : null;
   }

   private static int setKeyBindWithToggle(CommandContext context) {
      int id = IntegerArgumentType.getInteger(context, "id");
      String name = StringArgumentType.getString(context, "name");
      String command = StringArgumentType.getString(context, "command");
      String toggle = StringArgumentType.getString(context, "toggle");
      boolean toggleMode = false;
      String value1;
      String value2;
      if (toggle.startsWith("!") && toggle.contains("-")) {
         String toggleStr = toggle.substring(1);
         String[] values = toggleStr.split("-");
         if (values.length == 2) {
            toggleMode = true;
            value1 = values[0];
            value2 = values[1];
         } else {
            value2 = "";
            value1 = "";
         }
      } else {
         value2 = "";
         value1 = "";
      }

      KeyBindConfig.KeyBindData data = (KeyBindConfig.KeyBindData)keyBinds.get(id);
      data.setName(name);
      data.setCommand(command);
      data.setToggleMode(toggleMode);
      data.setToggleValue1(value1);
      data.setToggleValue2(value2);
      data.setSet(true);
      toggleStates.put(id, false);
      KeyMapping mapping = LEXIS_KEYS[id - 1];
      updateKeyMappingName(mapping, "lexis " + name);
      config.save();
      ((CommandSourceStack)context.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f§f按键 " + id + " 已设置");
      }, false);
      ((CommandSourceStack)context.getSource()).m_288197_(() -> {
         return Component.m_237113_("§7名称: Lexis " + name);
      }, false);
      ((CommandSourceStack)context.getSource()).m_288197_(() -> {
         return Component.m_237113_("§7指令: " + command);
      }, false);
      if (toggleMode) {
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§7切换模式: " + value1 + " <-> " + value2);
         }, false);
      }

      return 1;
   }

   private static int setKeyBindWithoutToggle(CommandContext context) {
      int id = IntegerArgumentType.getInteger(context, "id");
      String name = StringArgumentType.getString(context, "name");
      String command = StringArgumentType.getString(context, "command");
      KeyBindConfig.KeyBindData data = (KeyBindConfig.KeyBindData)keyBinds.get(id);
      data.setName(name);
      data.setCommand(command);
      data.setToggleMode(false);
      data.setSet(true);
      KeyMapping mapping = LEXIS_KEYS[id - 1];
      updateKeyMappingName(mapping, "lexis " + name);
      config.save();
      ((CommandSourceStack)context.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f§f按键 " + id + " 已设置");
      }, false);
      ((CommandSourceStack)context.getSource()).m_288197_(() -> {
         return Component.m_237113_("§7显示名称: Lexis " + name);
      }, false);
      ((CommandSourceStack)context.getSource()).m_288197_(() -> {
         return Component.m_237113_("§7指令: " + command);
      }, false);
      return 1;
   }

   private static int delKeyBind(CommandContext context) {
      int id = IntegerArgumentType.getInteger(context, "id");
      KeyBindConfig.KeyBindData data = (KeyBindConfig.KeyBindData)keyBinds.get(id);
      if (!data.isSet()) {
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§d[§6Lexis§d] §f§c按键 " + id + " 未设置");
         }, false);
         return 0;
      } else {
         data.reset();
         KeyMapping mapping = LEXIS_KEYS[id - 1];
         updateKeyMappingName(mapping, "key.lexis." + id);
         config.save();
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§d[§6Lexis§d] §f§f按键 " + id + " 已删除");
         }, false);
         return 1;
      }
   }

   private static int listKeyBinds(CommandContext context) {
      ((CommandSourceStack)context.getSource()).m_288197_(() -> {
         return Component.m_237113_("§6===== Lexis 按键列表 =====");
      }, false);
      boolean hasAny = false;

      for(int i = 1; i <= 32; ++i) {
         KeyBindConfig.KeyBindData data = (KeyBindConfig.KeyBindData)keyBinds.get(i);
         if (data.isSet()) {
            hasAny = true;
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§e按键 " + i + " [Lexis " + data.getName() + "]:");
            }, false);
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("  §7指令: " + data.getCommand());
            }, false);
            if (data.isToggleMode()) {
               ((CommandSourceStack)context.getSource()).m_288197_(() -> {
                  String var10000 = data.getToggleValue1();
                  return Component.m_237113_("  §7切换: " + var10000 + " <--> " + data.getToggleValue2());
               }, false);
            }
         }
      }

      if (!hasAny) {
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§7没有已设置的按键");
         }, false);
      }

      return 1;
   }

   private static void executeKeyBind(int id) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null) {
         KeyBindConfig.KeyBindData data = (KeyBindConfig.KeyBindData)keyBinds.get(id);
         if (data != null && data.isSet()) {
            String command = data.getCommand();
            if (data.isToggleMode()) {
               boolean currentState = (Boolean)toggleStates.getOrDefault(id, false);
               boolean newState = !currentState;
               toggleStates.put(id, newState);
               String value = newState ? data.getToggleValue2() : data.getToggleValue1();
               if (command.contains("<>")) {
                  command = command.replace("<>", value);
               }
            }

            mc.f_91074_.f_108617_.m_246623_(command.startsWith("/") ? command.substring(1) : command);
         }
      }
   }

   private static void updateKeyMappingName(KeyMapping mapping, String newName) {
      try {
         Field nameField = KeyMapping.class.getDeclaredField("name");
         nameField.setAccessible(true);
         nameField.set(mapping, newName);
      } catch (Exception var3) {
         System.out.println("[Lexis] 更新按键名称失败: " + var3.getMessage());
      }

   }

   static {
      keyBinds = config.keyBinds;
      isInitialized = false;
   }

   @EventBusSubscriber(
      value = {Dist.CLIENT},
      bus = Bus.FORGE
   )
   public static class ClientForgeHandler {
      @SubscribeEvent
      public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
         CommandDispatcher dispatcher = event.getDispatcher();
         dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("keybind").then(Commands.m_82127_("set").then(Commands.m_82129_("id", IntegerArgumentType.integer(1, 32)).then(((RequiredArgumentBuilder)Commands.m_82129_("name", StringArgumentType.string()).then(Commands.m_82129_("command", StringArgumentType.string()).then(Commands.m_82129_("toggle", StringArgumentType.string()).executes(KeyBindCommand::setKeyBindWithToggle)))).then(Commands.m_82129_("command", StringArgumentType.greedyString()).executes(KeyBindCommand::setKeyBindWithoutToggle)))))).then(Commands.m_82127_("del").then(Commands.m_82129_("id", IntegerArgumentType.integer(1, 32)).executes(KeyBindCommand::delKeyBind)))).then(Commands.m_82127_("list").executes(KeyBindCommand::listKeyBinds)))));
      }

      @SubscribeEvent
      public static void onClientTick(TickEvent.ClientTickEvent event) {
         if (event.phase == Phase.END) {
            for(int i = 1; i <= 32; ++i) {
               KeyMapping key = KeyBindCommand.getKey(i);
               if (key != null && key.m_90857_()) {
                  KeyBindCommand.executeKeyBind(i);
               }
            }
         }

      }

      @SubscribeEvent
      public static void onKeyInput(InputEvent.Key event) {
      }
   }
}
