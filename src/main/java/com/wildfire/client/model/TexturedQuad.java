package com.wildfire.client.model;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Vec3;

public class TexturedQuad {

    private final PositionTextureVertex[] vertexPositions;
    public final Vec3 normal;

    public TexturedQuad(Vec3[] vertices, int u, int v, int u2, int v2, float texWidth, float texHeight) {
        this.vertexPositions = new PositionTextureVertex[4];
        this.vertexPositions[0] = new PositionTextureVertex(vertices[0], u2 / texWidth, v / texHeight);
        this.vertexPositions[1] = new PositionTextureVertex(vertices[1], u / texWidth, v / texHeight);
        this.vertexPositions[2] = new PositionTextureVertex(vertices[2], u / texWidth, v2 / texHeight);
        this.vertexPositions[3] = new PositionTextureVertex(vertices[3], u2 / texWidth, v2 / texHeight);
        this.normal = vertices[1].subtract(vertices[0]).crossProduct(vertices[1].subtract(vertices[2])).normalize();
    }

    public void draw(WorldRenderer renderer, float scale) {
        Vec3 vec3d = this.normal;
        Tessellator tessellator = Tessellator.getInstance();
        renderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        renderer.normal((float) vec3d.xCoord, (float) vec3d.yCoord, (float) vec3d.zCoord);

        for (int i = 0; i < 4; ++i) {
            PositionTextureVertex vertex = this.vertexPositions[i];
            renderer.pos(vertex.x * scale, vertex.y * scale, vertex.z * scale).tex(vertex.u, vertex.v).endVertex();
        }

        tessellator.draw();
    }

    public void flipFace() {
        PositionTextureVertex[] avertex = new PositionTextureVertex[this.vertexPositions.length];

        for (int i = 0; i < this.vertexPositions.length; ++i) {
            avertex[i] = this.vertexPositions[this.vertexPositions.length - i - 1];
        }

        System.arraycopy(avertex, 0, this.vertexPositions, 0, this.vertexPositions.length);
    }
}
