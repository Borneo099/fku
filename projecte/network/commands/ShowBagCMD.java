package moze_intel.projecte.network.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import moze_intel.projecte.PEPermissions;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.AlchBagContainer;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.impl.capability.AlchBagImpl;
import moze_intel.projecte.network.commands.argument.ColorArgument;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class ShowBagCMD {
   private static final SimpleCommandExceptionType NOT_FOUND;

   public static LiteralArgumentBuilder register(CommandBuildContext context) {
      return (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("showbag").requires(PEPermissions.COMMAND_SHOW_BAG)).then(((RequiredArgumentBuilder)Commands.m_82129_("color", ColorArgument.color()).then(Commands.m_82129_("target", EntityArgument.m_91466_()).executes((ctx) -> {
         return showBag(ctx, ColorArgument.getColor(ctx, "color"), EntityArgument.m_91474_(ctx, "target"));
      }))).then(Commands.m_82129_("uuid", UuidArgument.m_113850_()).executes((ctx) -> {
         return showBag(ctx, ColorArgument.getColor(ctx, "color"), UuidArgument.m_113853_(ctx, "uuid"));
      })));
   }

   private static int showBag(CommandContext ctx, DyeColor color, ServerPlayer player) throws CommandSyntaxException {
      ServerPlayer senderPlayer = ((CommandSourceStack)ctx.getSource()).m_81375_();
      return showBag(senderPlayer, createContainer(senderPlayer, player, color));
   }

   private static int showBag(CommandContext ctx, DyeColor color, UUID uuid) throws CommandSyntaxException {
      ServerPlayer senderPlayer = ((CommandSourceStack)ctx.getSource()).m_81375_();
      return showBag(senderPlayer, createContainer(senderPlayer, uuid, color));
   }

   private static int showBag(ServerPlayer senderPlayer, MenuProvider container) {
      NetworkHooks.openScreen(senderPlayer, container, (b) -> {
         b.writeBoolean(false);
         b.writeBoolean(false);
      });
      return 1;
   }

   private static MenuProvider createContainer(ServerPlayer sender, ServerPlayer target, DyeColor color) {
      IItemHandlerModifiable inv = (IItemHandlerModifiable)((IAlchBagProvider)target.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).orElseThrow(NullPointerException::new)).getBag(color);
      Component name = PELang.SHOWBAG_NAMED.translate(new Object[]{PEItems.getBag(color), target.m_5446_()});
      return getContainer(sender, name, inv, false, () -> {
         return target.m_6084_() && !target.m_9232_();
      });
   }

   private static MenuProvider createContainer(ServerPlayer sender, UUID target, DyeColor color) throws CommandSyntaxException {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      IItemHandlerModifiable inv = loadOfflineBag(server, target, color);
      Optional profileByUUID = server.m_129927_().m_11002_(target);
      MutableComponent name;
      if (profileByUUID.isPresent()) {
         name = PELang.SHOWBAG_NAMED.translate(new Object[]{PEItems.getBag(color), ((GameProfile)profileByUUID.get()).getName()});
      } else {
         name = TextComponentUtil.build(PEItems.getBag(color));
      }

      return getContainer(sender, name, inv, true, () -> {
         return true;
      });
   }

   private static MenuProvider getContainer(final ServerPlayer sender, final Component name, final IItemHandlerModifiable inv, final boolean immutable, final BooleanSupplier canInteractWith) {
      return new MenuProvider() {
         public @NotNull Component m_5446_() {
            return name;
         }

         public AbstractContainerMenu m_7208_(int windowId, @NotNull Inventory playerInv, @NotNull Player player) {
            return new AlchBagContainer(windowId, sender.m_150109_(), InteractionHand.OFF_HAND, inv, 0, immutable) {
               public boolean m_6875_(@NotNull Player player) {
                  return canInteractWith.getAsBoolean();
               }
            };
         }
      };
   }

   private static IItemHandlerModifiable loadOfflineBag(MinecraftServer server, UUID playerUUID, DyeColor color) throws CommandSyntaxException {
      File playerData = server.m_129843_(LevelResource.f_78176_).toFile();
      if (playerData.exists()) {
         File player = new File(playerData, playerUUID.toString() + ".dat");
         if (player.exists() && player.isFile()) {
            try {
               FileInputStream in = new FileInputStream(player);

               IItemHandlerModifiable var9;
               try {
                  CompoundTag playerDat = NbtIo.m_128939_(in);
                  CompoundTag bagProvider = playerDat.m_128469_("ForgeCaps").m_128469_(AlchBagImpl.Provider.NAME.toString());
                  IAlchBagProvider provider = AlchBagImpl.getDefault();
                  provider.deserializeNBT(bagProvider);
                  var9 = (IItemHandlerModifiable)provider.getBag(color);
               } catch (Throwable var11) {
                  try {
                     in.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }

                  throw var11;
               }

               in.close();
               return var9;
            } catch (IOException var12) {
            }
         }
      }

      throw NOT_FOUND.create();
   }

   static {
      NOT_FOUND = new SimpleCommandExceptionType(PELang.SHOWBAG_NOT_FOUND.translate(new Object[0]));
   }
}
