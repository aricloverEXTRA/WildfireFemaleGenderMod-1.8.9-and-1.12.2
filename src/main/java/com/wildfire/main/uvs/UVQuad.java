package com.wildfire.main.uvs;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class UVLayout {
    private final EnumMap<UVDirection, UVQuad> quads = new EnumMap<>(UVDirection.class);

    public UVLayout() {
        for (UVDirection d : UVDirection.values()) {
            quads.put(d, null);
        }
    }

    public UVLayout(BreastTypes type) {
        this();
        UVLayout defaults;
        switch (type) {
            case LEFT: defaults = defaultsForLeft(); break;
            case RIGHT: defaults = defaultsForRight(); break;
            case LEFT_OVERLAY: defaults = leftOverlayDefaults(); break;
            case RIGHT_OVERLAY: defaults = rightOverlayDefaults(); break;
            default: defaults = defaultsForLeft();
        }
        for (Map.Entry<UVDirection, UVQuad> entry : defaults.getAllSides().entrySet()) {
            if (entry.getValue() != null) {
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void put(UVDirection dir, UVQuad quad) {
        quads.put(dir, quad);
    }

    public UVQuad get(UVDirection dir) {
        return quads.get(dir);
    }

    public Map<UVDirection, UVQuad> getAllSides() {
        return Collections.unmodifiableMap(quads);
    }

    public UVLayout copy() {
        UVLayout c = new UVLayout();
        for (UVDirection d : UVDirection.values()) {
            UVQuad q = quads.get(d);
            c.put(d, q == null ? null : new UVQuad(q.x1(), q.y1(), q.x2(), q.y2()));
        }
        return c;
    }

    // --- FAITHFUL 1.8+ SKIN DEFAULTS ---
    // Torso Base: X16, Y16 -> X32, Y32
    // Torso Jacket: X16, Y32 -> X32, Y48

    public static UVLayout defaultsForLeft() {
        UVLayout l = new UVLayout();
        l.put(UVDirection.NORTH, new UVQuad(20, 20, 24, 24)); // Front
        l.put(UVDirection.EAST,  new UVQuad(24, 20, 28, 24)); // Inner
        l.put(UVDirection.WEST,  new UVQuad(16, 20, 20, 24)); // Outer
        l.put(UVDirection.UP,    new UVQuad(20, 16, 24, 20)); // Top
        l.put(UVDirection.DOWN,  new UVQuad(24, 16, 28, 20)); // Bottom
        return l;
    }

    public static UVLayout defaultsForRight() {
        UVLayout l = new UVLayout();
        l.put(UVDirection.NORTH, new UVQuad(24, 20, 28, 24));
        l.put(UVDirection.EAST,  new UVQuad(28, 20, 32, 24));
        l.put(UVDirection.WEST,  new UVQuad(20, 20, 24, 24));
        l.put(UVDirection.UP,    new UVQuad(24, 16, 28, 20));
        l.put(UVDirection.DOWN,  new UVQuad(28, 16, 32, 20));
        return l;
    }

    public static UVLayout leftOverlayDefaults() {
        UVLayout l = new UVLayout();
        // Shifted down by 16 pixels to hit the Jacket layer
        l.put(UVDirection.NORTH, new UVQuad(20, 36, 24, 40)); 
        l.put(UVDirection.EAST,  new UVQuad(24, 36, 28, 40));
        l.put(UVDirection.WEST,  new UVQuad(16, 36, 20, 40));
        l.put(UVDirection.UP,    new UVQuad(20, 32, 24, 36));
        l.put(UVDirection.DOWN,  new UVQuad(24, 32, 28, 36));
        return l;
    }

    public static UVLayout rightOverlayDefaults() {
        UVLayout l = new UVLayout();
        l.put(UVDirection.NORTH, new UVQuad(24, 36, 28, 40));
        l.put(UVDirection.EAST,  new UVQuad(28, 36, 32, 40));
        l.put(UVDirection.WEST,  new UVQuad(20, 36, 24, 40));
        l.put(UVDirection.UP,    new UVQuad(24, 32, 28, 36));
        l.put(UVDirection.DOWN,  new UVQuad(28, 32, 32, 36));
        return l;
    }
}