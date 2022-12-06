package io.github.thatrobin.dpad.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.github.thatrobin.dpad.Dpad;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("all")
public class SimpleModDownload<S> implements ResourceReload {
    protected final CompletableFuture<Unit> prepareStageFuture = new CompletableFuture();
    protected CompletableFuture<List<S>> applyStageFuture;
    final Set<SimpleModDownload> waitingReloaders;
    private final int reloaderCount;
    private int toApplyCount;
    private int appliedCount;
    private MinecraftClient client;
    private ClientLoginNetworkHandler clientLoginNetworkHandler;
    private final AtomicInteger toPrepareCount = new AtomicInteger();
    private final AtomicInteger preparedCount = new AtomicInteger();

    public static SimpleModDownload<Void> create(MinecraftClient client, ClientLoginNetworkHandler clientLoginNetworkHandler, Map<String, String> map, List<ResourceReloader> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage) {
        return new SimpleModDownload(client, clientLoginNetworkHandler, prepareExecutor, applyExecutor, reloaders, (synchronizer, reloader, prepare, apply) -> ((CompletableFuture)CompletableFuture.supplyAsync(() -> prepare(map), prepareExecutor).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync((object) -> apply(object), applyExecutor), initialStage);
    }

    protected SimpleModDownload(MinecraftClient client, ClientLoginNetworkHandler clientLoginNetworkHandler, Executor prepareExecutor, Executor applyExecutor, List<SimpleModDownload> reloaders, SimpleModDownload.Factory<S> factory, CompletableFuture<Unit> initialStage) {
        this.client = client;
        this.clientLoginNetworkHandler = clientLoginNetworkHandler;
        this.reloaderCount = reloaders.size();
        this.toPrepareCount.incrementAndGet();
        initialStage.thenRun(this.preparedCount::incrementAndGet);
        ArrayList<CompletableFuture<S>> list = Lists.newArrayList();
        CompletableFuture<Unit> completableFuture = initialStage;
        this.waitingReloaders = Sets.newHashSet(reloaders);
            final CompletableFuture<Unit> completableFuture2 = completableFuture;
            CompletableFuture<S> completableFuture3 = factory.create(new ResourceReloader.Synchronizer(){

                @Override
                public <T> CompletableFuture<T> whenPrepared(T preparedObject) {
                    applyExecutor.execute(() -> {
                        SimpleModDownload.this.waitingReloaders.remove(this);
                        if (SimpleModDownload.this.waitingReloaders.isEmpty()) {
                            SimpleModDownload.this.prepareStageFuture.complete(Unit.INSTANCE);
                        }
                    });
                    return SimpleModDownload.this.prepareStageFuture.thenCombine(completableFuture2, (unit, object2) -> preparedObject);
                }
            }, this, preparation -> {
                this.toPrepareCount.incrementAndGet();
                prepareExecutor.execute(() -> {
                    preparation.run();
                    this.preparedCount.incrementAndGet();
                });
            }, application -> {
                ++this.toApplyCount;
                applyExecutor.execute(() -> {
                    application.run();
                    ++this.appliedCount;
                });
            });
            list.add(completableFuture3);

        this.applyStageFuture = Util.combine(list);
    }

    @Override
    public CompletableFuture<?> whenComplete() {
        clientLoginNetworkHandler.getConnection().disconnect(Text.literal("You have installed some mods for this server. Please restart your game to allow them to load."));
        client.setScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), Text.literal("DPAD has installed Mods."), Text.literal("You have installed some mods for this server. Please restart your game to allow them to load.")));
        return this.applyStageFuture;
    }

    @Override
    public float getProgress() {
        int i = this.reloaderCount - this.waitingReloaders.size();
        float f = this.preparedCount.get() * 2 + this.appliedCount * 2 + i;
        float g = this.toPrepareCount.get() * 2 + this.toApplyCount * 2 + this.reloaderCount;
        return f / g;
    }

    protected static Object prepare(Map<String, String> map) {
        return map;
    }

    protected static void apply(Object var1) {
        try {
            if (var1 instanceof Map map) {
                if (!map.isEmpty()) {
                    for (Map.Entry<String, String> entry : ((Map<String, String>) map).entrySet()) {
                        ModUtilities.downloadFile(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            Dpad.LOGGER.error(e.getMessage());
        }
    }

    protected interface Factory<S> {
        CompletableFuture<S> create(ResourceReloader.Synchronizer var1, SimpleModDownload var3, Executor var4, Executor var5);
    }
}
