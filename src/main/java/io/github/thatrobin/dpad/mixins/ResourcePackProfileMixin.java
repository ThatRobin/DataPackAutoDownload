package io.github.thatrobin.dpad.mixins;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.thatrobin.dpad.Dpad;
import io.github.thatrobin.dpad.utils.ResourcePackData;
import io.github.thatrobin.dpad.utils.ResourcePackRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;

@Mixin(ResourcePackProfile.class)
public class ResourcePackProfileMixin {

    @Inject(method = "<init>(Ljava/lang/String;ZLjava/util/function/Supplier;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/resource/ResourcePackCompatibility;Lnet/minecraft/resource/ResourcePackProfile$InsertionPosition;ZLnet/minecraft/resource/ResourcePackSource;)V", at = @At("TAIL"))
    private void registerDependencies(String name, boolean alwaysEnabled, Supplier<ResourcePack> packFactory, Text displayName, Text description, ResourcePackCompatibility compatibility, ResourcePackProfile.InsertionPosition direction, boolean pinned, ResourcePackSource source, CallbackInfo ci) {
        if(Dpad.DATAPACK_PATH != null) {
            try (ResourcePack resourcePack = packFactory.get()) {
                List<Pair<String, Object>> urls = resourcePack.parseMetadata(Dpad.READER);
                if (urls != null) {
                    for (Pair<String, Object> pair : urls) {
                        Object rawData = pair.getRight();
                        if(rawData instanceof String url) {
                            if (pair.getLeft().equals("datapacks")) {
                                Path path = Dpad.DATAPACK_PATH;
                                String[] fragmentUrl = url.split("/");
                                File newPath = Paths.get(path.toString(), fragmentUrl[fragmentUrl.length - 1]).toFile();
                                if (!newPath.exists()) {
                                    FileUtils.copyURLToFile(new URL(url), newPath);
                                }

                            }
                            if (pair.getLeft().equals("mods")) {
                                Path path = FabricLoader.getInstance().getGameDir().resolve("mods");
                                if(url.matches("https:\\/\\/www\\.curseforge\\.com\\/minecraft\\/mc-mods\\/.+\\/files\\/.+")) {
                                    String slug = url.substring("https://www.curseforge.com/minecraft/mc-mods/".length(), url.length()-1).split("/")[0];
                                    String[] temp = url.split("/");

                                    int fileId = Integer.parseInt(temp[temp.length-1]);

                                    int modId = getModID(slug);

                                    url = getUrl(modId, fileId);

                                    File newPath = Paths.get(path.toString(), slug+".jar").toFile();
                                    if (!newPath.exists()) {
                                        FileUtils.copyURLToFile(new URL(url), newPath);
                                    }
                                }
                                if (url.matches("https:\\/\\/cdn\\.modrinth\\.com\\/data\\/.+\\/versions\\/.+\\/.+\\.jar")) {
                                    String[] fragmentUrl = url.split("/");
                                    File newPath = Paths.get(path.toString(), fragmentUrl[fragmentUrl.length - 1]).toFile();
                                    if (!newPath.exists()) {
                                        FileUtils.copyURLToFile(new URL(url), newPath);
                                    }
                                }
                            }
                        } else if (rawData instanceof ResourcePackData data) {
                            if (pair.getLeft().equals("resourcepacks")) {
                                String[] fragmentUrl = data.getUrl().split("/");
                                String newPath = fragmentUrl[fragmentUrl.length - 1];
                                data.setParentPack(name);
                                if (!ResourcePackRegistry.contains(newPath)) {
                                    ResourcePackRegistry.register(newPath, data);
                                }
                            }
                        }
                    }
                }
            } catch (IOException iOException) {
                Dpad.LOGGER.warn("Couldn't get pack info for: {}", iOException.toString());
            }
        }
    }

    private int getModID(String slug) throws IOException {
        URL Url = new URL("https://api.curseforge.com/v1/mods/search?gameId=432&slug="+slug);
        HttpURLConnection con = (HttpURLConnection) Url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("x-api-key", "$2a$10$WbaLifzzwjuwqa64uZ/zKOtkfyqJobYi5OLiAraFSFziM7rLf03Xm");
        InputStream inputStream = con.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JsonObject object = (JsonObject) JsonParser.parseReader(reader);
        reader.close();
        inputStream.close();
        con.disconnect();

        return object.getAsJsonArray("data").get(0).getAsJsonObject().get("id").getAsInt();
    }

    private String getUrl(int modId, int fileId) throws IOException {
        URL Url = new URL("https://api.curseforge.com/v1/mods/" + modId + "/files/" + fileId + "/download-url");

        HttpURLConnection con = (HttpURLConnection) Url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("x-api-key", "$2a$10$WbaLifzzwjuwqa64uZ/zKOtkfyqJobYi5OLiAraFSFziM7rLf03Xm");
        InputStream inputStream = con.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JsonObject object = (JsonObject) JsonParser.parseReader(reader);
        reader.close();
        inputStream.close();
        con.disconnect();

        return JsonHelper.getString(object, "data");
    }
}
