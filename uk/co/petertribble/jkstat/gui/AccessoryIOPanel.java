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
import javax.swing.*;
import java.text.DecimalFormat;

/**
 * An accessory panel that graphically represents I/O activity.
 *
 * @author Peter Tribble
 */
public final class AccessoryIOPanel extends KstatAccessoryPanel {

    private static final long serialVersionUID = 1L;

    /**
     * The current max scale of io numbers.
     */
    private int iomax = 1000;
    /**
     * The current max scale of io rates.
     */
    private int kiomax = 10000;

    /**
     * A formatter to put numerical labels inside the progress bar.
     */
    private DecimalFormat df = new DecimalFormat("##0.0");

    /**
     * A progress bar used to show number of input IOs.
     */
    private JProgressBar jpIn;
    /**
     * A progress bar used to show number of output IOs.
     */
    private JProgressBar jpOut;
    /**
     * A progress bar used to show input IO.
     */
    private JProgressBar jpkIn;
    /**
     * A progress bar used to show output IO.
     */
    private JProgressBar jpkOut;

    private transient ChartableKstat cks;

    /**
     * Create a panel showing the input and output traffic on the given kstat
     * that updates every interval seconds.
     *
     * @param ks an IO kstat
     * @param interval the update interval in seconds
     * @param jkstat a JKstat
     */
    public AccessoryIOPanel(final Kstat ks, final int interval,
			    final JKstat jkstat) {
	super(ks, interval, jkstat);

	cks = new ChartableIOKstat(jkstat, ks);

	// the meters are measured in kbytes/s, rescaled later if need be
	jpIn = new JProgressBar(0, iomax);
	jpOut = new JProgressBar(0, iomax);
	jpkIn = new JProgressBar(0, kiomax);
	jpkOut = new JProgressBar(0, kiomax);

	jpIn.setStringPainted(true);
	jpOut.setStringPainted(true);
	jpkIn.setStringPainted(true);
	jpkOut.setStringPainted(true);

	JLabel jlIn = new JLabel("reads/s");
	JLabel jlOut = new JLabel("writes/s");
	JLabel jlkIn = new JLabel("k read/s");
	JLabel jlkOut = new JLabel("k write/s");

	GroupLayout layout = new GroupLayout(this);
	setLayout(layout);
	// the label text is vertically centered
	GroupLayout.Alignment gac = GroupLayout.Alignment.CENTER;
	// horizontally, we have a sequential group containing a parallel
	// group of all the labels and a parallel group of the bars
	// vertically, we have a sequential group containing 4 parallel
	// groups each with a label and its bar
	// there's a 6 pixel gap on the left, 2 pixels other sides and between
	// elements
	layout.setHorizontalGroup(
		layout.createSequentialGroup()
		.addGap(6)
		.addGroup(layout.createParallelGroup()
			  .addComponent(jlIn)
			  .addComponent(jlkIn)
			  .addComponent(jlOut)
			  .addComponent(jlkOut)
			  )
		.addGap(2)
		.addGroup(layout.createParallelGroup()
			  .addComponent(jpIn)
			  .addComponent(jpkIn)
			  .addComponent(jpOut)
			  .addComponent(jpkOut)
			  )
		.addGap(2)
	);
	layout.setVerticalGroup(
		layout.createSequentialGroup()
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jlIn)
			  .addComponent(jpIn)
			  )
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jlkIn)
			  .addComponent(jpkIn)
			  )
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jlOut)
			  .addComponent(jpOut)
			  )
		.addGap(2)
		.addGroup(layout.createParallelGroup(gac)
			  .addComponent(jlkOut)
			  .addComponent(jpkOut)
			  )
		.addGap(2)
	);

	updateAccessory();

	startLoop();
    }

    @Override
    public void updateAccessory() {
	cks.update();

	double di = cks.getRate("r/s");
	double dj = cks.getRate("w/s");
	int i = (int) di;
	int j = (int) dj;
	while (i > iomax || j > iomax) {
	    iomax <<= 1;
	    jpIn.setMaximum(iomax);
	    jpOut.setMaximum(iomax);
	}
	jpIn.setValue(i);
	jpOut.setValue(j);
	jpIn.setString(df.format(di));
	jpOut.setString(df.format(dj));

	di = cks.getRate("kr/s");
	dj = cks.getRate("kw/s");
	i = (int) di;
	j = (int) dj;
	while (i > kiomax || j > kiomax) {
	    kiomax <<= 1;
	    jpkIn.setMaximum(kiomax);
	    jpkOut.setMaximum(kiomax);
	}
	jpkIn.setValue(i);
	jpkOut.setValue(j);
	jpkIn.setString(df.format(di));
	jpkOut.setString(df.format(dj));
    }
}
