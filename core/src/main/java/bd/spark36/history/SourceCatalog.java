package bd.spark36.history;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Historical source register loaded from {@code assets/data/historical_sources.json}, the
 * machine-readable mirror of {@code docs/RESEARCH_SOURCES.md}. The credits screen and the
 * memorial context cards both read from here, so the game and the docs cannot drift apart.
 *
 * <p>Parsing uses libGDX's {@link JsonReader} only, so it runs in plain unit tests without a
 * libGDX application. {@link #get()} loads the shipped asset once at runtime.</p>
 */
public final class SourceCatalog {

    /** Path of the register inside the assets folder. */
    public static final String ASSET_PATH = "data/historical_sources.json";

    /** The five evidence labels, in the order the credits screen lists them. */
    public static final String VERIFIED = "VERIFIED";
    public static final String CORROBORATED = "CORROBORATED BUT INCOMPLETE";
    public static final String DISPUTED = "DISPUTED / UNCONFIRMED";
    public static final String FICTIONAL = "FICTIONAL GAMEPLAY";
    public static final String RECONSTRUCTION = "RECONSTRUCTION";
    public static final String[] LABELS = {VERIFIED, CORROBORATED, DISPUTED, FICTIONAL, RECONSTRUCTION};

    /** Publisher used in the register for entries that have no external source (game design). */
    public static final String GAME_PUBLISHER = "Spark: Birth of 36 July";

    /** One row of the register. Fields are never null (missing ones become ""). */
    public static final class Source {
        public final String id, claim, label, title, publisher, date, url, supports;

        public Source(String id, String claim, String label, String title, String publisher,
                      String date, String url, String supports) {
            this.id = nz(id);
            this.claim = nz(claim);
            this.label = nz(label);
            this.title = nz(title);
            this.publisher = nz(publisher);
            this.date = nz(date);
            this.url = nz(url);
            this.supports = nz(supports);
        }

        /** True when the row has no external source: the claim is the game's own design. */
        public boolean isGameDesign() {
            return url.isEmpty() && GAME_PUBLISHER.equals(publisher);
        }

        /** "Source: publisher, title, date" (date omitted when unknown). */
        public String sourceLine() {
            if (isGameDesign()) return "Source: none, game design (" + title + ")";
            StringBuilder sb = new StringBuilder("Source: ").append(publisher);
            if (!title.isEmpty()) sb.append(", ").append(title);
            if (!date.isEmpty()) sb.append(", ").append(date);
            return sb.toString();
        }
    }

    private final Map<String, Source> byId = new LinkedHashMap<>();

    private SourceCatalog() {
    }

    /** An empty catalog, used when the asset is missing or unreadable. */
    public static SourceCatalog empty() {
        return new SourceCatalog();
    }

    /**
     * Parses the register. The top level must be an array of objects with at least an {@code id}.
     *
     * @throws IllegalArgumentException when the text is not such an array
     */
    public static SourceCatalog parse(String json) {
        JsonValue root;
        try {
            root = new JsonReader().parse(json);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("historical_sources.json is not valid JSON: " + e.getMessage(), e);
        }
        if (root == null || !root.isArray()) {
            throw new IllegalArgumentException("historical_sources.json must be a JSON array");
        }
        SourceCatalog c = new SourceCatalog();
        for (JsonValue v = root.child; v != null; v = v.next) {
            if (!v.isObject()) continue;
            String id = v.getString("id", "").trim();
            if (id.isEmpty()) continue;
            c.byId.put(id, new Source(id,
                v.getString("claim", ""), v.getString("label", "").trim(), v.getString("title", ""),
                v.getString("publisher", ""), v.getString("date", ""), v.getString("url", ""),
                v.getString("supports", "")));
        }
        return c;
    }

    /** Reads and parses a register file from disk (UTF-8). */
    public static SourceCatalog load(File file) throws IOException {
        return parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
    }

    private static SourceCatalog shared;

    /**
     * The shipped register, loaded once through {@code Gdx.files.internal}. Returns an empty
     * catalog (and logs) if the asset cannot be read, so a broken file never crashes the game.
     */
    public static synchronized SourceCatalog get() {
        if (shared != null) return shared;
        try {
            com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.internal(ASSET_PATH);
            shared = parse(fh.readString("UTF-8"));
        } catch (RuntimeException e) {
            if (com.badlogic.gdx.Gdx.app != null) {
                com.badlogic.gdx.Gdx.app.error("SourceCatalog", "Could not load " + ASSET_PATH + ": " + e.getMessage());
            }
            shared = empty();
        }
        return shared;
    }

    public Source find(String id) {
        return id == null ? null : byId.get(id);
    }

    public int size() {
        return byId.size();
    }

    public List<Source> all() {
        return Collections.unmodifiableList(new ArrayList<>(byId.values()));
    }

    /**
     * Sources grouped by evidence label, in {@link #LABELS} order, keeping register order within a
     * group. Any unknown label is appended after the five known ones.
     */
    public Map<String, List<Source>> groupedByLabel() {
        Map<String, List<Source>> out = new LinkedHashMap<>();
        for (String l : LABELS) out.put(l, new ArrayList<>());
        for (Source s : byId.values()) {
            out.computeIfAbsent(s.label, k -> new ArrayList<>()).add(s);
        }
        out.values().removeIf(List::isEmpty);
        return out;
    }

    // ── Memorial / archive modal mapping ─────────────────────────────────

    /** Id of the HUD's 1921 boundary-stone entry ({@code WhereWindsMeetHUD.boundaryStoneEntry}). */
    public static final int BOUNDARY_STONE_ID = 99;

    /**
     * Register ids shown on the context card of a memorial / archive modal, most specific claim
     * first. Ids follow the "Where it is used" column of {@code docs/RESEARCH_SOURCES.md}.
     * Returns an empty array for entries without a mapping.
     */
    public static String[] idsForMemorial(int memorialId) {
        switch (memorialId) {
            case 1: return new String[] {"L1-02", "L1-01"};   // Curzon Hall: early-July protests; 5 June verdict
            case 2: return new String[] {"L1-04", "L1-03"};   // Aparajeyo Bangla; Bangla Blockade
            case 3: return new String[] {"L1-06", "L1-08"};   // Raju sculpture; 15 July attacks
            case 4: return new String[] {"L1-09", "L1-10"};   // internet shutdown; Hakim Chattar scene (fictional)
            case 5: return new String[] {"L6-05", "L6-03"};   // "36 July" name; 5 August resignation
            case BOUNDARY_STONE_ID: return new String[] {"L1-11", "L1-12"}; // DU founded 1921; the stone itself
            default: return new String[0];
        }
    }

    /** The register rows for a memorial, skipping ids that are not in this catalog. */
    public List<Source> sourcesForMemorial(int memorialId) {
        List<Source> out = new ArrayList<>();
        for (String id : idsForMemorial(memorialId)) {
            Source s = find(id);
            if (s != null) out.add(s);
        }
        return out;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
