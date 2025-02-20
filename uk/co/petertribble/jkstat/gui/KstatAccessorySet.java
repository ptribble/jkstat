/*
 * SPDX-License-Identifier: CDDL-1.0
 *
 * CDDL HEADER START
 *
 * This file and its contents are supplied under the terms of the
 * Common Development and Distribution License ("CDDL"), version 1.0.
 * You may only use this file in accordance with the terms of version
 * 1.0 of the CDDL.
 *
 * A full copy of the text of the CDDL should have accompanied this
 * source. A copy of the CDDL is also available via the Internet at
 * http://www.illumos.org/license/CDDL.
 *
 * CDDL HEADER END
 *
 * Copyright 2025 Peter Tribble
 *
 */

package uk.co.petertribble.jkstat.gui;

import java.util.Collection;
import javax.swing.Timer;
import java.awt.event.*;

/**
 * A class that allows a number of Kstat accessories to be controlled as one.
 *
 * @author Peter Tribble
 */
public final class KstatAccessorySet implements ActionListener {

    private Collection<KstatAccessoryPanel> accessories;
    private Timer timer;
    private int delay;

    /**
     * Constructs a KstatAccessorySet object, to manage a collection of
     * KstatAccessoryPanel objects.
     *
     * @param accessories a Collection of accessories
     * @param interval the update interval of the accessories
     */
    public KstatAccessorySet(Collection<KstatAccessoryPanel> accessories,
							int interval) {
	this.accessories = accessories;
	delay = interval * 1000;
	startLoop();
    }

    /**
     * Update all the accessories.
     */
    public void updateAccessories() {
	for (KstatAccessoryPanel kap : accessories) {
	    kap.updateAccessory();
	}
    }

    /**
     * Start the timer loop, so that the accessories are regularly updated.
     */
    public void startLoop() {
	if (timer == null) {
	    timer = new Timer(delay, this);
	}
	timer.start();
    }

    /**
     * Stop the timer loop, so that the accessories will no longer be updated.
     */
    public void stopLoop() {
	if (timer != null) {
	    timer.stop();
	}
    }

    /**
     * Set the accessory update delay, which should be specified in seconds.
     *
     * @param interval the desired delay, in seconds
     */
    public void setDelay(int interval) {
	delay = interval * 1000;
	if (timer != null) {
	    timer.setDelay(delay);
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	updateAccessories();
    }
}
