package moze_intel.projecte.gameObjs.items.armor;

import moze_intel.projecte.PECore;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public abstract class GemArmorBase extends PEArmor {
   public GemArmorBase(ArmorItem.Type armorType, Item.Properties props) {
      super(GemArmorBase.GemArmorMaterial.INSTANCE, armorType, props);
   }

   public float getFullSetBaseReduction() {
      return 0.9F;
   }

   public float getMaxDamageAbsorb(ArmorItem.Type type, DamageSource source) {
      if (source.m_269533_(DamageTypeTags.f_268415_)) {
         return 750.0F;
      } else if (type == Type.BOOTS && source.m_269533_(DamageTypeTags.f_268549_)) {
         return 15.0F / this.getPieceEffectiveness(type);
      } else if (type == Type.HELMET && source.m_269533_(DamageTypeTags.f_268581_)) {
         return 15.0F / this.getPieceEffectiveness(type);
      } else if (source.m_269533_(DamageTypeTags.f_268490_)) {
         return 0.0F;
      } else {
         return type != Type.HELMET && type != Type.BOOTS ? 500.0F : 400.0F;
      }
   }

   public static boolean hasAnyPiece(Player player) {
      return player.m_150109_().f_35975_.stream().anyMatch((i) -> {
         return !i.m_41619_() && i.m_41720_() instanceof GemArmorBase;
      });
   }

   public static boolean hasFullSet(Player player) {
      return player.m_150109_().f_35975_.stream().noneMatch((i) -> {
         return i.m_41619_() || !(i.m_41720_() instanceof GemArmorBase);
      });
   }

   private static class GemArmorMaterial implements ArmorMaterial {
      private static final GemArmorMaterial INSTANCE = new GemArmorMaterial();

      public int m_266425_(@NotNull ArmorItem.@NotNull Type type) {
         return 0;
      }

      public int m_7366_(@NotNull ArmorItem.@NotNull Type type) {
         byte var10000;
         switch (type) {
            case BOOTS:
               var10000 = 3;
               break;
            case LEGGINGS:
               var10000 = 6;
               break;
            case CHESTPLATE:
               var10000 = 8;
               break;
            case HELMET:
               var10000 = 3;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public int m_6646_() {
         return 0;
      }

      public @NotNull SoundEvent m_7344_() {
         return SoundEvents.f_11673_;
      }

      public @NotNull Ingredient m_6230_() {
         return Ingredient.f_43901_;
      }

      public @NotNull String m_6082_() {
         return PECore.rl("gem_armor").toString();
      }

      public float m_6651_() {
         return 2.0F;
      }

      public float m_6649_() {
         return 0.25F;
      }
   }
}
