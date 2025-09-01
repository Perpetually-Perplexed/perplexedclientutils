package me.perplexed.perplexedutils.mixin;


import me.perplexed.perplexedutils.features.PeekChat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Inject(method = "onScroll", at=@At( value = "HEAD"), cancellable = true)
    public void scroll(long l, double d, double e, CallbackInfo ci) {
        KeyMapping key = PeekChat.INSTANCE.getPeekChatKey();
        Minecraft mc = Minecraft.getInstance();
        if (key == null || !key.isDown()) return;
        double h = (mc.options.discreteMouseScroll().get() ? Math.signum(e) : e)
                * mc.options.mouseWheelSensitivity().get();
        mc.gui.getChat().scrollChat((int) h * 7);
        ci.cancel();
    }
}
