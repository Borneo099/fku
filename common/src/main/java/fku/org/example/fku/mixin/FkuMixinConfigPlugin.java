package fku.org.example.fku.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * FKU Mixin 条件注册插件
 *
 * 当前所有 Mixin 均无条件应用；原 YSM 模块的 GeckoLib 条件注册已随 YSM 模块移除而删除。
 */
public class FkuMixinConfigPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("FKU-Mixin");

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("[FKU-Mixin] 插件已加载");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
