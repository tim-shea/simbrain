package org.simbrain.network.nodes.subnetworks;

import javax.swing.JDialog;
import javax.swing.JPopupMenu;

import org.simbrain.network.NetworkPanel;
import org.simbrain.network.dialog.network.WTAPropertiesDialog;
import org.simbrain.network.nodes.SubnetworkNode;
import org.simnet.networks.WinnerTakeAll;

/**
 * <b>BackpropNetworkNode</b> is the graphical representation of a Backprop network.
 */
public class WTANetworkNode extends SubnetworkNode {


    /**
     * Create a new CompetitiveNetworkNode.
     *
     * @param networkPanel reference to network panel
     * @param subnetwork reference to subnetwork
     * @param x initial x position
     * @param y initial y position
     */
    public WTANetworkNode(final NetworkPanel networkPanel,
                                     final WinnerTakeAll subnetwork,
                                     final double x,
                                     final double y) {

        super(networkPanel, subnetwork, x, y);
    }

    /** @see org.simbrain.network.nodes.ScreenElement */
    protected boolean hasToolTipText() {
        return true;
    }

    /** @see org.simbrain.network.nodes.ScreenElement */
    protected String getToolTipText() {
        return "Backprop Network";
    }

    /** @see org.simbrain.network.nodes.ScreenElement */
    protected boolean hasContextMenu() {
        return true;
    }

    /** @see org.simbrain.network.nodes.ScreenElement */
    protected JPopupMenu getContextMenu() {
        JPopupMenu contextMenu = super.getContextMenu();
        contextMenu.add(super.getSetPropertiesAction());
        return contextMenu;

    }

    /** @see org.simbrain.network.nodes.ScreenElement */
    protected boolean hasPropertyDialog() {
        return true;
    }

    /** @see org.simbrain.network.nodes.ScreenElement */
    protected JDialog getPropertyDialog() {
        return new WTAPropertiesDialog(getWTASubnetwork()); }

    /** @see org.simbrain.network.nodes.ScreenElement */
    public WinnerTakeAll getWTASubnetwork() {
        return ((WinnerTakeAll) getSubnetwork());
    }

}
