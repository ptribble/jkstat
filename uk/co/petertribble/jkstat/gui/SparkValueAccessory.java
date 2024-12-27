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

import java.text.DecimalFormat;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jstripchart.JSparkChart;

/**
 * An accessory panel that represents a kstat statistic as a sparkline.
 *
 * @author Peter Tribble
 */
public class SparkValueAccessory extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private JSparkChart jsc;
    private String stat;
    private boolean dotips;
    private String tiptext;
    private boolean doscale;
    private double scale;
    private static final DecimalFormat DF = new DecimalFormat("##0.00");

    /**
     * Create a panel showing a sparkline of the value of the given statistic.
     *
     * @param ks a {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     * @param stat the statistic to display
     */
    public SparkValueAccessory(Kstat ks, int interval, JKstat jkstat,
			String stat) {
	super(ks, interval, jkstat);
	this.stat = stat;
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
     * @param tiptext the initial text of the tooltip, preceding the value
     */
    public void enableTips(String tiptext) {
	dotips = true;
	this.tiptext = tiptext;
    }

    /**
     * Enable tooltips. The value will be displayed as a scaled float to
     * 2 decimal places.
     *
     * @param tiptext the initial text of the tooltip, preceding the value
     * @param scale the scale factor to apply to the number displayed in the
     * tooltip
     */
    public void enableTips(String tiptext, double scale) {
	dotips = true;
	doscale = true;
	this.tiptext = tiptext;
	this.scale = scale;
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

