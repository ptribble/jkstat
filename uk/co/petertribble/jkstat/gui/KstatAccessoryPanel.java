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

import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.JKstat;
import javax.swing.*;
import java.awt.event.*;

/**
 * A panel to hold a graphical accessory.
 *
 * @author Peter Tribble
 */
public class KstatAccessoryPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    /**
     * The current snaptime.
     */
    protected long snaptime;

    /**
     * The snaptime of the previous measurement.
     */
    protected long oldsnaptime;

    /**
     * The difference between current and previous snaptimes, and thus the
     * interval over which rates can be calculated.
     */
    protected long snapdelta;

    /**
     * A Timer, to update the model in a loop.
     */
    protected Timer timer;

    /**
     * The initial update delay, in milliseconds.
     */
    protected int delay;

    /**
     * A reference to a JKstat object.
     */
    protected JKstat jkstat;

    /**
     * The Kstat of interest.
     */
    protected Kstat ks;

    /**
     * Create an accessory panel. Subclasses should call super() with the
     * same arguments as the first part of their constructor, then have code
     * to create the panel contents, and then call startLoop().
     *
     * If the requested interval is less than or equal to zero, then this
     * panel will not update itself. In that case, it is assumed that updates
     * are initiated externally, as KstatAccessorySet would.
     *
     * Avoid passing in an interval of 0, as it may be used as the divisor
     * to calculate a rate.
     *
     * @param ks the Kstat to be represented by this accessory
     * @param interval the desired update interval, in seconds
     * @param jkstat a JKstat object
     */
    public KstatAccessoryPanel(Kstat ks, int interval, JKstat jkstat) {
	this.ks = ks;
	delay = interval*1000;
	this.jkstat = jkstat;
    }

    /**
     * Update the current Kstat.
     */
    public void updateKstat() {
	oldsnaptime = snaptime;
	ks = jkstat.getKstat(ks);
	snaptime = ks.getSnaptime();
	snapdelta = snaptime - oldsnaptime;
    }

    /**
     * Subclasses must override this method to update the display.
     */
    public void updateAccessory() {
    }

    /**
     * Start the timer loop, so that the accessory updates itself.
     */
    public void startLoop() {
	if (delay > 0) {
	    if (timer == null) {
		timer = new Timer(delay, this);
	    }
	    timer.start();
	}
    }

    /**
     * Stop the timer loop, so that the accessory will no longer be updated.
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
	updateAccessory();
    }
}
