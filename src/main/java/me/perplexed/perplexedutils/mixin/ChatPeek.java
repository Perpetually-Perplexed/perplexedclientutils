package me.perplexed.perplexedutils.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.perplexed.perplexedutils.features.PeekChat;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public abstract class ChatPeek {


    @ModifyVariable(method = "render", at = @At(value = "HEAD"), index = 5, argsOnly = true)
    public boolean onGetChatHud(boolean old) {
        var key = PeekChat.INSTANCE.getPeekChatKey();
        return old || (key != null && key.isDown());
    }

    @ModifyExpressionValue(method = "getHeight()I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;isChatFocused()Z"))
    public boolean onGetChatHudHeight(boolean old) {
        var key = PeekChat.INSTANCE.getPeekChatKey();
        return old || (key != null && key.isDown());
    }

}