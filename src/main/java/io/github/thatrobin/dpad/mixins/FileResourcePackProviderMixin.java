package io.github.thatrobin.dpad.mixins;

import io.github.thatrobin.dpad.Dpad;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(FileResourcePackProvider.class)
public class FileResourcePackProviderMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(File packsFolder, ResourcePackSource source, CallbackInfo ci) {
        Dpad.DATAPACK_PATH = packsFolder.toPath();
    }
}
