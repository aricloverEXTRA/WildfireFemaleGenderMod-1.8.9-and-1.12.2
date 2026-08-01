package com.wildfire.main.uvs;

public class UVQuad {
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;

    public UVQuad(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int x1() {
        return x1;
    }

    public int y1() {
        return y1;
    }

    public int x2() {
        return x2;
    }

    public int y2() {
        return y2;
    }

    public UVQuad addX1(int delta) {
        return new UVQuad(x1 + delta, y1, x2, y2);
    }

    public UVQuad addX2(int delta) {
        return new UVQuad(x1, y1, x2 + delta, y2);
    }

    public UVQuad addY1(int delta) {
        return new UVQuad(x1, y1 + delta, x2, y2);
    }

    public UVQuad addY2(int delta) {
        return new UVQuad(x1, y1, x2, y2 + delta);
    }

    @Override
    public String toString() {
        return String.format("UVQuad[%d,%d -> %d,%d]", x1, y1, x2, y2);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UVQuad)) {
            return false;
        }
        UVQuad q = (UVQuad) o;
        return x1 == q.x1 && y1 == q.y1 && x2 == q.x2 && y2 == q.y2;
    }

    @Override
    public int hashCode() {
        return ((x1 * 31 + y1) * 31 + x2) * 31 + y2;
    }
}