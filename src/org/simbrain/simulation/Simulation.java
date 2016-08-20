package org.simbrain.simulation;

import java.util.Hashtable;

import javax.swing.JInternalFrame;

import org.simbrain.network.NetworkComponent;
import org.simbrain.network.core.Network;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.plot.projection.ProjectionComponent;
import org.simbrain.plot.timeseries.TimeSeriesPlotComponent;
import org.simbrain.workspace.AttributeManager;
import org.simbrain.workspace.Coupling;
import org.simbrain.workspace.PotentialConsumer;
import org.simbrain.workspace.PotentialProducer;
import org.simbrain.workspace.UmatchedAttributesException;
import org.simbrain.workspace.Workspace;
import org.simbrain.workspace.gui.SimbrainDesktop;
import org.simbrain.world.odorworld.OdorWorld;
import org.simbrain.world.odorworld.OdorWorldComponent;
import org.simbrain.world.odorworld.effectors.Effector;
import org.simbrain.world.odorworld.entities.RotatingEntity;
import org.simbrain.world.odorworld.sensors.Sensor;
import org.simbrain.world.odorworld.sensors.SmellSensor;

//TODO: Document everything
public class Simulation {

    final SimbrainDesktop desktop;
    final Workspace workspace;

    Hashtable<Network, NetworkComponent> netMap = new Hashtable();
    Hashtable<OdorWorld, OdorWorldComponent> odorMap = new Hashtable();

    /**
     * @param desktop
     */
    public Simulation(SimbrainDesktop desktop) {
        super();
        this.desktop = desktop;
        this.workspace = desktop.getWorkspace();
    }

    /**
     * @return the desktop
     */
    public SimbrainDesktop getDesktop() {
        return desktop;
    }

    // TODO: Move to workspace level?
    /**
     * Helper to add couplings and deal with exception (rather poorly for now).
     *
     * @param coupling
     */
    public void addCoupling(Coupling coupling) {
        try {
            workspace.getCouplingManager().addCoupling(coupling);
        } catch (UmatchedAttributesException e) {
            System.err.println("Unmatched attributes");
            e.printStackTrace();
        }
    }

    // Neurons to agent. So far just one to one
    public void couple(NeuronGroup ng, RotatingEntity entity) {
        AttributeManager producers = netMap.get(ng.getParentNetwork())
                .getAttributeManager();
        AttributeManager consumers = odorMap.get(entity.getParentWorld())
                .getAttributeManager();

        PotentialProducer straightProducer = producers.createPotentialProducer(
                ng.getNeuronList().get(0), "getActivation", double.class);
        PotentialProducer leftProducer = producers.createPotentialProducer(
                ng.getNeuronList().get(1), "getActivation", double.class);
        PotentialProducer rightProducer = producers.createPotentialProducer(
                ng.getNeuronList().get(2), "getActivation", double.class);

        PotentialConsumer straightConsumer = producers
                .createPotentialConsumer(entity, "goStraight", double.class);
        PotentialConsumer leftConsumer = producers
                .createPotentialConsumer(entity, "turnLeft", double.class);
        PotentialConsumer rightConsumer = producers
                .createPotentialConsumer(entity, "turnRight", double.class);

        Coupling straightCoupling = new Coupling(straightProducer,
                straightConsumer);
        Coupling leftCoupling = new Coupling(leftProducer, leftConsumer);
        Coupling rightCoupling = new Coupling(rightProducer, rightConsumer);
        addCoupling(straightCoupling);
        addCoupling(leftCoupling);
        addCoupling(rightCoupling);

    }

    // Coupling a neuron to the indicated (by index) time series of a time
    // series plot.
    public void couple(NetworkComponent network, Neuron neuron,
            TimeSeriesPlotComponent plot, int index) {
        PotentialProducer neuronProducer = network.getAttributeManager()
                .createPotentialProducer(neuron, "getActivation", double.class);
        PotentialConsumer timeSeriesConsumer1 = plot.getPotentialConsumers()
                .get(index);
        addCoupling(
                new Coupling<double[]>(neuronProducer, timeSeriesConsumer1));
    }

    /**
     * Coupling a neuron group to a projection plot.
     */
    public void couple(NetworkComponent network, NeuronGroup ng,
            ProjectionComponent plot) {
        PotentialProducer ngProducer = network.getAttributeManager()
                .createPotentialProducer(ng, "getActivations", double[].class);
        PotentialConsumer projConsumer = plot.getPotentialConsumers().get(0); // Ugh
        addCoupling(new Coupling<double[]>(ngProducer, projConsumer));
    }

    /**
     * Vector based coupling from an agent to a neuron group.
     *
     * TODO: Fix ugly sensorIndex thing
     *
     * @param entity
     * @param ng
     */
    public void couple(RotatingEntity entity, NeuronGroup ng, int sensorIndex) {
        AttributeManager producers = odorMap.get(entity.getParentWorld())
                .getAttributeManager();
        AttributeManager consumers = netMap.get(ng.getParentNetwork())
                .getAttributeManager();

        // TODO: Sensor
        PotentialProducer sensoryProducer = producers.createPotentialProducer(
                ((SmellSensor) entity.getSensors().get(sensorIndex)),
                "getCurrentValue", double[].class);
        PotentialConsumer sensoryConsumer = consumers.createPotentialConsumer(
                ng, "forceSetActivations", double[].class);

        addCoupling(new Coupling(sensoryProducer, sensoryConsumer));
    }

    public NetBuilder addNetwork(int x, int y, int width, int height,
            String name) {
        NetworkComponent networkComponent = new NetworkComponent(name);
        workspace.addWorkspaceComponent(networkComponent);
        desktop.getDesktopComponent(networkComponent).getParentFrame()
                .setBounds(x, y, width, height);
        netMap.put(networkComponent.getNetwork(), networkComponent);
        return new NetBuilder(networkComponent);
    }

    public PlotBuilder addTimeSeriesPlot(int x, int y, int width, int height,
            String name) {
        TimeSeriesPlotComponent timeSeriesComponent = new TimeSeriesPlotComponent(
                name);
        workspace.addWorkspaceComponent(timeSeriesComponent);
        desktop.getDesktopComponent(timeSeriesComponent).getParentFrame()
                .setBounds(x, y, width, height);
        return new PlotBuilder(timeSeriesComponent);
    }

    public PlotBuilder addProjectionPlot(int x, int y, int width, int height,
            String name) {
        ProjectionComponent projectionComponent = new ProjectionComponent(name);
        workspace.addWorkspaceComponent(projectionComponent);
        desktop.getDesktopComponent(projectionComponent).getParentFrame()
                .setBounds(x, y, width, height);
        return new PlotBuilder(projectionComponent);
    }

    public OdorWorldBuilder addOdorWorld(int x, int y, int width, int height,
            String name) {
        OdorWorldComponent odorWorldComponent = new OdorWorldComponent(name);
        workspace.addWorkspaceComponent(odorWorldComponent);
        desktop.getDesktopComponent(odorWorldComponent).getParentFrame()
                .setBounds(x, y, width, height);
        odorMap.put(odorWorldComponent.getWorld(), odorWorldComponent);
        return new OdorWorldBuilder(odorWorldComponent);
    }

    public JInternalFrame addFrame(int x, int y, String name) {
        JInternalFrame frame = new JInternalFrame(name, true, true);
        frame.setLocation(x, y);
        frame.setVisible(true);
        frame.pack();
        desktop.addInternalFrame(frame);
        return frame;
    }

    // TODO: Same kind of thing as above for odorworld, plots, etc.

    /**
     * @return the workspace
     */
    public Workspace getWorkspace() {
        return workspace;
    }

    // TODO: These coupling methods are ugly. Hopefully they get better
    // after the planned coupling refactor, so I'm leaving them for now.

    /**
     * Make a coupling from a smell sensor to a neuron. Couples the provided
     * smell sensor one the indicated dimension to the provided neuron.
     *
     * @param producingSensor the smell sensor. Takes a scalar value.
     * @param stimulusDimension Which component of the smell vector on the agent
     *            to "smell", beginning at index "0"
     * @param consumingNeuron the neuron to write the values to
     */
    public void couple(Sensor producingSensor, int stimulusDimension,
            Neuron consumingNeuron) {
        AttributeManager producers = odorMap
                .get(producingSensor.getParent().getParentWorld())
                .getAttributeManager();
        AttributeManager consumers = netMap.get(consumingNeuron.getNetwork())
                .getAttributeManager();

        PotentialProducer agentSensor = producers.createPotentialProducer(
                producingSensor, "getCurrentValue", double.class,
                new Class[] { int.class }, new Object[] { stimulusDimension });
        PotentialConsumer sensoryNeuron = consumers.createPotentialConsumer(
                consumingNeuron, "forceSetActivation", double.class);

        Coupling sensorToNeuronCoupling = new Coupling(agentSensor,
                sensoryNeuron);
        addCoupling(sensorToNeuronCoupling);

    }

    public void couple(Neuron straight, Effector effector) {

        AttributeManager producers = netMap.get(straight.getNetwork())
                .getAttributeManager();
        AttributeManager consumers = odorMap
                .get(effector.getParent().getParentWorld())
                .getAttributeManager();

        PotentialProducer effectorNeuron = producers.createPotentialProducer(
                straight, "getActivation", double.class);

        PotentialConsumer agentEffector = consumers
                .createPotentialConsumer(effector, "addAmount", double.class);

        addCoupling(new Coupling(effectorNeuron, agentEffector));
    }

}
