package wh.entities.world.layers;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.Packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Server-to-client envelope for a vanilla packet associated with a layer ID.
 */
public final class LayerRpcPacket extends Packet {
    static final int maxIdLength = 128;
    static final int maxFragment = 600;
    static final int maxFragments = 100;
    private static Receiver receiver;
    private static LayerRpcAssembler assembler = new LayerRpcAssembler();

    public String layerId;
    public byte[] payload;
    public long messageId;
    public int fragment, fragments = 1;
    private byte[] data = NODATA;

    public LayerRpcPacket() {
    }

    public LayerRpcPacket(String layerId, byte[] payload) {
        this(layerId, 0L, 0, 1, payload);
    }

    public LayerRpcPacket(String layerId, long messageId, int fragment, int fragments, byte[] payload) {
        validate(layerId, fragment, fragments, payload);
        this.layerId = layerId;
        this.messageId = messageId;
        this.fragment = fragment;
        this.fragments = fragments;
        this.payload = payload.clone();
    }

    public static void setReceiver(Receiver value) {
        receiver = value;
        assembler = new LayerRpcAssembler();
    }

    @Override
    public void write(Writes write) {
        validate(layerId, fragment, fragments, payload);
        write.str(layerId);
        write.l(messageId);
        write.s(fragment);
        write.s(fragments);
        write.i(payload.length);
        write.b(payload);
    }

    @Override
    public void read(Reads read, int length) {
        if (length < 20 || length > maxFragment + maxIdLength * 3 + 18) {
            throw new IllegalArgumentException("Invalid layer RPC size");
        }
        data = read.b(length);
    }

    @Override
    public void handled() {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
            String id = input.readUTF();
            long messageNumber = input.readLong();
            int index = input.readUnsignedShort();
            int count = input.readUnsignedShort();
            int size = input.readInt();
            if (size < 1 || size > maxFragment || size != input.available()) {
                throw new IllegalArgumentException("Invalid layer RPC payload size");
            }
            byte[] message = new byte[size];
            input.readFully(message);
            validate(id, index, count, message);
            layerId = id;
            messageId = messageNumber;
            fragment = index;
            fragments = count;
            payload = message;
            data = NODATA;
        } catch (IOException failure) {
            throw new IllegalArgumentException("Invalid layer RPC", failure);
        }
    }

    @Override
    public boolean allow(boolean server) {
        return !server;
    }

    @Override
    public void handleClient() {
        Receiver current = receiver;
        if (current == null) throw new IllegalStateException("No layer RPC receiver is installed");
        byte[] complete = assembler.accept(this, System.currentTimeMillis());
        if (complete != null) current.receive(layerId, complete);
    }

    private static void validate(String id, int fragment, int fragments, byte[] payload) {
        Objects.requireNonNull(id, "layerId");
        Objects.requireNonNull(payload, "payload");
        if (id.isEmpty() || id.length() > maxIdLength || payload.length == 0 || payload.length > maxFragment ||
                fragments < 1 || fragments > maxFragments || fragment < 0 || fragment >= fragments) {
            throw new IllegalArgumentException("Invalid layer RPC fields");
        }
    }

    public interface Receiver {
        void receive(String layerId, byte[] payload);
    }
}
