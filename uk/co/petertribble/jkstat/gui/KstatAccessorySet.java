/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
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
public class KstatAccessorySet implements ActionListener {

    private Collection <KstatAccessoryPanel> accessories;
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
	delay = interval*1000;
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
	delay = interval*1000;
	if (timer != null) {
	    timer.setDelay(delay);
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	updateAccessories();
    }
}
