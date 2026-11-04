package wh.entities.world.layers;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Bounded reassembly for layer RPC fragments; called on the game thread.
 */
final class LayerRpcAssembler {
    private static final long timeoutMillis = 30000;
    private static final int maxPending = 32;
    private static final int maxCompleted = 256;
    private final LinkedHashMap<Key, Pending> pending = new LinkedHashMap<>();
    private final LinkedHashMap<Key, Long> completed = new LinkedHashMap<>();

    byte[] accept(LayerRpcPacket packet, long now) {
        if (packet.fragments == 1) return packet.payload;
        expire(now);
        Key key = new Key(packet.layerId, packet.messageId);
        if (completed.containsKey(key)) return null;
        Pending message = pending.get(key);
        if (message == null) {
            if (pending.size() >= maxPending) pending.remove(pending.keySet().iterator().next());
            message = new Pending(packet.fragments, now);
            pending.put(key, message);
        } else if (message.parts.length != packet.fragments) {
            pending.remove(key);
            throw new IllegalArgumentException("Inconsistent layer RPC fragment count");
        }
        byte[] previous = message.parts[packet.fragment];
        if (previous != null) {
            if (!Arrays.equals(previous, packet.payload)) {
                pending.remove(key);
                throw new IllegalArgumentException("Conflicting layer RPC fragment");
            }
            return null;
        }
        message.parts[packet.fragment] = packet.payload.clone();
        message.received++;
        message.bytes += packet.payload.length;
        if (message.received != message.parts.length) return null;
        ByteArrayOutputStream output = new ByteArrayOutputStream(message.bytes);
        for (byte[] part : message.parts) output.write(part, 0, part.length);
        pending.remove(key);
        if (completed.size() >= maxCompleted) completed.remove(completed.keySet().iterator().next());
        completed.put(key, now);
        return output.toByteArray();
    }

    private void expire(long now) {
        Iterator<Map.Entry<Key, Pending>> messages = pending.entrySet().iterator();
        while (messages.hasNext()) {
            if (now - messages.next().getValue().created > timeoutMillis) messages.remove();
        }
        Iterator<Map.Entry<Key, Long>> recent = completed.entrySet().iterator();
        while (recent.hasNext()) {
            if (now - recent.next().getValue() > timeoutMillis) recent.remove();
        }
    }

    private static final class Pending {
        final byte[][] parts;
        final long created;
        int received, bytes;

        Pending(int fragments, long created) {
            parts = new byte[fragments][];
            this.created = created;
        }
    }

    private static final class Key {
        final String layer;
        final long message;

        Key(String layer, long message) {
            this.layer = layer;
            this.message = message;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key)) return false;
            Key key = (Key) other;
            return message == key.message && layer.equals(key.layer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(layer, message);
        }
    }
}
