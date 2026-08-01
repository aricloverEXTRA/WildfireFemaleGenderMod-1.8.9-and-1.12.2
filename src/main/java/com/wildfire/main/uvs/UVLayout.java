package com.wildfire.main.uvs;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class UVLayout {
    private final EnumMap<UVDirection, UVQuad> quads;

    public UVLayout() {
        this.quads = new EnumMap<UVDirection, UVQuad>(UVDirection.class);
        for (UVDirection direction : UVDirection.values()) {
            this.quads.put(direction, null);
        }
    }

    public UVLayout(BreastTypes type) {
        this();
        UVLayout defaults;
        switch (type) {
            case LEFT:
                defaults = defaultsForLeft();
                break;
            case RIGHT:
                defaults = defaultsForRight();
                break;
            case LEFT_OVERLAY:
                defaults = leftOverlayDefaults();
                break;
            case RIGHT_OVERLAY:
                defaults = rightOverlayDefaults();
                break;
            default:
                defaults = defaultsForLeft();
                break;
        }
        for (Map.Entry<UVDirection, UVQuad> entry : defaults.getAllSides().entrySet()) {
            if (entry.getValue() != null) {
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void put(UVDirection dir, UVQuad quad) {
        if (dir != null) {
            this.quads.put(dir, quad);
        }
    }

    public UVQuad get(UVDirection dir) {
        return this.quads.get(dir);
    }

    public Map<UVDirection, UVQuad> getAllSides() {
        return Collections.unmodifiableMap(this.quads);
    }

    public UVLayout copy() {
        UVLayout copy = new UVLayout();
        for (UVDirection direction : UVDirection.values()) {
            UVQuad quad = this.quads.get(direction);
            copy.put(direction, quad == null ? null : new UVQuad(quad.x1(), quad.y1(), quad.x2(), quad.y2()));
        }
        return copy;
    }

    public static UVLayout defaultsForLeft() {
        UVLayout layout = new UVLayout();
        layout.put(UVDirection.EAST, new UVQuad(24, 21, 27, 26));
        layout.put(UVDirection.WEST, new UVQuad(16, 21, 20, 26));
        layout.put(UVDirection.DOWN, new UVQuad(20, 17, 24, 21));
        layout.put(UVDirection.UP, new UVQuad(20, 25, 24, 27));
        layout.put(UVDirection.NORTH, new UVQuad(20, 21, 24, 26));
        return layout;
    }

    public static UVLayout defaultsForRight() {
        UVLayout layout = new UVLayout();
        layout.put(UVDirection.EAST, new UVQuad(28, 21, 32, 26));
        layout.put(UVDirection.WEST, new UVQuad(21, 21, 24, 26));
        layout.put(UVDirection.DOWN, new UVQuad(24, 17, 28, 21));
        layout.put(UVDirection.UP, new UVQuad(24, 25, 28, 27));
        layout.put(UVDirection.NORTH, new UVQuad(24, 21, 28, 26));
        return layout;
    }

    public static UVLayout leftOverlayDefaults() {
        UVLayout layout = new UVLayout();
        layout.put(UVDirection.EAST, new UVQuad(0, 0, 0, 0));
        layout.put(UVDirection.WEST, new UVQuad(17, 37, 20, 42));
        layout.put(UVDirection.DOWN, new UVQuad(20, 34, 24, 37));
        layout.put(UVDirection.UP, new UVQuad(20, 42, 24, 45));
        layout.put(UVDirection.NORTH, new UVQuad(20, 37, 24, 42));
        return layout;
    }

    public static UVLayout rightOverlayDefaults() {
        UVLayout layout = new UVLayout();
        layout.put(UVDirection.EAST, new UVQuad(28, 37, 31, 42));
        layout.put(UVDirection.WEST, new UVQuad(0, 0, 0, 0));
        layout.put(UVDirection.DOWN, new UVQuad(24, 34, 28, 37));
        layout.put(UVDirection.UP, new UVQuad(24, 42, 28, 45));
        layout.put(UVDirection.NORTH, new UVQuad(24, 37, 28, 42));
        return layout;
    }
}