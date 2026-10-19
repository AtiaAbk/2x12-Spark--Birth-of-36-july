package bd.spark36.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TreeVertexCountTest {
    @Test
    public void testTreeVertexCounts() {
        ModelBuilder limbsB = new ModelBuilder();
        ModelBuilder coreB = new ModelBuilder();
        ModelBuilder cardsB = new ModelBuilder();
        long attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
        Material dummy = new Material();

        limbsB.begin(); coreB.begin(); cardsB.begin();
        TreeGeometry.rainTree(
            limbsB.part("limbs", GL20.GL_TRIANGLES, attr, dummy),
            coreB.part("core", GL20.GL_TRIANGLES, attr, dummy),
            cardsB.part("leaves", GL20.GL_TRIANGLES, attr, dummy),
            0f, 0f, 1.0f, 42f
        );
        Model limbsM = limbsB.end();
        Model coreM = coreB.end();
        Model cardsM = cardsB.end();

        int limbsVerts = limbsM.meshes.first().getNumVertices();
        int coreVerts = coreM.meshes.first().getNumVertices();
        int cardsVerts = cardsM.meshes.first().getNumVertices();

        System.out.println("Rain Tree - Limbs: " + limbsVerts + ", Core: " + coreVerts + ", Cards: " + cardsVerts);
        assertTrue(cardsVerts < 32767, "Cards vertex count must be under 32767 limit");
    }
}
