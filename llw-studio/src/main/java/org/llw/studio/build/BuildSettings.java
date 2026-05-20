package org.llw.studio.build;

/**
 * Project build and player export settings.
 */
public final class BuildSettings {
    private String productName = "MyGame";
    private String version = "1.0.0";
    private String outputDirectory = "";
    private String windowTitle = "MyGame";
    private int windowWidth = 1280;
    private int windowHeight = 720;
    private boolean vsync = true;
    private String iconAssetGuid = "";

    public String productName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName == null ? "" : productName;
    }

    public String version() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version == null ? "" : version;
    }

    public String outputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory == null ? "" : outputDirectory;
    }

    public String windowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle == null ? "" : windowTitle;
    }

    public int windowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = Math.max(320, windowWidth);
    }

    public int windowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = Math.max(240, windowHeight);
    }

    public boolean vsync() {
        return vsync;
    }

    public void setVsync(boolean vsync) {
        this.vsync = vsync;
    }

    public String iconAssetGuid() {
        return iconAssetGuid;
    }

    public void setIconAssetGuid(String iconAssetGuid) {
        this.iconAssetGuid = iconAssetGuid == null ? "" : iconAssetGuid;
    }

    public BuildSettings copy() {
        BuildSettings copy = new BuildSettings();
        copy.productName = productName;
        copy.version = version;
        copy.outputDirectory = outputDirectory;
        copy.windowTitle = windowTitle;
        copy.windowWidth = windowWidth;
        copy.windowHeight = windowHeight;
        copy.vsync = vsync;
        copy.iconAssetGuid = iconAssetGuid;
        return copy;
    }
}
