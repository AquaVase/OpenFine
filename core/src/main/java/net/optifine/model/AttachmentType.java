package net.optifine.model;

import net.optifine.Config;

public enum AttachmentType {
    LEFT_HANDHELD_ITEM("left_handheld_item"),
    RIGHT_HANDHELD_ITEM("right_handheld_item"),
    HANDHELD_ITEM("handheld_item"),
    HEAD("head"),
    LEAD("lead");

    private String name;

    AttachmentType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name + "";
    }

    public static AttachmentType parse(String str) {
        if (str == null) {
            return null;
        }

        for (AttachmentType type : values()) {
            if (Config.equals(type.getName(), str)) {
                return type;
            }
        }

        return null;
    }
}
