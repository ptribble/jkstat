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
import javax.swing.*;
import java.text.DecimalFormat;

/**
 * An accessory panel that graphically represents the system load average.
 *
 * @author Peter Tribble
 */
public final class AccessoryLoadPanel extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    /**
     * The maximum load displayed by the progress bars.
     */
    private double lmax = 1.0;
    private static final double LSCALE = 256.0;

    /**
     * A formatter to put numerical labels inside the progress bar.
     */
    private DecimalFormat df = new DecimalFormat("##0.00");
    /**
     * A progress bar used to show the 1-minute load average.
     */
    private JProgressBar jp1;
    /**
     * A progress bar used to show the 5-minute load average.
     */
    private JProgressBar jp5;
    /**
     * A progress bar used to show the 15-minute load average.
     */
    private JProgressBar jp15;

    /**
     * Create a panel showing the 1,5,15 minute load averages in Progress Bars
     * that update every interval seconds.
     *
     * @param ks a unix:0:system_misc kstat
     * @param interval the update interval in seconds
     * @param jkstat a JKstat
     */
    public AccessoryLoadPanel(Kstat ks, int interval, JKstat jkstat) {
	super(ks, interval, jkstat);
	init();
    }

    private void init() {
	updateKstat();

	// initial scale set by the number of cpus present
	lmax = (double) ks.longData("ncpus");

	jp1 = new JProgressBar(0, (int) LSCALE);
	jp5 = new JProgressBar(0, (int) LSCALE);
	jp15 = new JProgressBar(0, (int) LSCALE);

	jp1.setStringPainted(true);
	jp5.setStringPainted(true);
	jp15.setStringPainted(true);

	JLabel jl1 = new JLabel("1 minute load");
	JLabel jl5 = new JLabel("5 minute load");
	JLabel jl15 = new JLabel("15 minute load");

	GroupLayout layout = new GroupLayout(this);
	setLayout(layout);
	// the label text is vertically centered
	GroupLayout.Alignment gac = GroupLayout.Alignment.CENTER;
	// horizontally, we have a sequential group containing a parallel
	// group of all the labels and a parallel group of the bars
	// vertically, we have a sequential group containing 3 parallel
	// groups each with a label and its bar
	// there's a 6 pixel gap on the left, 2 pixels other sides and between
	// elements
	layout.setHorizontalGroup(
		layout.createSequentialGroup()
		.addGap(6)
		.addGroup(layout.createParallelGroup()
			  .addComponent(jl1)
			  .addComponent(jl5)
			  .addComponent(jl15)
			  )
		.addGap(2)
		.addGroup(layout.createParallelGroup()
			  .addComponent(jp1)
			  .addComponent(jp5)
			  .addComponent(jp15)
			  )
		.addGap(2)
	);
	layout.setVerticalGroup(
		layout.createSequentialGroup()
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jl1)
			  .addComponent(jp1)
			  )
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jl5)
			  .addComponent(jp5)
			  )
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jl15)
			  .addComponent(jp15)
			  )
		.addGap(2)
	);

	updateAccessory();

	startLoop();
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	long l1 = ks.longData("avenrun_1min");
	long l5 = ks.longData("avenrun_5min");
	long l15 = ks.longData("avenrun_15min");
	double lsmax = lmax * LSCALE;
	while (l1 > lsmax || l5 > lsmax || l15 > lsmax) {
	    lmax = lmax * 2.0;
	    lsmax = lsmax * 2.0;
	}
	jp1.setValue((int) (l1 / lmax));
	jp5.setValue((int) (l5 / lmax));
	jp15.setValue((int) (l15 / lmax));
	jp1.setString(df.format(l1 / LSCALE));
	jp5.setString(df.format(l5 / LSCALE));
	jp15.setString(df.format(l15 / LSCALE));
    }
}
