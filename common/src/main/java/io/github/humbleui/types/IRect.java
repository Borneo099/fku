package io.github.humbleui.types;




public class IRect {
   public final int _left;
   public final int _top;
   public final int _right;
   public final int _bottom;

   public IRect(int l, int t, int r, int b) {
      this._left = l;
      this._top = t;
      this._right = r;
      this._bottom = b;
   }

   public int getWidth() {
      return this._right - this._left;
   }

   public int getHeight() {
      return this._bottom - this._top;
   }

   public  IRect withWidth(int width) {
      return new IRect(this._left, this._top, this._left + width, this._bottom);
   }

   public  IRect withHeight(int height) {
      return new IRect(this._left, this._top, this._right, this._top + height);
   }

   public static  IRect makeLTRB(int l, int t, int r, int b) {
      if (l > r) {
         throw new IllegalArgumentException("IRect::makeLTRB expected l <= r, got " + l + " > " + r);
      } else if (t > b) {
         throw new IllegalArgumentException("IRect::makeLTRB expected t <= b, got " + t + " > " + b);
      } else {
         return new IRect(l, t, r, b);
      }
   }

   public static  IRect makeXYWH(int l, int t, int w, int h) {
      if (w < 0) {
         throw new IllegalArgumentException("IRect::makeXYWH expected w >= 0, got: " + w);
      } else if (h < 0) {
         throw new IllegalArgumentException("IRect::makeXYWH expected h >= 0, got: " + h);
      } else {
         return w >= 0 && h >= 0 ? new IRect(l, t, l + w, t + h) : null;
      }
   }

   public static  IRect makeWH(int w, int h) {
      if (w < 0) {
         throw new IllegalArgumentException("IRect::makeWH expected w >= 0, got: " + w);
      } else if (h < 0) {
         throw new IllegalArgumentException("IRect::makeWH expected h >= 0, got: " + h);
      } else {
         return w >= 0 && h >= 0 ? new IRect(0, 0, w, h) : null;
      }
   }

   public  IRect intersect( IRect other) {
      assert other != null : "IRect::intersect expected other != null";

      return this._right > other._left && other._right > this._left && this._bottom > other._top && other._bottom > this._top ? new IRect(Math.max(this._left, other._left), Math.max(this._top, other._top), Math.min(this._right, other._right), Math.min(this._bottom, other._bottom)) : null;
   }

   public  IRect scale(int scale) {
      return this.scale(scale, scale);
   }

   public  IRect scale(int sx, int sy) {
      return sx == 1 && sy == 1 ? this : new IRect(this._left * sx, this._top * sy, this._right * sx, this._bottom * sy);
   }

   public  IRect offset(int dx, int dy) {
      return dx == 0 && dy == 0 ? this : new IRect(this._left + dx, this._top + dy, this._right + dx, this._bottom + dy);
   }

   public  IRect offset( IPoint vec) {
      assert vec != null : "IRect::offset expected vec != null";

      return this.offset(vec._x, vec._y);
   }

   public  Rect toRect() {
      return new Rect((float)this._left, (float)this._top, (float)this._right, (float)this._bottom);
   }

   public boolean isEmpty() {
      return this._right == this._left || this._top == this._bottom;
   }

   public boolean contains(int x, int y) {
      return this._left <= x && x <= this._right && this._top <= y && y <= this._bottom;
   }

   public boolean contains( IPoint vec) {
      assert vec != null : "IRect::contains expected vec != null";

      return this._left <= vec._x && vec._x <= this._right && this._top <= vec._y && vec._y <= this._bottom;
   }

   public int getLeft() {
      return this._left;
   }

   public int getTop() {
      return this._top;
   }

   public int getRight() {
      return this._right;
   }

   public int getBottom() {
      return this._bottom;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IRect)) {
         return false;
      } else {
         IRect other = (IRect)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (this.getLeft() != other.getLeft()) {
            return false;
         } else if (this.getTop() != other.getTop()) {
            return false;
         } else if (this.getRight() != other.getRight()) {
            return false;
         } else {
            return this.getBottom() == other.getBottom();
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof IRect;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getLeft();
      result = result * 59 + this.getTop();
      result = result * 59 + this.getRight();
      result = result * 59 + this.getBottom();
      return result;
   }

   public String toString() {
      int var10000 = this.getLeft();
      return "IRect(_left=" + var10000 + ", _top=" + this.getTop() + ", _right=" + this.getRight() + ", _bottom=" + this.getBottom() + ")";
   }

   public IRect withLeft(final int _left) {
      return this._left == _left ? this : new IRect(_left, this._top, this._right, this._bottom);
   }

   public IRect withTop(final int _top) {
      return this._top == _top ? this : new IRect(this._left, _top, this._right, this._bottom);
   }

   public IRect withRight(final int _right) {
      return this._right == _right ? this : new IRect(this._left, this._top, _right, this._bottom);
   }

   public IRect withBottom(final int _bottom) {
      return this._bottom == _bottom ? this : new IRect(this._left, this._top, this._right, _bottom);
   }
}
