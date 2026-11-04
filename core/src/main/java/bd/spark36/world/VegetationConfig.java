// ei file ta gachpala, batash ar pata er size config kore
package bd.spark36.world;

import com.badlogic.gdx.math.Vector3;

public final class VegetationConfig {

    private VegetationConfig() {}

    public static final int SPECIES_RAIN_TREE     = 0;
    public static final int SPECIES_KRISHNACHURA  = 1;
    public static final int SPECIES_ROYAL_PALM    = 2;
    public static final int SPECIES_WHITE_BLOSSOM = 3;
    public static final int SPECIES_BANYAN        = 4;

    public static final float RAIN_TREE_LEAF_CARD_SIZE = 0.42f;

    public static final float KRISHNA_LEAF_CARD_SIZE = 0.42f;

    public static final float WHITE_BLOSSOM_CARD_SIZE = 0.38f;

    public static final float PALM_FROND_LENGTH = 4.8f;
    public static final float PALM_FROND_WIDTH  = 1.1f;

    public static final Vector3 WIND_DIRECTION = new Vector3(0.82f, 0.0f, -0.57f).nor();

    public static final float WIND_BASE_SPEED = 1.25f;

    public static final float WIND_SWAY_FREQ_PRIMARY = 2.20f;

    public static final float WIND_SWAY_FREQ_SECONDARY = 5.34f;

    public static final float WIND_FLUTTER_FREQ = 21.36f;

    public static final float MAX_TRUNK_SWAY_DEG = 1.15f;

    public static final float MAX_CANOPY_SWAY_DEG = 2.40f;

    public static final float LEAF_FLUTTER_AMPLITUDE = 0.025f;

    public static final float LOD0_DISTANCE_HIGH   = 85.0f;
    public static final float LOD0_DISTANCE_MEDIUM = 65.0f;
    public static final float LOD0_DISTANCE_LOW    = 40.0f;

    public static final float LOD1_DISTANCE_HIGH   = 160.0f;
    public static final float LOD1_DISTANCE_MEDIUM = 120.0f;
    public static final float LOD1_DISTANCE_LOW    = 80.0f;

    public static final float LOD_HYSTERESIS = 5.0f;

    public static final float LEAF_ALPHA_CUTOFF = 0.35f;

    public static final float LEAF_METALLIC = 0.0f;

    public static final float LEAF_ROUGHNESS = 0.62f;

    public static final float LEAF_TRANSMISSION_EMISSIVE = 0.022f;

    public static final float BARK_ROUGHNESS = 0.88f;
}
