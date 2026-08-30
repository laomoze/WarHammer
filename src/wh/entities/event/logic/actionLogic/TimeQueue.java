package wh.entities.event.logic.actionLogic;

import arc.struct.Queue;

public class TimeQueue<T extends TimeQueue.Timed> {
    public Queue<T> queue = new Queue<>();
    public T current;

    public void add(T item) {
        if (item != null) queue.addFirst(item);
    }

    public void addAll(T... items) {
        for (T item : items) add(item);
    }

    public void update() {
        if (current == null && !queue.isEmpty()) {
            current = queue.removeLast();
            current.begin();
        }
        if (current != null && current.complete()) {
            current.end();
            current = null;
        }
        if (current != null && !current.complete()) current.update();
    }

    public void clear() {
        if (current != null) {
            current.end();
            current = null;
        }
        queue.clear();
    }

    public void skipCurrent() {
        if (current != null) {
            current.skip();
            current.end();
            current = null;
        }
    }

    public boolean complete() {
        return current == null && queue.isEmpty();
    }

    public interface Timed {
        void begin();

        void update();

        void end();

        boolean complete();

        default void skip() {
        }
    }
}
