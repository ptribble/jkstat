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

import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.util.Humanize;
import javax.swing.*;
import java.awt.Dimension;
import uk.co.petertribble.jingle.SpringUtilities;

/**
 * An accessory panel that graphically represents network activity.
 *
 * @author Peter Tribble
 */
public class AccessoryNetPanel extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private JProgressBar jpIn;
    private JProgressBar jpOut;

    private long r;
    private long w;

    /**
     * Create a panel showing the input and output traffic on the given network
     * interface that update every interval seconds.
     *
     * @param ks a network kstat
     * @param interval the update interval in seconds
     * @param jkstat a JKstat
     */
    public AccessoryNetPanel(Kstat ks, int interval, JKstat jkstat) {
	super(ks, interval, jkstat);
	init();
    }

    private void init() {
	// necessary to initialize ks
	updateKstat();

	// try to deduce the network speed, assume 100M if we can't
	long netmax = ks.isNumeric("ifspeed") ? ks.longData("ifspeed")
	    : 100000000;
	if (netmax == 0) {
	    netmax = 100000000;
	}

	// the progress meters are measured in kbytes/s
	jpIn = new JProgressBar(0, (int) (netmax/8192));
	jpOut = new JProgressBar(0, (int) (netmax/8192));

	jpIn.setMinimumSize(new Dimension(150, 20));
	jpOut.setMinimumSize(new Dimension(150, 20));
	jpIn.setPreferredSize(new Dimension(150, 20));
	jpOut.setPreferredSize(new Dimension(150, 20));

	jpIn.setStringPainted(true);
	jpOut.setStringPainted(true);

	setLayout(new SpringLayout());

	add(new JLabel("Input"));
	add(jpIn);

	add(new JLabel("Output"));
	add(jpOut);
	SpringUtilities.makeCompactGrid(this, 2, 2, 6, 3, 2, 2);

	snaptime = ks.getCrtime();
	updateAccessory();

	startLoop();
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	long nr = ks.longData("rbytes64");
	long nw = ks.longData("obytes64");
	jpIn.setValue((int) ((nr-r)*1000000000/(1024*snapdelta)));
	jpOut.setValue((int) ((nw-w)*1000000000/(1024*snapdelta)));
	jpIn.setString(Humanize.scale((nr-r)*1000000000/snapdelta, "bytes/s"));
	jpOut.setString(Humanize.scale((nw-w)*1000000000/snapdelta, "bytes/s"));
	r = nr;
	w = nw;
    }
}
