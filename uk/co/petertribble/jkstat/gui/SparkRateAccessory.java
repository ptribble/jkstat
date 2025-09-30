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
import uk.co.petertribble.jstripchart.JSparkChart;

/**
 * An accessory panel that represents a kstat statistic as a sparkline.
 *
 * @author Peter Tribble
 */
public final class SparkRateAccessory extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    /**
     * The chart displaying the values stored by this accessory.
     */
    private JSparkChart jsc;
    /**
     * The saved rate.
     */
    private long r;
    /**
     * The name of the statistic being displayed.
     */
    private String stat;
    /**
     * Whether tooltips should be shown.
     */
    private boolean tips;
    /**
     * The text of the tooltip.
     */
    private String tiptext;
    /**
     * The scale factor for the tooltip.
     */
    private double scale;

    /**
     * Create a panel showing a sparkline of the rate of change of the given
     * statistic.
     *
     * @param ks a {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat {@code JKstat}
     * @param statistic the statistic to display
     */
    public SparkRateAccessory(final Kstat ks, final int interval,
			      final JKstat jkstat, final String statistic) {
	super(ks, interval, jkstat);
	stat = statistic;
	init();
    }

    private void init() {
	// necessary to initialize ks
	updateKstat();
	r = ks.longData(stat);

	jsc = new JSparkChart(150, 20);
	add(jsc);

	updateAccessory();
	startLoop();
    }

    /**
     * Enable tooltips.
     *
     * @param ttext the initial text of the tooltip, preceding the rate
     */
    public void enableTips(final String ttext) {
	enableTips(ttext, 1.0d);
    }

    /**
     * Enable tooltips.
     *
     * @param ttext the initial text of the tooltip, preceding the rate
     * @param dscale the scale factor to apply to the number displayed in the
     * tooltip
     */
    public void enableTips(final String ttext, final double dscale) {
	tips = true;
	tiptext = ttext;
	scale = dscale;
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	long nr = ks.longData(stat);
	jsc.add(nr - r);
	// the delay may be negative if externally driven, hence absolute
	if (tips) {
	    setToolTipText(tiptext + " "
			+ scale * 1000.0 * (nr - r) / Math.abs(delay));
	}
	r = nr;
    }
}
