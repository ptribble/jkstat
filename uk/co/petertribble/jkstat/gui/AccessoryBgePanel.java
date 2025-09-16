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
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * An accessory panel that graphically represents the distribution of packet
 * sizes transmitted by a bge network interface.
 *
 * @author Peter Tribble
 */
public final class AccessoryBgePanel extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    /*
     * The bge statistics kstat has a number of etherStats
     * statistics. 11 in all.
     */
    private static final int STATNUM = 11;
    private static final String[] STATNAMES = {"etherStatsUndersizePkts",
				"etherStatsPkts64Octets",
				"etherStatsPkts65to127Octets",
				"etherStatsPkts128to255Octets",
				"etherStatsPkts256to511Octets",
				"etherStatsPkts512to1023Octets",
				"etherStatsPkts1024to1518Octets",
				"etherStatsPkts1519to2047Octets",
				"etherStatsPkts2048to4095Octets",
				"etherStatsPkts4096to8191Octets",
				"etherStatsPkts8192to9022Octets"};
    /**
     * An array to hold the old values.
     */
    private long[] oldvalues = new long[STATNUM];
    /**
     * An array to hold the new values.
     */
    private long[] newvalues = new long[STATNUM];
    /**
     * An array to hold the difference between old and new values.
     */
    private long[] deltas = new long[STATNUM];
    /**
     * The total number of new packets, used to scale the display.
     */
    private double sumdelta;
    /**
     * The total number of packets, used to scale the display.
     */
    private double sumvalues;

    /**
     * Create a panel showing the histogram of packet sizes that updates every
     * interval seconds.
     *
     * @param ks a Kstat object
     * @param interval an int specifying the update interval in seconds
     * @param jkstat a JKstat object
     */
    public AccessoryBgePanel(final Kstat ks, final int interval,
			     final JKstat jkstat) {
	super(ks, interval, jkstat);

	setMinimumSize(new Dimension(64, 4 * STATNUM));
	setPreferredSize(new Dimension(64, 4 * STATNUM));

	for (int i = 0; i < STATNUM; i++) {
	    oldvalues[i] = 0;
	}

	updateAccessory();
	startLoop();
    }

    private void loadHistoData() {
	sumdelta = 0;
	sumvalues = 0;
	for (int i = 0; i < STATNUM; i++) {
	    newvalues[i] = ks.longData(STATNAMES[i]);
	    sumvalues += newvalues[i];
	    deltas[i] = newvalues[i] - oldvalues[i];
	    sumdelta += deltas[i];
	}
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	loadHistoData();
	repaint();
	for (int i = 0; i < STATNUM; i++) {
	    oldvalues[i] = newvalues[i];
	}
    }

    @Override
    public void paint(final Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	Dimension d = getSize();
	double w = d.width;
	double h = d.height;
	// blank the background
	g2.setPaint(Color.WHITE);
	g2.fill(new Rectangle2D.Double(0, 0, w, h));
	// green is the deltas, blue the cumulative
	if (sumvalues > 0) {
	    double dh = h / ((double) 2 * STATNUM);
	    double dsscale = w / sumvalues;
	    g2.setPaint(Color.BLUE);
	    for (int i = 0; i < STATNUM; i++) {
		g2.fill(new Rectangle2D.Double(0, (2 * i + 1) * dh,
			dsscale * ((double) newvalues[i]), dh));
	    }
	    if (sumdelta > 0) {
		double ddscale = w / sumdelta;
		g2.setPaint(Color.GREEN);
		for (int i = 0; i < STATNUM; i++) {
		    g2.fill(new Rectangle2D.Double(0, 2 * i * dh,
			ddscale * ((double) deltas[i]), dh));
		}
	    }
	}
    }
}
