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
import uk.co.petertribble.jingle.SpringUtilities;
import javax.swing.*;
import java.text.DecimalFormat;

/**
 * An accessory to display kernel memory allocation statistics.
 */
public final class AccessoryKmemAlloc extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private DecimalFormat df = new DecimalFormat("##0.0#");

    private JProgressBar jpAlloc;
    private JProgressBar jpFree;

    private long iomax = 100;
    private long numalloc;
    private long numfree;

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

	setLayout(new SpringLayout());

	add(jpAlloc);
	add(new JLabel("alloc/s "));
	add(jpFree);
	add(new JLabel("free/s"));
	SpringUtilities.makeCompactGrid(this, 1, 4, 6, 3, 2, 2);
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
