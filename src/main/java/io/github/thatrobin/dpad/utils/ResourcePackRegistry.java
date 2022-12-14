package io.github.thatrobin.dpad.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ResourcePackRegistry {
    private static final HashMap<String, ResourcePackData> fileUrlMap = new HashMap<>();

    public static ResourcePackData register(String id, ResourcePackData data) {
        if(fileUrlMap.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate ResourcePack tried to register: '" + id + "'");
        }
        fileUrlMap.put(id, data);
        return data;
    }

    @SuppressWarnings("all")
    public static ResourcePackData update(String id, ResourcePackData data) {
        if(fileUrlMap.containsKey(id)) {
            ResourcePackData old = fileUrlMap.get(id);
            fileUrlMap.remove(id);
        }
        return register(id, data);
    }

    @SuppressWarnings("unused")
    public static int size() {
        return fileUrlMap.size();
    }

    @SuppressWarnings("unused")
    public static Stream<String> identifiers() {
        return fileUrlMap.keySet().stream();
    }

    public static Iterable<Map.Entry<String, ResourcePackData>> entries() {
        return fileUrlMap.entrySet();
    }

    @SuppressWarnings("unused")
    public static Iterable<ResourcePackData> values() {
        return fileUrlMap.values();
    }

    @SuppressWarnings("unused")
    public static ResourcePackData get(String id) {
        if(!fileUrlMap.containsKey(id)) {
            throw new IllegalArgumentException("Could not get ResourcePack from key '" + id + "', as it was not registered!");
        }
        return fileUrlMap.get(id);
    }
    

    public static boolean contains(String id) {
        return fileUrlMap.containsKey(id);
    }

    public static void clear() {
        fileUrlMap.clear();
    }

    @SuppressWarnings("unused")
    public static void reset() {
        clear();
    }
}

