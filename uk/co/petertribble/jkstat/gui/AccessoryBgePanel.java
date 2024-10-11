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
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * An accessory panel that graphically represents the distribution of packet
 * sizes transmitted by a bge network interface.
 *
 * @author Peter Tribble
 */
public class AccessoryBgePanel extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    /*
     * The bge statistics kstat has a number of etherStats
     * statistics. 11 in all
     */
    private static int STATNUM = 11;
    private static String[] statNames = { "etherStatsUndersizePkts",
				"etherStatsPkts64Octets",
				"etherStatsPkts65to127Octets",
				"etherStatsPkts128to255Octets",
				"etherStatsPkts256to511Octets",
				"etherStatsPkts512to1023Octets",
				"etherStatsPkts1024to1518Octets",
				"etherStatsPkts1519to2047Octets",
				"etherStatsPkts2048to4095Octets",
				"etherStatsPkts4096to8191Octets",
				"etherStatsPkts8192to9022Octets" };
    private long[] oldvalues = new long[STATNUM];
    private long[] newvalues = new long[STATNUM];
    private long[] deltas = new long[STATNUM];
    private double sumdelta;
    private double sumvalues;

    /**
     * Create a panel showing the histogram of packet sizes that updates every
     * interval seconds.
     *
     * @param ks a Kstat object
     * @param interval an int specifying the update interval in seconds
     * @param jkstat a JKstat object
     */
    public AccessoryBgePanel(Kstat ks, int interval, JKstat jkstat) {
	super(ks, interval, jkstat);

	setMinimumSize(new Dimension(64, 4*STATNUM));
	setPreferredSize(new Dimension(64, 4*STATNUM));

	for (int i = 0; i < STATNUM; i++) {
	    oldvalues[i] = 0;
	}

	updateAccessory();
	startLoop();
    }

    private void getHistoData() {
	sumdelta = 0;
	sumvalues = 0;
	for (int i = 0; i < STATNUM; i++) {
	    newvalues[i] = ks.longData(statNames[i]);
	    sumvalues += newvalues[i];
	    deltas[i] = newvalues[i] - oldvalues[i];
	    sumdelta += deltas[i];
	}
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	getHistoData();
	repaint();
	for (int i = 0; i < STATNUM; i++) {
	    oldvalues[i] = newvalues[i];
	}
    }

    @Override
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	Dimension d = getSize();
	double w = d.width;
	double h = d.height;
	// blank the background
	g2.setPaint(Color.WHITE);
	g2.fill(new Rectangle2D.Double(0, 0, w, h));
	// green is the deltas, blue the cumulative
	if (sumvalues > 0) {
	    double dh = h/((double) 2*STATNUM);
	    double dsscale = w/sumvalues;
	    g2.setPaint(Color.BLUE);
	    for (int i = 0; i < STATNUM; i++) {
		g2.fill(new Rectangle2D.Double(0, (2*i+1)*dh,
			dsscale*((double) newvalues[i]), dh));
	    }
	    if (sumdelta > 0) {
		double ddscale = w/sumdelta;
		g2.setPaint(Color.GREEN);
		for (int i = 0; i < STATNUM; i++) {
		    g2.fill(new Rectangle2D.Double(0, (2*i)*dh,
			ddscale*((double) deltas[i]), dh));
		}
	    }
	}
    }
}
