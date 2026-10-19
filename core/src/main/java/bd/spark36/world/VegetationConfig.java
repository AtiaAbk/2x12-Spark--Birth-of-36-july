package bd.spark36.world;

import com.badlogic.gdx.math.Vector3;

/**
 * Authoritative configuration parameters for campus vegetation, trees, foliage density,
 * leaf sizing, wind simulation, and level-of-detail (LOD) thresholds.
 */
public final class VegetationConfig {

    private VegetationConfig() {}

    // ── Species Identifiers ──────────────────────────────────────────────────
    public static final int SPECIES_RAIN_TREE     = 0; // Samanea saman (Spreading broadleaf umbrella dome)
    public static final int SPECIES_KRISHNACHURA  = 1; // Delonix regia (Scarlet flame tree with feathery fronds)
    public static final int SPECIES_ROYAL_PALM    = 2; // Roystonea regia (Columnar ringed palm with drooping fronds)
    public static final int SPECIES_WHITE_BLOSSOM = 3; // Peltophorum / Bokul (Dense white/cream blossom clusters)
    public static final int SPECIES_BANYAN        = 4; // Ficus benghalensis (Sprawling limbs and aerial prop roots)

    // ── Physically Grounded Leaf & Card Dimensions (World Scale: 1 unit = 1m) ──
    /** Realistic broadleaf card size (Rain tree): ~35 cm across. */
    public static final float RAIN_TREE_LEAF_CARD_SIZE = 0.35f;

    /** Bipinnate frond card size (Krishnachura compound leaf): ~32 cm across. */
    public static final float KRISHNA_LEAF_CARD_SIZE = 0.32f;

    /** Blossom spray card size (White blossom / cherry / magnolia): ~34 cm across. */
    public static final float WHITE_BLOSSOM_CARD_SIZE = 0.34f;

    /** Palm frond feather card width and length. */
    public static final float PALM_FROND_LENGTH = 4.8f;
    public static final float PALM_FROND_WIDTH  = 1.1f;

    // ── Wind Physics & Animation Parameters ──────────────────────────────────
    /** Primary prevailing wind direction (South-West monsoon breeze: X=0.82, Z=-0.57). */
    public static final Vector3 WIND_DIRECTION = new Vector3(0.82f, 0.0f, -0.57f).nor();

    /** Base wind velocity in m/s (gentle campus monsoon breeze). */
    public static final float WIND_BASE_SPEED = 1.25f;

    /** Primary broad canopy sway frequency in radians/sec (~0.35 Hz). */
    public static final float WIND_SWAY_FREQ_PRIMARY = 2.20f;

    /** Secondary cross-wind sway frequency in radians/sec (~0.85 Hz). */
    public static final float WIND_SWAY_FREQ_SECONDARY = 5.34f;

    /** High-frequency leaf and twig flutter frequency in radians/sec (~3.4 Hz). */
    public static final float WIND_FLUTTER_FREQ = 21.36f;

    /** Maximum trunk sway angle in degrees (anchored base, flexes at top). */
    public static final float MAX_TRUNK_SWAY_DEG = 1.15f;

    /** Maximum canopy foliage sway angle in degrees. */
    public static final float MAX_CANOPY_SWAY_DEG = 2.40f;

    /** High-frequency leaf card flutter amplitude in world units (~2.5 cm). */
    public static final float LEAF_FLUTTER_AMPLITUDE = 0.025f;

    // ── Level-of-Detail (LOD) & Visibility Distances ─────────────────────────
    /** Distance within which full high-detail tree geometry and leaf cards render. */
    public static final float LOD0_DISTANCE_HIGH   = 85.0f;
    public static final float LOD0_DISTANCE_MEDIUM = 65.0f;
    public static final float LOD0_DISTANCE_LOW    = 40.0f;

    /** Maximum distance for mid-distance simplified volumetric canopy rendering. */
    public static final float LOD1_DISTANCE_HIGH   = 160.0f;
    public static final float LOD1_DISTANCE_MEDIUM = 120.0f;
    public static final float LOD1_DISTANCE_LOW    = 80.0f;

    /** Hysteresis margin in meters to prevent rapid LOD toggling at transition borders. */
    public static final float LOD_HYSTERESIS = 5.0f;

    // ── Material & Alpha Shading Constants ───────────────────────────────────
    /** Alpha cutout threshold for crisp foliage cards without halos or fringing. */
    public static final float LEAF_ALPHA_CUTOFF = 0.35f;

    /** PBR leaf metallic response (organic foliage). */
    public static final float LEAF_METALLIC = 0.0f;

    /** PBR leaf surface roughness (waxy organic sheen). */
    public static final float LEAF_ROUGHNESS = 0.62f;

    /** Subtle subsurface daylight transmission factor (green/amber light scatter). */
    public static final float LEAF_TRANSMISSION_EMISSIVE = 0.022f;

    /** PBR tree bark roughness (rough weathered grooves). */
    public static final float BARK_ROUGHNESS = 0.88f;
}
