// ei file ta campus er matir texture, ghonotto ar height configure kore
package bd.spark36.world;

public final class GroundConfig {

    private GroundConfig() {}

    public static final float CAMPUS_MIN_X = -140.0f;
    public static final float CAMPUS_MAX_X =  140.0f;
    public static final float CAMPUS_MIN_Z = -110.0f;
    public static final float CAMPUS_MAX_Z =  150.0f;
    public static final float CAMPUS_WIDTH  = CAMPUS_MAX_X - CAMPUS_MIN_X;
    public static final float CAMPUS_LENGTH = CAMPUS_MAX_Z - CAMPUS_MIN_Z;
    public static final float CAMPUS_BASE_Y = 0.0f;

    public static final float POND_MIN_X = -8.8f;
    public static final float POND_MAX_X =  8.8f;
    public static final float POND_MIN_Z = -5.8f;
    public static final float POND_MAX_Z = 17.8f;
    public static final float POND_FLOOR_Y = -0.45f;
    public static final float POND_WATER_Y =  0.05f;

    public static final float GRASS_UV_SCALE   = 0.25f;
    public static final float BRICK_UV_SCALE   = 0.55f;
    public static final float SOIL_UV_SCALE    = 0.33f;
    public static final float DIRT_UV_SCALE    = 0.33f;
    public static final float ASPHALT_UV_SCALE = 0.166f;

    public static final float GRASS_METALLIC = 0.0f;
    public static final float GRASS_ROUGHNESS = 0.82f;
    public static final float SOIL_METALLIC = 0.0f;
    public static final float SOIL_ROUGHNESS = 0.88f;
    public static final float DIRT_METALLIC = 0.0f;
    public static final float DIRT_ROUGHNESS = 0.90f;
    public static final float ASPHALT_METALLIC = 0.0f;
    public static final float ASPHALT_ROUGHNESS = 0.85f;
    public static final float BRICK_METALLIC = 0.0f;
    public static final float BRICK_ROUGHNESS = 0.72f;
    public static final float CURB_METALLIC = 0.0f;
    public static final float CURB_ROUGHNESS = 0.80f;

    public static final float TRANSITION_Y_OFFSET = 0.003f;
    public static final float TREE_SOIL_RADIUS_RAIN_TREE = 2.8f;
    public static final float TREE_SOIL_RADIUS_KRISHNA   = 2.2f;
    public static final float TREE_SOIL_RADIUS_WHITE_BLOSSOM = 2.4f;
    public static final float TREE_SOIL_RADIUS_PALM      = 1.6f;
    public static final float PATH_SHOULDER_WIDTH        = 0.55f;
    public static final float POND_VERGE_WIDTH           = 0.75f;
}
