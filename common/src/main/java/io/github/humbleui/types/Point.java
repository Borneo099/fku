package io.github.humbleui.types;




public class Point {
   public static final Point ZERO = new Point(0.0F, 0.0F);
   
   public final float _x;
   
   public final float _y;

   public static  float[] flattenArray( Point[] pts) {
      if (pts == null) {
         return null;
      } else {
         float[] arr = new float[pts.length * 2];

         for(int i = 0; i < pts.length; ++i) {
            arr[i * 2] = pts[i]._x;
            arr[i * 2 + 1] = pts[i]._y;
         }

         return arr;
      }
   }

   public static  Point[] fromArray( float[] pts) {
      if (pts == null) {
         return null;
      } else {
         assert pts.length % 2 == 0 : "Expected " + pts.length + " % 2 == 0";

         Point[] arr = new Point[pts.length / 2];

         for(int i = 0; i < pts.length / 2; ++i) {
            arr[i] = new Point(pts[i * 2], pts[i * 2 + 1]);
         }

         return arr;
      }
   }

   public  Point offset(float dx, float dy) {
      return dx == 0.0F && dy == 0.0F ? this : new Point(this._x + dx, this._y + dy);
   }

   public  Point offset( Point vec) {
      assert vec != null : "Point::offset expected other != null";

      return this.offset(vec._x, vec._y);
   }

   public  Point scale(float scale) {
      return this.scale(scale, scale);
   }

   public  Point scale(float sx, float sy) {
      return (sx != 1.0F || sy != 1.0F) && (this._x != 0.0F || this._y != 0.0F) ? new Point(this._x * sx, this._y * sy) : this;
   }

   public  Point inverse() {
      return this.scale(-1.0F, -1.0F);
   }

   public boolean isEmpty() {
      return this._x <= 0.0F || this._y <= 0.0F;
   }

   public  IPoint toIPoint() {
      return new IPoint((int)this._x, (int)this._y);
   }

   public Point(final float x, final float y) {
      this._x = x;
      this._y = y;
   }

   public float getX() {
      return this._x;
   }

   public float getY() {
      return this._y;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Point)) {
         return false;
      } else {
         Point other = (Point)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (Float.compare(this.getX(), other.getX()) != 0) {
            return false;
         } else {
            return Float.compare(this.getY(), other.getY()) == 0;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof Point;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getX());
      result = result * 59 + Float.floatToIntBits(this.getY());
      return result;
   }

   public String toString() {
      float var10000 = this.getX();
      return "Point(_x=" + var10000 + ", _y=" + this.getY() + ")";
   }

   public Point withX(final float _x) {
      return this._x == _x ? this : new Point(_x, this._y);
   }

   public Point withY(final float _y) {
      return this._y == _y ? this : new Point(this._x, _y);
   }
}
