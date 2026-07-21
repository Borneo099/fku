package fku.org.example.fku.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"lbxrman.mymod.opmod.features.anticheat.CheatModDetector"}, remap=false)
public class MixinCheatModDetector {
    @Inject(method={"runCheck"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private static void onRunCheck(CallbackInfo ci) {
        ci.cancel();
    }
}

