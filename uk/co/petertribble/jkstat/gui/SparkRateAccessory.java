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
import uk.co.petertribble.jstripchart.JSparkChart;

/**
 * An accessory panel that represents a kstat statistic as a sparkline.
 *
 * @author Peter Tribble
 */
public class SparkRateAccessory extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private JSparkChart jsc;
    private long r;
    private String stat;
    private boolean tips;
    private String tiptext;
    private double scale;

    /**
     * Create a panel showing a sparkline of the rate of change of the given
     * statistic.
     *
     * @param ks a {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat {@code JKstat}
     * @param stat the statistic to display
     */
    public SparkRateAccessory(Kstat ks, int interval, JKstat jkstat,
			String stat) {
	super(ks, interval, jkstat);
	this.stat = stat;
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
     * @param tiptext the initial text of the tooltip, preceding the rate
     */
    public void enableTips(String tiptext) {
	enableTips(tiptext, 1.0d);
    }

    /**
     * Enable tooltips.
     *
     * @param tiptext the initial text of the tooltip, preceding the rate
     * @param scale the scale factor to apply to the number displayed in the
     * tooltip
     */
    public void enableTips(String tiptext, double scale) {
	tips = true;
	this.tiptext = tiptext;
	this.scale = scale;
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	long nr = ks.longData(stat);
	jsc.add(nr-r);
	// the delay may be negative if externally driven, hence absolute
	if (tips) {
	    setToolTipText(tiptext + " " + scale*1000.0*(nr-r)/Math.abs(delay));
	}
	r = nr;
    }
}
