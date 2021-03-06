/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005 Jeff Yoshimi <www.jeffyoshimi.net>
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.simbrain.gauge.GaugeFrame;
import org.simbrain.network.NetworkFrame;
import org.simbrain.util.Utils;
import org.simbrain.world.dataworld.DataWorldFrame;
import org.simbrain.world.odorworld.OdorWorldFrame;


/**
 * <b>WorkspaceSerializer</b> handles workspace persistence.  It contains static methods for reading and writing
 * workspace files, and also serves as a buffer for Castor initialization.
 */
public class WorkspaceSerializer {

    /** File system property. */
    private static final String FS = System.getProperty("file.separator");

    /** Network list. */
    private ArrayList networkList = new ArrayList();

    /** Odor world list. */
    private ArrayList odorWorldList = new ArrayList();

    /** Data world list. */
    private ArrayList dataWorldList = new ArrayList();

    /** Gauge list. */
    private ArrayList gaugeList = new ArrayList();

    /**
     * Read  workspace file.
     *
     * @param wspace reference to current workspace
     * @param f file containing new workspace information
     * @param isImport whether this workspace is being imported or opened
     */
    public static void readWorkspace(final Workspace wspace, final File f, final boolean isImport) {
        wspace.clearWorkspace();

        WorkspaceSerializer wSerializer = new WorkspaceSerializer();

        try {
            Reader reader = new FileReader(f);
            Mapping map = new Mapping();
            map.loadMapping("." + FS + "lib" + FS + "workspace_mapping.xml");

            Unmarshaller unmarshaller = new Unmarshaller(wSerializer);
            unmarshaller.setMapping(map);

            // unmarshaller.setDebug(true);
            wSerializer = (WorkspaceSerializer) unmarshaller.unmarshal(reader);
        } catch (java.io.FileNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not find workspace file \n" + f, "Warning",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

            return;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "There was a problem opening the workspace file \n" + f,
                    "Warning", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

            return;
        }

        for (int i = 0; i < wSerializer.getOdorWorldList().size(); i++) {
            OdorWorldFrame wld = (OdorWorldFrame) wSerializer.getOdorWorldList().get(i);
            wld.init();
            wld.setWorkspace(wspace);
            wld.setBounds(wld.getXpos(), wld.getYpos(), wld.getTheWidth(), wld.getTheHeight());

            if (wld.getGenericPath() != null) {
                if (isImport) {
                    String name = Utils.getDir(f) + Utils.getNameFromPath(wld.getGenericPath());
                    wld.readWorld(new File(name));
                } else {
                    wld.readWorld(new File(wld.getGenericPath()));
                }
            }

            wld.getWorld().setParentWorkspace(wspace);

            wspace.addOdorWorld(wld, true);
        }

        for (int i = 0; i < wSerializer.getDataWorldList().size(); i++) {
            DataWorldFrame wld = (DataWorldFrame) wSerializer.getDataWorldList().get(i);
            wld.init();
            wld.setWorkspace(wspace);
            wld.setBounds(wld.getXpos(), wld.getYpos(), wld.getTheWidth(), wld.getTheHeight());

            if (wld.getGenericPath() != null) {
                if (isImport) {
                    String name = Utils.getDir(f) + Utils.getNameFromPath(wld.getGenericPath());
                    wld.readWorld(new File(name));
                } else {
                    wld.readWorld(new File(wld.getGenericPath()));
                }
            }

            wspace.addDataWorld(wld, true);
        }

        for (int i = 0; i < wSerializer.getNetworkList().size(); i++) {
            NetworkFrame net = (NetworkFrame) wSerializer.getNetworkList().get(i);

            net.setBounds(net.getXpos(), net.getYpos(), net.getTheWidth(), net.getTheHeight());

            wspace.addNetwork(net, true);

            if (net.getGenericPath() != null) {
                if (isImport) {
                    String name = Utils.getDir(f) + Utils.getNameFromPath(net.getGenericPath());
                    net.getNetworkPanel().openNetwork(new File(name));
                } else {
                    net.getNetworkPanel().openNetwork(new File(net.getGenericPath()));
                }
            }
        }

        for (int i = 0; i < wSerializer.getGaugeList().size(); i++) {
            GaugeFrame gauge = new GaugeFrame(wspace); // Initializing this way
                                                        // instead of directory
                                                        // from the wserializer
                                                        // list
                                                        // stopped the gauge from being totally hosed,
                                                        // The gauge panel is initialized this way
            GaugeFrame temp =  (GaugeFrame) wSerializer.getGaugeList().get(i);
            gauge.setBounds(temp.getXpos(), temp.getYpos(), temp.getTheWidth(), temp.getTheHeight());
            gauge.getGaugePanel().setBounds(gauge.getBounds());
            //This is what I had to do to solve the problem that was killing me all morning!
            // WHY IS THIS NEEDED?  Why won't the gaugepanel be initilized automatically?  IT works elsewhere!

            if (temp.getGenericPath() != null) {
                if (isImport) {
                    String name = Utils.getDir(f) + Utils.getNameFromPath(temp.getGenericPath());
                    gauge.readGauge(new File(name));
                } else {
                    gauge.readGauge(new File(temp.getGenericPath()));
                }
            }

            wspace.addGauge(gauge, true);
            gauge.update();  // So graphics are updated.
        }

        // Create couplings and attach agents to them
        ArrayList couplings = wspace.getCouplingList();
        wspace.attachAgentsToCouplings(couplings);

        // Graphics clean up
        wspace.setTitle(f.getName());
        wspace.setCurrentFile(f);

        wspace.setWorkspaceChanged(false);
    }

    /**
     * Initialize the bounds of all workspace components so they can be serialized.
     *
     * @param ws reference to workspace.
     */
    private static void initComponentBounds(final Workspace ws) {
        for (int i = 0; i < ws.getNetworkList().size(); i++) {
            NetworkFrame net = (NetworkFrame) ws.getNetworkList().get(i);
            net.initBounds();
        }

        for (int i = 0; i < ws.getOdorWorldList().size(); i++) {
            OdorWorldFrame wld = (OdorWorldFrame) ws.getOdorWorldList().get(i);
            wld.initBounds();
        }

        for (int i = 0; i < ws.getDataWorldList().size(); i++) {
            DataWorldFrame wld = (DataWorldFrame) ws.getDataWorldList().get(i);
            wld.initBounds();
        }

        for (int i = 0; i < ws.getGaugeList().size(); i++) {
            GaugeFrame gauge = (GaugeFrame) ws.getGaugeList().get(i);
            gauge.initBounds();
        }
    }

    /**
     * Save workspace information.
     *
     * @param ws reference to current workspace
     * @param theFile file to save information to
     */
    public static void writeWorkspace(final Workspace ws, final File theFile) {
        WorkspaceSerializer serializer = new WorkspaceSerializer();

        initComponentBounds(ws);

        serializer.setNetworkList(ws.getNetworkList());
        serializer.setOdorWorldList(ws.getOdorWorldList());
        serializer.setDataWorldList(ws.getDataWorldList());
        serializer.setGaugeList(ws.getGaugeList());

        LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "true");

        try {
            FileWriter writer = new FileWriter(theFile);
            Mapping map = new Mapping();
            map.loadMapping("." + FS + "lib" + FS + "workspace_mapping.xml");

            Marshaller marshaller = new Marshaller(writer);
            marshaller.setMapping(map);

            //marshaller.setDebug(true);
            marshaller.marshal(serializer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ws.setTitle(theFile.getName());
        ws.setWorkspaceChanged(false);
    }


    /**
     * @return Returns the networkList.
     */
    public ArrayList getNetworkList() {
        return networkList;
    }

    /**
     * @param networkList The networkList to set.
     */
    public void setNetworkList(final ArrayList networkList) {
        this.networkList = networkList;
    }

    /**
     * @return Returns the odorWorldList.
     */
    public ArrayList getOdorWorldList() {
        return odorWorldList;
    }

    /**
     * @param worldList The odorWorldList to set.
     */
    public void setOdorWorldList(final ArrayList worldList) {
        this.odorWorldList = worldList;
    }

    /**
     * @return Returns the gaugeList.
     */
    public ArrayList getGaugeList() {
        return gaugeList;
    }

    /**
     * @param gaugeList The gaugeList to set.
     */
    public void setGaugeList(final ArrayList gaugeList) {
        this.gaugeList = gaugeList;
    }

    /**
     * @return Returns the dataWorldList.
     */
    public ArrayList getDataWorldList() {
        return dataWorldList;
    }

    /**
     * @param dataWorldList The dataWorldList to set.
     */
    public void setDataWorldList(final ArrayList dataWorldList) {
        this.dataWorldList = dataWorldList;
    }
}
