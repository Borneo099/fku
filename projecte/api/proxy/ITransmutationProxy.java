package moze_intel.projecte.api.proxy;

import java.util.ServiceLoader;
import java.util.UUID;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import org.jetbrains.annotations.NotNull;

public interface ITransmutationProxy {
   ITransmutationProxy INSTANCE = (ITransmutationProxy)ServiceLoader.load(ITransmutationProxy.class).findFirst().orElseThrow(() -> {
      return new IllegalStateException("No valid ServiceImpl for ITransmutationProxy found, ProjectE may be absent, damaged, or outdated");
   });

   @NotNull IKnowledgeProvider getKnowledgeProviderFor(@NotNull UUID var1);
}
