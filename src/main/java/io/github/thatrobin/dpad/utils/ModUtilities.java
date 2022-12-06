package io.github.thatrobin.dpad.utils;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.thatrobin.dpad.Dpad;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ModUtilities {

    public static List<String> getModrinthVersionId(String slug) throws IOException {
        URL Url = new URL("https://api.modrinth.com/v2/project/"+slug);
        HttpURLConnection con = (HttpURLConnection) Url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        InputStream inputStream = con.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JsonObject object = (JsonObject) JsonParser.parseReader(reader);
        reader.close();
        inputStream.close();
        con.disconnect();
        List<String> versions = Lists.newArrayList();
        JsonArray arr = object.getAsJsonArray("versions");
        for (JsonElement element : arr) {
            versions.add(element.getAsString());
        }
        return versions;
    }

    public static String getModrinthUrl(String versionID, String versionNumber) throws IOException {
        URL Url = new URL("https://api.modrinth.com/v2/version/" + versionID);

        HttpURLConnection con = (HttpURLConnection) Url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        InputStream inputStream = con.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JsonObject object = (JsonObject) JsonParser.parseReader(reader);
        reader.close();
        inputStream.close();
        con.disconnect();
        String versionNumber2 = JsonHelper.getString(object, "version_number");
        if(versionNumber.equals(versionNumber2)) {
            JsonElement element = JsonHelper.getArray(object, "files").get(0);
            return JsonHelper.getString(element.getAsJsonObject(), "url");
        }
        return "";
    }

    public static int getCurseForgeModID(String slug) throws IOException {
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

    public static String getCurseForgeUrl(int modId, int fileId) throws IOException {
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

    public static void downloadFile(String slug, String url) throws IOException {
        Path path = FabricLoader.getInstance().getGameDir().resolve("mods");
        File newPath = Paths.get(path.toString(), slug+".jar").toFile();
        if (!newPath.exists()) {
            FileUtils.copyURLToFile(new URL(url), newPath);
            Dpad.LOGGER.warn("You have installed a mod, you should restart load it!");
        }
    }

    public static byte[] downloadUrlAsBytes(URL toDownload) {
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
