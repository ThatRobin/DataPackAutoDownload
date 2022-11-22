package io.github.thatrobin.dpad.utils;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;

import java.util.List;

public class PackResourceDependencyReader implements ResourceMetadataReader<List<Pair<String, Object>>> {
    @Override
    public List<Pair<String, Object>> fromJson(JsonObject jsonObject) {
        List<Pair<String, Object>> urls = Lists.newArrayList();
        if(JsonHelper.hasArray(jsonObject, "datapacks")) {
            JsonArray datapackArray = JsonHelper.getArray(jsonObject, "datapacks");
            for (JsonElement element : datapackArray) {
                String pack = element.getAsString();
                urls.add(new Pair<>("datapacks", pack));
            }
        }
        if(JsonHelper.hasArray(jsonObject, "mods")) {
            JsonArray datapackArray = JsonHelper.getArray(jsonObject, "mods");
            for (JsonElement element : datapackArray) {
                String pack = element.getAsString();
                urls.add(new Pair<>("mods", pack));
            }
        }
        if(JsonHelper.hasArray(jsonObject, "resourcepacks")) {
            JsonArray datapackArray = JsonHelper.getArray(jsonObject, "resourcepacks");
            for (JsonElement element : datapackArray) {
                if(element.isJsonObject()) {
                    JsonObject jo = element.getAsJsonObject();
                    String url = JsonHelper.getString(jo, "url");
                    boolean required = JsonHelper.getBoolean(jo, "required", false);
                    ResourcePackData data = new ResourcePackData(url, required);
                    urls.add(new Pair<>("resourcepacks", data));
                } else if(element.isJsonPrimitive()) {
                    String url = element.getAsString();
                    ResourcePackData data = new ResourcePackData(url, false);
                    urls.add(new Pair<>("resourcepacks", data));
                }
            }
        }
        return urls;
    }

    @Override
    public String getKey() {
        return "dependencies";
    }

}
