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
import javax.swing.*;
import java.text.DecimalFormat;
import uk.co.petertribble.jingle.SpringUtilities;

/**
 * An accessory panel that graphically represents the system load average.
 *
 * @author Peter Tribble
 */
public class AccessoryLoadPanel extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    private double lmax = 1.0;
    private static final double LSCALE = 256.0;

    private DecimalFormat df = new DecimalFormat("##0.00");
    private JProgressBar jp1;
    private JProgressBar jp5;
    private JProgressBar jp15;

    /**
     * Create a panel showing the 1,5,15 minute load averages in Progress Bars
     * that update every interval seconds.
     *
     * @param ks a unix:0:system_misc kstat
     * @param interval the update interval in seconds
     * @param jkstat a JKstat
     */
    public AccessoryLoadPanel(Kstat ks, int interval, JKstat jkstat) {
	super(ks, interval, jkstat);
	init();
    }

    private void init() {
	updateKstat();

	// initial scale set by the number of cpus present
	lmax = (double) ks.longData("ncpus");

	jp1 = new JProgressBar(0, (int) LSCALE);
	jp5 = new JProgressBar(0, (int) LSCALE);
	jp15 = new JProgressBar(0, (int) LSCALE);

	jp1.setStringPainted(true);
	jp5.setStringPainted(true);
	jp15.setStringPainted(true);

	setLayout(new SpringLayout());

	add(new JLabel("1 minute load"));
	add(jp1);
	add(new JLabel("5 minute load"));
	add(jp5);
	add(new JLabel("15 minute load"));
	add(jp15);
	SpringUtilities.makeCompactGrid(this, 3, 2, 6, 3, 2, 2);

	updateAccessory();

	startLoop();
    }

    @Override
    public void updateAccessory() {
	updateKstat();
	long l1 = ks.longData("avenrun_1min");
	long l5 = ks.longData("avenrun_5min");
	long l15 = ks.longData("avenrun_15min");
	while (l1 > lmax*LSCALE || l5 > lmax*LSCALE || l15 > lmax*LSCALE) {
	    lmax = lmax*2.0;
	}
	jp1.setValue((int) (l1/lmax));
	jp5.setValue((int) (l5/lmax));
	jp15.setValue((int) (l15/lmax));
	jp1.setString(df.format(l1/LSCALE));
	jp5.setString(df.format(l5/LSCALE));
	jp15.setString(df.format(l15/LSCALE));
    }
}
