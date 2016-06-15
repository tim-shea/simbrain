package org.simbrain.world.threedworld.entities;

import java.util.Arrays;
import java.util.List;

import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.threedworld.entities.EditorDialog.Editor;

import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * Entity is an interface for an object in a ThreeDWorld simulation which can apply translations and rotations,
 * possibly produce or consume external values, and be updated, deleted, or edited.
 */
public interface Entity {
    /**
     * Collect the position and orientation attributes from the adapter classes.
     * @param component The workspace component to use for creating couplings.
     * @return A list of producer types.
     */
    static List<AttributeType> getProducerTypes(WorkspaceComponent component) {
        return Arrays.asList(EntityLocationAdapter.getProducerType(component),
                EntityRotationAdapter.getProducerType(component));
    }

    /**
     * Collect the position and orientation attributes from the adapter classes.
     * @param component The workspace component to use for creating couplings.
     * @return A list of producer types.
     */
    static List<AttributeType> getConsumerTypes(WorkspaceComponent component) {
        return Arrays.asList(EntityLocationAdapter.getConsumerType(component),
                EntityRotationAdapter.getConsumerType(component));
    }

    /**
     * @return Return the name of the entity within the ThreeDWorld.
     */
    String getName();

    /**
     * @param value Assign the name of the entity within the ThreeDWorld.
     */
    void setName(String value);

    /**
     * @return Return the 3D engine object for this entity, a JME3 scene node.
     */
    Node getNode();

    /**
     * @return Return the x, y, and z coordinates of this entity as a vector.
     */
    Vector3f getPosition();

    /**
     * @param value Assign a vector containing the x, y, and z coordinates to this entity.
     */
    void setPosition(Vector3f value);

    /**
     * @param value Assign the position of the entity during the next 3d engine update. Use this method
     * when setting position from the main Simbrain thread.
     */
    void queuePosition(Vector3f value);

    /**
     * Translate the entity by an offset vector.
     * @param offset The relative offset to apply to the position of the entity.
     */
    void move(Vector3f offset);

    /**
     * @return Return the w, x, y, and z elements of the entity's rotation as a quaternion.
     */
    Quaternion getRotation();

    /**
     * @param value Assign a quaternion containing the w, x, y, and z components of a rotation.
     */
    void setRotation(Quaternion value);

    /**
     * Set the rotation of the entity during the next engine update.
     * @param value The quaternion to assign to the entity.
     */
    void queueRotation(Quaternion value);

    /**
     * Rotate the entity by a relative quaternion.
     * @param rotation The quaternion to append to the entity's current rotation.
     */
    void rotate(Quaternion rotation);

    /**
     * @return Return a bounding volume to use for selection and overlap tests.
     */
    BoundingVolume getBounds();

    /**
     * @return Return a list of the producer types this entity can provide.
     */
    List<PotentialProducer> getPotentialProducers();

    /**
     * @return Return a list of the consumer types this entity can provide.
     */
    List<PotentialConsumer> getPotentialConsumers();

    /**
     * Update the entity state and the state of any subcomponents.
     * @param t The current time.
     */
    void update(float t);

    /**
     * Remove the entity from the ThreeDWorld and potentially free any resources.
     */
    void delete();

    /**
     * Construct a property editor customized for this entity.
     * @return An editor with fields appropriate for the entity.
     */
    Editor getEditor();
}
