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

    public static UVLayout defaultsForLeft() {
        UVLayout l = new UVLayout();
        l.put(UVDirection.EAST,  new UVQuad(24, 21, 27, 26));
        l.put(UVDirection.WEST,  new UVQuad(16, 21, 20, 26));
        l.put(UVDirection.DOWN,  new UVQuad(20, 17, 24, 21));
        l.put(UVDirection.UP,    new UVQuad(20, 25, 24, 27));
        l.put(UVDirection.NORTH, new UVQuad(20, 21, 24, 26));
        return l;
    }

    public static UVLayout defaultsForRight() {
        UVLayout l = new UVLayout();
        l.put(UVDirection.EAST,  new UVQuad(28, 21, 32, 26));
        l.put(UVDirection.WEST,  new UVQuad(21, 21, 24, 26));
        l.put(UVDirection.DOWN,  new UVQuad(24, 17, 28, 21));
        l.put(UVDirection.UP,    new UVQuad(24, 25, 28, 27));
        l.put(UVDirection.NORTH, new UVQuad(24, 21, 28, 26));
        return l;
    }

    public static UVLayout leftOverlayDefaults() {
        UVLayout l = new UVLayout();
        l.put(UVDirection.EAST,  new UVQuad(0, 0, 0, 0));
        l.put(UVDirection.WEST,  new UVQuad(17, 37, 20, 42));
        l.put(UVDirection.DOWN,  new UVQuad(20, 34, 24, 37));
        l.put(UVDirection.UP,    new UVQuad(20, 42, 24, 45));
        l.put(UVDirection.NORTH, new UVQuad(20, 37, 24, 42));
        return l;
    }

    public static UVLayout rightOverlayDefaults() {
        UVLayout l = new UVLayout();
        l.put(UVDirection.EAST,  new UVQuad(28, 37, 31, 42));
        l.put(UVDirection.WEST,  new UVQuad(0, 0, 0, 0));
        l.put(UVDirection.DOWN,  new UVQuad(24, 34, 28, 37));
        l.put(UVDirection.UP,    new UVQuad(24, 42, 28, 45));
        l.put(UVDirection.NORTH, new UVQuad(24, 37, 28, 42));
        return l;
    }
}