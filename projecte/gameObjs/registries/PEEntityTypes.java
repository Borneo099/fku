package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.gameObjs.entity.EntityFireProjectile;
import moze_intel.projecte.gameObjs.entity.EntityHomingArrow;
import moze_intel.projecte.gameObjs.entity.EntityLavaProjectile;
import moze_intel.projecte.gameObjs.entity.EntityLensProjectile;
import moze_intel.projecte.gameObjs.entity.EntityMobRandomizer;
import moze_intel.projecte.gameObjs.entity.EntityNovaCataclysmPrimed;
import moze_intel.projecte.gameObjs.entity.EntityNovaCatalystPrimed;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.entity.EntityWaterProjectile;
import moze_intel.projecte.gameObjs.registration.impl.EntityTypeDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.EntityTypeRegistryObject;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public class PEEntityTypes {
   public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister("projecte");
   public static final EntityTypeRegistryObject FIRE_PROJECTILE;
   public static final EntityTypeRegistryObject HOMING_ARROW;
   public static final EntityTypeRegistryObject LAVA_PROJECTILE;
   public static final EntityTypeRegistryObject LENS_PROJECTILE;
   public static final EntityTypeRegistryObject MOB_RANDOMIZER;
   public static final EntityTypeRegistryObject NOVA_CATALYST_PRIMED;
   public static final EntityTypeRegistryObject NOVA_CATACLYSM_PRIMED;
   public static final EntityTypeRegistryObject SWRG_PROJECTILE;
   public static final EntityTypeRegistryObject WATER_PROJECTILE;

   static {
      FIRE_PROJECTILE = ENTITY_TYPES.register("fire_projectile", Builder.m_20704_(EntityFireProjectile::new, MobCategory.MISC).setTrackingRange(256).setUpdateInterval(10));
      HOMING_ARROW = ENTITY_TYPES.register("homing_arrow", Builder.m_20704_(EntityHomingArrow::new, MobCategory.MISC).setTrackingRange(5).setUpdateInterval(20).setShouldReceiveVelocityUpdates(true));
      LAVA_PROJECTILE = ENTITY_TYPES.register("lava_projectile", Builder.m_20704_(EntityLavaProjectile::new, MobCategory.MISC).setTrackingRange(256).setUpdateInterval(10));
      LENS_PROJECTILE = ENTITY_TYPES.register("lens_projectile", Builder.m_20704_(EntityLensProjectile::new, MobCategory.MISC).setTrackingRange(256).setUpdateInterval(10));
      MOB_RANDOMIZER = ENTITY_TYPES.register("mob_randomizer", Builder.m_20704_(EntityMobRandomizer::new, MobCategory.MISC).setTrackingRange(256).setUpdateInterval(10));
      NOVA_CATALYST_PRIMED = ENTITY_TYPES.register("nova_catalyst_primed", Builder.m_20704_(EntityNovaCatalystPrimed::new, MobCategory.MISC).setTrackingRange(10).setUpdateInterval(10));
      NOVA_CATACLYSM_PRIMED = ENTITY_TYPES.register("nova_cataclysm_primed", Builder.m_20704_(EntityNovaCataclysmPrimed::new, MobCategory.MISC).setTrackingRange(10).setUpdateInterval(10));
      SWRG_PROJECTILE = ENTITY_TYPES.register("swrg_projectile", Builder.m_20704_(EntitySWRGProjectile::new, MobCategory.MISC).setTrackingRange(256).setUpdateInterval(10));
      WATER_PROJECTILE = ENTITY_TYPES.register("water_projectile", Builder.m_20704_(EntityWaterProjectile::new, MobCategory.MISC).setTrackingRange(256).setUpdateInterval(10));
   }
}
