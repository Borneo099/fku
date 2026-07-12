package moze_intel.projecte.gameObjs.registries;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.utils.text.IHasTranslationKey;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PEDamageTypes {
   private static final Map INTERNAL_DAMAGE_TYPES = new HashMap();
   public static final Map DAMAGE_TYPES;
   public static final PEDamageType BYPASS_ARMOR_PLAYER_ATTACK;

   static {
      DAMAGE_TYPES = Collections.unmodifiableMap(INTERNAL_DAMAGE_TYPES);
      BYPASS_ARMOR_PLAYER_ATTACK = new PEDamageType("player_attack", "player", 0.1F);
   }

   public static record PEDamageType(ResourceKey key, String msgId, float exhaustion) implements IHasTranslationKey {
      public PEDamageType(ResourceKey key, String msgId, float exhaustion) {
         PEDamageTypes.INTERNAL_DAMAGE_TYPES.put(key.m_135782_().toString(), this);
         this.key = key;
         this.msgId = msgId;
         this.exhaustion = exhaustion;
      }

      private PEDamageType(String name, float exhaustion) {
         this(name, "projecte." + name, exhaustion);
      }

      private PEDamageType(String name, String msgId, float exhaustion) {
         this(ResourceKey.m_135785_(Registries.f_268580_, PECore.rl(name)), msgId, exhaustion);
      }

      public @NotNull String getTranslationKey() {
         return "death.attack." + this.msgId();
      }

      public DamageSource source(@NotNull LivingEntity entity) {
         return this.source(entity.m_9236_(), entity);
      }

      public DamageSource source(Level level, @Nullable LivingEntity entity) {
         return this.source(level.m_9598_(), entity);
      }

      public DamageSource source(RegistryAccess registryAccess, @Nullable LivingEntity entity) {
         Holder.Reference damageTypeReference = registryAccess.m_175515_(Registries.f_268580_).m_246971_(this.key());
         return new DamageSource(damageTypeReference, entity);
      }

      public ResourceKey key() {
         return this.key;
      }

      public String msgId() {
         return this.msgId;
      }

      public float exhaustion() {
         return this.exhaustion;
      }
   }
}
