package moze_intel.projecte.api.nss;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractNSSTag implements NSSTag {
   private static final Set createdTags = new HashSet();
   private final @NotNull ResourceLocation resourceLocation;
   private final boolean isTag;

   public static Set getAllCreatedTags() {
      return ImmutableSet.copyOf(createdTags);
   }

   public static void clearCreatedTags() {
      createdTags.clear();
   }

   protected AbstractNSSTag(@NotNull ResourceLocation resourceLocation, boolean isTag) {
      this.resourceLocation = resourceLocation;
      this.isTag = isTag;
      if (isTag) {
         createdTags.add(this);
      }

   }

   public @NotNull ResourceLocation getResourceLocation() {
      return this.resourceLocation;
   }

   protected abstract boolean isInstance(AbstractNSSTag var1);

   protected abstract @NotNull String getType();

   protected abstract @NotNull String getJsonPrefix();

   protected abstract @NotNull Optional getTag();

   protected final Optional getTag(Registry registry) {
      return this.representsTag() ? registry.m_203431_(TagKey.m_203882_(registry.m_123023_(), this.getResourceLocation())).map(Either::left) : Optional.empty();
   }

   protected final Optional getTag(IForgeRegistry registry) {
      if (this.representsTag()) {
         ITagManager tags = registry.tags();
         if (tags != null) {
            return Optional.of(Either.right(tags.getTag(tags.createTagKey(this.getResourceLocation()))));
         }
      }

      return Optional.empty();
   }

   protected abstract Function createNew();

   public boolean representsTag() {
      return this.isTag;
   }

   public void forEachElement(Consumer consumer) {
      this.getTag().ifPresent((tag) -> {
         ((Stream)tag.map((t) -> {
            return t.m_203614_().map(Holder::m_203334_);
         }, ITag::stream)).map(this.createNew()).forEach(consumer);
      });
   }

   public String json() {
      String var10000;
      if (this.representsTag()) {
         var10000 = this.getJsonPrefix();
         return var10000 + "#" + this.getResourceLocation();
      } else {
         var10000 = this.getJsonPrefix();
         return var10000 + this.getResourceLocation();
      }
   }

   public String toString() {
      String var10000;
      if (this.representsTag()) {
         var10000 = this.getType();
         return var10000 + " Tag: " + this.getResourceLocation();
      } else {
         var10000 = this.getType();
         return var10000 + ": " + this.getResourceLocation();
      }
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         if (o instanceof AbstractNSSTag) {
            AbstractNSSTag other = (AbstractNSSTag)o;
            if (this.isInstance(other)) {
               return this.representsTag() == other.representsTag() && this.getResourceLocation().equals(other.getResourceLocation());
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return this.representsTag() ? 31 + this.resourceLocation.hashCode() : this.resourceLocation.hashCode();
   }
}
