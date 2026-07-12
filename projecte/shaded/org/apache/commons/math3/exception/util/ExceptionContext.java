package moze_intel.projecte.shaded.org.apache.commons.math3.exception.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ExceptionContext implements Serializable {
   private static final long serialVersionUID = -6024911025449780478L;
   private Throwable throwable;
   private List msgPatterns;
   private List msgArguments;
   private Map context;

   public ExceptionContext(Throwable throwable) {
      this.throwable = throwable;
      this.msgPatterns = new ArrayList();
      this.msgArguments = new ArrayList();
      this.context = new HashMap();
   }

   public Throwable getThrowable() {
      return this.throwable;
   }

   public void addMessage(Localizable pattern, Object... arguments) {
      this.msgPatterns.add(pattern);
      this.msgArguments.add(ArgUtils.flatten(arguments));
   }

   public void setValue(String key, Object value) {
      this.context.put(key, value);
   }

   public Object getValue(String key) {
      return this.context.get(key);
   }

   public Set getKeys() {
      return this.context.keySet();
   }

   public String getMessage() {
      return this.getMessage(Locale.US);
   }

   public String getLocalizedMessage() {
      return this.getMessage(Locale.getDefault());
   }

   public String getMessage(Locale locale) {
      return this.buildMessage(locale, ": ");
   }

   public String getMessage(Locale locale, String separator) {
      return this.buildMessage(locale, separator);
   }

   private String buildMessage(Locale locale, String separator) {
      StringBuilder sb = new StringBuilder();
      int count = 0;
      int len = this.msgPatterns.size();

      for(int i = 0; i < len; ++i) {
         Localizable pat = (Localizable)this.msgPatterns.get(i);
         Object[] args = (Object[])this.msgArguments.get(i);
         MessageFormat fmt = new MessageFormat(pat.getLocalizedString(locale), locale);
         sb.append(fmt.format(args));
         ++count;
         if (count < len) {
            sb.append(separator);
         }
      }

      return sb.toString();
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeObject(this.throwable);
      this.serializeMessages(out);
      this.serializeContext(out);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      this.throwable = (Throwable)in.readObject();
      this.deSerializeMessages(in);
      this.deSerializeContext(in);
   }

   private void serializeMessages(ObjectOutputStream out) throws IOException {
      int len = this.msgPatterns.size();
      out.writeInt(len);

      for(int i = 0; i < len; ++i) {
         Localizable pat = (Localizable)this.msgPatterns.get(i);
         out.writeObject(pat);
         Object[] args = (Object[])this.msgArguments.get(i);
         int aLen = args.length;
         out.writeInt(aLen);

         for(int j = 0; j < aLen; ++j) {
            if (args[j] instanceof Serializable) {
               out.writeObject(args[j]);
            } else {
               out.writeObject(this.nonSerializableReplacement(args[j]));
            }
         }
      }

   }

   private void deSerializeMessages(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int len = in.readInt();
      this.msgPatterns = new ArrayList(len);
      this.msgArguments = new ArrayList(len);

      for(int i = 0; i < len; ++i) {
         Localizable pat = (Localizable)in.readObject();
         this.msgPatterns.add(pat);
         int aLen = in.readInt();
         Object[] args = new Object[aLen];

         for(int j = 0; j < aLen; ++j) {
            args[j] = in.readObject();
         }

         this.msgArguments.add(args);
      }

   }

   private void serializeContext(ObjectOutputStream out) throws IOException {
      int len = this.context.size();
      out.writeInt(len);
      Iterator i$ = this.context.entrySet().iterator();

      while(i$.hasNext()) {
         Map.Entry entry = (Map.Entry)i$.next();
         out.writeObject(entry.getKey());
         Object value = entry.getValue();
         if (value instanceof Serializable) {
            out.writeObject(value);
         } else {
            out.writeObject(this.nonSerializableReplacement(value));
         }
      }

   }

   private void deSerializeContext(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int len = in.readInt();
      this.context = new HashMap();

      for(int i = 0; i < len; ++i) {
         String key = (String)in.readObject();
         Object value = in.readObject();
         this.context.put(key, value);
      }

   }

   private String nonSerializableReplacement(Object obj) {
      return "[Object could not be serialized: " + obj.getClass().getName() + "]";
   }
}
