package com.github.lunatrius.ingameinfo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.github.lunatrius.core.util.vector.Vector2f;
import com.github.lunatrius.ingameinfo.reference.Reference;

public class InfoIcon extends Info {

    private ResourceLocation resourceLocation;
    private final Vector2f xy0 = new Vector2f();
    private final Vector2f xy1 = new Vector2f();
    private final Vector2f uv0 = new Vector2f();
    private final Vector2f uv1 = new Vector2f();
    private int displayWidth;
    private int displayHeight;

    public InfoIcon(String location) {
        this(location, 0, 0, 8, 8, 0, 0, 8, 8, 8, 8, 0, 0);
    }

    public InfoIcon(String location, int displayX, int displayY, int displayWidth, int displayHeight, int iconX,
            int iconY, int iconWidth, int iconHeight, int textureWidth, int textureHeight, int x, int y) {
        super(x, y);
        this.resourceLocation = new ResourceLocation(location);
        setDisplayDimensions(displayX, displayY, displayWidth, displayHeight);
        setTextureData(iconX, iconY, iconWidth, iconHeight, textureWidth, textureHeight);
    }

    public void setDisplayDimensions(int displayX, int displayY, int displayWidth, int displayHeight) {
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;

        this.xy0.set(displayX, displayY);
        this.xy1.set(displayX + displayWidth, displayY + displayHeight);
    }

    public void setTextureData(int iconX, int iconY, int iconWidth, int iconHeight, int textureWidth,
            int textureHeight) {
        this.uv0.set((float) iconX / textureWidth, (float) iconY / textureHeight);
        this.uv1.set((float) (iconX + iconWidth) / textureWidth, (float) (iconY + iconHeight) / textureHeight);
    }

    @Override
    public void drawInfo() {
        try {
            ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(resourceLocation);
            if (texture == null) {
                Reference.logger.debug("Unable to find texture for icon {}", resourceLocation);
                return;
            }

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());

            GL11.glTranslatef(getX(), getY(), 0);

            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            double zLevel = 300;
            tess.addVertexWithUV(xy0.x, xy1.y, zLevel, uv0.x, uv1.y);
            tess.addVertexWithUV(xy1.x, xy1.y, zLevel, uv1.x, uv1.y);
            tess.addVertexWithUV(xy1.x, xy0.y, zLevel, uv1.x, uv0.y);
            tess.addVertexWithUV(xy0.x, xy0.y, zLevel, uv0.x, uv0.y);
            tess.draw();

            GL11.glTranslatef(-getX(), -getY(), 0);
        } catch (Exception e) {
            Reference.logger.debug(e);
        }
    }

    @Override
    public void setValue(@NotNull Object value) {
        this.resourceLocation = new ResourceLocation(value.toString());
    }

    @Override
    public int getWidth() {
        return this.displayWidth;
    }

    @Override
    public int getHeight() {
        return this.displayHeight;
    }

    @Override
    public String toString() {
        return String.format(
                "InfoIcon{resource: %s, x: %d, y: %d, offsetX: %d, offsetY: %d, children: %s}",
                this.resourceLocation,
                this.x,
                this.y,
                this.offsetX,
                this.offsetY,
                this.children);
    }
}
