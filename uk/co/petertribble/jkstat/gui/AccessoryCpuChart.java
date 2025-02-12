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
import uk.co.petertribble.jstripchart.JStripChart;
import uk.co.petertribble.jstripchart.JStackedStripChart2;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * An accessory panel that graphically charts cpu utilization.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class AccessoryCpuChart extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private long luser;
    private long lsys;
    private long lidle;

    private JStackedStripChart2 jsc;

    /**
     * Create a panel showing a strip chart of the cpu utilization that
     * updates every interval seconds.
     *
     * @param ks a unix:0:system_misc kstat
     * @param interval the update interval in seconds
     * @param jkstat a JKstat
     */
    public AccessoryCpuChart(Kstat ks, int interval, JKstat jkstat) {
	super(ks, interval, jkstat);
	setLayout(new BorderLayout());

	jsc = new JStackedStripChart2(150, 64, Color.BLUE, Color.YELLOW,
				Color.GREEN);
	jsc.setStyle(JStripChart.STYLE_SOLID);
	jsc.setMax(1.0d);
	add(jsc);

	updateAccessory();
	startLoop();
    }

    @Override
    public void updateAccessory() {
	updateKstat();

	long nuser = ks.longData("user");
	long nsys = ks.longData("kernel");
	long nidle = ks.longData("idle");

	double duser = nuser - luser;
	double dsys = nsys - lsys;
	double didle = nidle - lidle;

	double dscale = duser + dsys + didle;

	jsc.add(dsys / dscale, duser / dscale);

	luser = nuser;
	lsys = nsys;
	lidle = nidle;
    }
}
