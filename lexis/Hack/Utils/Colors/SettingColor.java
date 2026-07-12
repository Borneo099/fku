package lexis.Hack.Utils.Colors;

public class SettingColor extends Color {
   public boolean rainbow;
   public float rainbowSpeed = 1.0F;

   public SettingColor() {
   }

   public SettingColor(int packed) {
      super(packed);
   }

   public SettingColor(int r, int g, int b) {
      super(r, g, b);
   }

   public SettingColor(int r, int g, int b, int a) {
      super(r, g, b, a);
   }

   public SettingColor(int r, int g, int b, boolean rainbow) {
      super(r, g, b);
      this.rainbow = rainbow;
   }

   public SettingColor(int r, int g, int b, int a, boolean rainbow) {
      super(r, g, b, a);
      this.rainbow = rainbow;
   }

   public SettingColor(SettingColor color) {
      super(color);
      this.rainbow = color.rainbow;
      this.rainbowSpeed = color.rainbowSpeed;
   }

   public SettingColor rainbow(boolean rainbow) {
      this.rainbow = rainbow;
      return this;
   }

   public SettingColor rainbowSpeed(float speed) {
      this.rainbowSpeed = speed;
      return this;
   }

   public SettingColor copy() {
      return new SettingColor(this.r, this.g, this.b, this.a, this.rainbow);
   }
}
