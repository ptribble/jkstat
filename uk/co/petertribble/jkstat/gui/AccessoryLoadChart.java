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
import uk.co.petertribble.jstripchart.JStripChart2;
import java.awt.BorderLayout;

/**
 * An accessory panel that displays a strip chart of the system load average.
 *
 * @author Peter Tribble
 */
public class AccessoryLoadChart extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private static final double LSCALE = 256.0;

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
	jsc.add(ks.longData("avenrun_1min")/LSCALE,
		ks.longData("avenrun_15min")/LSCALE);
    }
}
