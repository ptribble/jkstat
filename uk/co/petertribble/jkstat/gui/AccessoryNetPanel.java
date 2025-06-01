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
public final class AccessoryNetPanel extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    /**
     * A progress bar used to show input traffic.
     */
    private JProgressBar jpIn;
    /**
     * A progress bar used to show output traffic.
     */
    private JProgressBar jpOut;

    /**
     * Save the value of reads.
     */
    private long r;
     /**
     * Save the value of writes.
     */
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
	jpIn = new JProgressBar(0, (int) (netmax / 8192));
	jpOut = new JProgressBar(0, (int) (netmax / 8192));

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
	jpIn.setValue((int) ((nr - r) * 1000000000 / (1024 * snapdelta)));
	jpOut.setValue((int) ((nw - w) * 1000000000 / (1024 * snapdelta)));
	jpIn.setString(Humanize.scale((nr - r) * 1000000000 / snapdelta,
				"bytes/s"));
	jpOut.setString(Humanize.scale((nw - w) * 1000000000 / snapdelta,
				"bytes/s"));
	r = nr;
	w = nw;
    }
}
