package lexis.Hack.Hacks.Render;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class OldWeaponHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "RXS物品";
   private float posX = -0.8F;
   private float posY = -0.6F;
   private float posZ = -1.2F;
   private float scale = 0.8F;

   public OldWeaponHack() {
      super("RXS物品", "物品渲染位置改为位置拍视频装逼？母牛100亿玩家看哭了", Hack.Category.RENDER, true);
      this.loadConfig();
   }

   private void loadConfig() {
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public float getPosX() {
      return this.posX;
   }

   public float getPosY() {
      return this.posY;
   }

   public float getPosZ() {
      return this.posZ;
   }

   public float getScale() {
      return this.scale;
   }

   public void onClick() {
      this.toggle();
   }
}
