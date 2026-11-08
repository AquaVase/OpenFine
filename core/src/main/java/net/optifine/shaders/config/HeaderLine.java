package net.optifine.shaders.config;

public abstract class HeaderLine {
    private boolean last;

    public abstract String getText();

    public abstract boolean matches(String var1);

    public abstract String removeFrom(String var1);

    public boolean isLast() {
        return this.last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HeaderLine)) {
            return false;
        }

        String objText = ((HeaderLine)obj).getText();
        return this.getText().equals(objText);
    }

    @Override
    public int hashCode() {
        return this.getText().hashCode();
    }

    @Override
    public String toString() {
        return this.getText();
    }
}
