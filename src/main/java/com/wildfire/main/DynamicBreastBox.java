package com.wildfire.main;

import com.wildfire.main.uvs.UVDirection;
import com.wildfire.main.uvs.UVLayout;
import com.wildfire.main.uvs.UVQuad;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class DynamicBreastBox {
    private static final float TEX_W = 64.0F;
    private static final float TEX_H = 64.0F;

    private final ModelRenderer leftBreast;
    private final ModelRenderer rightBreast;
    private final float x;
    private final float y;
    private final float z;
    private final int dx;
    private final int dy;
    private final int dz;
    private UVLayout uvLayout;

    public float rotationPointX = 0.0F;
    public float rotationPointY = 0.0F;
    public float rotationPointZ = 0.0F;

    public DynamicBreastBox() {
        this(0.0F, 0.0F, 0.0F, 4, 4, 4, 0.5F, null);
    }

    public DynamicBreastBox(float x, float y, float z, int dx, int dy, int dz, float delta, UVLayout uvLayout) {
        this.x = x - delta;
        this.y = y - delta;
        this.z = z - delta;
        this.dx = dx + (int) (delta * 2.0F);
        this.dy = dy + (int) (delta * 2.0F);
        this.dz = dz + (int) (delta * 2.0F);
        this.uvLayout = uvLayout;

        this.leftBreast = new ModelRenderer(null, 0, 0);
        this.rightBreast = new ModelRenderer(null, 0, 0);
        this.leftBreast.addBox(-3.2F, 0.0F, -1.8F, 4, 4, 4, 0.0F);
        this.rightBreast.addBox(-0.8F, 0.0F, -1.8F, 4, 4, 4, 0.0F);
        this.leftBreast.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.rightBreast.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    public void setUVLayout(UVLayout layout) {
        this.uvLayout = layout;
    }

    public void setRotationPoint(float rx, float ry, float rz) {
        this.rotationPointX = rx;
        this.rotationPointY = ry;
        this.rotationPointZ = rz;
    }

    public void render(float scale, float bounce, float sway) {
        if (this.leftBreast == null || this.rightBreast == null) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0.0F, bounce * 0.03F, 0.0F);

        this.leftBreast.rotateAngleZ = sway * 0.02F;
        this.rightBreast.rotateAngleZ = -sway * 0.02F;
        this.leftBreast.render(0.0625F);
        this.rightBreast.render(0.0625F);
        GlStateManager.popMatrix();
    }

    public void render(float renderScale) {
        if (this.uvLayout == null) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        float x1 = this.x;
        float y1 = this.y;
        float z1 = this.z;
        float x2 = this.x + this.dx;
        float y2 = this.y + this.dy;
        float z2 = this.z + this.dz;

        drawFace(worldRenderer, UVDirection.EAST, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, 1.0F, 0.0F, 0.0F, renderScale);
        drawFace(worldRenderer, UVDirection.WEST, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, -1.0F, 0.0F, 0.0F, renderScale);
        drawFace(worldRenderer, UVDirection.DOWN, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, 0.0F, -1.0F, 0.0F, renderScale);
        drawFace(worldRenderer, UVDirection.UP, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, 0.0F, 1.0F, 0.0F, renderScale);
        drawFace(worldRenderer, UVDirection.NORTH, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, 0.0F, 0.0F, -1.0F, renderScale);

        tessellator.draw();
    }

    private void drawFace(WorldRenderer worldRenderer, UVDirection direction, double v0x, double v0y, double v0z,
                          double v1x, double v1y, double v1z, double v2x, double v2y, double v2z,
                          double v3x, double v3y, double v3z, float nx, float ny, float nz, float renderScale) {
        if (this.uvLayout == null) {
            return;
        }
        UVQuad quad = this.uvLayout.get(direction);
        if (quad == null) {
            return;
        }

        double u1 = quad.x1() / TEX_W;
        double v1 = quad.y1() / TEX_H;
        double u2 = (quad.x2() + 1) / TEX_W;
        double v2 = (quad.y2() + 1) / TEX_H;

        worldRenderer.pos(v0x * renderScale, v0y * renderScale, v0z * renderScale).tex(u2, v2).normal(nx, ny, nz).endVertex();
        worldRenderer.pos(v1x * renderScale, v1y * renderScale, v1z * renderScale).tex(u1, v2).normal(nx, ny, nz).endVertex();
        worldRenderer.pos(v2x * renderScale, v2y * renderScale, v2z * renderScale).tex(u1, v1).normal(nx, ny, nz).endVertex();
        worldRenderer.pos(v3x * renderScale, v3y * renderScale, v3z * renderScale).tex(u2, v1).normal(nx, ny, nz).endVertex();
    }
}