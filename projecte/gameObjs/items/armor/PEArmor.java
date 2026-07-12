package moze_intel.projecte.gameObjs.items.armor;

import java.util.function.Consumer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

public abstract class PEArmor extends ArmorItem {
   protected PEArmor(ArmorMaterial material, ArmorItem.Type armorPiece, Item.Properties props) {
      super(material, armorPiece, props);
   }

   public boolean m_8120_(@NotNull ItemStack stack) {
      return false;
   }

   public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
      return false;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return false;
   }

   public int damageItem(ItemStack stack, int amount, LivingEntity entity, Consumer onBroken) {
      return 0;
   }

   public abstract float getFullSetBaseReduction();

   public abstract float getMaxDamageAbsorb(ArmorItem.Type var1, DamageSource var2);

   public float getPieceEffectiveness(ArmorItem.Type type) {
      if (type != Type.BOOTS && type != Type.HELMET) {
         return type != Type.CHESTPLATE && type != Type.LEGGINGS ? 0.0F : 0.3F;
      } else {
         return 0.2F;
      }
   }
}
