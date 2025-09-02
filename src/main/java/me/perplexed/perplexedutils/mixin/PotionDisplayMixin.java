package me.perplexed.perplexedutils.mixin;

import com.google.common.collect.Ordering;
import me.perplexed.perplexedutils.features.PotionDisplay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(Gui.class)
public class PotionDisplayMixin {

    @Inject(method = "renderEffects", at=@At("TAIL"))
    public void potionDisplay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        Collection<MobEffectInstance> collection = minecraft.player.getActiveEffects();
        if (collection.isEmpty() || minecraft.screen != null || !PotionDisplay.INSTANCE.getActive()) {
            return;
        }
        int beneficalRow = 0;
        int harmfulRow = 0;

        for (MobEffectInstance mobEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
            Holder<MobEffect> holder = mobEffectInstance.getEffect();
            if (!mobEffectInstance.showIcon() || mobEffectInstance.isAmbient()) continue;
            int potX = guiGraphics.guiWidth();
            int potY = 1;

            if (holder.value().isBeneficial()) {
                ++beneficalRow;
                potX -= 25 * beneficalRow;
            } else {
                ++harmfulRow;
                potX -= 25 * harmfulRow;
                potY += 26;
            }

            if (mobEffectInstance.getAmplifier() > 0) {
                String repre = switch (mobEffectInstance.getAmplifier()) {
                    case 1 -> "II";
                    case 2 -> "III";
                    case 3 -> "IV";
                    case 4 -> "V";
                    case 5 -> "VI";
                    default -> mobEffectInstance.getAmplifier() + "";
                };

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(12,2,0);
                guiGraphics.drawString(minecraft.font,repre,potX,potY,0xadffffff,true);
                guiGraphics.pose().popPose();
            }

            String timer = PotionDisplay.INSTANCE.getDisplayType().getTick2str().apply(mobEffectInstance.getDuration());
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(12 - (float) minecraft.font.width(timer) /2,14,0);
            int colr = mobEffectInstance.getDuration() < 200 ? 0xadff0000: 0xadffffff;
            guiGraphics.drawString(minecraft.font,timer,potX,potY, colr,true);
            guiGraphics.pose().popPose();
        }


    }
}
