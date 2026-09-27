package io.github.humbleui.types;




public class Rect {
   public final float _left;
   public final float _top;
   public final float _right;
   public final float _bottom;

   public Rect(float l, float t, float r, float b) {
      this._left = l;
      this._top = t;
      this._right = r;
      this._bottom = b;
   }

   public float getWidth() {
      return this._right - this._left;
   }

   public float getHeight() {
      return this._bottom - this._top;
   }

   public  Rect withWidth(float width) {
      return new Rect(this._left, this._top, this._left + width, this._bottom);
   }

   public  Rect withHeight(float height) {
      return new Rect(this._left, this._top, this._right, this._top + height);
   }

   public static  Rect makeLTRB(float l, float t, float r, float b) {
      if (l > r) {
         throw new IllegalArgumentException("Rect::makeLTRB expected l <= r, got " + l + " > " + r);
      } else if (t > b) {
         throw new IllegalArgumentException("Rect::makeLTRB expected t <= b, got " + t + " > " + b);
      } else {
         return new Rect(l, t, r, b);
      }
   }

   public static  Rect makeWH(float w, float h) {
      if (w < 0.0F) {
         throw new IllegalArgumentException("Rect::makeWH expected w >= 0, got: " + w);
      } else if (h < 0.0F) {
         throw new IllegalArgumentException("Rect::makeWH expected h >= 0, got: " + h);
      } else {
         return new Rect(0.0F, 0.0F, w, h);
      }
   }

   public static  Rect makeWH( Point size) {
      assert size != null : "Rect::makeWH expected size != null";

      return makeWH(size._x, size._y);
   }

   public static  Rect makeXYWH(float l, float t, float w, float h) {
      if (w < 0.0F) {
         throw new IllegalArgumentException("Rect::makeXYWH expected w >= 0, got: " + w);
      } else if (h < 0.0F) {
         throw new IllegalArgumentException("Rect::makeXYWH expected h >= 0, got: " + h);
      } else {
         return new Rect(l, t, l + w, t + h);
      }
   }

   public  Rect intersect( Rect other) {
      assert other != null : "Rect::intersect expected other != null";

      return !(this._right <= other._left) && !(other._right <= this._left) && !(this._bottom <= other._top) && !(other._bottom <= this._top) ? new Rect(Math.max(this._left, other._left), Math.max(this._top, other._top), Math.min(this._right, other._right), Math.min(this._bottom, other._bottom)) : null;
   }

   public  Rect scale(float scale) {
      return this.scale(scale, scale);
   }

   public  Rect scale(float sx, float sy) {
      return sx == 1.0F && sy == 1.0F ? this : new Rect(this._left * sx, this._top * sy, this._right * sx, this._bottom * sy);
   }

   public  Rect offset(float dx, float dy) {
      return dx == 0.0F && dy == 0.0F ? this : new Rect(this._left + dx, this._top + dy, this._right + dx, this._bottom + dy);
   }

   public  Rect offset( Point vec) {
      assert vec != null : "Rect::offset expected vec != null";

      return this.offset(vec._x, vec._y);
   }

   public  IRect toIRect() {
      return new IRect((int)this._left, (int)this._top, (int)this._right, (int)this._bottom);
   }

   public  RRect withRadii(float radius) {
      return RRect.makeLTRB(this._left, this._top, this._right, this._bottom, radius);
   }

   public  RRect withRadii(float xRad, float yRad) {
      return RRect.makeLTRB(this._left, this._top, this._right, this._bottom, xRad, yRad);
   }

   public  RRect withRadii(float tlRad, float trRad, float brRad, float blRad) {
      return RRect.makeLTRB(this._left, this._top, this._right, this._bottom, tlRad, trRad, brRad, blRad);
   }

   public  RRect withRadii(float[] radii) {
      return RRect.makeComplexLTRB(this._left, this._top, this._right, this._bottom, radii);
   }

   public  Rect inflate(float spread) {
      return (Rect)(spread <= 0.0F ? makeLTRB(this._left - spread, this._top - spread, Math.max(this._left - spread, this._right + spread), Math.max(this._top - spread, this._bottom + spread)) : RRect.makeLTRB(this._left - spread, this._top - spread, Math.max(this._left - spread, this._right + spread), Math.max(this._top - spread, this._bottom + spread), spread));
   }

   public boolean isEmpty() {
      return this._right == this._left || this._top == this._bottom;
   }

   public boolean contains(float x, float y) {
      return this._left <= x && x <= this._right && this._top <= y && y <= this._bottom;
   }

   public boolean contains( Point vec) {
      assert vec != null : "Rect::contains expected vec != null";

      return this._left <= vec._x && vec._x <= this._right && this._top <= vec._y && vec._y <= this._bottom;
   }

   public float getLeft() {
      return this._left;
   }

   public float getTop() {
      return this._top;
   }

   public float getRight() {
      return this._right;
   }

   public float getBottom() {
      return this._bottom;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Rect)) {
         return false;
      } else {
         Rect other = (Rect)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (Float.compare(this.getLeft(), other.getLeft()) != 0) {
            return false;
         } else if (Float.compare(this.getTop(), other.getTop()) != 0) {
            return false;
         } else if (Float.compare(this.getRight(), other.getRight()) != 0) {
            return false;
         } else {
            return Float.compare(this.getBottom(), other.getBottom()) == 0;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof Rect;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getLeft());
      result = result * 59 + Float.floatToIntBits(this.getTop());
      result = result * 59 + Float.floatToIntBits(this.getRight());
      result = result * 59 + Float.floatToIntBits(this.getBottom());
      return result;
   }

   public String toString() {
      float var10000 = this.getLeft();
      return "Rect(_left=" + var10000 + ", _top=" + this.getTop() + ", _right=" + this.getRight() + ", _bottom=" + this.getBottom() + ")";
   }

   public Rect withLeft(final float _left) {
      return this._left == _left ? this : new Rect(_left, this._top, this._right, this._bottom);
   }

   public Rect withTop(final float _top) {
      return this._top == _top ? this : new Rect(this._left, _top, this._right, this._bottom);
   }

   public Rect withRight(final float _right) {
      return this._right == _right ? this : new Rect(this._left, this._top, _right, this._bottom);
   }

   public Rect withBottom(final float _bottom) {
      return this._bottom == _bottom ? this : new Rect(this._left, this._top, this._right, _bottom);
   }
}
