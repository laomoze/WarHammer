package wh.entities.world.layers;

import arc.func.Boolf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.Net;
import mindustry.net.Packet;

import java.io.*;
import java.util.Objects;

/**
 * Encodes a vanilla packet so it can be delivered inside a selected layer.
 */
public final class LayerPacketCodec {
    private static final int maxPayload = LayerRpcPacket.maxFragment * LayerRpcPacket.maxFragments;

    private LayerPacketCodec() {
    }

    public static byte[] encode(Packet packet) throws IOException {
        Objects.requireNonNull(packet, "packet");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(buffer);
        data.writeByte(Net.getPacketId(packet));
        packet.write(new Writes(data));
        data.flush();
        if (buffer.size() > maxPayload) throw new IOException("Layer packet is too large");
        return buffer.toByteArray();
    }

    /**
     * Only call for data received from the authoritative server.
     */
    public static void deliverClient(LayerContext layer, byte[] bytes, Boolf<Class<?>> allowed) throws IOException {
        Objects.requireNonNull(layer, "layer");
        Objects.requireNonNull(bytes, "bytes");
        Objects.requireNonNull(allowed, "allowed");
        if (bytes.length == 0 || bytes.length > maxPayload) throw new IOException("Invalid layer packet size");
        Packet packet;
        try {
            packet = Net.newPacket(bytes[0]);
        } catch (RuntimeException failure) {
            throw new IOException("Unknown layer packet ID", failure);
        }
        if (!packet.allow(false) || !allowed.get(packet.getClass())) {
            throw new IOException("Layer packet type is not allowed: " + packet.getClass().getName());
        }
        layer.run(() -> {
            Reads input = new Reads(new DataInputStream(new ByteArrayInputStream(bytes, 1, bytes.length - 1)));
            packet.read(input, bytes.length - 1);
            packet.handled();
            packet.handleClient();
        });
    }
}
