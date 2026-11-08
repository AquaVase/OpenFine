package net.optifine.shaders;

public enum ShadersTextureType {
    NORMAL("_n"),
    SPECULAR("_s");

    private String suffix;

    ShadersTextureType(String suffixIn) {
        this.suffix = suffixIn;
    }

    public String getSuffix() {
        return this.suffix;
    }
}
