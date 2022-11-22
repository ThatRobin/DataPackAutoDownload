package io.github.thatrobin.dpad;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import io.github.thatrobin.dpad.utils.PackResourceDependencyReader;
import io.github.thatrobin.dpad.utils.ResourcePackData;
import io.github.thatrobin.dpad.utils.ResourcePackRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.DatapackCommand;
import net.minecraft.server.command.ReloadCommand;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

public class Dpad implements ModInitializer {

    public static String MODID = "datapack_auto_download";
    public static final Logger LOGGER = LogManager.getLogger(Dpad.class);
    public static final PackResourceDependencyReader READER = new PackResourceDependencyReader();
    public static Path DATAPACK_PATH;


    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            server.getDataPackManager().scanPacks();
            server.reloadResources(server.getDataPackManager().getNames());
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (Map.Entry<String, ResourcePackData> entry : ResourcePackRegistry.entries()) {
                String hash = "";
                try {
                    byte[] file = downloadUrl(new URL(entry.getValue().getUrl()));
                    hash = ByteSource.wrap(file).hash(Hashing.sha1()).toString();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
                handler.player.sendResourcePackUrl(entry.getValue().getUrl(), hash, entry.getValue().isRequired(), Text.literal("Required by " + entry.getValue().getParentPack()));
            }
        });
    }

    public static Identifier identifier(String path) {
        return new Identifier(MODID, path);
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
