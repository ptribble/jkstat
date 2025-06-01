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
import uk.co.petertribble.jstripchart.JStripChart2;
import java.awt.BorderLayout;

/**
 * An accessory panel that displays a strip chart of the system load average.
 *
 * @author Peter Tribble
 */
public final class AccessoryLoadChart extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private static final double LSCALE = 256.0;

    /**
     * The stripchart embedded in this panel.
     */
    private JStripChart2 jsc;

    /**
     * Create a panel showing a strip chart of the 1 and 15 minute load
     * averages that updates every interval seconds.
     *
     * @param ks a unix:0:system_misc kstat
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     */
    public AccessoryLoadChart(Kstat ks, int interval, JKstat jkstat) {
	super(ks, interval, jkstat);
	setLayout(new BorderLayout());

	jsc = new JStripChart2();
	jsc.setStyle(JStripChart.STYLE_SOLID);
	add(jsc);

	updateAccessory();
	startLoop();
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	jsc.add(ks.longData("avenrun_1min") / LSCALE,
		ks.longData("avenrun_15min") / LSCALE);
    }
}
