package moze_intel.projecte.gameObjs.items.armor;

import moze_intel.projecte.PECore;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class DMArmor extends PEArmor {
   public DMArmor(ArmorItem.Type armorPiece, Item.Properties props) {
      super(DMArmor.DMArmorMaterial.INSTANCE, armorPiece, props);
   }

   public float getFullSetBaseReduction() {
      return 0.8F;
   }

   public float getMaxDamageAbsorb(ArmorItem.Type type, DamageSource source) {
      if (source.m_269533_(DamageTypeTags.f_268415_)) {
         return 350.0F;
      } else if (type == Type.BOOTS && source.m_269533_(DamageTypeTags.f_268549_)) {
         return 5.0F / this.getPieceEffectiveness(type);
      } else if (type == Type.HELMET && source.m_269533_(DamageTypeTags.f_268581_)) {
         return 5.0F / this.getPieceEffectiveness(type);
      } else if (source.m_269533_(DamageTypeTags.f_268490_)) {
         return 0.0F;
      } else {
         return type != Type.HELMET && type != Type.BOOTS ? 150.0F : 100.0F;
      }
   }

   private static class DMArmorMaterial implements ArmorMaterial {
      private static final DMArmorMaterial INSTANCE = new DMArmorMaterial();

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
         return PECore.rl("dark_matter").toString();
      }

      public float m_6651_() {
         return 2.0F;
      }

      public float m_6649_() {
         return 0.1F;
      }
   }
}
