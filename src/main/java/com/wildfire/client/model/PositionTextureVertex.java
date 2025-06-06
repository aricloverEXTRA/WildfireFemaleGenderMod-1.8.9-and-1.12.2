package com.wildfire.client.model;

import net.minecraft.util.Vec3;

public class PositionTextureVertex {

    public final Vec3 vector3D;
    public final float x;
    public final float y;
    public final float z;
    public final float u;
    public final float v;

    public PositionTextureVertex(Vec3 vector3D, float u, float v) {
        this.vector3D = vector3D;
        this.x = (float) vector3D.xCoord;
        this.y = (float) vector3D.yCoord;
        this.z = (float) vector3D.zCoord;
        this.u = u;
        this.v = v;
    }

    public PositionTextureVertex(PositionTextureVertex vertex, float u, float v) {
        this.vector3D = vertex.vector3D;
        this.x = vertex.x;
        this.y = vertex.y;
        this.z = vertex.z;
        this.u = u;
        this.v = v;
    }
}
