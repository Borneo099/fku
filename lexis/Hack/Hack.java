package lexis.Hack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.HUD.HUDRenderer;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Minecraft;

public abstract class Hack {
   public static final Minecraft mc = Minecraft.m_91087_();
   private final String fixedName;
   private String description;
   private String[] descriptionLines;
   private Category category;
   private int keyBind;
   private boolean enabled;
   private boolean collapsed;
   private boolean toggleable;
   private List settings;
   private long lastKeyPressTime;
   private static final long KEY_COOLDOWN = 200L;
   private static boolean isLoading = false;

   public Hack(String name, String description, Category category) {
      this(name, description, category, true);
   }

   public Hack(String name, String description, Category category, boolean toggleable) {
      this.lastKeyPressTime = 0L;
      this.fixedName = name;
      this.description = description;
      this.descriptionLines = description.split("\n");
      this.category = category;
      this.toggleable = toggleable;
      this.keyBind = -1;
      this.enabled = false;
      this.collapsed = false;
      this.settings = new ArrayList();
   }

   public String getButtonName() {
      return this.getName();
   }

   public Hack(String name, String[] descriptionLines, Category category) {
      this(name, descriptionLines, category, true);
   }

   public Hack(String name, String[] descriptionLines, Category category, boolean toggleable) {
      this.lastKeyPressTime = 0L;
      this.fixedName = name;
      this.description = String.join("\n", descriptionLines);
      this.descriptionLines = descriptionLines;
      this.category = category;
      this.toggleable = toggleable;
      this.keyBind = -1;
      this.enabled = false;
      this.collapsed = false;
      this.settings = new ArrayList();
   }

   public abstract void onEnable();

   public abstract void onDisable();

   public abstract void onUpdate();

   public abstract void onClick();

   public void toggle() {
      if (this.toggleable) {
         this.setEnabled(!this.enabled);
      }
   }

   public void setEnabled(boolean enabled) {
      if (this.toggleable) {
         if (this.enabled != enabled) {
            this.enabled = enabled;
            if (!isLoading) {
               if (enabled) {
                  this.onEnable();
               } else {
                  this.onDisable();
               }

               HUDRenderer.onHackToggle(this);
               this.autoSave();
               HackManager.getInstance().saveToggleState(this);
            }

         }
      }
   }

   public static boolean isLoading() {
      return isLoading;
   }

   public static void setLoading(boolean loading) {
      isLoading = loading;
   }

   public static void setLoadingState(boolean loading) {
      isLoading = loading;
   }

   public void onRightClick() {
      if (this.getSettings() != null && !this.getSettings().isEmpty()) {
         Minecraft.m_91087_().m_91152_(new SettingsWindow(this, Minecraft.m_91087_().f_91080_));
      }

   }

   public boolean handleKeyPress() {
      if (this.keyBind == -1) {
         return false;
      } else {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastKeyPressTime < 200L) {
            return false;
         } else {
            this.lastKeyPressTime = currentTime;
            if (this.toggleable) {
               this.toggle();
            } else {
               this.onClick();
            }

            return true;
         }
      }
   }

   public final String getName() {
      return this.fixedName;
   }

   public String getDisplayName() {
      return this.getName();
   }

   public String getDescription() {
      return this.description;
   }

   public String[] getDescriptionLines() {
      return this.descriptionLines;
   }

   public Category getCategory() {
      return this.category;
   }

   public int getKeyBind() {
      return this.keyBind;
   }

   public void setKeyBind(int keyBind) {
      this.keyBind = keyBind;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isToggleable() {
      return this.toggleable;
   }

   public boolean isCollapsed() {
      return this.collapsed;
   }

   public void setCollapsed(boolean collapsed) {
      this.collapsed = collapsed;
   }

   public List getSettings() {
      return this.settings;
   }

   public void addSetting(Setting setting) {
      this.settings.add(setting);
      setting.setHack(this);
   }

   public void autoSave() {
      HackConfig.getInstance().saveHackSettings(this);
   }

   public static enum Category {
      COMBAT("战斗", -1146130),
      MOVEMENT("移动", -2461482),
      RENDER("渲染", -2252579),
      WORLD("世界", -18751),
      CHAT("聊天", -38476),
      BLOCKS("方块", -1644806),
      FUN("娱乐", -4565549),
      ITEMS("物品", -7114533),
      MISC("其他", -3730043),
      LEXIS("Lexis", -7722014),
      PROTECT("保护", -4565549),
      BARITONE("§dBaritone", -15617813),
      TACZ("§b§l§oTaCZ-永恒枪械工坊", -16728065),
      TACZ_SERVER("§9§lTaCZ-服务端", -14774017),
      CATACLYSM("§4§l灾变·L_Ender's Cataclysm", -47872);

      public final String displayName;
      public final int color;

      private Category(String displayName, int color) {
         this.displayName = displayName;
         this.color = color;
      }

      // $FF: synthetic method
      private static Category[] $values() {
         return new Category[]{COMBAT, MOVEMENT, RENDER, WORLD, CHAT, BLOCKS, FUN, ITEMS, MISC, LEXIS, PROTECT, BARITONE, TACZ, TACZ_SERVER, CATACLYSM};
      }
   }

   public static class Setting {
      private String name;
      private String description;
      private String[] descriptionLines;
      private SettingType type;
      private Object value;
      private Object defaultValue;
      private Object min;
      private Object max;
      private Runnable action;
      private Hack hack;
      private ValueDisplay displayFormat;

      public Setting(String name, String description, boolean defaultValue) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = description;
         this.descriptionLines = description.split("\n");
         this.type = Hack.Setting.SettingType.BOOLEAN;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
      }

      public Setting(String name, String[] descriptionLines, boolean defaultValue) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = String.join("\n", descriptionLines);
         this.descriptionLines = descriptionLines;
         this.type = Hack.Setting.SettingType.BOOLEAN;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
      }

      public Setting(String name, String description, int defaultValue, int min, int max) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = description;
         this.descriptionLines = description.split("\n");
         this.type = Hack.Setting.SettingType.INTEGER;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
         this.min = min;
         this.max = max;
      }

      public Setting(String name, String[] descriptionLines, int defaultValue, int min, int max) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = String.join("\n", descriptionLines);
         this.descriptionLines = descriptionLines;
         this.type = Hack.Setting.SettingType.INTEGER;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
         this.min = min;
         this.max = max;
      }

      public Setting(String name, String description, double defaultValue, double min, double max) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = description;
         this.descriptionLines = description.split("\n");
         this.type = Hack.Setting.SettingType.DOUBLE;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
         this.min = min;
         this.max = max;
      }

      public Setting(String name, String[] descriptionLines, double defaultValue, double min, double max) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = String.join("\n", descriptionLines);
         this.descriptionLines = descriptionLines;
         this.type = Hack.Setting.SettingType.DOUBLE;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
         this.min = min;
         this.max = max;
      }

      public Setting(String name, String description, String defaultValue, String[] options) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = description;
         this.descriptionLines = description.split("\n");
         this.type = Hack.Setting.SettingType.MODE;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
         this.min = options;
      }

      public Setting(String name, String[] descriptionLines, String defaultValue, String[] options) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = String.join("\n", descriptionLines);
         this.descriptionLines = descriptionLines;
         this.type = Hack.Setting.SettingType.MODE;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
         this.min = options;
      }

      public Setting(String name, String description, int defaultColor) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = description;
         this.descriptionLines = description.split("\n");
         this.type = Hack.Setting.SettingType.COLOR;
         this.value = defaultColor;
         this.defaultValue = defaultColor;
      }

      public Setting(String name, String[] descriptionLines, int defaultColor) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = String.join("\n", descriptionLines);
         this.descriptionLines = descriptionLines;
         this.type = Hack.Setting.SettingType.COLOR;
         this.value = defaultColor;
         this.defaultValue = defaultColor;
      }

      public Setting(String name, String description, String defaultValue) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = description;
         this.descriptionLines = description.split("\n");
         this.type = Hack.Setting.SettingType.STRING;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
      }

      public Setting(String name, String[] descriptionLines, String defaultValue) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = String.join("\n", descriptionLines);
         this.descriptionLines = descriptionLines;
         this.type = Hack.Setting.SettingType.STRING;
         this.value = defaultValue;
         this.defaultValue = defaultValue;
      }

      public Setting(String name, String description, String buttonText, Runnable action) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = description;
         this.descriptionLines = description.split("\n");
         this.type = Hack.Setting.SettingType.BUTTON;
         this.value = buttonText;
         this.defaultValue = buttonText;
         this.action = action;
      }

      public Setting(String name, String[] descriptionLines, String buttonText, Runnable action) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = String.join("\n", descriptionLines);
         this.descriptionLines = descriptionLines;
         this.type = Hack.Setting.SettingType.BUTTON;
         this.value = buttonText;
         this.defaultValue = buttonText;
         this.action = action;
      }

      public Setting(String name, String description, int defaultValue, int min, int max, ValueDisplay display) {
         this(name, description, defaultValue, min, max);
         this.displayFormat = display;
      }

      public Setting(String name, String description, double defaultValue, double min, double max, ValueDisplay display) {
         this(name, description, defaultValue, min, max);
         this.displayFormat = display;
      }

      public Setting(String name, String description, SettingType type) {
         this.displayFormat = Hack.ValueDisplay.DECIMAL;
         this.name = name;
         this.description = description;
         this.descriptionLines = description.split("\n");
         this.type = type;
      }

      public void setHack(Hack hack) {
         this.hack = hack;
      }

      public Hack getHack() {
         return this.hack;
      }

      public void autoSave() {
         if (this.hack != null) {
            HackConfig.getInstance().saveHackSettings(this.hack);
         }

      }

      public String getName() {
         return this.name;
      }

      public String getDescription() {
         return this.description;
      }

      public String[] getDescriptionLines() {
         return this.descriptionLines;
      }

      public SettingType getType() {
         return this.type;
      }

      public Runnable getAction() {
         return this.action;
      }

      public ValueDisplay getDisplayFormat() {
         return this.displayFormat;
      }

      public boolean getBoolean() {
         return (Boolean)this.value;
      }

      public int getInt() {
         if (this.value instanceof Integer) {
            return (Integer)this.value;
         } else if (this.value instanceof Double) {
            return ((Double)this.value).intValue();
         } else if (this.value instanceof Float) {
            return ((Float)this.value).intValue();
         } else if (this.value instanceof Long) {
            return ((Long)this.value).intValue();
         } else {
            return this.value instanceof Number ? ((Number)this.value).intValue() : 0;
         }
      }

      public double getDouble() {
         if (this.value instanceof Double) {
            return (Double)this.value;
         } else if (this.value instanceof Integer) {
            return ((Integer)this.value).doubleValue();
         } else if (this.value instanceof Float) {
            return ((Float)this.value).doubleValue();
         } else if (this.value instanceof Long) {
            return ((Long)this.value).doubleValue();
         } else {
            return this.value instanceof Number ? ((Number)this.value).doubleValue() : 0.0;
         }
      }

      public String getString() {
         return (String)this.value;
      }

      public Object getValue() {
         return this.value;
      }

      public String[] getOptions() {
         return this.type == Hack.Setting.SettingType.MODE ? (String[])this.min : null;
      }

      public Object getMin() {
         return this.min;
      }

      public Object getMax() {
         return this.max;
      }

      public double getMinDouble() {
         return this.min instanceof Number ? ((Number)this.min).doubleValue() : 0.0;
      }

      public double getMaxDouble() {
         return this.max instanceof Number ? ((Number)this.max).doubleValue() : 0.0;
      }

      public int getMinInt() {
         return this.min instanceof Number ? ((Number)this.min).intValue() : 0;
      }

      public int getMaxInt() {
         return this.max instanceof Number ? ((Number)this.max).intValue() : 0;
      }

      public void setValue(Object value) {
         switch (this.type) {
            case BOOLEAN:
               this.value = value;
               break;
            case INTEGER:
               int intVal = value instanceof Number ? ((Number)value).intValue() : 0;
               intVal = Math.max((Integer)this.min, Math.min((Integer)this.max, intVal));
               this.value = intVal;
               break;
            case DOUBLE:
               double doubleVal = value instanceof Number ? ((Number)value).doubleValue() : 0.0;
               doubleVal = Math.max(((Number)this.min).doubleValue(), Math.min(((Number)this.max).doubleValue(), doubleVal));
               this.value = doubleVal;
               break;
            case MODE:
            case STRING:
            case BUTTON:
               this.value = value;
               break;
            case COLOR:
               this.value = value instanceof Number ? ((Number)value).intValue() : value;
               break;
            default:
               this.value = value;
         }

      }

      public static enum SettingType {
         BOOLEAN,
         INTEGER,
         DOUBLE,
         MODE,
         COLOR,
         STRING,
         BUTTON,
         ITEM_LIST,
         BLOCK_LIST;

         // $FF: synthetic method
         private static SettingType[] $values() {
            return new SettingType[]{BOOLEAN, INTEGER, DOUBLE, MODE, COLOR, STRING, BUTTON, ITEM_LIST, BLOCK_LIST};
         }
      }
   }

   public static enum ValueDisplay {
      INTEGER((v) -> {
         return String.valueOf((int)v);
      }),
      DECIMAL((v) -> {
         String s = String.format("%.10f", v);
         s = s.replaceAll("0*$", "").replaceAll("\\.$", "");
         return s;
      }),
      PERCENTAGE((v) -> {
         return (int)(v * 100.0) + "%";
      }),
      LOGARITHMIC((v) -> {
         return String.format("%,.0f", Math.pow(10.0, v));
      }),
      DEGREES((v) -> {
         return (int)v + "°";
      }),
      ROUNDING_PRECISION((v) -> {
         int precision = (int)v;
         return precision == 0 ? "1" : "0." + "0".repeat(precision - 1) + "1";
      }),
      AREA_FROM_RADIUS((v) -> {
         int d = 2 * (int)v + 1;
         return "" + d + "x" + d;
      }),
      NONE((v) -> {
         return "";
      });

      private final DoubleFunction formatter;

      private ValueDisplay(DoubleFunction formatter) {
         this.formatter = formatter;
      }

      public String format(double v) {
         return (String)this.formatter.apply(v);
      }

      // $FF: synthetic method
      private static ValueDisplay[] $values() {
         return new ValueDisplay[]{INTEGER, DECIMAL, PERCENTAGE, LOGARITHMIC, DEGREES, ROUNDING_PRECISION, AREA_FROM_RADIUS, NONE};
      }
   }
}
