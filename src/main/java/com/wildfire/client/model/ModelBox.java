package com.wildfire.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.Vec3;

public class ModelBox extends net.minecraft.client.model.ModelBox {

    public final TexturedQuad[] quads;

    public ModelBox(ModelRenderer renderer, int u, int v, float x, float y, float z, int dx, int dy, int dz, float delta, boolean mirror) {
        super(renderer, u, v, x, y, z, dx, dy, dz, delta, mirror);
        this.quads = new TexturedQuad[6];
        float f = x + dx;
        float f1 = y + dy;
        float f2 = z + dz;
        x -= delta;
        y -= delta;
        z -= delta;
        f += delta;
        f1 += delta;
        f2 += delta;
        if (mirror) {
            float f3 = f;
            f = x;
            x = f3;
        }
        Vec3 vec0 = new Vec3(x, y, z);
        Vec3 vec1 = new Vec3(f, y, z);
        Vec3 vec2 = new Vec3(f, f1, z);
        Vec3 vec3 = new Vec3(x, f1, z);
        Vec3 vec4 = new Vec3(x, y, f2);
        Vec3 vec5 = new Vec3(f, y, f2);
        Vec3 vec6 = new Vec3(f, f1, f2);
        Vec3 vec7 = new Vec3(x, f1, f2);

        this.quads[0] = new TexturedQuad(new Vec3[] {vec5, vec1, vec2, vec6}, u, v, u + dz, v + dy, renderer.textureWidth, renderer.textureHeight);
        this.quads[1] = new TexturedQuad(new Vec3[] {vec0, vec4, vec7, vec3}, u, v, u + dz, v + dy, renderer.textureWidth, renderer.textureHeight);
        this.quads[2] = new TexturedQuad(new Vec3[] {vec5, vec4, vec0, vec1}, u, v, u + dx, v + dz, renderer.textureWidth, renderer.textureHeight);
        this.quads[3] = new TexturedQuad(new Vec3[] {vec2, vec3, vec7, vec6}, u, v, u + dx, v + dz, renderer.textureWidth, renderer.textureHeight);
        this.quads[4] = new TexturedQuad(new Vec3[] {vec1, vec0, vec3, vec2}, u, v, u + dx, v + dz, renderer.textureWidth, renderer.textureHeight);
        this.quads[5] = new TexturedQuad(new Vec3[] {vec4, vec5, vec6, vec7}, u, v, u + dx, v + dz, renderer.textureWidth, renderer.textureHeight);

        if (mirror) {
            for (int i = 0; i < this.quads.length; ++i) {
                this.quads[i].flipFace();
            }
        }
    }

    @Override
    public void render(WorldRenderer renderer, float scale) {
        for (int i = 0; i < this.quads.length; ++i) {
            this.quads[i].draw(renderer, scale);
        }
    }
}
