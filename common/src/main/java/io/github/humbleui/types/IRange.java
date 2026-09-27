package io.github.humbleui.types;


public class IRange {
   
   public final int _start;
   
   public final int _end;

   public static IRange _makeFromLong(long l) {
      return new IRange((int)(l >>> 32), (int)(l & -1L));
   }

   public IRange(final int start, final int end) {
      this._start = start;
      this._end = end;
   }

   public int getStart() {
      return this._start;
   }

   public int getEnd() {
      return this._end;
   }

   public boolean equals(final Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof IRange)) {
         return false;
      } else {
         IRange other = (IRange)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (this.getStart() != other.getStart()) {
            return false;
         } else {
            return this.getEnd() == other.getEnd();
         }
      }
   }

   protected boolean canEqual(final Object other) {
      return other instanceof IRange;
   }

   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getStart();
      result = result * 59 + this.getEnd();
      return result;
   }

   public String toString() {
      int var10000 = this.getStart();
      return "IRange(_start=" + var10000 + ", _end=" + this.getEnd() + ")";
   }

   public IRange withStart(final int _start) {
      return this._start == _start ? this : new IRange(_start, this._end);
   }

   public IRange withEnd(final int _end) {
      return this._end == _end ? this : new IRange(this._start, _end);
   }
}
