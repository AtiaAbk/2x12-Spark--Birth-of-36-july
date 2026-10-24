package bd.spark36.capture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GalleryIndexTest {

    private static File png(File dir, String name, long modified) throws IOException {
        File f = new File(dir, name);
        Files.write(f.toPath(), new byte[]{1, 2, 3});
        assertTrue(f.setLastModified(modified));
        return f;
    }

    private static List<String> names(List<File> files) {
        List<String> out = new ArrayList<>();
        for (File f : files) out.add(f.getName());
        return out;
    }

    @Test
    void listsPngsNewestFirst(@TempDir Path tmp) throws IOException {
        File dir = tmp.toFile();
        png(dir, "old.png", 1_000_000_000_000L);
        png(dir, "newest.png", 1_000_000_300_000L);
        png(dir, "middle.PNG", 1_000_000_200_000L);
        assertEquals(List.of("newest.png", "middle.PNG", "old.png"), names(GalleryIndex.list(dir)));
    }

    @Test
    void equalTimesFallBackToNameDescending(@TempDir Path tmp) throws IOException {
        File dir = tmp.toFile();
        long t = 1_000_000_000_000L;
        png(dir, "Spark36_20260716-102030-456.png", t);
        png(dir, "Spark36_20260716-102030-456_2.png", t);
        png(dir, "Spark36_20260716-102030-457.png", t);
        assertEquals(List.of("Spark36_20260716-102030-457.png", "Spark36_20260716-102030-456_2.png",
            "Spark36_20260716-102030-456.png"), names(GalleryIndex.list(dir)));
    }

    @Test
    void skipsNonPngEmptyFilesTempFilesAndSubfolders(@TempDir Path tmp) throws IOException {
        File dir = tmp.toFile();
        png(dir, "keep.png", 1_000_000_000_000L);
        png(dir, "notes.txt", 1_000_000_000_000L);
        png(dir, "keep2.png.part", 1_000_000_000_000L);
        Files.createFile(new File(dir, "placeholder.png").toPath()); // zero bytes: still being written
        File sub = new File(dir, "00_baseline");
        assertTrue(sub.mkdir());
        png(sub, "spawn.png", 1_000_000_000_000L);
        assertEquals(List.of("keep.png"), names(GalleryIndex.list(dir)));
    }

    @Test
    void missingFolderGivesEmptyList(@TempDir Path tmp) {
        assertTrue(GalleryIndex.list(new File(tmp.toFile(), "nope")).isEmpty());
        assertTrue(GalleryIndex.list(null).isEmpty());
    }
}
