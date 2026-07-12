package lexis.Hack.Hacks.Chat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Server.AICHAT.AIUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatAIHack extends Hack {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String CONFIG_KEY = "聊天AI";
   private static final Pattern PLAYER_MSG_PATTERN = Pattern.compile("^<([^>]+)>\\s*(.*)");
   private int memorySize = 6;
   private int maxResponseLength = 256;
   private boolean showProcessing = true;
   private final ExecutorService executor = Executors.newSingleThreadExecutor();
   private final Map playerHistory = new ConcurrentHashMap();
   private final Map playerProcessing = new ConcurrentHashMap();

   public ChatAIHack() {
      super("聊天AI", new String[]{"自动使用AI回复其他玩家的消息", "§c§l注意：这模型是 Gemini-3 反应很慢回复很慢"}, Hack.Category.CHAT, true);
      this.addSetting(new Hack.Setting("记忆轮数", "记住的对话轮数", 6.0, 1.0, 20.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("最大回复长度", "AI回复最大字符数", 256.0, 64.0, 512.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("显示思考中", "回复前发送提示", true));
      this.loadConfig();
   }

   private void loadConfig() {
      HackConfig config = HackConfig.getInstance();
      this.memorySize = config.getIntSetting("聊天AI", "记忆轮数", 6);
      this.maxResponseLength = config.getIntSetting("聊天AI", "最大回复长度", 256);
      this.showProcessing = config.getBooleanSetting("聊天AI", "显示思考中", true);
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "记忆轮数":
               s.setValue((double)this.memorySize);
               break;
            case "最大回复长度":
               s.setValue((double)this.maxResponseLength);
               break;
            case "显示思考中":
               s.setValue(this.showProcessing);
         }
      }

   }

   private void saveConfig() {
      HackConfig.getInstance().saveHackSettings("聊天AI", this.getSettings());
   }

   public void onEnable() {
      MinecraftForge.EVENT_BUS.register(this);
      this.playerHistory.clear();
      this.playerProcessing.clear();
   }

   public void onDisable() {
      MinecraftForge.EVENT_BUS.unregister(this);
      this.playerHistory.clear();
      this.playerProcessing.clear();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "记忆轮数":
               int newMem = (int)s.getDouble();
               if (newMem != this.memorySize) {
                  this.memorySize = newMem;
                  needSave = true;
               }
               break;
            case "最大回复长度":
               int newLen = (int)s.getDouble();
               if (newLen != this.maxResponseLength) {
                  this.maxResponseLength = newLen;
                  needSave = true;
               }
               break;
            case "显示思考中":
               boolean newShow = s.getBoolean();
               if (newShow != this.showProcessing) {
                  this.showProcessing = newShow;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

   }

   @SubscribeEvent
   public void onChatReceived(ClientChatReceivedEvent event) {
      if (mc.f_91074_ != null && this.isEnabled()) {
         String raw = ChatFormatting.m_126649_(event.getMessage().getString());
         if (raw != null && !raw.isEmpty()) {
            Matcher matcher = PLAYER_MSG_PATTERN.matcher(raw);
            if (matcher.matches()) {
               String sender = matcher.group(1);
               String content = matcher.group(2).trim();
               if (!content.isEmpty()) {
                  if (!sender.equals(mc.f_91074_.m_36316_().getName())) {
                     if ((Boolean)this.playerProcessing.getOrDefault(sender, false)) {
                        if (this.showProcessing) {
                           mc.f_91074_.m_213846_(Component.m_237113_("§d[§6Lexis§d] §f正在处理 " + sender + " 的上一条消息，等一会"));
                        }

                     } else {
                        this.playerProcessing.put(sender, true);
                        if (this.showProcessing) {
                           mc.f_91074_.m_213846_(Component.m_237113_("§d[§6Lexis§d] §f正在思考 " + sender + " 的问题..."));
                        }

                        this.executor.submit(() -> {
                           try {
                              List history = (List)this.playerHistory.computeIfAbsent(sender, (k) -> {
                                 return new ArrayList();
                              });
                              history.add(sender + ": " + content);

                              while(history.size() > this.memorySize * 2) {
                                 history.remove(0);
                              }

                              String fullMessage = sender + ": " + content;
                              String reply = AIUtils.chatWithAIWithHistory(fullMessage, history);
                              reply = ChatFormatting.m_126649_(reply);
                              if (reply == null || reply.isEmpty()) {
                                 reply = "抱歉，我暂时无法回答。";
                              }

                              reply = reply.replaceAll("\\s+", " ").trim();
                              history.add("助手: " + reply);

                              while(history.size() > this.memorySize * 2) {
                                 history.remove(0);
                              }

                              mc.execute(() -> {
                                 if (mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
                                    if (reply.length() <= this.maxResponseLength) {
                                       mc.f_91074_.f_108617_.m_246175_(reply);
                                    } else {
                                       int start = 0;

                                       while(start < reply.length()) {
                                          int end = Math.min(start + this.maxResponseLength, reply.length());
                                          if (end < reply.length()) {
                                             int lastSpace = reply.lastIndexOf(32, end);
                                             if (lastSpace > start) {
                                                end = lastSpace;
                                             }
                                          }

                                          String part = reply.substring(start, end).trim();
                                          if (!part.isEmpty()) {
                                             mc.f_91074_.f_108617_.m_246175_(part);
                                          }

                                          start = end;

                                          try {
                                             Thread.sleep(50L);
                                          } catch (InterruptedException var6) {
                                          }
                                       }
                                    }

                                 }
                              });
                           } catch (Exception var10) {
                              var10.printStackTrace();
                              mc.execute(() -> {
                                 if (mc.f_91074_ != null) {
                                    mc.f_91074_.m_213846_(Component.m_237113_("§d[§6Lexis§d] §fAI服务错误: " + var10.getMessage()));
                                 }

                              });
                           } finally {
                              this.playerProcessing.remove(sender);
                           }

                        });
                     }
                  }
               }
            }
         }
      }
   }

   public void onClick() {
      this.toggle();
   }
}
