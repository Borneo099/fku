package io.github.humbleui.types;

import java.util.Arrays;


public class RRect extends Rect {
   public final float[] _radii;

   public RRect(float l, float t, float r, float b, float[] radii) {
      super(l, t, r, b);
      this._radii = radii;
   }

   public static RRect makeLTRB(float l, float t, float r, float b, float radius) {
      return new RRect(l, t, r, b, new float[]{radius});
   }

   public static RRect makeLTRB(float l, float t, float r, float b, float xRad, float yRad) {
      return new RRect(l, t, r, b, new float[]{xRad, yRad});
   }

   public static RRect makeLTRB(float l, float t, float r, float b, float tlRad, float trRad, float brRad, float blRad) {
      return new RRect(l, t, r, b, new float[]{tlRad, trRad, brRad, blRad});
   }

   public static RRect makeNinePatchLTRB(float l, float t, float r, float b, float lRad, float tRad, float rRad, float bRad) {
      return new RRect(l, t, r, b, new float[]{lRad, tRad, rRad, tRad, rRad, bRad, lRad, bRad});
   }

   public static RRect makeComplexLTRB(float l, float t, float r, float b, float[] radii) {
      return new RRect(l, t, r, b, radii);
   }

   public static RRect makeOvalLTRB(float l, float t, float r, float b) {
      return new RRect(l, t, r, b, new float[]{Math.abs(r - l) / 2.0F, Math.abs(b - t) / 2.0F});
   }

   public static RRect makePillLTRB(float l, float t, float r, float b) {
      return new RRect(l, t, r, b, new float[]{Math.min(Math.abs(r - l), Math.abs(t - b)) / 2.0F});
   }

   public static RRect makeXYWH(float l, float t, float w, float h, float radius) {
      return new RRect(l, t, l + w, t + h, new float[]{radius});
   }

   public static RRect makeXYWH(float l, float t, float w, float h, float xRad, float yRad) {
      return new RRect(l, t, l + w, t + h, new float[]{xRad, yRad});
   }

   public static RRect makeXYWH(float l, float t, float w, float h, float tlRad, float trRad, float brRad, float blRad) {
      return new RRect(l, t, l + w, t + h, new float[]{tlRad, trRad, brRad, blRad});
   }

   public static RRect makeNinePatchXYWH(float l, float t, float w, float h, float lRad, float tRad, float rRad, float bRad) {
      return new RRect(l, t, l + w, t + h, new float[]{lRad, tRad, rRad, tRad, rRad, bRad, lRad, bRad});
   }

   public static RRect makeComplexXYWH(float l, float t, float w, float h, float[] radii) {
      return new RRect(l, t, l + w, t + h, radii);
   }

   public static RRect makeOvalXYWH(float l, float t, float w, float h) {
      return new RRect(l, t, l + w, t + h, new float[]{w / 2.0F, h / 2.0F});
   }

   public static RRect makePillXYWH(float l, float t, float w, float h) {
      return new RRect(l, t, l + w, t + h, new float[]{Math.min(w, h) / 2.0F});
   }

   public  RRect scale(float scale) {
      return this.scale(scale, scale);
   }

   public  RRect scale(float sx, float sy) {
      if (sx == 1.0F && sy == 1.0F) {
         return this;
      } else {
         if (sx == sy) {
            switch (this._radii.length) {
               case 1:
                  return new RRect(this._left * sx, this._top * sx, this._right * sx, this._bottom * sx, new float[]{this._radii[0] * sx});
               case 2:
                  return new RRect(this._left * sx, this._top * sx, this._right * sx, this._bottom * sx, new float[]{this._radii[0] * sx, this._radii[1] * sx});
               case 3:
               case 5:
               case 6:
               case 7:
               default:
                  break;
               case 4:
                  return new RRect(this._left * sx, this._top * sx, this._right * sx, this._bottom * sx, new float[]{this._radii[0] * sx, this._radii[1] * sx, this._radii[2] * sx, this._radii[3] * sx});
               case 8:
                  return new RRect(this._left * sx, this._top * sx, this._right * sx, this._bottom * sx, new float[]{this._radii[0] * sx, this._radii[1] * sx, this._radii[2] * sx, this._radii[3] * sx, this._radii[4] * sx, this._radii[5] * sx, this._radii[6] * sx, this._radii[7] * sx});
            }
         } else {
            switch (this._radii.length) {
               case 1:
                  return new RRect(this._left * sx, this._top * sy, this._right * sx, this._bottom * sy, new float[]{this._radii[0] * sx, this._radii[0] * sy});
               case 2:
                  return new RRect(this._left * sx, this._top * sy, this._right * sx, this._bottom * sy, new float[]{this._radii[0] * sx, this._radii[1] * sy});
               case 3:
               case 5:
               case 6:
               case 7:
               default:
                  break;
               case 4:
                  return new RRect(this._left * sx, this._top * sy, this._right * sx, this._bottom * sy, new float[]{this._radii[0] * sx, this._radii[0] * sy, this._radii[1] * sx, this._radii[1] * sy, this._radii[2] * sx, this._radii[2] * sy, this._radii[3] * sx, this._radii[3] * sy});
               case 8:
                  return new RRect(this._left * sx, this._top * sy, this._right * sx, this._bottom * sy, new float[]{this._radii[0] * sx, this._radii[1] * sy, this._radii[2] * sx, this._radii[3] * sy, this._radii[4] * sx, this._radii[5] * sy, this._radii[6] * sx, this._radii[7] * sy});
            }
         }

         throw new RuntimeException("Unreachable, _radii=" + Arrays.toString(this._radii));
      }
   }

   public  RRect offset(float dx, float dy) {
      return dx == 0.0F && dy == 0.0F ? this : new RRect(this._left + dx, this._top + dy, this._right + dx, this._bottom + dy, this._radii);
   }

   public  RRect offset( Point vec) {
      assert vec != null : "Rect::offset expected vec != null";

      return this.offset(vec._x, vec._y);
   }

   public  Rect inflate(float spread) {
      boolean becomesRect = true;

      for(int i = 0; i < this._radii.length; ++i) {
         if (this._radii[i] + spread >= 0.0F) {
            becomesRect = false;
            break;
         }
      }

      if (becomesRect) {
         return makeLTRB(this._left - spread, this._top - spread, Math.max(this._left - spread, this._right + spread), Math.max(this._top - spread, this._bottom + spread));
      } else {
         float[] radii = Arrays.copyOf(this._radii, this._radii.length);

         for(int i = 0; i < radii.length; ++i) {
            radii[i] = Math.max(0.0F, radii[i] + spread);
         }

         return new RRect(this._left - spread, this._top - spread, Math.max(this._left - spread, this._right + spread), Math.max(this._top - spread, this._bottom + spread), radii);
      }
   }

   public String toString() {
      float var10000 = this._left;
      return "RRect(_left=" + var10000 + ", _top=" + this._top + ", _right=" + this._right + ", _bottom=" + this._bottom + ", _radii=" + Arrays.toString(this._radii) + ")";
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof RRect)) {
         return false;
      } else {
         RRect other = (RRect)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            return Arrays.equals(this._radii, other._radii);
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof RRect;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      result = result * 59 + Arrays.hashCode(this._radii);
      return result;
   }
}
