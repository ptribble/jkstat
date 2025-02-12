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
import javax.swing.SwingConstants;

/**
 * An accessory panel that graphically represents cpu activity in the
 * style of xcpustate.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class AccessoryCpuPanel extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private long luser;
    private long lsys;
    private long lwait;
    private long lidle;
    private double duser;
    private double dsys;
    private double dwait;
    private double didle;

    private int orientation;

    /**
     * Create a panel showing the cpu activity.
     *
     * @param ks a cpu_stat {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     */
    public AccessoryCpuPanel(Kstat ks, int interval, JKstat jkstat) {
	this(ks, interval, jkstat, SwingConstants.HORIZONTAL);
    }

    /**
     * Create a panel showing the cpu activity.
     *
     * @param ks a cpu_stat {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     * @param orientation the desired orientation of the accessory, which
     * should be either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL
     */
    public AccessoryCpuPanel(Kstat ks, int interval, JKstat jkstat,
			int orientation) {
	super(ks, interval, jkstat);
	this.orientation = orientation;

	if (orientation == SwingConstants.VERTICAL) {
	    setMinimumSize(new Dimension(12, 48));
	    setPreferredSize(new Dimension(12, 48));
	} else {
	    setMinimumSize(new Dimension(64, 32));
	    setPreferredSize(new Dimension(64, 32));
	}

	updateAccessory();

	startLoop();
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	long nuser = ks.longData("user");
	long nsys = ks.longData("kernel");
	long nwait = ks.longData("wait");
	long nidle = ks.longData("idle");

	duser = nuser - luser;
	dsys = nsys - lsys;
	dwait = nwait - lwait;
	didle = nidle - lidle;

	repaint();

	luser = nuser;
	lsys = nsys;
	lwait = nwait;
	lidle = nidle;
    }

    @Override
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;
	Dimension d = getSize();

	double h = d.height;
	double w = d.width;
	double x = 0.0d;
	double dscale = ((orientation == SwingConstants.VERTICAL) ? h : w)
	    / (duser + dsys + dwait + didle);
	double dx = dscale * didle;
	g2.setPaint(Color.BLUE);
	if (orientation == SwingConstants.VERTICAL) {
	    g2.fill(new Rectangle2D.Double(0.0d, x, w, dx));
	} else {
	    g2.fill(new Rectangle2D.Double(x, 0.0d, dx, h));
	}
	x += dx;
	dx = dscale * dwait;
	g2.setPaint(Color.RED);
	if (orientation == SwingConstants.VERTICAL) {
	    g2.fill(new Rectangle2D.Double(0.0d, x, w, dx));
	} else {
	    g2.fill(new Rectangle2D.Double(x, 0.0d, dx, h));
	}
	x += dx;
	dx = dscale * duser;
	g2.setPaint(Color.GREEN);
	if (orientation == SwingConstants.VERTICAL) {
	    g2.fill(new Rectangle2D.Double(0.0d, x, w, dx));
	} else {
	    g2.fill(new Rectangle2D.Double(x, 0.0d, dx, h));
	}
	x += dx;
	dx = dscale * dsys;
	g2.setPaint(Color.YELLOW);
	if (orientation == SwingConstants.VERTICAL) {
	    g2.fill(new Rectangle2D.Double(0.0d, x, w, dx));
	} else {
	    g2.fill(new Rectangle2D.Double(x, 0.0d, dx, h));
	}
    }
}
