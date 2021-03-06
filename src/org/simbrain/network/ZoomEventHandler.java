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
package org.simbrain.network;

import java.awt.geom.Point2D;

import java.awt.event.InputEvent;

import edu.umd.cs.piccolo.PCamera;

import edu.umd.cs.piccolo.util.PBounds;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;

import org.simbrain.network.nodes.SelectionMarquee;

/**
 * Zoom event handler.
 */
final class ZoomEventHandler
    extends PDragSequenceEventHandler {

    /** Zoom in factor. */
    private static final double ZOOM_IN_FACTOR = 0.5d;

    /** Zoom out factor. */
    private static final double ZOOM_OUT_FACTOR = 1.5d;

    /** Duration in microseconds of the zoom animation. */
    private static final int ZOOM_ANIMATION_DURATION = 1000;

    /** Minimum width for a marquee drag zoom event. */
    private static final double DRAG_WIDTH_THRESHOLD = 2.0d;

    /** Minimum height for a marquee drag zoom event. */
    private static final double DRAG_HEIGHT_THRESHOLD = 2.0d;

    /** Selection marquee, used to define zoom bounds on drag. */
    private SelectionMarquee marquee;

    /** Marquee selection start position. */
    private Point2D marqueeStartPosition;


    /**
     * Create a new zoom event handler.
     */
    public ZoomEventHandler() {
        super();
        setEventFilter(new ZoomEventFilter());
    }


    /** @see PDragSequenceEventHandler */
    public void mouseClicked(final PInputEvent event) {

        NetworkPanel networkPanel = (NetworkPanel) event.getComponent();
        PCamera camera = networkPanel.getCamera();

        double zoom = event.isAltDown() ? ZOOM_OUT_FACTOR : ZOOM_IN_FACTOR;

        double x = event.getPosition().getX();
        double y = event.getPosition().getY();
        double w = camera.getViewBounds().getWidth() * zoom;

        PBounds rect = new PBounds(x - (w / 2), y - (w / 2), w, w);
        camera.animateViewToCenterBounds(rect, true, ZOOM_ANIMATION_DURATION);

        rect = null;
    }

    /** @see PDragSequenceEventHandler */
    protected void startDrag(final PInputEvent event) {
        super.startDrag(event);

        marqueeStartPosition = event.getPosition();
        NetworkPanel networkPanel = (NetworkPanel) event.getComponent();

        marquee = new SelectionMarquee((float) marqueeStartPosition.getX(),
                                       (float) marqueeStartPosition.getY());

        networkPanel.getLayer().addChild(marquee);
    }

    /** @see PDragSequenceEventHandler */
    protected void drag(final PInputEvent event) {
        super.drag(event);

        NetworkPanel networkPanel = (NetworkPanel) event.getComponent();
        Point2D position = event.getPosition();
        PBounds rect = new PBounds();
        rect.add(marqueeStartPosition);
        rect.add(position);

        marquee.globalToLocal(rect);
        marquee.setPathToRectangle((float) rect.getX(), (float) rect.getY(),
                                   (float) rect.getWidth(), (float) rect.getHeight());
        rect = null;
    }

    /** @see PDragSequenceEventHandler */
    protected void endDrag(final PInputEvent event) {
        super.endDrag(event);

        // even short single clicks are (incorrectly) recognized as drag
        // sequences, so we'll arbitrarily decide that marquee bounds rects
        // of less than a certian size were the result of a single click
        // and not of a click and drag to create a marquee
        PBounds zoomRect = marquee.getGlobalBounds();
        if ((zoomRect.getWidth() >= DRAG_WIDTH_THRESHOLD) || (zoomRect.getHeight() >= DRAG_HEIGHT_THRESHOLD)) {
            NetworkPanel networkPanel = (NetworkPanel) event.getComponent();
            PCamera camera = networkPanel.getCamera();
            camera.animateViewToCenterBounds(zoomRect, true, ZOOM_ANIMATION_DURATION);
        }

        marquee.removeFromParent();
        marquee = null;
        marqueeStartPosition = null;
    }


    /**
     * Zoom event filter, accepts left mouse clicks, but only when the network
     * panel's edit mode is either <code>EditMode.ZOOM_IN</code> or <code>EditMode.ZOOM_OUT</code>.
     */
    private class ZoomEventFilter
        extends PInputEventFilter {

        /**
         * Create a new zoom event filter.
         */
        public ZoomEventFilter() {
            super(InputEvent.BUTTON1_MASK);
        }


        /** @see PInputEventFilter */
        public boolean acceptsEvent(final PInputEvent event, final int type) {

            NetworkPanel networkPanel = (NetworkPanel) event.getComponent();
            EditMode editMode = networkPanel.getEditMode();

            return (editMode.isZoom() && super.acceptsEvent(event, type));
        }
    }
}