package lexis.Hack.Hacks.Lexis;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import org.lwjgl.glfw.GLFW;

public class GuiKeyBindHack extends Hack {
   private static final String CONFIG_KEY = "更改GUI打开";
   private int guiKey = 77;
   private boolean binding = false;
   private HackConfig config = HackConfig.getInstance();

   public GuiKeyBindHack() {
      super("更改GUI打开", "更改打开 GUI 的按键", Hack.Category.LEXIS, true);
      this.loadConfig();
   }

   private void loadConfig() {
      this.guiKey = this.config.getIntSetting("更改GUI打开", "按键", 77);
   }

   public void save() {
      this.config.saveHackSettings("更改GUI打开", this.getSettings());
   }

   public int getGuiKey() {
      return this.guiKey;
   }

   public void setGuiKey(int key) {
      this.guiKey = key;
      this.save();
   }

   public boolean isBinding() {
      return this.binding;
   }

   public void startBinding() {
      this.binding = true;
   }

   public void stopBinding() {
      this.binding = false;
   }

   public void handleKeyPress(int key) {
      if (this.binding) {
         if (key == 256) {
            this.binding = false;
         } else {
            this.setGuiKey(key);
            this.binding = false;
         }
      }
   }

   public String getButtonName() {
      if (this.binding) {
         return this.getName() + " §f[...]";
      } else {
         String keyName = this.getKeyName(this.guiKey);
         if (keyName.isEmpty()) {
            return this.getName();
         } else {
            String var10000 = this.getName();
            return var10000 + " §f[" + keyName + "]";
         }
      }
   }

   private String getKeyName(int key) {
      if (key == -1) {
         return "";
      } else {
         String name = GLFW.glfwGetKeyName(key, 0);
         if (name != null && !name.isEmpty()) {
            return name.toUpperCase();
         } else {
            switch (key) {
               case 32:
                  return "SPACE";
               case 256:
                  return "ESC";
               case 257:
                  return "ENTER";
               case 258:
                  return "TAB";
               case 259:
                  return "BACKSPACE";
               case 260:
                  return "INSERT";
               case 261:
                  return "DELETE";
               case 262:
                  return "→";
               case 263:
                  return "←";
               case 264:
                  return "↓";
               case 265:
                  return "↑";
               case 266:
                  return "PAGEUP";
               case 267:
                  return "PAGEDOWN";
               case 268:
                  return "HOME";
               case 269:
                  return "END";
               case 280:
                  return "CAPS";
               case 281:
                  return "SCROLLLOCK";
               case 282:
                  return "NUMLOCK";
               case 340:
                  return "LSHIFT";
               case 341:
                  return "LCTRL";
               case 342:
                  return "LALT";
               case 344:
                  return "RSHIFT";
               case 345:
                  return "RCTRL";
               case 346:
                  return "RALT";
               default:
                  if (key >= 290 && key <= 314) {
                     return "F" + (key - 290 + 1);
                  } else {
                     return key >= 320 && key <= 329 ? "KP_" + (key - 320) : "KEY_" + key;
                  }
            }
         }
      }
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.startBinding();
   }
}
