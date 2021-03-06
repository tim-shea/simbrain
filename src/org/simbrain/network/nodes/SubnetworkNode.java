/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005-2006 Jeff Yoshimi <www.jeffyoshimi.net>
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
package org.simbrain.network.nodes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;

import org.simbrain.network.NetworkPanel;
import org.simnet.interfaces.Network;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * Abstract subnetwork node.
 */
public abstract class SubnetworkNode extends ScreenElement implements PropertyChangeListener {

    /** Tab height. */
    public static final double TAB_HEIGHT = 22.0d;

    /** Default tab width. */
    private static final double DEFAULT_TAB_WIDTH = 100.0d;

    /** Tab inset or border height. */
    private static final double TAB_INSET_HEIGHT = 5.0d;

    /** Tab inset or border width. */
    private static final double TAB_INSET_WIDTH = 6.0d;

    /** Outline inset or border height. */
    public static final double OUTLINE_INSET_HEIGHT = 12.0d;

    /** Outline inset or border width. */
    public static final double OUTLINE_INSET_WIDTH = 12.0d;

    /** Default outline height. */
    private static final double DEFAULT_OUTLINE_HEIGHT = 150.0d;

    /** Default outline width. */
    private static final double DEFAULT_OUTLINE_WIDTH = 150.0d;

    /** Default tab paint. */
    private static final Paint DEFAULT_TAB_PAINT = Color.LIGHT_GRAY;

    /** Default tab stroke. */
    private static final Stroke DEFAULT_TAB_STROKE = new BasicStroke(1.0f);

    /** Default tab stroke paint. */
    private static final Paint DEFAULT_TAB_STROKE_PAINT = Color.DARK_GRAY;

    /** Default outline stroke. */
    private static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(1.0f);

    /** Default outline stroke paint. */
    private static final Paint DEFAULT_OUTLINE_STROKE_PAINT = Color.LIGHT_GRAY;

    /** The subnetwork for this subnetwork node. */
    private final Network subnetwork;

    /** Tab node. */
    private final TabNode tab;

    /** Outline node. */
    private final OutlineNode outline;

    /** The tab paint for this subnetwork node. */
    private Paint tabPaint;

    /** The label for this subnetwork node. */
    private String label;

    /** The tab stroke for this subnetwork node. */
    private Stroke tabStroke;

    /** The tab stroke paint for this subnetwork node. */
    private Paint tabStrokePaint;

    /** The outline stroke for this subnetwork node. */
    private Stroke outlineStroke;

    /** The outline stroke paint for this subnetwork node. */
    private Paint outlineStrokePaint;

    /** The last outline stroke, if any. */
    private Stroke lastOutlineStroke;

    /** Intial child layout complete. */
    private boolean initialChildLayoutComplete;

    /** True if this subnetwork node is to show its outline. */
    private boolean showOutline;

    /** Show outline action. */
    private Action showOutlineAction;

    /** Hide outline action. */
    private Action hideOutlineAction;

    /** Delete subnet action. */
    private Action deleteSubnetAction;

    /** Set properties action. */
    private Action setPropertiesAction;


    /**
     * Create a new abstract subnetwork node from the specified parameters.
     *
     * @param networkPanel networkPanel for this subnetwork node, must not be null
     * @param subnetwork subnetwork for this subnetwork node, must not be null
     * @param x x offset for this subnetwork node
     * @param y y offset for this subnetwork node
     */
    protected SubnetworkNode(final NetworkPanel networkPanel,
                             final Network subnetwork,
                             final double x, final double y) {

        super(networkPanel);

        if (subnetwork == null) {
            throw new IllegalArgumentException("subnetwork must not be null");
        }
        this.subnetwork = subnetwork;

        offset(x - OUTLINE_INSET_WIDTH, y - OUTLINE_INSET_HEIGHT - TAB_HEIGHT);
        setPickable(true);
        setChildrenPickable(true);

        tab = new TabNode();
        outline = new OutlineNode();

        addChild(outline);
        addChild(tab);

        setBounds(tab.getBounds());

        setLabel(subnetwork.getType());
        setTabPaint(DEFAULT_TAB_PAINT);
        setTabStroke(DEFAULT_TAB_STROKE);
        setTabStrokePaint(DEFAULT_TAB_STROKE_PAINT);
        setOutlineStroke(DEFAULT_OUTLINE_STROKE);
        setOutlineStrokePaint(DEFAULT_OUTLINE_STROKE_PAINT);

        initialChildLayoutComplete = false;

        showOutlineAction = new AbstractAction("Show outline") {
            public void actionPerformed(final ActionEvent event) {
                setShowOutline(true);
            }
        };

        hideOutlineAction = new AbstractAction("Hide outline") {
            public void actionPerformed(final ActionEvent event) {
                setShowOutline(false);
            }
        };

        deleteSubnetAction = new AbstractAction("Delete subnetwork") {
            public void actionPerformed(final ActionEvent event) {
                subnetwork.getNetworkParent().deleteNetwork(subnetwork);
            }
        };

        setPropertiesAction = new AbstractAction("Set properties of " + subnetwork.getType() + " network") {
            public void actionPerformed(final ActionEvent event) {
                JDialog propertyDialog = getPropertyDialog();
                propertyDialog.pack();
                propertyDialog.setLocationRelativeTo(null);
                propertyDialog.setVisible(true);
            }
        };
    }

    /** @see ScreenElement */
    protected JPopupMenu getContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.add(hideOutlineAction);
        contextMenu.add(showOutlineAction);
        contextMenu.addSeparator();
        contextMenu.add(deleteSubnetAction);
        contextMenu.addSeparator();
        return contextMenu;
    }

   /**
    * Set to true if this subnetwork node is to show its outline.
    *
    * <p>This is a bound property.</p>
    *
    * @param showOutline true if this subnetwork node is to show its outline
    */
   public void setShowOutline(final boolean showOutline) {
       boolean oldShowOutline = this.showOutline;
       this.showOutline = showOutline;

       if (oldShowOutline != this.showOutline) {
           if (this.showOutline) {
               if (lastOutlineStroke == null) {
                   outline.setStroke(lastOutlineStroke);
               } else {
                   outline.setStroke(DEFAULT_OUTLINE_STROKE);
               }
           } else {
               lastOutlineStroke = outline.getStroke();
               outline.setStroke(null);
           }
       }
      firePropertyChange("showOutline", Boolean.valueOf(oldShowOutline), Boolean.valueOf(showOutline));
   }

    /** @see ScreenElement */
    public final boolean isSelectable() {
        return true;
    }

    /** @see ScreenElement */
    public final boolean showSelectionHandle() {
        return false;
    }

    /** @see ScreenElement */
    public final boolean isDraggable() {
        return true;
    }

    /** @see ScreenElement */
    public final void resetColors() {
        // empty
    }

    /**
     * Return the label for this subnetwork node.
     * The label may be null.
     *
     * @return the label for this subnetwork node
     */
    public final String getLabel() {
        return tab.getLabel();
    }

    /**
     * Set the label for this subnetwork node to <code>label</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param label label for this subnetwork node
     */
    public void setLabel(final String label) {
        String oldLabel = this.label;
        this.label = label;
        tab.setLabel(this.label);
        firePropertyChange("label", oldLabel, this.label);
    }

    /**
     * Return the subnetwork for this subnetwork node.
     * The subnetwork will not be null.
     *
     * @return the subnetwork for this subnetwork node
     */
    public final Network getSubnetwork() {
        return subnetwork;
    }

    /**
     * Return the tab paint for this subnetwork node.
     * The tab paint may be null.
     *
     * @return the tab paint for this subnetwork node
     */
    public final Paint getTabPaint() {
        return tabPaint;
    }

    /**
     * Set the tab paint for this subnetwork node to <code>tabPaint</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param tabPaint tab paint for this subnetwork node
     */
    public final void setTabPaint(final Paint tabPaint) {
        Paint oldTabPaint = this.tabPaint;
        this.tabPaint = tabPaint;
        tab.setTabPaint(tabPaint);
        firePropertyChange("tabPaint", oldTabPaint, this.tabPaint);
    }

    /**
     * Return the tab stroke for this subnetwork node.
     * The tab stroke may be null.
     *
     * @return the tab stroke for this subnetwork node
     */
    public final Stroke getTabStroke() {
        return tabStroke;
    }

    /**
     * Set the tab stroke for this subnetwork node to <code>tabStroke</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param tabStroke tab stroke for this subnetwork node
     */
    public final void setTabStroke(final Stroke tabStroke) {
        Stroke oldTabStroke = this.tabStroke;
        this.tabStroke = tabStroke;
        tab.setTabStroke(tabStroke);
        firePropertyChange("tabStroke", oldTabStroke, this.tabStroke);
    }

    /**
     * Return the tab stroke paint for this subnetwork node.
     * The tab stroke paint may be null.
     *
     * @return the tab stroke paint for this subnetwork node
     */
    public final Paint getTabStrokePaint() {
        return tabStrokePaint;
    }

    /**
     * Set the tab stroke paint for this subnetwork node to <code>tabStrokePaint</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param tabStrokePaint tab stroke paint for this subnetwork node
     */
    public final void setTabStrokePaint(final Paint tabStrokePaint) {
        Paint oldTabStrokePaint = this.tabStrokePaint;
        this.tabStrokePaint = tabStrokePaint;
        tab.setTabStrokePaint(tabStrokePaint);
        firePropertyChange("tabStrokePaint", oldTabStrokePaint, this.tabStrokePaint);
    }

    /**
     * Return the outline stroke for this subnetwork node.
     * The outline stroke may be null.
     *
     * @return the outline stroke for this subnetwork node
     */
    public final Stroke getOutlineStroke() {
        return outlineStroke;
    }

    /**
     * Set the outline stroke for this subnetwork node to <code>outlineStroke</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param outlineStroke outline stroke for this subnetwork node
     */
    public final void setOutlineStroke(final Stroke outlineStroke) {
        Stroke oldOutlineStroke = this.outlineStroke;
        this.outlineStroke = outlineStroke;
        outline.setStroke(outlineStroke);
        firePropertyChange("outlineStroke", oldOutlineStroke, this.outlineStroke);
    }

    /**
     * Return the outline stroke paint for this subnetwork node.
     * The outline stroke paint may be null.
     *
     * @return the outline stroke paint for this subnetwork node
     */
    public final Paint getOutlineStrokePaint() {
        return outlineStrokePaint;
    }

    /**
     * Set the outline stroke paint for this subnetwork node to <code>outlineStrokePaint</code>.
     *
     * <p>This is a bound property.</p>
     *
     * @param outlineStrokePaint outline stroke paint for this subnetwork node
     */
    public final void setOutlineStrokePaint(final Paint outlineStrokePaint) {
        Paint oldOutlineStrokePaint = this.outlineStrokePaint;
        this.outlineStrokePaint = outlineStrokePaint;
        outline.setStrokePaint(outlineStrokePaint);
        firePropertyChange("outlineStrokePaint", oldOutlineStrokePaint, this.outlineStrokePaint);
    }

    /** @see PNode */
    protected void layoutChildren() {
        if (!initialChildLayoutComplete) {
            updateOutlineBoundsAndPath();
            initialChildLayoutComplete = true;
        }
        updateSynapseNodePositions();
    }

    /** @see PNode */
    public void addChild(final PNode child) {
        child.addPropertyChangeListener("fullBounds", this);
        super.addChild(child);
    }

    /** @see PNode */
    public PNode removeChild(final PNode child) {
        child.removePropertyChangeListener("fullBounds", this);
        PNode ret = super.removeChild(child);
        updateOutlineBoundsAndPath();
        return ret;
    }

    /** @see PropertyChangeListener */
    public void propertyChange(final PropertyChangeEvent event) {
        updateOutlineBoundsAndPath();
    }

    /**
     * Update synapse node positions.
     */
    private void updateSynapseNodePositions() {
        for (Iterator i = getChildrenIterator(); i.hasNext(); ) {
            PNode node = (PNode) i.next();
            if (node instanceof NeuronNode) {
                NeuronNode neuronNode = (NeuronNode) node;
                neuronNode.updateSynapseNodePositions();
            }
        }
    }

    /**
     * Update outline bounds and path.
     */
    private void updateOutlineBoundsAndPath() {

        // one of the child nodes' full bounds changed
        PBounds bounds = new PBounds();
        for (Iterator i = getChildrenIterator(); i.hasNext(); ) {
            PNode child = (PNode) i.next();
            if ((!tab.equals(child)) && (!outline.equals(child))) {
                PBounds childBounds = child.getBounds();
                child.localToParent(childBounds);
                bounds.add(childBounds);
            }
        }

        // add cheater
        bounds.add(bounds.getX(), bounds.getY() - TAB_HEIGHT);

        // add (0.0d, 0.0d)
        bounds.add(OUTLINE_INSET_WIDTH, OUTLINE_INSET_HEIGHT);

        // add border
        bounds.setRect(bounds.getX() - OUTLINE_INSET_WIDTH,
                       bounds.getY() - OUTLINE_INSET_HEIGHT,
                       bounds.getWidth() + (2 * OUTLINE_INSET_WIDTH),
                       bounds.getHeight() + (2 * OUTLINE_INSET_HEIGHT) - TAB_HEIGHT);

        // set outline to new bounds
        // TODO:  only update rect if it needs updating
        outline.setBounds(bounds);
        outline.setPathToRectangle((float) bounds.getX(), (float) bounds.getY(),
                                   (float) bounds.getWidth(), (float) bounds.getHeight());

        // Move tab to correct position and set its bounds to be the whole node's bounds
        tab.setOffset(bounds.getX(), bounds.getY());
        this.setBounds(tab.getFullBounds());

    }

    /**
     * @return Returns the setPropertiesAction.
     */
    public Action getSetPropertiesAction() {
        return setPropertiesAction;
    }

    /**
     * @param setPropertiesAction The setPropertiesAction to set.
     */
    public void setSetPropertiesAction(final Action setPropertiesAction) {
        this.setPropertiesAction = setPropertiesAction;
    }

    /**
     * Tab node.
     */
    private class TabNode
        extends PNode {

        /** Label. */
        private PText label;

        /** Background. */
        private PPath background;


        /**
         * Create a new tab node.
         */
        public TabNode() {
            super();

            setPickable(false);
            setChildrenPickable(false);

            label = new PText();
            label.offset(TAB_INSET_HEIGHT, TAB_INSET_WIDTH);

            double backgroundWidth = Math.max(label.getWidth() + (2 * TAB_INSET_WIDTH), DEFAULT_TAB_WIDTH);
            background = PPath.createRectangle(0.0f, 0.0f, (float) backgroundWidth, (float) TAB_HEIGHT);

            setBounds(0.0d, 0.0d, backgroundWidth, TAB_HEIGHT);

            addChild(background);
            addChild(label);
        }


        /**
         * Return the label for this tab node.
         * The label may be null.
         *
         * @return the label for this tab node
         */
        public final String getLabel() {
            return label.getText();
        }

        /**
         * Set the label text to <code>labelText</code>.
         *
         * @param labelText
         *            label text
         */
        private void setLabel(final String labelText) {
            label.setText(labelText);
            double backgroundWidth = Math.max(label.getWidth()
                    + (2 * TAB_INSET_WIDTH), DEFAULT_TAB_WIDTH);
            background.setPathToRectangle(0.0f, 0.0f, (float) backgroundWidth,
                    (float) TAB_HEIGHT);
            setBounds(0.0d, 0.0d, backgroundWidth, TAB_HEIGHT);
        }

        /**
         * Set the tab paint for this tab node to <code>tabPaint</code>.
         *
         * @param tabPaint tab paint for this tab node
         */
        public final void setTabPaint(final Paint tabPaint) {
            background.setPaint(tabPaint);
        }

        /**
         * Set the tab stroke for this tab node to <code>tabStroke</code>.
         *
         * @param tabStroke tab stroke for this tab node
         */
        public final void setTabStroke(final Stroke tabStroke) {
            background.setStroke(tabStroke);
        }

        /**
         * Set the tab stroke paint for this tab node to <code>tabStrokePaint</code>.
         *
         * @param tabStrokePaint tab stroke paint for this tab node
         */
        public final void setTabStrokePaint(final Paint tabStrokePaint) {
            background.setStrokePaint(tabStrokePaint);
        }
    }

    /**
     * Outline node.
     */
    private class OutlineNode
        extends PPath {

        /**
         * Outline node.
         */
        public OutlineNode() {
            super();

            setPickable(false);
            setChildrenPickable(false);

            offset(0.0d, TAB_HEIGHT);
            setBounds(0.0d, 0.0d, DEFAULT_OUTLINE_WIDTH, DEFAULT_OUTLINE_HEIGHT);
            setPathToRectangle(0.0f, 0.0f, (float) DEFAULT_OUTLINE_WIDTH, (float) DEFAULT_OUTLINE_HEIGHT);
        }


        /**
         * Set the outline stroke for this outline node to <code>outlineStroke</code>.
         *
         * @param outlineStroke outline stroke for this outline node
         */
        public final void setOutlineStroke(final Stroke outlineStroke) {
            setStroke(outlineStroke);
        }

        /**
         * Set the outline stroke paint for this outline node to <code>outlineStrokePaint</code>.
         *
         * @param outlineStrokePaint outline stroke paint for this outline node
         */
        public final void setOutlineStrokePaint(final Paint outlineStrokePaint) {
            setStrokePaint(outlineStrokePaint);
        }
    }

}