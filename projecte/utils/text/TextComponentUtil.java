package moze_intel.projecte.utils.text;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class TextComponentUtil {
   private TextComponentUtil() {
   }

   public static MutableComponent build(Object... components) {
      MutableComponent result = null;
      Style cachedStyle = Style.f_131099_;
      Object[] var3 = components;
      int var4 = components.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Object component = var3[var5];
         if (component != null) {
            MutableComponent current = null;
            if (component instanceof IHasTextComponent) {
               IHasTextComponent hasTextComponent = (IHasTextComponent)component;
               current = hasTextComponent.getTextComponent().m_6881_();
            } else if (component instanceof IHasTranslationKey) {
               IHasTranslationKey hasTranslationKey = (IHasTranslationKey)component;
               current = translate(hasTranslationKey.getTranslationKey());
            } else if (component instanceof Component) {
               Component c = (Component)component;
               current = c.m_6881_();
            } else if (component instanceof ChatFormatting) {
               cachedStyle = cachedStyle.m_131157_((ChatFormatting)component);
            } else if (component instanceof ClickEvent) {
               cachedStyle = cachedStyle.m_131142_((ClickEvent)component);
            } else if (component instanceof HoverEvent) {
               cachedStyle = cachedStyle.m_131144_((HoverEvent)component);
            } else if (component instanceof Block) {
               Block block = (Block)component;
               current = translate(block.m_7705_());
            } else if (component instanceof Item) {
               Item item = (Item)component;
               current = translate(item.m_5524_());
            } else if (component instanceof ItemStack) {
               ItemStack stack = (ItemStack)component;
               current = stack.m_41786_().m_6881_();
            } else if (component instanceof FluidStack) {
               FluidStack stack = (FluidStack)component;
               current = stack.getDisplayName().m_6881_();
            } else if (component instanceof Fluid) {
               Fluid fluid = (Fluid)component;
               current = translate(fluid.getFluidType().getDescriptionId());
            } else {
               current = getString(component.toString());
            }

            if (current != null) {
               if (!cachedStyle.m_131179_()) {
                  current.m_6270_(cachedStyle);
                  cachedStyle = Style.f_131099_;
               }

               if (result == null) {
                  result = current;
               } else {
                  result.m_7220_(current);
               }
            }
         }
      }

      return result;
   }

   public static MutableComponent getString(String component) {
      return Component.m_237113_(cleanString(component));
   }

   private static String cleanString(String component) {
      return component.replace(" ", " ");
   }

   public static MutableComponent translate(String key, Object... args) {
      return Component.m_237110_(key, args);
   }

   public static MutableComponent smartTranslate(String key, Object... components) {
      if (components.length == 0) {
         return translate(key);
      } else {
         List args = new ArrayList();
         Style cachedStyle = Style.f_131099_;
         Object[] var4 = components;
         int var5 = components.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Object component = var4[var6];
            if (component == null) {
               args.add((Object)null);
               cachedStyle = Style.f_131099_;
            } else {
               MutableComponent current = null;
               if (component instanceof IHasTextComponent) {
                  IHasTextComponent hasTextComponent = (IHasTextComponent)component;
                  current = hasTextComponent.getTextComponent().m_6881_();
               } else if (component instanceof IHasTranslationKey) {
                  IHasTranslationKey hasTranslationKey = (IHasTranslationKey)component;
                  current = translate(hasTranslationKey.getTranslationKey());
               } else if (component instanceof Block) {
                  Block block = (Block)component;
                  current = translate(block.m_7705_());
               } else if (component instanceof Item) {
                  Item item = (Item)component;
                  current = translate(item.m_5524_());
               } else if (component instanceof ItemStack) {
                  ItemStack stack = (ItemStack)component;
                  current = stack.m_41786_().m_6881_();
               } else if (component instanceof FluidStack) {
                  FluidStack stack = (FluidStack)component;
                  current = stack.getDisplayName().m_6881_();
               } else if (component instanceof Fluid) {
                  Fluid fluid = (Fluid)component;
                  current = translate(fluid.getFluidType().getDescriptionId());
               } else {
                  if (component instanceof ChatFormatting) {
                     ChatFormatting formatting = (ChatFormatting)component;
                     if (!hasStyleType(cachedStyle, formatting)) {
                        cachedStyle = cachedStyle.m_131157_(formatting);
                        continue;
                     }
                  }

                  if (component instanceof ClickEvent && cachedStyle.m_131182_() == null) {
                     cachedStyle = cachedStyle.m_131142_((ClickEvent)component);
                     continue;
                  }

                  if (component instanceof HoverEvent && cachedStyle.m_131186_() == null) {
                     cachedStyle = cachedStyle.m_131144_((HoverEvent)component);
                     continue;
                  }

                  if (!cachedStyle.m_131179_()) {
                     if (component instanceof Component) {
                        Component c = (Component)component;
                        current = c.m_6881_();
                     } else {
                        current = getString(component.toString());
                     }
                  } else if (component instanceof String) {
                     component = cleanString((String)component);
                  }
               }

               if (!cachedStyle.m_131179_()) {
                  if (current == null) {
                     args.add(component);
                  } else {
                     args.add(current.m_6270_(cachedStyle));
                  }

                  cachedStyle = Style.f_131099_;
               } else if (current == null) {
                  args.add(component);
               } else {
                  args.add(current);
               }
            }
         }

         if (!cachedStyle.m_131179_()) {
            args.add(components[components.length - 1]);
         }

         return translate(key, args.toArray());
      }
   }

   private static boolean hasStyleType(Style current, ChatFormatting formatting) {
      boolean var10000;
      switch (formatting) {
         case OBFUSCATED:
            var10000 = current.m_131176_();
            break;
         case BOLD:
            var10000 = current.m_131154_();
            break;
         case STRIKETHROUGH:
            var10000 = current.m_131168_();
            break;
         case UNDERLINE:
            var10000 = current.m_131171_();
            break;
         case ITALIC:
            var10000 = current.m_131161_();
            break;
         case RESET:
            var10000 = current.m_131179_();
            break;
         default:
            var10000 = current.m_131135_() != null;
      }

      return var10000;
   }
}
