// ei file ta tola screenshot gular list ar metadata track kore
package bd.spark36.capture;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class GalleryIndex {

    private GalleryIndex() {}

    static final Comparator<File> NEWEST_FIRST = new Comparator<File>() {
        @Override
        public int compare(File a, File b) {
            int byTime = Long.compare(b.lastModified(), a.lastModified());
            return byTime != 0 ? byTime : b.getName().compareTo(a.getName());
        }
    };

    public static List<File> list(File dir) {
        if (dir == null) return Collections.emptyList();
        File[] files = dir.listFiles();
        if (files == null) return Collections.emptyList();
        List<File> out = new ArrayList<>();
        for (File f : Arrays.asList(files)) {

            if (f.isFile() && f.getName().toLowerCase().endsWith(".png") && f.length() > 0L) {
                out.add(f);
            }
        }
        out.sort(NEWEST_FIRST);
        return out;
    }
}
