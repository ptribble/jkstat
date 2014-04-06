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

	jsc.add(dsys/dscale, duser/dscale);

	luser = nuser;
	lsys = nsys;
	lidle = nidle;
    }
}
