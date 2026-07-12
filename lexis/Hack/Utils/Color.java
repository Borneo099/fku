package lexis.Hack.Utils;

public class Color {
   public int r;
   public int g;
   public int b;
   public int a;

   public Color() {
      this(255, 255, 255, 255);
   }

   public Color(int r, int g, int b) {
      this(r, g, b, 255);
   }

   public Color(int r, int g, int b, int a) {
      this.r = r;
      this.g = g;
      this.b = b;
      this.a = a;
      this.validate();
   }

   public Color(float r, float g, float b, float a) {
      this.r = (int)(r * 255.0F);
      this.g = (int)(g * 255.0F);
      this.b = (int)(b * 255.0F);
      this.a = (int)(a * 255.0F);
      this.validate();
   }

   public Color(int packed) {
      this.r = packed >> 16 & 255;
      this.g = packed >> 8 & 255;
      this.b = packed & 255;
      this.a = packed >> 24 & 255;
   }

   public Color(Color color) {
      this.r = color.r;
      this.g = color.g;
      this.b = color.b;
      this.a = color.a;
   }

   public Color set(int r, int g, int b, int a) {
      this.r = r;
      this.g = g;
      this.b = b;
      this.a = a;
      this.validate();
      return this;
   }

   public Color set(Color color) {
      this.r = color.r;
      this.g = color.g;
      this.b = color.b;
      this.a = color.a;
      return this;
   }

   public void validate() {
      if (this.r < 0) {
         this.r = 0;
      }

      if (this.r > 255) {
         this.r = 255;
      }

      if (this.g < 0) {
         this.g = 0;
      }

      if (this.g > 255) {
         this.g = 255;
      }

      if (this.b < 0) {
         this.b = 0;
      }

      if (this.b > 255) {
         this.b = 255;
      }

      if (this.a < 0) {
         this.a = 0;
      }

      if (this.a > 255) {
         this.a = 255;
      }

   }

   public int getPacked() {
      return this.a << 24 | this.r << 16 | this.g << 8 | this.b;
   }

   public float[] getFloatComponents() {
      return new float[]{(float)this.r / 255.0F, (float)this.g / 255.0F, (float)this.b / 255.0F, (float)this.a / 255.0F};
   }

   public Color copy() {
      return new Color(this.r, this.g, this.b, this.a);
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         Color color = (Color)obj;
         return this.r == color.r && this.g == color.g && this.b == color.b && this.a == color.a;
      } else {
         return false;
      }
   }
}
