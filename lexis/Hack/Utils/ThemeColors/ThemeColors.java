package lexis.Hack.Utils.ThemeColors;

import java.util.HashMap;
import java.util.Map;
import lexis.Hack.Utils.Colors.SettingColor;

public class ThemeColors {
   public SettingColor windowBackground = new SettingColor(30, 30, 30, 230);
   public SettingColor windowBorder = new SettingColor(80, 80, 100, 255);
   public SettingColor windowShadow = new SettingColor(0, 0, 0, 100);
   public SettingColor titleBackground = new SettingColor(40, 40, 50, 255);
   public SettingColor titleBackgroundHovered = new SettingColor(60, 60, 80, 255);
   public SettingColor titleText = new SettingColor(255, 255, 255, 255);
   public SettingColor buttonOn = new SettingColor(76, 175, 80, 255);
   public SettingColor buttonOff = new SettingColor(34, 34, 34, 68);
   public SettingColor buttonHovered = new SettingColor(51, 51, 51, 102);
   public SettingColor buttonText = new SettingColor(255, 255, 255, 255);
   public SettingColor textPrimary = new SettingColor(255, 255, 255, 255);
   public SettingColor keyText = new SettingColor(170, 170, 170, 255);
   public SettingColor scrollbarBg = new SettingColor(60, 60, 70, 200);
   public SettingColor scrollbarKnob = new SettingColor(120, 120, 130, 255);
   public SettingColor scrollbarKnobHovered = new SettingColor(180, 180, 200, 255);
   public SettingColor tooltipBg = new SettingColor(30, 30, 40, 230);
   public SettingColor tooltipBorder = new SettingColor(80, 80, 100, 255);
   public SettingColor tooltipText = new SettingColor(200, 200, 200, 255);

   public Map toMap() {
      Map map = new HashMap();
      map.put("windowBackground", this.colorToMap(this.windowBackground));
      map.put("windowBorder", this.colorToMap(this.windowBorder));
      map.put("windowShadow", this.colorToMap(this.windowShadow));
      map.put("titleBackground", this.colorToMap(this.titleBackground));
      map.put("titleBackgroundHovered", this.colorToMap(this.titleBackgroundHovered));
      map.put("titleText", this.colorToMap(this.titleText));
      map.put("buttonOn", this.colorToMap(this.buttonOn));
      map.put("buttonOff", this.colorToMap(this.buttonOff));
      map.put("buttonHovered", this.colorToMap(this.buttonHovered));
      map.put("buttonText", this.colorToMap(this.buttonText));
      map.put("textPrimary", this.colorToMap(this.textPrimary));
      map.put("keyText", this.colorToMap(this.keyText));
      map.put("scrollbarBg", this.colorToMap(this.scrollbarBg));
      map.put("scrollbarKnob", this.colorToMap(this.scrollbarKnob));
      map.put("scrollbarKnobHovered", this.colorToMap(this.scrollbarKnobHovered));
      map.put("tooltipBg", this.colorToMap(this.tooltipBg));
      map.put("tooltipBorder", this.colorToMap(this.tooltipBorder));
      map.put("tooltipText", this.colorToMap(this.tooltipText));
      return map;
   }

   public void fromMap(Map map) {
      if (map.containsKey("windowBackground")) {
         this.windowBackground = this.mapToColor((Map)map.get("windowBackground"));
      }

      if (map.containsKey("windowBorder")) {
         this.windowBorder = this.mapToColor((Map)map.get("windowBorder"));
      }

      if (map.containsKey("windowShadow")) {
         this.windowShadow = this.mapToColor((Map)map.get("windowShadow"));
      }

      if (map.containsKey("titleBackground")) {
         this.titleBackground = this.mapToColor((Map)map.get("titleBackground"));
      }

      if (map.containsKey("titleBackgroundHovered")) {
         this.titleBackgroundHovered = this.mapToColor((Map)map.get("titleBackgroundHovered"));
      }

      if (map.containsKey("titleText")) {
         this.titleText = this.mapToColor((Map)map.get("titleText"));
      }

      if (map.containsKey("buttonOn")) {
         this.buttonOn = this.mapToColor((Map)map.get("buttonOn"));
      }

      if (map.containsKey("buttonOff")) {
         this.buttonOff = this.mapToColor((Map)map.get("buttonOff"));
      }

      if (map.containsKey("buttonHovered")) {
         this.buttonHovered = this.mapToColor((Map)map.get("buttonHovered"));
      }

      if (map.containsKey("buttonText")) {
         this.buttonText = this.mapToColor((Map)map.get("buttonText"));
      }

      if (map.containsKey("textPrimary")) {
         this.textPrimary = this.mapToColor((Map)map.get("textPrimary"));
      }

      if (map.containsKey("keyText")) {
         this.keyText = this.mapToColor((Map)map.get("keyText"));
      }

      if (map.containsKey("scrollbarBg")) {
         this.scrollbarBg = this.mapToColor((Map)map.get("scrollbarBg"));
      }

      if (map.containsKey("scrollbarKnob")) {
         this.scrollbarKnob = this.mapToColor((Map)map.get("scrollbarKnob"));
      }

      if (map.containsKey("scrollbarKnobHovered")) {
         this.scrollbarKnobHovered = this.mapToColor((Map)map.get("scrollbarKnobHovered"));
      }

      if (map.containsKey("tooltipBg")) {
         this.tooltipBg = this.mapToColor((Map)map.get("tooltipBg"));
      }

      if (map.containsKey("tooltipBorder")) {
         this.tooltipBorder = this.mapToColor((Map)map.get("tooltipBorder"));
      }

      if (map.containsKey("tooltipText")) {
         this.tooltipText = this.mapToColor((Map)map.get("tooltipText"));
      }

   }

   private Map colorToMap(SettingColor c) {
      Map map = new HashMap();
      map.put("r", c.r);
      map.put("g", c.g);
      map.put("b", c.b);
      map.put("a", c.a);
      return map;
   }

   private SettingColor mapToColor(Map map) {
      return new SettingColor(((Double)map.get("r")).intValue(), ((Double)map.get("g")).intValue(), ((Double)map.get("b")).intValue(), ((Double)map.get("a")).intValue());
   }
}
