package io.github.thatrobin.dpad.mixins;

import io.github.thatrobin.dpad.Dpad;
import io.github.thatrobin.dpad.utils.ModRegistry;
import io.github.thatrobin.dpad.utils.ModUtilities;
import io.github.thatrobin.dpad.utils.ResourcePackData;
import io.github.thatrobin.dpad.utils.ResourcePackRegistry;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
                                byte[] bytes = ModUtilities.downloadUrlAsBytes(new URL(url));
                                if (bytes != null) {
                                    if("PK".equals(new String(bytes, 0,2))) {
                                        File newPath = Paths.get(path.toString(), fragmentUrl[fragmentUrl.length - 1]).toFile();
                                        if (!newPath.exists()) {
                                            FileUtils.copyURLToFile(new URL(url), newPath);
                                        }
                                    } else {
                                        Dpad.LOGGER.warn("Trying to download a non-zip file. Something is off.");
                                    }
                                }
                            }
                            if (pair.getLeft().equals("mods")) {
                                if(url.matches("https://www\\.curseforge\\.com/minecraft/mc-mods/.+/files/.+")) {
                                    String slug = url.substring("https://www.curseforge.com/minecraft/mc-mods/".length(), url.length()-1).split("/")[0];
                                    String[] temp = url.split("/");
                                    int fileId = Integer.parseInt(temp[temp.length-1]);
                                    int modId = ModUtilities.getCurseForgeModID(slug);
                                    url = ModUtilities.getCurseForgeUrl(modId, fileId);
                                    if(!ModRegistry.contains(slug)) {
                                        ModRegistry.register(slug, url);
                                    } else {
                                        ModRegistry.update(slug, url);
                                    }
                                    ModUtilities.downloadFile(slug, url);
                                }
                                if (url.matches("https://modrinth\\.com/mod/.+/version/.+")) {
                                    String slug = url.substring("https://modrinth.com/mod/".length(), url.length()-1).split("/")[0];
                                    String[] fragmentUrl = url.split("/");
                                    String versionNumber = fragmentUrl[fragmentUrl.length - 1];
                                    String newUrl = "";
                                    while(newUrl.equals("")) {
                                        List<String> versionIDs = ModUtilities.getModrinthVersionId(slug);
                                        for (String versionID : versionIDs) {
                                            newUrl = ModUtilities.getModrinthUrl(versionID, versionNumber);
                                        }
                                    }
                                    if(!ModRegistry.contains(slug)) {
                                        ModRegistry.register(slug, newUrl);
                                    } else {
                                        ModRegistry.update(slug, newUrl);
                                    }
                                    ModUtilities.downloadFile(slug, url);
                                }
                            }
                        } else if (rawData instanceof ResourcePackData data) {
                            if (pair.getLeft().equals("resourcepacks")) {
                                String[] fragmentUrl = data.getUrl().split("/");
                                String newPath = fragmentUrl[fragmentUrl.length - 1];
                                data.setParentPack(name);
                                if (!ResourcePackRegistry.contains(newPath)) {
                                    ResourcePackRegistry.register(newPath, data);
                                } else {
                                    ResourcePackRegistry.update(newPath, data);
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


}
