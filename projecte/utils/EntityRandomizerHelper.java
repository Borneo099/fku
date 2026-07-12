package moze_intel.projecte.utils;

import java.util.Optional;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.PETags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Rabbit.Variant;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.Nullable;

public class EntityRandomizerHelper {
   public static @Nullable Mob getRandomEntity(Level level, Mob toRandomize) {
      EntityType entType = toRandomize.m_6095_();
      boolean isPeaceful = entType.m_204039_(PETags.Entities.RANDOMIZER_PEACEFUL);
      boolean isHostile = entType.m_204039_(PETags.Entities.RANDOMIZER_HOSTILE);
      if (isPeaceful && isHostile && toRandomize instanceof Rabbit rabbit) {
         if (rabbit.m_28554_() == Variant.EVIL) {
            isPeaceful = false;
         }
      }

      if (isPeaceful) {
         return createRandomEntity(level, toRandomize, PETags.Entities.RANDOMIZER_PEACEFUL);
      } else if (isHostile) {
         Mob ent = createRandomEntity(level, toRandomize, PETags.Entities.RANDOMIZER_HOSTILE);
         if (ent instanceof Rabbit) {
            Rabbit rabbit = (Rabbit)ent;
            rabbit.m_28464_(Variant.EVIL);
         }

         return ent;
      } else {
         return null;
      }
   }

   private static @Nullable Mob createRandomEntity(Level level, Entity current, TagKey type) {
      ITag tag = LazyTagLookup.tagManager(ForgeRegistries.ENTITY_TYPES).getTag(type);
      EntityType currentType = current.m_6095_();
      EntityType newType = (EntityType)getRandomTagEntry(level.m_213780_(), tag, currentType);
      if (currentType == newType) {
         return null;
      } else {
         Entity newEntity = newType.m_20615_(level);
         if (newEntity instanceof Mob) {
            return (Mob)newEntity;
         } else {
            if (newEntity != null) {
               newEntity.m_146870_();
               PECore.LOGGER.warn("Invalid Entity type {} in mob randomizer tag {}. All entities in this tag are expected to be a mob.", RegistryUtils.getName(newType), type.f_203868_());
            }

            return null;
         }
      }
   }

   private static Object getRandomTagEntry(RandomSource random, ITag tag, Object toExclude) {
      int size = tag.size();
      if (size == 0 || size == 1 && tag.contains(toExclude)) {
         return toExclude;
      } else {
         Optional obj;
         do {
            obj = tag.getRandomElement(random);
         } while(obj.isPresent() && obj.get().equals(toExclude));

         return obj.orElse(toExclude);
      }
   }
}
