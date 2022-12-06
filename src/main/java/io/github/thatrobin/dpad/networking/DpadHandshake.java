package io.github.thatrobin.dpad.networking;

import io.github.thatrobin.dpad.Dpad;
import io.github.thatrobin.dpad.screen.ModDownloadSplashOverlay;
import io.github.thatrobin.dpad.utils.ModRegistry;
import io.github.thatrobin.dpad.utils.SimpleModDownload;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import org.apache.commons.compress.utils.Lists;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DpadHandshake {

    public static final Identifier CHANNEL_ID = new Identifier("dpad", "handshake");

    static {
        ServerLoginConnectionEvents.QUERY_START.register(DpadHandshake::queryStart);
        ServerLoginNetworking.registerGlobalReceiver(CHANNEL_ID, DpadHandshake::syncServer);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientLoginNetworking.registerGlobalReceiver(CHANNEL_ID, DpadHandshake::syncClient);
        }
    }

    public static void enable() {
    }

    private static void queryStart(ServerLoginNetworkHandler serverLoginNetworkHandler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer loginSynchronizer) {
        var request = PacketByteBufs.create();
        request.writeMap(ModRegistry.getMap(), PacketByteBuf::writeString, PacketByteBuf::writeString);
        sender.sendPacket(CHANNEL_ID, request);
    }

    @Environment(EnvType.CLIENT)
    @SuppressWarnings("all")
    private static CompletableFuture<PacketByteBuf> syncClient(MinecraftClient client, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        Map<String, String> map = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readString);
        Map<String, String> map2 = new HashMap<>();
        if (!map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                Path path = FabricLoader.getInstance().getGameDir().resolve("mods");
                File newPath = Paths.get(path.toString(), entry.getKey() + ".jar").toFile();
                if (!newPath.exists()) {
                    map2.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (!map2.isEmpty()) {
            try {
                SimpleModDownload simpleModDownload = SimpleModDownload.create(client, clientLoginNetworkHandler, map2, Lists.newArrayList(), Util.getMainWorkerExecutor(), client, CompletableFuture.completedFuture(Unit.INSTANCE));
                client.setOverlay(new ModDownloadSplashOverlay(client, simpleModDownload));
            } catch (Exception e) {
                Dpad.LOGGER.error(e.getMessage());
            }
        }
        var response = PacketByteBufs.create();
        return CompletableFuture.completedFuture(response);
    }

    private static void syncServer(MinecraftServer server, ServerLoginNetworkHandler handler, boolean responded, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer loginSynchronizer, PacketSender packetSender) {
    }
}
