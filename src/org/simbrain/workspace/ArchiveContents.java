/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.workspace;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.simbrain.workspace.updater.UpdateAction;
import org.simbrain.workspace.updater.UpdateActionCustom;
import org.simbrain.workspace.updater.UpdateAllBuffered;
import org.simbrain.workspace.updater.UpdateComponent;
import org.simbrain.workspace.updater.UpdateCoupling;
import org.simbrain.workspace.updater.WorkspaceUpdater;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Instances of this class are used for building and reading the TOC of an
 * archive.
 * <p>
 * This is the class that XStream serializes.
 *
 * @author Matt Watson
 * @author Jeff Yoshimi
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class ArchiveContents {

    /** A map of all the components to their uris. */
    private transient Map<WorkspaceComponent, String> componentUris = new HashMap<WorkspaceComponent, String>();

    /** All of the components in the archive. */
    private List<ArchivedComponent> archivedComponents = new ArrayList<ArchivedComponent>();

    /** All of the couplings in the archive. */
    private List<ArchivedCoupling> archivedCouplings = new ArrayList<ArchivedCoupling>();

    /** All of the updateactions in the archive. */
    private List<ArchivedUpdateAction> archivedActions = new ArrayList<ArchivedUpdateAction>();

    /** The serializer for this archive. */
    @XmlTransient
    private WorkspaceComponentSerializer serializer;

    // TODO: Below is saving currentFile and Directory, which it shouldn't
    /** Reference to workspace used to serialize parameters in workspace. */
    private Workspace workspaceParameters;

    // TODO
    /**
     * No-argument consructor for JAXB.
     */
    ArchiveContents() {
    }

    /**
     * The component serializer for this archive.
     *
     * @param workspace references to parent workspace
     * @param serializer The component serializer for this archive.
     */
    ArchiveContents(final Workspace workspace,
            final WorkspaceComponentSerializer serializer) {
        this.workspaceParameters = workspace;
        this.serializer = serializer;
    }

    /**
     * Adds a new workspace Component to the archive.
     *
     * @param workspaceComponent The workspace component to add.
     * @return The component created for this WorkspaceComponent.
     */
    ArchivedComponent addComponent(
            final WorkspaceComponent workspaceComponent) {
        ArchivedComponent component = new ArchivedComponent(serializer,
                workspaceComponent);
        archivedComponents.add(component);
        componentUris.put(workspaceComponent, component.uri);
        return component;
    }

    /**
     * Adds an update action the archive.
     *
     * @param action the action to archive.
     */
    void addUpdateAction(UpdateAction action) {
        archivedActions.add(getArchivedAction(action));
    }

    /**
     * Creates the archived action given the "real" update action.
     *
     * @param action the real update action.
     * @return teh archived action.
     */
    private ArchivedUpdateAction getArchivedAction(final UpdateAction action) {
        String component_id = null;
        String coupling_id = null;

        // Get a component id if this is an update component action
        if (action instanceof UpdateComponent) {
            component_id = componentUris
                    .get(((UpdateComponent) action).getComponent());
        }
        // Get a coupling id, if this is coupling action
        if (action instanceof UpdateCoupling) {
            Coupling2<?> coupling = ((UpdateCoupling) action).getCoupling();
            if (coupling != null) {
                coupling_id = coupling.getId();
            } else {
                System.err.println("Invalid coupling action found while saving:"
                        + action.getDescription());
            }
        }

        // Create and return the archived action
        return new ArchivedUpdateAction(action, component_id, coupling_id);
    }

    /**
     * Returns an immutable list of the components in this archive.
     *
     * @return An immutable list of the components in this archive.
     */
    List<? extends ArchivedComponent> getArchivedComponents() {
        if (archivedComponents == null) {
            archivedComponents = Collections.emptyList();
        }

        return Collections.unmodifiableList(archivedComponents);
    }

    /**
     * Returns an immutable list of the couplings in this archive.
     *
     * @return An immutable list of the couplings in this archive.
     */
    List<? extends ArchivedCoupling> getArchivedCouplings() {
        if (archivedCouplings == null) {
            archivedCouplings = Collections.emptyList();
        }
        return Collections.unmodifiableList(archivedCouplings);
    }

    /**
     * Returns the component associated with the uri.
     *
     * @param uri The uri for the component.
     * @return The component associated with the uri.
     */
    ArchivedComponent getArchivedComponent(final String uri) {
        for (ArchivedComponent component : archivedComponents) {
            if (component.uri.equals(uri)) {
                return component;
            }
        }

        return null;
    }

    /**
     * Create a "real" update action from an archived update action.
     *
     * @param workspace parent workspace in which to place the new action
     * @param componentDeserializer used to get the workspace component
     *            corresponding to a workspace component id
     * @param archivedAction the archived action to convert into a real action
     * @return the "real" update action
     */
    UpdateAction createUpdateAction(final Workspace workspace,
            final WorkspaceComponentDeserializer componentDeserializer,
            final ArchivedUpdateAction archivedAction) {
        
        // TODO: Something fishy here.   
        // Maybe try  http://blog.bdoughan.com/2012/12/jaxbs-xmlanyelementlaxtrue-explained.html
        
        // Use reflection to create the update action, based on what type of
        // action was archived. For actions whose constructors require
        // components or couplings, the archived ids are used to find the
        // component or coupling.
        UpdateAction retAction = null;
        if (archivedAction.getUpdateAction() instanceof UpdateComponent) {
            try {
                WorkspaceComponent comp = componentDeserializer
                        .getComponent(archivedAction.getComponentId());
                retAction = archivedAction.getUpdateAction().getClass()
                        .getConstructor(new Class[] { WorkspaceUpdater.class,
                                WorkspaceComponent.class })
                        .newInstance(workspace.getUpdater(), comp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (archivedAction
                .getUpdateAction() instanceof UpdateAllBuffered) {
            try {
                retAction = archivedAction.getUpdateAction().getClass()
                        .getConstructor(new Class[] { WorkspaceUpdater.class })
                        .newInstance(workspace.getUpdater());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (archivedAction
                .getUpdateAction() instanceof UpdateActionCustom) {
            try {
                String script = ((UpdateActionCustom) archivedAction
                        .getUpdateAction()).getScriptString();
                retAction = archivedAction.getUpdateAction().getClass()
                        .getConstructor(new Class[] { WorkspaceUpdater.class,
                                String.class })
                        .newInstance(workspace.getUpdater(), script);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (archivedAction.getUpdateAction() instanceof UpdateCoupling) {
            try {
                String id = archivedAction.getCouplingId();
                Coupling2<?> coupling = workspace.getCoupling(id);
                retAction = archivedAction.getUpdateAction().getClass()
                        .getConstructor(new Class[] { Coupling2.class })
                        .newInstance(coupling);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return retAction;
    }

    /**
     * Adds a coupling to the archive.
     *
     * @param coupling The coupling to add.
     * @return The coupling entry in the archive.
     */
    ArchivedCoupling addCoupling(final Coupling2<?> coupling) {
        ArchivedCoupling c = new ArchivedCoupling(this, coupling);
        archivedCouplings.add(c);
        return c;
    }

    /**
     * A persistable form of update action that can be used to recreate the
     * action.
     *
     * @author Jeff Yoshimi
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    static final class ArchivedUpdateAction {

        /** Reference to the action itself. */
        @XmlAnyElement(lax=true)
        private UpdateAction updateAction;

        /**
         * Reference to the component id for this action, or null if not needed.
         */
        private String componentId;

        /**
         * Reference to the coupling id for this action, or null if not needed.
         */
        private String couplingId;

        // TODO
        /**
         * No-argument consructor for JAXB.
         */
        ArchivedUpdateAction() {
        }

        /**
         * Construct the archived update action.
         *
         * @param action reference to the update action itself.
         * @param componentId component id or null if none needed
         * @param couplingId coupling id or null if none needed
         */
        private ArchivedUpdateAction(UpdateAction action, String componentId,
                String couplingId) {
            this.updateAction = action;
            this.componentId = componentId;
            this.couplingId = couplingId;
        }

        /**
         * @return the componentId
         */
        public String getComponentId() {
            return componentId;
        }

        /**
         * @return the couplingId
         */
        public String getCouplingId() {
            return couplingId;
        }

        /**
         * @return the updateAction
         */
        public UpdateAction getUpdateAction() {
            return updateAction;
        }

    }

    /**
     * Represents the data used to store components in the archive.
     *
     * @author Matt Watson
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    static final class ArchivedComponent {

        /** The name of the class for the component. */
        private String className;

        /** The name of the Component. */
        private String name;

        /** The uri for the serialized component. */
        private String uri;

        // TODO Does this need to be persisted?
        /** A unique id for the component in the archive. */
        private int id;

        // TODO Does this need to be persisted?
        /**
         * A short String used to signify the format of the serialized
         * component.
         */
        private String format;

        /**
         * The desktop component associated with the component (if there is
         * one).
         */
        private ArchivedDesktopComponent desktopComponent;

        // TODO
        /**
         * No-argument consructor for JAXB.
         */
        ArchivedComponent() {
        }

        /**
         * Creates a new Component entry.
         *
         * @param serializer The component serializer for the archive.
         * @param component The workspace component this entry represents.
         */
        private ArchivedComponent(final WorkspaceComponentSerializer serializer,
                final WorkspaceComponent component) {
            this.className = component.getClass().getCanonicalName();
            this.id = serializer.getId(component);
            this.name = component.getName();
            this.format = component.getDefaultFormat();
            this.uri = "components/" + id + '_' + name.replaceAll("\\s", "_")
                    + '.' + format;
        }

        /**
         * Adds a desktop component to this component entry.
         *
         * @param dc The desktop component to add an entry for.
         * @return The entry for the desktop component.
         */
        ArchivedDesktopComponent addDesktopComponent(
                final org.simbrain.workspace.gui.GuiComponent<?> dc) {
            return desktopComponent = new ArchivedDesktopComponent(this, dc);
        }

        /**
         * Class used to represent a desktop component in the archive.
         *
         * @author Matt Watson
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        static final class ArchivedDesktopComponent {

            /** The class for the desktop component. */
            private String className;

            /** The uri for the serialized data. */
            private String uri;

            /** The format for the serialized data. */
            private String format;

            // TODO
            /**
             * No-argument constructor for JAXB.
             */
            public ArchivedDesktopComponent() {
            }

            /**
             * Creates a new instance.
             *
             * @param parent The parent component entry.
             * @param dc The desktop component this instance represents.
             */
            private ArchivedDesktopComponent(final ArchivedComponent parent,
                    final org.simbrain.workspace.gui.GuiComponent<?> dc) {
                this.className = dc.getClass().getCanonicalName();
                this.format = dc.getWorkspaceComponent().getDefaultFormat();
                this.uri = "guis/" + parent.id + '_'
                        + parent.name.replaceAll("\\s", "_") + '.' + format;
            }

            /**
             * @return the uri
             */
            public String getUri() {
                return uri;
            }
        }

        /**
         * @return the className
         */
        public String getClassName() {
            return className;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * @return the format
         */
        public String getFormat() {
            return format;
        }

        /**
         * @return the desktopComponent
         */
        public ArchivedDesktopComponent getDesktopComponent() {
            return desktopComponent;
        }
    }

    /**
     * Class used to represent a coupling in the archive.
     *
     * @author Matt Watson
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    static final class ArchivedCoupling {

        /** The source attribute for the coupling. */
        private ArchivedAttribute archivedProducer;

        /** The target attribute for the coupling. */
        private ArchivedAttribute archivedConsumer;

        // TODO
        /**
         * No-argument constructor for JAXB.
         */
        public ArchivedCoupling() {
        }

        /**
         * Creates a new instance.
         *
         * @param parent The parent archive.
         * @param coupling The coupling this instance represents.
         */
        ArchivedCoupling(final ArchiveContents parent,
                final Coupling2<?> coupling) {

            this.archivedProducer = new ArchivedAttribute(parent,
                    coupling.producer);
            this.archivedConsumer = new ArchivedAttribute(parent,
                    coupling.consumer);
        }

        /**
         * @return the archivedProducer
         */
        public ArchivedAttribute getArchivedProducer() {
            return archivedProducer;
        }

        /**
         * @return the archivedConsumer
         */
        public ArchivedAttribute getArchivedConsumer() {
            return archivedConsumer;
        }

    }

    /**
     * The class used to represent an attribute in the archive.
     *
     * @author Matt Watson
     * @author Jeff Yoshimi
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    static final class ArchivedAttribute {

        /** The uri for the parent component of this attribute. */
        private String parentComponentRef;

        // TODO
        private String attributeId;

        // TODO
        /**
         * No-argument constructor for JAXB.
         */
        public ArchivedAttribute() {
        }

        /**
         * Creates a new instance.
         *
         * @param parent The parent archive.
         * @param attribute The attribute this instance represents.
         */
        ArchivedAttribute(final ArchiveContents parent,
                final Attribute2 attribute) {

            this.parentComponentRef = parent.componentUris
                    .get(attribute.parentComponent);
            this.attributeId = attribute.getId();

            // WorkspaceComponent comp = attribute.getParentComponent();
            // this.parentComponentRef = parent.componentUris.get(comp);
            // this.baseObjectKey = comp.getKeyFromObject(attribute
            // .getBaseObject());
            // this.methodBaseName = attribute.getMethodName();
            // this.argumentDataTypes = attribute.getArgumentDataTypes();
            // this.argumentValues = attribute.getArgumentValues();
            // this.dataType = attribute.getDataType();
            // this.description = attribute.getDescription();

        }

        /**
         * @return the parentComponentRef
         */
        public String getParentRef() {
            return parentComponentRef;
        }

        /**
         * @return the parentComponentRef
         */
        public String getParentComponentRef() {
            return parentComponentRef;
        }

        /**
         * @return the attributeId
         */
        public String getAttributeId() {
            return attributeId;
        }

    }

    /**
     * Writes this instance to XML.
     *
     * @param stream The stream to write to.
     */
    void toXml(final OutputStream stream) {
        // xstream().toXML(this, stream);
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(ArchiveContents.class, UpdateAllBuffered.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this, stream);
            marshaller.marshal(this, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the XStream instance used to serialize and deserialize instances
     * of this class.
     *
     * @return An XStream instance.
     */
    static XStream xstream() {
        XStream xstream = new XStream(new DomDriver());

        xstream.omitField(ArchiveContents.class, "serializer");
        xstream.omitField(ArchiveContents.class, "archivedAvailableActions");
        xstream.omitField(ArchivedComponent.class, "serializer");
        xstream.omitField(ArchivedCoupling.class, "serializer");
        xstream.omitField(ArchivedUpdateAction.class, "serializer");
        xstream.omitField(ArchivedUpdateAction.class, "updater");
        xstream.omitField(ArchivedComponent.class, "data");
        xstream.omitField(ArchivedComponent.ArchivedDesktopComponent.class,
                "data");

        xstream.omitField(Workspace.class, "LOGGER");
        xstream.omitField(Workspace.class, "manager");
        xstream.omitField(Workspace.class, "componentList");
        xstream.omitField(Workspace.class, "workspaceChanged");
        xstream.omitField(Workspace.class, "currentDirectory");
        xstream.omitField(Workspace.class, "currentFile");
        xstream.omitField(Workspace.class, "updater");
        xstream.omitField(Workspace.class, "listeners");
        xstream.omitField(Workspace.class, "componentNameIndices");
        xstream.omitField(Workspace.class, "updaterLock");
        xstream.omitField(Workspace.class, "componentLock");

        xstream.omitField(UpdateComponent.class, "component");
        xstream.omitField(UpdateComponent.class, "updater");
        // xstream.omitField(UpdateCoupling.class, "coupling");
        xstream.omitField(UpdateActionCustom.class, "interpreter");
        xstream.omitField(UpdateActionCustom.class, "theAction");
        xstream.omitField(UpdateActionCustom.class, "updater");
        xstream.omitField(UpdateAllBuffered.class, "updater");

        xstream.alias("Workspace", ArchiveContents.class);
        xstream.alias("Component", ArchivedComponent.class);
        xstream.alias("Coupling", ArchivedCoupling.class);
        xstream.alias("UpdateAction", ArchivedUpdateAction.class);
        xstream.alias("DesktopComponent",
                ArchivedComponent.ArchivedDesktopComponent.class);

        // xstream.addImplicitCollection(ArchiveContents.class, "components",
        // ArchivedComponent.class);
        // xstream.addImplicitCollection(ArchiveContents.class, "couplings",
        // ArchivedCoupling.class);
        // xstream.addImplicitCollection(ArchivedComponent.class,
        // "desktopComponents");

        return xstream;
    }

    /**
     * @return the workspaceParameters
     */
    public Workspace getWorkspaceParameters() {
        return workspaceParameters;
    }

    /**
     * @return the archivedActions
     */
    public List<ArchivedUpdateAction> getArchivedActions() {
        return archivedActions;
    }

    // TODO
    void beforeUnmarshal(Unmarshaller u, Object network) {
        System.out.println("Before");
    }

    void afterUnmarshal(Unmarshaller u, Object network) {
        System.out.println("Before");
    }
}