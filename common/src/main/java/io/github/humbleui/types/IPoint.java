package io.github.humbleui.types;



public class IPoint {
   public static final IPoint ZERO = new IPoint(0, 0);
   
   public final int _x;
   
   public final int _y;

   public static IPoint _makeFromLong(long l) {
      return new IPoint((int)(l >>> 32), (int)(l & -1L));
   }

   public  IPoint offset(int dx, int dy) {
      return dx == 0 && dy == 0 ? this : new IPoint(this._x + dx, this._y + dy);
   }

   public  IPoint offset( IPoint vec) {
      assert vec != null : "IPoint::offset expected other != null";

      return this.offset(vec._x, vec._y);
   }

   public  IPoint scale(int scale) {
      return this.scale(scale, scale);
   }

   public  IPoint scale(int sx, int sy) {
      return (sx != 1 || sy != 1) && (this._x != 0 || this._y != 0) ? new IPoint(this._x * sx, this._y * sy) : this;
   }

   public  IPoint inverse() {
      return this.scale(-1, -1);
   }

   public boolean isEmpty() {
      return this._x <= 0 || this._y <= 0;
   }

   public  Point toPoint() {
      return new Point((float)this._x, (float)this._y);
   }

   public IPoint(final int x, final int y) {
      this._x = x;
      this._y = y;
   }

   public int getX() {
      return this._x;
   }

   public int getY() {
      return this._y;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IPoint)) {
         return false;
      } else {
         IPoint other = (IPoint)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (this.getX() != other.getX()) {
            return false;
         } else {
            return this.getY() == other.getY();
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof IPoint;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getX();
      result = result * 59 + this.getY();
      return result;
   }

   public String toString() {
      int var10000 = this.getX();
      return "IPoint(_x=" + var10000 + ", _y=" + this.getY() + ")";
   }

   public IPoint withX(final int _x) {
      return this._x == _x ? this : new IPoint(_x, this._y);
   }

   public IPoint withY(final int _y) {
      return this._y == _y ? this : new IPoint(this._x, _y);
   }
}
