package io.github.thatrobin.dpad.utils;

public class ResourcePackData {

    private final String url;
    private String parentPack;
    private final boolean required;

    public ResourcePackData(String url, boolean required) {
        this.url = url;
        this.required = required;
    }

    public void setParentPack(String parentPack) {
        this.parentPack = parentPack;
    }

    public String getParentPack() {
        return parentPack;
    }

    public String getUrl() {
        return url;
    }

    public boolean isRequired() {
        return required;
    }
}
