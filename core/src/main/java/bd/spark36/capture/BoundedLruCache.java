package bd.spark36.capture;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Least-recently-used cache bounded by entry count and total weight (e.g. texture bytes). Evicted
 * values are handed to the listener so GPU resources can be disposed. Not thread-safe.
 */
public final class BoundedLruCache<K, V> {

    public interface Weigher<V> { long weigh(V value); }
    public interface EvictionListener<V> { void evicted(V value); }

    private final int maxEntries;
    private final long maxWeight;
    private final Weigher<V> weigher;
    private final EvictionListener<V> listener;
    private final LinkedHashMap<K, V> map = new LinkedHashMap<>(16, 0.75f, true);
    private long totalWeight;

    public BoundedLruCache(int maxEntries, long maxWeight, Weigher<V> weigher, EvictionListener<V> listener) {
        this.maxEntries = Math.max(1, maxEntries);
        this.maxWeight = Math.max(1L, maxWeight);
        this.weigher = weigher;
        this.listener = listener;
    }

    public V get(K key) {
        return map.get(key);
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public void put(K key, V value) {
        V old = map.remove(key);
        if (old != null) {
            totalWeight -= weigher.weigh(old);
            if (old != value) listener.evicted(old);
        }
        map.put(key, value);
        totalWeight += weigher.weigh(value);
        trim();
    }

    private void trim() {
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        // Always keep the newest entry, even if it alone exceeds the weight budget.
        while (map.size() > 1 && (map.size() > maxEntries || totalWeight > maxWeight) && it.hasNext()) {
            V v = it.next().getValue();
            it.remove();
            totalWeight -= weigher.weigh(v);
            listener.evicted(v);
        }
    }

    public void clear() {
        for (V v : map.values()) listener.evicted(v);
        map.clear();
        totalWeight = 0L;
    }

    public int size() {
        return map.size();
    }

    public long totalWeight() {
        return totalWeight;
    }
}
