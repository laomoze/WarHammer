package wh.entities.world.layers;

import mindustry.net.Net;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Wraps broadcasts emitted by vanilla calls while a layer is active.
 */
public final class LayerNet extends Net {
    private static final AtomicLong nextMessageId = new AtomicLong(System.nanoTime());
    private final Net delegate;
    private final String layerId;

    public LayerNet(Net delegate, String layerId) {
        super(null);
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.layerId = Objects.requireNonNull(layerId, "layerId");
        if (layerId.isEmpty() || layerId.length() > 128) throw new IllegalArgumentException("Invalid layer ID");
    }

    @Override
    public boolean active() {
        return delegate.active();
    }

    @Override
    public boolean server() {
        return delegate.server();
    }

    @Override
    public boolean client() {
        return delegate.client();
    }

    @Override
    public Iterable<NetConnection> getConnections() {
        return delegate.getConnections();
    }

    @Override
    public void send(Object object, boolean reliable) {
        sendWrapped(object, packet -> delegate.send(packet, reliable));
    }

    @Override
    public void send(Object object, Iterable<NetConnection> connections, boolean reliable) {
        sendWrapped(object, packet -> delegate.send(packet, connections, reliable));
    }

    @Override
    public void sendExcept(NetConnection except, Object object, boolean reliable) {
        sendWrapped(object, packet -> delegate.sendExcept(except, packet, reliable));
    }

    private void sendWrapped(Object object, Consumer<LayerRpcPacket> send) {
        if (!delegate.server() || !(object instanceof Packet)) {
            throw new IllegalStateException("Only server packets can be sent through a layer");
        }
        try {
            byte[] payload = LayerPacketCodec.encode((Packet) object);
            int count = (payload.length + LayerRpcPacket.maxFragment - 1) / LayerRpcPacket.maxFragment;
            long id = nextMessageId.incrementAndGet();
            for (int index = 0; index < count; index++) {
                int start = index * LayerRpcPacket.maxFragment;
                byte[] fragment = Arrays.copyOfRange(payload, start, Math.min(start + LayerRpcPacket.maxFragment, payload.length));
                send.accept(new LayerRpcPacket(layerId, id, index, count, fragment));
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot encode layer packet", failure);
        }
    }
}
