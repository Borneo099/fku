package moze_intel.projecte.utils;

import net.minecraft.tags.TagKey;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

public record LazyTagLookup(TagKey key, Lazy lazyTag) {
   public LazyTagLookup(TagKey key, Lazy lazyTag) {
      this.key = key;
      this.lazyTag = lazyTag;
   }

   public static LazyTagLookup create(IForgeRegistry registry, TagKey key) {
      return new LazyTagLookup(key, Lazy.of(() -> {
         return tagManager(registry).getTag(key);
      }));
   }

   public ITag tag() {
      return (ITag)this.lazyTag.get();
   }

   public boolean contains(Object element) {
      return this.tag().contains(element);
   }

   public boolean isEmpty() {
      return this.tag().isEmpty();
   }

   public static ITagManager tagManager(IForgeRegistry registry) {
      ITagManager tags = registry.tags();
      if (tags == null) {
         throw new IllegalStateException("Expected " + registry.getRegistryName() + " to have tags.");
      } else {
         return tags;
      }
   }

   public TagKey key() {
      return this.key;
   }

   public Lazy lazyTag() {
      return this.lazyTag;
   }
}
