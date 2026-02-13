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
 * Copyright 2026 Peter Tribble
 *
 */

package uk.co.petertribble.jkstat.gui;

import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.util.Humanize;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.Dimension;

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
    public AccessoryNetPanel(final Kstat ks, final int interval,
			     final JKstat jkstat) {
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

	JLabel jlIn = new JLabel("Input");
	JLabel jlOut = new JLabel("Output");

	GroupLayout layout = new GroupLayout(this);
	setLayout(layout);
	// the label text is vertically centered
	GroupLayout.Alignment gac = GroupLayout.Alignment.CENTER;
	// horizontally, we have a sequential group containing a parallel
	// group of all the labels and a parallel group of the bars
	// vertically, we have a sequential group containing 2 parallel
	// groups each with a label and its bar
	// there's a 6 pixel gap on the left, 2 pixels other sides and between
	// elements
	layout.setHorizontalGroup(
		layout.createSequentialGroup()
		.addGap(6)
		.addGroup(layout.createParallelGroup()
			  .addComponent(jlIn)
			  .addComponent(jlOut)
			  )
		.addGap(2)
		.addGroup(layout.createParallelGroup()
			  .addComponent(jpIn)
			  .addComponent(jpOut)
			  )
		.addGap(2)
	);
	layout.setVerticalGroup(
		layout.createSequentialGroup()
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jlIn)
			  .addComponent(jpIn)
			  )
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jlOut)
			  .addComponent(jpOut)
			  )
		.addGap(2)
	);

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
