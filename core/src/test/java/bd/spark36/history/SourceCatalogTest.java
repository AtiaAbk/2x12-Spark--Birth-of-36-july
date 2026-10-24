package bd.spark36.history;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SourceCatalogTest {

    /** The shipped register; Gradle runs core tests with core/ as the working directory. */
    static File shippedFile() {
        File f = new File("../assets/" + SourceCatalog.ASSET_PATH);
        if (!f.isFile()) f = new File("assets/" + SourceCatalog.ASSET_PATH);
        assertTrue(f.isFile(), "historical_sources.json not found from " + new File(".").getAbsolutePath());
        return f;
    }

    static SourceCatalog shipped() throws IOException {
        return SourceCatalog.load(shippedFile());
    }

    @Test
    void parsesInlineJson() {
        SourceCatalog c = SourceCatalog.parse("[{\"id\":\"X-1\",\"label\":\"VERIFIED\",\"title\":\"T\","
            + "\"publisher\":\"P\",\"date\":\"2024-08-05\",\"url\":\"https://e.org\"},"
            + "{\"id\":\"X-2\",\"label\":\"FICTIONAL GAMEPLAY\",\"title\":\"No external source (game design)\","
            + "\"publisher\":\"Spark: Birth of 36 July\",\"url\":\"\"}, {\"claim\":\"no id, skipped\"}]");
        assertEquals(2, c.size());
        SourceCatalog.Source a = c.find("X-1");
        assertEquals("Source: P, T, 2024-08-05", a.sourceLine());
        assertFalse(a.isGameDesign());
        SourceCatalog.Source b = c.find("X-2");
        assertTrue(b.isGameDesign());
        assertEquals("", b.date);
        assertTrue(b.sourceLine().startsWith("Source: none, game design"));
    }

    @Test
    void rejectsNonArray() {
        assertThrows(IllegalArgumentException.class, () -> SourceCatalog.parse("{\"id\":\"x\"}"));
        assertThrows(IllegalArgumentException.class, () -> SourceCatalog.parse("not json ["));
    }

    @Test
    void shippedRegisterHas38CompleteEntries() throws IOException {
        SourceCatalog c = shipped();
        assertEquals(38, c.size());
        List<String> labels = Arrays.asList(SourceCatalog.LABELS);
        for (SourceCatalog.Source s : c.all()) {
            assertFalse(s.label.isEmpty(), s.id + " has no label");
            assertFalse(s.title.isEmpty(), s.id + " has no title");
            assertFalse(s.publisher.isEmpty(), s.id + " has no publisher");
            assertTrue(labels.contains(s.label), s.id + " has unknown label " + s.label);
            if (!s.isGameDesign()) assertFalse(s.url.isEmpty(), s.id + " is external but has no URL");
        }
    }

    @Test
    void lookupByIdAndMissingId() throws IOException {
        SourceCatalog c = shipped();
        SourceCatalog.Source s = c.find("L1-09");
        assertNotNull(s);
        assertEquals(SourceCatalog.VERIFIED, s.label);
        assertTrue(s.publisher.contains("OHCHR"));
        assertNull(c.find("L9-99"));
        assertNull(c.find(null));
        assertNull(SourceCatalog.empty().find("L1-01"));
    }

    @Test
    void groupedByLabelKeepsSchemeOrderAndCoversAll() throws IOException {
        SourceCatalog c = shipped();
        Map<String, List<SourceCatalog.Source>> g = c.groupedByLabel();
        assertEquals(Arrays.asList(SourceCatalog.LABELS), List.copyOf(g.keySet()));
        int n = 0;
        for (List<SourceCatalog.Source> l : g.values()) n += l.size();
        assertEquals(c.size(), n);
    }

    @Test
    void everyMemorialMapsToExistingSources() throws IOException {
        SourceCatalog c = shipped();
        int[] ids = {1, 2, 3, 4, 5, SourceCatalog.BOUNDARY_STONE_ID};
        for (int id : ids) {
            String[] mapped = SourceCatalog.idsForMemorial(id);
            assertTrue(mapped.length > 0, "memorial " + id + " has no mapping");
            for (String sid : mapped) assertNotNull(c.find(sid), "memorial " + id + " maps to missing " + sid);
            assertEquals(mapped.length, c.sourcesForMemorial(id).size());
        }
        assertEquals(0, SourceCatalog.idsForMemorial(42).length);
        // The Hakim Chattar scene is a game reconstruction: its card must say so.
        assertEquals(SourceCatalog.FICTIONAL, c.find(SourceCatalog.idsForMemorial(4)[1]).label);
    }
}
