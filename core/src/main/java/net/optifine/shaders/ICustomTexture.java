package net.optifine.shaders;

public interface ICustomTexture {
    int getTextureId();

    int getTextureUnit();

    void deleteTexture();

    default void bindVulkan() {
        throw new UnsupportedOperationException("Native Vulkan texture binding is not implemented for " + this.getClass().getName());
    }

    default int getTarget() {
        return 3553;
    }
}
