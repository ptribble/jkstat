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

import java.text.DecimalFormat;
import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jstripchart.JSparkChart;

/**
 * An accessory panel that represents a kstat statistic as a sparkline.
 *
 * @author Peter Tribble
 */
public final class SparkValueAccessory extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    /**
     * The chart displaying the values stored by this accessory.
     */
    private JSparkChart jsc;
    /**
     * The name of the statistic being displayed.
     */
    private String stat;
    /**
     * Whether tooltips should be shown.
     */
    private boolean dotips;
    /**
     * The text of the tooltip.
     */
    private String tiptext;
    /**
     * Whether to scale the data.
     */
    private boolean doscale;
    /**
     * If scaling, the scale factor.
     */
    private double scale;
    private static final DecimalFormat DF = new DecimalFormat("##0.00");

    /**
     * Create a panel showing a sparkline of the value of the given statistic.
     *
     * @param ks a {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     * @param statistic the statistic to display
     */
    public SparkValueAccessory(final Kstat ks, final int interval,
			       final JKstat jkstat, final String statistic) {
	super(ks, interval, jkstat);
	stat = statistic;
	init();
    }

    private void init() {
	// necessary to initialize ks
	updateKstat();

	jsc = new JSparkChart(150, 20);
	add(jsc);

	updateAccessory();
	startLoop();
    }

    /**
     * Enable tooltips. The value will be displayed as is, unscaled,
     * as an integer.
     *
     * @param ttext the initial text of the tooltip, preceding the value
     */
    public void enableTips(final String ttext) {
	dotips = true;
	tiptext = ttext;
    }

    /**
     * Enable tooltips. The value will be displayed as a scaled float to
     * 2 decimal places.
     *
     * @param ttext the initial text of the tooltip, preceding the value
     * @param dscale the scale factor to apply to the number displayed in the
     * tooltip
     */
    public void enableTips(final String ttext, final double dscale) {
	dotips = true;
	doscale = true;
	tiptext = ttext;
	scale = dscale;
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	jsc.add(ks.longData(stat));
	if (dotips) {
	    if (doscale) {
		setToolTipText(tiptext + " "
			       + DF.format(scale * ks.longData(stat)));
	    } else {
		setToolTipText(tiptext + " " + ks.longData(stat));
	    }
	}
    }
}

