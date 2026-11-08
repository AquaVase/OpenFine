package net.optifine.render;

public class GlScissorState {
    private boolean enabled;
    private int x;
    private int y;
    private int width;
    private int height;

    public GlScissorState() {
        this(false);
    }

    public GlScissorState(boolean enabled) {
        this.enabled = enabled;
        this.clearBox();
    }

    public GlScissorState(boolean enabled, int x, int y, int width, int height) {
        this.enabled = enabled;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEnabled() {
        this.enabled = true;
    }

    public void setDisabled() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setBox(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isBoxSet() {
        return this.width >= 0 && this.height >= 0;
    }

    public void clearBox() {
        this.x = -1;
        this.y = -1;
        this.width = -1;
        this.height = -1;
    }

    public void setState(GlScissorState state) {
        if (state != null) {
            this.enabled = state.enabled;
            if (state.isBoxSet()) {
                this.setBox(state.getX(), state.getY(), state.getWidth(), state.getHeight());
            }
        }
    }

    @Override
    public String toString() {
        return "enabled: " + this.enabled + ", x: " + this.x + ", y: " + this.y + ", width: " + this.width + ", height: " + this.height;
    }
}
