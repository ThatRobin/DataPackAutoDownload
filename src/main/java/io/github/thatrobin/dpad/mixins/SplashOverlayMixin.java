package io.github.thatrobin.dpad.mixins;

import io.github.thatrobin.dpad.screen.ModDownloadSplashOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private static void init(MinecraftClient client, CallbackInfo ci) {
        ModDownloadSplashOverlay.init(client);
    }
}
