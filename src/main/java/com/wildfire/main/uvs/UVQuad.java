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

    public int x1() { return x1; }
    public int y1() { return y1; }
    public int x2() { return x2; }
    public int y2() { return y2; }

    public UVQuad addX1(int v) { return new UVQuad(this.x1 + v, this.y1, this.x2, this.y2); }
    public UVQuad addY1(int v) { return new UVQuad(this.x1, this.y1 + v, this.x2, this.y2); }
    public UVQuad addX2(int v) { return new UVQuad(this.x1, this.y1, this.x2 + v, this.y2); }
    public UVQuad addY2(int v) { return new UVQuad(this.x1, this.y1, this.x2, this.y2 + v); }

    @Override
    public String toString() {
        return x1 + "," + y1 + "," + x2 + "," + y2;
    }

    public static UVQuad fromString(String s) throws NumberFormatException {
        String[] parts = s.split(",");
        if (parts.length != 4) throw new NumberFormatException("Expected 4 ints");
        int a = Integer.parseInt(parts[0].trim());
        int b = Integer.parseInt(parts[1].trim());
        int c = Integer.parseInt(parts[2].trim());
        int d = Integer.parseInt(parts[3].trim());
        return new UVQuad(a, b, c, d);
    }
}