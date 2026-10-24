package bd.spark36.hud.screens;

import bd.spark36.history.SourceCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Text of the Credits &amp; Sources screen as a flat list of lines. Pure logic (no GL), so the
 * content is unit-tested and the screen only has to lay the lines out and scroll them.
 *
 * <p>Every statement here must be checkable in the repository: the author line is the one on the
 * main menu, licences come from {@code assets/fonts/LICENSE-*} and the Gradle build files, and the
 * source list is read from {@code assets/data/historical_sources.json}. Do not add names, roles or
 * quotes that are not in the project.</p>
 */
public final class CreditsContent {

    public enum Kind { TITLE, HEADER, LABEL, TEXT, SMALL, LINK, GAP }

    public static final class Line {
        public final Kind kind;
        public final String text;
        /** Evidence label for {@link Kind#LABEL} lines, else null. */
        public final String label;

        Line(Kind kind, String text, String label) {
            this.kind = kind;
            this.text = text;
            this.label = label;
        }

        @Override
        public String toString() {
            return kind + ": " + text;
        }
    }

    /** Header text of the source section; the screenshot harness scrolls to it. */
    public static final String SOURCES_HEADER = "HISTORICAL SOURCES";

    /** Same author line as the main menu footer. */
    public static final String AUTHOR_LINE = "Atia Sanjida  •  ICE, BAUET  •  Student Code: 2x12";

    private CreditsContent() {}

    /** One-sentence meaning of each evidence label (from DECISIONS.md / RESEARCH_SOURCES.md scheme). */
    public static String labelMeaning(String label) {
        if (SourceCatalog.VERIFIED.equals(label)) return "Stated by a primary or authoritative source.";
        if (SourceCatalog.CORROBORATED.equals(label)) return "Supported by sources, but details (place, numbers, wording) are incomplete.";
        if (SourceCatalog.DISPUTED.equals(label)) return "Sources disagree or no source confirms it. Not presented as fact in the game.";
        if (SourceCatalog.FICTIONAL.equals(label)) return "Invented for gameplay. Not a historical claim.";
        if (SourceCatalog.RECONSTRUCTION.equals(label)) return "Approximate recreation of places, objects or layouts.";
        return "";
    }

    public static List<Line> build(SourceCatalog catalog) {
        List<Line> out = new ArrayList<>();
        add(out, Kind.SMALL, "2x12: SPARK  •  Birth of 36 July");
        gap(out);

        add(out, Kind.HEADER, "IN MEMORY");
        add(out, Kind.TEXT, "In memory of everyone who lost their life or was injured during the July and August 2024 "
            + "mass uprising in Bangladesh, and with respect for their families.");
        gap(out);

        add(out, Kind.HEADER, "ABOUT THIS GAME");
        add(out, Kind.TEXT, "This is a dramatised work. The protagonist and every dialogue character are fictional. "
            + "It is not a historical record.");
        add(out, Kind.TEXT, "Historical statements in the game are tied to the sources listed below, each with an evidence "
            + "label. Scenes without a source are labelled FICTIONAL GAMEPLAY or RECONSTRUCTION. Campus layout and "
            + "building sizes are approximate. Real people who died are remembered, never playable or scored.");
        gap(out);

        add(out, Kind.HEADER, "PROJECT");
        add(out, Kind.TEXT, AUTHOR_LINE);
        gap(out);

        add(out, Kind.HEADER, "FONTS");
        add(out, Kind.TEXT, "Noto Sans Bengali  —  Copyright 2022 The Noto Project Authors. SIL Open Font License 1.1.");
        add(out, Kind.LINK, "https://github.com/notofonts/bengali   (licence: assets/fonts/LICENSE-NotoSansBengali.txt)");
        add(out, Kind.TEXT, "Liberation Sans  —  Copyright (c) 2012 Red Hat, Inc.; digitized data copyright (c) 2010 "
            + "Google Corporation. SIL Open Font License 1.1.");
        add(out, Kind.LINK, "https://github.com/liberationfonts   (licence: assets/fonts/LICENSE-LiberationSans.txt)");
        gap(out);

        add(out, Kind.HEADER, "LIBRARIES");
        add(out, Kind.TEXT, "libGDX 1.14.2, including gdx-freetype and the LWJGL 3 backend  —  Apache License 2.0.");
        add(out, Kind.LINK, "https://libgdx.com");
        add(out, Kind.TEXT, "LWJGL 3.4.1  —  BSD 3-Clause License.");
        add(out, Kind.LINK, "https://www.lwjgl.org");
        add(out, Kind.TEXT, "gdx-gltf 2.2.1 (PBR shaders)  —  Apache License 2.0.");
        add(out, Kind.LINK, "https://github.com/mgsx-dev/gdx-gltf");
        add(out, Kind.TEXT, "FreeType, bundled in the gdx-freetype natives  —  FreeType License. Portions of this "
            + "software are copyright The FreeType Project (www.freetype.org). All rights reserved.");
        gap(out);

        add(out, Kind.HEADER, "ART AND AUDIO");
        add(out, Kind.TEXT, "All 3D geometry, textures and sound effects are generated procedurally in code at run time. "
            + "No third-party models, texture packs or sound recordings are used. The shaders are original to this project.");
        add(out, Kind.TEXT, "Two illustrated images ship with the game: the campus layout map and the finish-screen "
            + "background. Their origin is not documented and is recorded as unknown in the asset register.");
        gap(out);

        Map<String, List<SourceCatalog.Source>> groups = catalog.groupedByLabel();
        add(out, Kind.HEADER, SOURCES_HEADER);
        add(out, Kind.SMALL, catalog.size() + " claims, loaded from assets/data/historical_sources.json "
            + "(mirror of docs/RESEARCH_SOURCES.md). Rows sharing one source are listed once with their claim ids.");
        if (catalog.size() == 0) {
            add(out, Kind.TEXT, "The source register could not be loaded.");
        }
        for (Map.Entry<String, List<SourceCatalog.Source>> g : groups.entrySet()) {
            gap(out);
            out.add(new Line(Kind.LABEL, g.getKey() + "  (" + g.getValue().size() + ")", g.getKey()));
            String meaning = labelMeaning(g.getKey());
            if (!meaning.isEmpty()) add(out, Kind.SMALL, meaning);
            for (List<SourceCatalog.Source> same : bySameSource(g.getValue())) {
                SourceCatalog.Source s = same.get(0);
                StringBuilder ids = new StringBuilder();
                for (SourceCatalog.Source x : same) {
                    if (ids.length() > 0) ids.append(", ");
                    ids.append(x.id);
                }
                add(out, Kind.TEXT, s.title);
                if (s.isGameDesign()) {
                    add(out, Kind.SMALL, "Claims " + ids + ": no external source.");
                    for (SourceCatalog.Source x : same) add(out, Kind.SMALL, "   " + x.id + "  " + x.claim);
                } else {
                    String meta = s.publisher + (s.date.isEmpty() ? "  •  undated" : "  •  " + s.date)
                        + "  •  claims " + ids;
                    add(out, Kind.SMALL, meta);
                    if (!s.url.isEmpty()) add(out, Kind.LINK, s.url);
                }
            }
        }
        return Collections.unmodifiableList(out);
    }

    /** Rows of one label grouped by identical (title, publisher, date, url), keeping first-seen order. */
    static List<List<SourceCatalog.Source>> bySameSource(List<SourceCatalog.Source> rows) {
        Map<String, List<SourceCatalog.Source>> m = new LinkedHashMap<>();
        for (SourceCatalog.Source s : rows) {
            String key = s.title + "\u0000" + s.publisher + "\u0000" + s.date + "\u0000" + s.url;
            m.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(m.values());
    }

    private static void add(List<Line> out, Kind k, String text) {
        out.add(new Line(k, text, null));
    }

    private static void gap(List<Line> out) {
        out.add(new Line(Kind.GAP, "", null));
    }
}
