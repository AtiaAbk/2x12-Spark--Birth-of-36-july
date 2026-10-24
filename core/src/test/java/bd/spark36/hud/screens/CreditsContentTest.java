package bd.spark36.hud.screens;

import static org.junit.jupiter.api.Assertions.*;

import bd.spark36.history.SourceCatalog;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/** Headless check of the Credits &amp; Sources text (the screen itself needs GL). */
class CreditsContentTest {

    private static String joined(List<CreditsContent.Line> lines) {
        return lines.stream().map(l -> l.text).collect(Collectors.joining("\n"));
    }

    private static SourceCatalog shipped() throws IOException {
        File f = new File("../assets/" + SourceCatalog.ASSET_PATH);
        if (!f.isFile()) f = new File("assets/" + SourceCatalog.ASSET_PATH);
        return SourceCatalog.load(f);
    }

    @Test
    void requiredSectionsAndStatementsPresent() throws IOException {
        List<CreditsContent.Line> lines = CreditsContent.build(shipped());
        String all = joined(lines);
        assertTrue(all.contains("Atia Sanjida  •  ICE, BAUET  •  Student Code: 2x12"), "author line as on main menu");
        assertTrue(all.contains("Noto Sans Bengali") && all.contains("Liberation Sans"));
        assertTrue(all.contains("SIL Open Font License 1.1"));
        assertTrue(all.contains("libGDX 1.14.2") && all.contains("Apache License 2.0"));
        assertTrue(all.contains("LWJGL 3.4.1") && all.contains("BSD 3-Clause"));
        assertTrue(all.contains("gdx-gltf 2.2.1"));
        assertTrue(all.contains("generated procedurally in code"));
        assertTrue(all.contains("dramatised work") && all.contains("not a historical record"));
        assertTrue(all.contains("In memory of"));
        assertFalse(all.toLowerCase().contains("fair use"));
    }

    @Test
    void everySourceAppearsUnderItsLabel() throws IOException {
        SourceCatalog c = shipped();
        List<CreditsContent.Line> lines = CreditsContent.build(c);
        String all = joined(lines);
        long labelLines = lines.stream().filter(l -> l.kind == CreditsContent.Kind.LABEL).count();
        assertEquals(SourceCatalog.LABELS.length, labelLines);
        for (SourceCatalog.Source s : c.all()) {
            assertTrue(all.contains(s.id), "claim id missing: " + s.id);
            assertTrue(all.contains(s.title), "title missing: " + s.id);
            if (!s.url.isEmpty()) assertTrue(all.contains(s.url), "url missing: " + s.id);
            if (!s.date.isEmpty()) assertTrue(all.contains(s.date), "date missing: " + s.id);
        }
        // Label order follows the scheme
        int last = -1;
        for (String label : SourceCatalog.LABELS) {
            int at = all.indexOf(label + "  (");
            assertTrue(at > last, "label out of order: " + label);
            last = at;
        }
        assertTrue(all.indexOf(CreditsContent.SOURCES_HEADER) < all.indexOf(SourceCatalog.VERIFIED + "  ("));
    }

    @Test
    void emptyCatalogStillBuilds() {
        String all = joined(CreditsContent.build(SourceCatalog.empty()));
        assertTrue(all.contains("could not be loaded"));
        assertTrue(all.contains(CreditsContent.AUTHOR_LINE));
    }

    @Test
    void bengaliDetection() {
        assertTrue(CreditsScreen.containsBengali("‘পানি’"));
        assertFalse(CreditsScreen.containsBengali("Timeline"));
    }
}
