package com.wildfire.main.uvs;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class UVLayout {
    private final EnumMap<UVDirection, UVQuad> quads = new EnumMap<>(UVDirection.class);

    public UVLayout() {
        for (UVDirection d : UVDirection.values()) quads.put(d, null);
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

    public static UVLayout defaultsForLargeFemale() {
        UVLayout l = new UVLayout();
        l.put(UVDirection.EAST, new UVQuad(24,21,28,26));
        l.put(UVDirection.WEST, new UVQuad(16,21,20,26));
        l.put(UVDirection.DOWN, new UVQuad(20,17,24,21));
        l.put(UVDirection.UP, new UVQuad(20,25,24,27));
        l.put(UVDirection.NORTH, new UVQuad(20,21,24,26));
        return l;
    }
}