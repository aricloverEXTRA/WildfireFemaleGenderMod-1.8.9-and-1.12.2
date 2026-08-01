package com.wildfire.main.uvs;

public enum UVDirection {
    EAST("east", "E", 0xFFFF0000),
    WEST("west", "W", 0xFF00FF00),
    DOWN("down", "D", 0xFF0000FF),
    UP("up", "U", 0xFF00FFFF),
    NORTH("north", "N", 0xFFFF00FF),
    SOUTH("south", "S", 0xFFFFFF00);

    private final String saveName;
    private final String shortName;
    private final int baseColor;

    UVDirection(String saveName, String shortName, int baseColor) {
        this.saveName = saveName;
        this.shortName = shortName;
        this.baseColor = baseColor;
    }

    public String getSaveName() {
        return saveName;
    }

    public String getShortName() {
        return shortName;
    }

    public int getFaceColor(boolean faded) {
        if (!faded) {
            return baseColor;
        }
        int alpha = 0x33;
        int rgb = baseColor & 0x00FFFFFF;
        return (alpha << 24) | rgb;
    }

    public String getDirectionText(BreastTypes type) {
        if (this == EAST || this == WEST) {
            return (type == BreastTypes.LEFT || type == BreastTypes.LEFT_OVERLAY)
                    ? "Inner Face" : "Outer Face";
        }
        switch (this) {
            case DOWN:
                return "Bottom Face";
            case UP:
                return "Top Face";
            case NORTH:
                return "Front Face";
            case SOUTH:
                return "Back Face";
            default:
                return saveName;
        }
    }

    public static UVDirection[] valuesCached() {
        return values();
    }
}