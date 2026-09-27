package io.github.humbleui.types;


public class Point3 {
   public static final Point ZERO = new Point(0.0F, 0.0F);
   
   public final float _x;
   
   public final float _y;
   
   public final float _z;

   public Point3(final float x, final float y, final float z) {
      this._x = x;
      this._y = y;
      this._z = z;
   }

   public float getX() {
      return this._x;
   }

   public float getY() {
      return this._y;
   }

   public float getZ() {
      return this._z;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Point3)) {
         return false;
      } else {
         Point3 other = (Point3)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (Float.compare(this.getX(), other.getX()) != 0) {
            return false;
         } else if (Float.compare(this.getY(), other.getY()) != 0) {
            return false;
         } else {
            return Float.compare(this.getZ(), other.getZ()) == 0;
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof Point3;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getX());
      result = result * 59 + Float.floatToIntBits(this.getY());
      result = result * 59 + Float.floatToIntBits(this.getZ());
      return result;
   }

   public String toString() {
      float var10000 = this.getX();
      return "Point3(_x=" + var10000 + ", _y=" + this.getY() + ", _z=" + this.getZ() + ")";
   }

   public Point3 withX(final float _x) {
      return this._x == _x ? this : new Point3(_x, this._y, this._z);
   }

   public Point3 withY(final float _y) {
      return this._y == _y ? this : new Point3(this._x, _y, this._z);
   }

   public Point3 withZ(final float _z) {
      return this._z == _z ? this : new Point3(this._x, this._y, _z);
   }
}
