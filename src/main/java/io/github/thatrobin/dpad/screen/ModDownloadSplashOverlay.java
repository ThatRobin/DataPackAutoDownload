package io.github.thatrobin.dpad.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.IntSupplier;

public class ModDownloadSplashOverlay extends Overlay {

    static final Identifier LOGO = new Identifier("dpad", "textures/gui/icon.png");
    private static final int MOJANG_RED = ColorHelper.Argb.getArgb(255, 25, 94, 119);
    private static final int MONOCHROME_BLACK = ColorHelper.Argb.getArgb(255, 0, 0, 0);
    private static final IntSupplier BRAND_ARGB = () -> MinecraftClient.getInstance().options.getMonochromeLogo().getValue() ? MONOCHROME_BLACK : MOJANG_RED;
    private final MinecraftClient client;
    private final ResourceReload reload;
    private float progress;
    private long reloadCompleteTime = -1L;
    private long reloadStartTime = -1L;

    public ModDownloadSplashOverlay(MinecraftClient client, ResourceReload monitor) {
        this.client = client;
        this.reload = monitor;
    }

    public static void init(MinecraftClient client) {
        client.getTextureManager().registerTexture(LOGO, new LogoTexture());
    }

    private static int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | alpha << 24;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        float h;
        int k;
        int i = this.client.getWindow().getScaledWidth();
        int j = this.client.getWindow().getScaledHeight();
        long l = Util.getMeasuringTimeMs();
        if (this.reloadStartTime == -1L) {
            this.reloadStartTime = l;
        }
        float f = this.reloadCompleteTime > -1L ? (float)(l - this.reloadCompleteTime) / 1000.0f : -1.0f;
        float g = this.reloadStartTime > -1L ? (float)(l - this.reloadStartTime) / 500.0f : -1.0f;
        if (f >= 1.0f) {
            if (this.client.currentScreen != null) {
                this.client.currentScreen.render(matrices, 0, 0, delta);
            }
            k = MathHelper.ceil((1.0f - MathHelper.clamp(f - 1.0f, 0.0f, 1.0f)) * 255.0f);
            ModDownloadSplashOverlay.fill(matrices, 0, 0, i, j, ModDownloadSplashOverlay.withAlpha(BRAND_ARGB.getAsInt(), k));
            h = 1.0f - MathHelper.clamp(f - 1.0f, 0.0f, 1.0f);
        } else {
            k = BRAND_ARGB.getAsInt();
            float m = (float)(k >> 16 & 0xFF) / 255.0f;
            float n = (float)(k >> 8 & 0xFF) / 255.0f;
            float o = (float)(k & 0xFF) / 255.0f;
            GlStateManager._clearColor(m, n, o, 1.0f);
            GlStateManager._clear(16384, MinecraftClient.IS_SYSTEM_MAC);
            h = 1.0f;
        }
        int p = (int)((double)this.client.getWindow().getScaledHeight() * 0.5);
        double d = Math.min((double)this.client.getWindow().getScaledWidth() * 0.75, this.client.getWindow().getScaledHeight()) * 0.25;
        double e = d * 4.0;
        int r = (int)(e * 0.5);
        RenderSystem.setShaderTexture(0, LOGO);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, h);
        drawTexture(matrices, this.client.getWindow().getScaledWidth()/2 - 30, p - 1, 60, 60, 0, 0, 60, 60, 60, 60);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        int s = (int)((double)this.client.getWindow().getScaledHeight() * 0.8325);
        float t = this.reload.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95f + t * 0.050000012f, 0.0f, 1.0f);
        if (f < 1.0f) {
            this.renderProgressBar(matrices, i / 2 - r, s - 5, i / 2 + r, s + 5, 1.0f - MathHelper.clamp(f, 0.0f, 1.0f));
        }
        if (f >= 2.0f) {
            this.client.setOverlay(null);
        }
        if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (g >= 2.0f)) {
            this.reloadCompleteTime = Util.getMeasuringTimeMs();
            if (this.client.currentScreen != null) {
                this.client.currentScreen.init(this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
            }
        }
    }

    private void renderProgressBar(MatrixStack matrices, int minX, int minY, int maxX, int maxY, float opacity) {
        int i = MathHelper.ceil((float)(maxX - minX - 2) * this.progress);
        int j = Math.round(opacity * 255.0f);
        int k = ColorHelper.Argb.getArgb(j, 255, 255, 255);
        ModDownloadSplashOverlay.fill(matrices, minX + 2, minY + 2, minX + i, maxY - 2, k);
        ModDownloadSplashOverlay.fill(matrices, minX + 1, minY, maxX - 1, minY + 1, k);
        ModDownloadSplashOverlay.fill(matrices, minX + 1, maxY, maxX - 1, maxY - 1, k);
        ModDownloadSplashOverlay.fill(matrices, minX, minY, minX + 1, maxY, k);
        ModDownloadSplashOverlay.fill(matrices, maxX, minY, maxX - 1, maxY, k);
    }

    @Override
    public boolean pausesGame() {
        return true;
    }

    @Environment(value= EnvType.CLIENT)
    static class LogoTexture
            extends ResourceTexture {
        public LogoTexture() {
            super(LOGO);
        }

        @Override
        protected ResourceTexture.TextureData loadTextureData(ResourceManager resourceManager) {
            ResourceTexture.TextureData textureData;
            try {
                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                DefaultResourcePack defaultResourcePack = minecraftClient.getResourcePackProvider().getPack();
                InputStream inputStream = defaultResourcePack.open(ResourceType.CLIENT_RESOURCES, LOGO);
                try {
                    textureData = new ResourceTexture.TextureData(new TextureResourceMetadata(true, true), NativeImage.read(inputStream));
                } catch (Throwable throwable) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable var7) {
                            throwable.addSuppressed(var7);
                        }
                    }

                    throw throwable;
                }

                inputStream.close();

                return textureData;
            } catch (IOException iOException) {
                return new ResourceTexture.TextureData(iOException);
            }
        }
    }
}