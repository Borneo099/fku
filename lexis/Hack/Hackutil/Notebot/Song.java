package lexis.Hack.Hackutil.Notebot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Song {
   private final Map notesMap;
   private int lastTick;
   private final String title;
   private final String author;
   private final Set requirements = new HashSet();
   private boolean finishedLoading = false;

   public Song(Map notesMap, String title, String author) {
      this.notesMap = notesMap;
      this.title = title;
      this.author = author;
   }

   public void finishLoading() {
      if (this.finishedLoading) {
         throw new IllegalStateException("Song has already finished loading!");
      } else {
         this.lastTick = (Integer)Collections.max(this.notesMap.keySet());
         Iterator var1 = this.notesMap.values().iterator();

         while(var1.hasNext()) {
            List notes = (List)var1.next();
            this.requirements.addAll(notes);
         }

         this.finishedLoading = true;
      }
   }

   public Map getNotesMap() {
      return this.notesMap;
   }

   public Set getRequirements() {
      if (!this.finishedLoading) {
         throw new IllegalStateException("Song is still loading!");
      } else {
         return this.requirements;
      }
   }

   public int getLastTick() {
      if (!this.finishedLoading) {
         throw new IllegalStateException("Song is still loading!");
      } else {
         return this.lastTick;
      }
   }

   public String getTitle() {
      return this.title;
   }

   public String getAuthor() {
      return this.author;
   }
}
