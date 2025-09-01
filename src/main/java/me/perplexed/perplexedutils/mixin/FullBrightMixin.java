package me.perplexed.perplexedutils.mixin;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(OptionInstance.UnitDouble.class)
public class FullBrightMixin {

    @Inject(method = "validateValue(Ljava/lang/Double;)Ljava/util/Optional;", at=@At("RETURN"), cancellable = true)
    public void validate(Double value, CallbackInfoReturnable<Optional<Double>> cir) {
        cir.setReturnValue(value >= 0.0 && value <= 16.0 ? Optional.of(value) : Optional.empty());
    }
}
