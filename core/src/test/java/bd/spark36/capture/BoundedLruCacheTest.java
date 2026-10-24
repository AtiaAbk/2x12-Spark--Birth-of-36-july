package bd.spark36.capture;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoundedLruCacheTest {

    private final List<String> evicted = new ArrayList<>();

    private BoundedLruCache<String, String> cache(int maxEntries, long maxWeight) {
        return new BoundedLruCache<>(maxEntries, maxWeight, String::length, evicted::add);
    }

    @Test
    void evictsLeastRecentlyUsedBeyondCount() {
        BoundedLruCache<String, String> c = cache(2, 1000);
        c.put("a", "A");
        c.put("b", "B");
        c.get("a"); // touch a, so b is now the eldest
        c.put("c", "C");
        assertEquals(List.of("B"), evicted);
        assertTrue(c.containsKey("a"));
        assertTrue(c.containsKey("c"));
        assertEquals(2, c.size());
    }

    @Test
    void evictsBeyondWeightBudget() {
        BoundedLruCache<String, String> c = cache(100, 10);
        c.put("a", "aaaa");
        c.put("b", "bbbb");
        c.put("c", "cccc"); // 12 > 10
        assertEquals(List.of("aaaa"), evicted);
        assertEquals(8, c.totalWeight());
    }

    @Test
    void replacingValueDisposesOldOne() {
        BoundedLruCache<String, String> c = cache(5, 100);
        c.put("a", "old");
        c.put("a", "new");
        assertEquals(List.of("old"), evicted);
        assertEquals("new", c.get("a"));
        assertEquals(3, c.totalWeight());
    }

    @Test
    void clearDisposesEverything() {
        BoundedLruCache<String, String> c = cache(5, 100);
        c.put("a", "A");
        c.put("b", "B");
        c.clear();
        assertEquals(2, evicted.size());
        assertEquals(0, c.size());
        assertEquals(0, c.totalWeight());
    }

    @Test
    void folderOpenerPicksPlatformCommand() {
        File dir = new File("shots").getAbsoluteFile();
        assertEquals(List.of("explorer.exe", dir.getAbsolutePath()), FolderOpener.command("Windows 11", dir));
        assertEquals(List.of("open", dir.getAbsolutePath()), FolderOpener.command("Mac OS X", dir));
        assertEquals(List.of("xdg-open", dir.getAbsolutePath()), FolderOpener.command("Linux", dir));
        assertNull(FolderOpener.command("Plan9", dir));
    }
}
