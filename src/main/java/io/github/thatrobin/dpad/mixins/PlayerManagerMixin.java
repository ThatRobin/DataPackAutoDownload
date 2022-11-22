package io.github.thatrobin.dpad.mixins;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import io.github.thatrobin.dpad.Dpad;
import io.github.thatrobin.dpad.utils.ResourcePackData;
import io.github.thatrobin.dpad.utils.ResourcePackRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;<init>(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)V"))
    private void handlePlayerConnection(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        for (Map.Entry<String, ResourcePackData> entry : ResourcePackRegistry.entries()) {
            String hash = "";
            try {
                byte[] file = downloadUrl(new URL(entry.getValue().getUrl()));
                hash = ByteSource.wrap(file).hash(Hashing.sha1()).toString();
            } catch (Exception e) {
                Dpad.LOGGER.error(e.getMessage());
            }
            player.sendResourcePackUrl(entry.getValue().getUrl(), hash, entry.getValue().isRequired(), Text.literal("Required by " + entry.getValue().getParentPack()));
        }
    }

    private byte[] downloadUrl(URL toDownload) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            byte[] chunk = new byte[4096];
            int bytesRead;
            InputStream stream = toDownload.openStream();

            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outputStream.toByteArray();
    }
}
