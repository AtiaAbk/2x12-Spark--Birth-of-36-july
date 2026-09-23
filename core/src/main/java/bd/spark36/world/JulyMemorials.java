package bd.spark36.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Manages the 5 authentic historical memorials from the July 2024 Student Mass Uprising.
 * Provides 3D commemorative monuments with pulsing glowing beacons and historical archival records.
 */
public class JulyMemorials implements Disposable {

    public static class MemorialEntry {
        public final int id;
        public final String title;
        public final String date;
        public final String location;
        public final String description;
        public final Vector3 position;
        public boolean inspected = false;

        public MemorialEntry(int id, String title, String date, String location, String description, Vector3 position) {
            this.id = id;
            this.title = title;
            this.date = date;
            this.location = location;
            this.description = description;
            this.position = position;
        }
    }

    private final Array<MemorialEntry> entries = new Array<>();
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final Array<ModelInstance> beaconInstances = new Array<>();

    private float beaconPulseTime = 0f;

    public JulyMemorials() {
        initEntries();
        createMonumentMeshes();
    }

    private void initEntries() {
        entries.add(new MemorialEntry(
            1,
            "THE SPARK OF SOLIDARITY",
            "July 1, 2024",
            "Curzon Hall Central Arcade",
            "Here on the historic red-brick verandahs of Curzon Hall, Dhaka University science students joined arts and social science peers to launch peaceful demonstrations. Demanding reform in the civil service quota system, they declared: 'Merit over privilege. Equality for all citizens.'",
            new Vector3(0f, 0f, -16f)
        ));

        entries.add(new MemorialEntry(
            2,
            "THE OATH OF REFORM",
            "July 7, 2024",
            "Language Monument Plaza",
            "Gathering beneath the soaring arches of the Shaheed Minar, thousands of university students took a collective pledge of non-violent resistance. The iconic slogan resonated across the campus: 'Amar Shonar Bangla, Boishammo Mukto Bangla' (My Golden Bengal, Free of Discrimination).",
            new Vector3(-45f, 0f, 18f)
        ));

        entries.add(new MemorialEntry(
            3,
            "RESISTANCE AGAINST INJUSTICE",
            "July 15, 2024",
            "TSC Raju Anti-Terrorism Sculpture",
            "Named after martyr Moin Hossain Raju, this intersection became the heartbeat of student resistance. When peaceful protesters faced brutal crackdowns by ruling party cadres, students held hands, faced water cannons, and stood unshaken for their fundamental rights.",
            new Vector3(0f, 0f, 52f)
        ));

        entries.add(new MemorialEntry(
            4,
            "COURAGE OF FEMALE LEADERS",
            "July 16, 2024",
            "Women's Hall Quadrangle",
            "In the face of nighttime intimidation, female students from Shamsunnahar and Rokeya Halls broke university hall locks, marched into the dark streets, and led mass chanting. Their fearless leadership turned a student movement into an unstoppable nationwide revolution.",
            new Vector3(48f, 0f, 18f)
        ));

        entries.add(new MemorialEntry(
            5,
            "THE 36 JULY TRIUMPH",
            "August 5, 2024 (36 July)",
            "Central Campus Gateway",
            "After weeks of immense sacrifice and unwavering unity, millions marched to Dhaka on August 5. Protesters famously called this historic day '36 July' to signify that the calendar could not advance until justice was won. Authoritarian rule collapsed, heralding a new dawn of freedom.",
            new Vector3(28f, 0f, -10f)
        ));
    }

    private void createMonumentMeshes() {
        ModelBuilder mb = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal;

        // Monument Materials
        Material baseMat = new Material(ColorAttribute.createDiffuse(new Color(0.20f, 0.22f, 0.24f, 1f))); // Dark granite
        Material pillarMat = new Material(ColorAttribute.createDiffuse(new Color(0.72f, 0.28f, 0.22f, 1f))); // Red terracotta
        Material goldMat = new Material(ColorAttribute.createDiffuse(new Color(0.95f, 0.78f, 0.30f, 1f))); // Inscription gold
        Material beaconMat = new Material(ColorAttribute.createDiffuse(new Color(1f, 0.85f, 0.35f, 1f))); // Glowing beacon

        Model baseModel = mb.createBox(2.2f, 0.4f, 2.2f, baseMat, attr);
        Model pillarModel = mb.createBox(1.0f, 2.6f, 1.0f, pillarMat, attr);
        Model plaqueModel = mb.createBox(0.85f, 1.2f, 1.04f, goldMat, attr);
        Model beaconModel = mb.createSphere(0.55f, 0.55f, 0.55f, 16, 16, beaconMat, attr);

        models.add(baseModel);
        models.add(pillarModel);
        models.add(plaqueModel);
        models.add(beaconModel);

        for (MemorialEntry entry : entries) {
            ModelInstance baseInst = new ModelInstance(baseModel);
            baseInst.transform.setTranslation(entry.position.x, 0.2f, entry.position.z);
            instances.add(baseInst);

            ModelInstance pillarInst = new ModelInstance(pillarModel);
            pillarInst.transform.setTranslation(entry.position.x, 1.7f, entry.position.z);
            instances.add(pillarInst);

            ModelInstance plaqueInst = new ModelInstance(plaqueModel);
            plaqueInst.transform.setTranslation(entry.position.x, 1.8f, entry.position.z);
            instances.add(plaqueInst);

            ModelInstance beaconInst = new ModelInstance(beaconModel);
            beaconInst.transform.setTranslation(entry.position.x, 3.4f, entry.position.z);
            beaconInstances.add(beaconInst);
        }
    }

    public void update(float delta) {
        beaconPulseTime += delta;
        float pulse = (MathUtils.sin(beaconPulseTime * 3f) + 1f) * 0.5f;

        for (int i = 0; i < entries.size; i++) {
            MemorialEntry entry = entries.get(i);
            ModelInstance beacon = beaconInstances.get(i);

            // Gentle floating bob
            float hoverY = 3.4f + MathUtils.sin(beaconPulseTime * 2f + i) * 0.12f;
            beacon.transform.setTranslation(entry.position.x, hoverY, entry.position.z);

            // If inspected, beacon changes to bright green/gold
            ColorAttribute diff = (ColorAttribute) beacon.materials.first().get(ColorAttribute.Diffuse);
            if (entry.inspected) {
                diff.color.set(0.2f + 0.4f * pulse, 0.85f + 0.15f * pulse, 0.4f, 1f);
            } else {
                diff.color.set(1f, 0.70f + 0.30f * pulse, 0.25f, 1f);
            }
        }
    }

    public void render(ModelBatch batch, Environment env) {
        for (ModelInstance inst : instances) {
            batch.render(inst, env);
        }
        for (ModelInstance beacon : beaconInstances) {
            batch.render(beacon, env);
        }
    }

    /**
     * Finds the nearest memorial within the interaction distance (3.8m).
     */
    public MemorialEntry getNearbyMemorial(Vector3 playerPos) {
        for (MemorialEntry entry : entries) {
            if (playerPos.dst(entry.position) <= 3.8f) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Gets the nearest unvisited memorial, or closest memorial overall.
     */
    public MemorialEntry getNextObjective(Vector3 playerPos) {
        MemorialEntry closestUnvisited = null;
        float minUnvisitedDst = Float.MAX_VALUE;

        MemorialEntry closestOverall = null;
        float minOverallDst = Float.MAX_VALUE;

        for (MemorialEntry entry : entries) {
            float dst = playerPos.dst(entry.position);
            if (!entry.inspected && dst < minUnvisitedDst) {
                minUnvisitedDst = dst;
                closestUnvisited = entry;
            }
            if (dst < minOverallDst) {
                minOverallDst = dst;
                closestOverall = entry;
            }
        }

        return closestUnvisited != null ? closestUnvisited : closestOverall;
    }

    public int getInspectedCount() {
        int count = 0;
        for (MemorialEntry e : entries) {
            if (e.inspected) count++;
        }
        return count;
    }

    public int getTotalCount() {
        return entries.size;
    }

    public Array<MemorialEntry> getEntries() {
        return entries;
    }

    @Override
    public void dispose() {
        for (Model m : models) {
            m.dispose();
        }
        models.clear();
        instances.clear();
        beaconInstances.clear();
    }
}
