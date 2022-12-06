package io.github.thatrobin.dpad.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ModRegistry {
    private static final HashMap<String, String> fileUrlMap = new HashMap<>();

    public static String register(String id, String data) {
        if(fileUrlMap.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate ResourcePack tried to register: '" + id + "'");
        }
        fileUrlMap.put(id, data);
        return data;
    }

    @SuppressWarnings("all")
    public static String update(String id, String data) {
        if(fileUrlMap.containsKey(id)) {
            String old = fileUrlMap.get(id);
            fileUrlMap.remove(id);
        }
        return register(id, data);
    }

    public static Map<String, String> getMap() {
        return fileUrlMap;
    }

    @SuppressWarnings("unused")
    public static int size() {
        return fileUrlMap.size();
    }

    @SuppressWarnings("unused")
    public static Stream<String> identifiers() {
        return fileUrlMap.keySet().stream();
    }

    @SuppressWarnings("unused")
    public static Iterable<Map.Entry<String, String>> entries() {
        return fileUrlMap.entrySet();
    }

    @SuppressWarnings("unused")
    public static Iterable<String> values() {
        return fileUrlMap.values();
    }

    @SuppressWarnings("unused")
    public static String get(String id) {
        if(!fileUrlMap.containsKey(id)) {
            throw new IllegalArgumentException("Could not get ResourcePack from key '" + id + "', as it was not registered!");
        }
        return fileUrlMap.get(id);
    }

    @SuppressWarnings("all")
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

