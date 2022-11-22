package io.github.thatrobin.dpad.mixins;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow public abstract ResourcePackManager getDataPackManager();

    @Shadow public abstract CompletableFuture<Void> reloadResources(Collection<String> dataPacks);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V", ordinal = 0), method = "runServer")
    private void afterSetupServer(CallbackInfo info) {
        this.getDataPackManager().scanPacks();
        this.reloadResources(this.getDataPackManager().getNames());
    }
}
