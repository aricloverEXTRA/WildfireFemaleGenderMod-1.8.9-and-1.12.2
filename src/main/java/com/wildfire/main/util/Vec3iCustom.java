package com.wildfire.main.util;

public class Vec3iCustom {
    private final int x;
    private final int y;
    private final int z;

    public Vec3iCustom(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Vec3iCustom add(Vec3iCustom other) {
        return new Vec3iCustom(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vec3iCustom subtract(Vec3iCustom other) {
        return new Vec3iCustom(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vec3iCustom vec3i = (Vec3iCustom) obj;
        return x == vec3i.x && y == vec3i.y && z == vec3i.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return "Vec3iCustom{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
