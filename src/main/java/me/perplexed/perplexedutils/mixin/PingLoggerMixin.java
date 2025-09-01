package me.perplexed.perplexedutils.mixin;

import me.perplexed.perplexedutils.features.PingDisplayKt;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PingDebugMonitor.class)
public class PingLoggerMixin {

    @Inject(method = "onPongReceived", at=@At(value = "HEAD"))
    public void logPing(ClientboundPongResponsePacket clientboundPongResponsePacket, CallbackInfo ci) {
        PingDisplayKt.setPing(Util.getMillis() - clientboundPongResponsePacket.time());
    }
}
