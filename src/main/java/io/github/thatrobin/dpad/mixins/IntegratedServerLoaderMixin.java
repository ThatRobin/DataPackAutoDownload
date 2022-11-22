package io.github.thatrobin.dpad.mixins;

import io.github.thatrobin.dpad.Dpad;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin {


    @Inject(method = "createSession", at = @At(value = "RETURN"))
    private void setSessionPath(String levelName, CallbackInfoReturnable<LevelStorage.Session> cir) {
        Dpad.DATAPACK_PATH = cir.getReturnValue().getDirectory(WorldSavePath.DATAPACKS);
    }
}
