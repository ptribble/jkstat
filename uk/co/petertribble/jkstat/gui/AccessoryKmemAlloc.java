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
 * An accessory to display kernel memory allocation statistics.
 */
public final class AccessoryKmemAlloc extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    /**
     * A formatter to put numerical labels inside the progress bar.
     */
    private DecimalFormat df = new DecimalFormat("##0.0#");

    /**
     * A progress bar used to show allocation rate.
     */
    private JProgressBar jpAlloc;
    /**
     * A progress bar used to show free rate.
     */
    private JProgressBar jpFree;

    /**
     * The current maximum value of the bars, will be scaled as appropriate.
     */
    private long iomax = 100;
    private transient long numalloc;
    private transient long numfree;

    /**
     * Create a panel showing memory allocation statistics that updates every
     * interval seconds.
     *
     * @param ks a kmem_alloc kstat
     * @param interval the update interval in seconds
     * @param jkstat a JKstat
     */
    public AccessoryKmemAlloc(Kstat ks, int interval, JKstat jkstat) {
	super(ks, interval, jkstat);

	jpAlloc = new JProgressBar(0, (int) iomax);
	jpFree = new JProgressBar(0, (int) iomax);
	// these will be rescaled later if need be

	jpAlloc.setStringPainted(true);
	jpFree.setStringPainted(true);

	JLabel jlAlloc = new JLabel("alloc/s");
	JLabel jlFree = new JLabel("free/s");

	GroupLayout layout = new GroupLayout(this);
	setLayout(layout);
	// the label text is vertically centered
	GroupLayout.Alignment gac = GroupLayout.Alignment.CENTER;
	// horizontally, we have a sequential group of bar label bar label
	// vertically, we have a sequential group containing 1 parallel
	// group each with a label and its bar
	// there's a 6 pixel gap on the left, 2 pixels other sides and between
	// elements
	layout.setHorizontalGroup(
		layout.createSequentialGroup()
		.addGap(6)
		.addComponent(jpAlloc)
		.addGap(2)
		.addComponent(jlAlloc)
		.addGap(6)
		.addComponent(jpFree)
		.addGap(2)
		.addComponent(jlFree)
		.addGap(2)
	);
	layout.setVerticalGroup(
		layout.createSequentialGroup()
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jpAlloc)
			  .addComponent(jlAlloc)
			  .addComponent(jpFree)
			  .addComponent(jlFree)
			  )
		.addGap(2)
	);

	updateAccessory();
	startLoop();
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	long nnumalloc = ks.longData("alloc");
	long nnumfree = ks.longData("free");
	int i = (int) ((nnumalloc - numalloc) * 1000000000 / snapdelta);
	int j = (int) ((nnumfree - numfree) * 1000000000 / snapdelta);
	while ((i > iomax) || (j > iomax)) {
	    iomax <<= 1;
	    jpAlloc.setMaximum((int) iomax);
	    jpFree.setMaximum((int) iomax);
	}
	jpAlloc.setValue(i);
	jpAlloc.setString(df.format(i));
	jpFree.setValue(j);
	jpFree.setString(df.format(j));
	numalloc = nnumalloc;
	numfree = nnumfree;
    }
}
