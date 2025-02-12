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

package uk.co.petertribble.jkstat.demo;

import javax.swing.*;
import java.awt.BorderLayout;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.gui.IOstatTable;
import uk.co.petertribble.jkstat.client.*;

/**
 * A graphical tabular display of I/O statistics.
 *
 * @author Peter Tribble
 */
public class JIOstat extends JKdemo {

    private static final long serialVersionUID = 1L;

    private IOstatTable iotable;

    private static final String VERSION = "JIOstat version 1.2";

    /**
     * Construct a new JIOstat application.
     *
     * @param kcc the client configuration
     */
    public JIOstat(KClientConfig kcc) {
	this(new RemoteJKstat(kcc), true);
    }

    /**
     * Construct a new JIOstat application.
     */
    public JIOstat() {
	this(new NativeJKstat(), true);
    }

    /**
     * Construct a new JIOstat application.
     *
     * @param jkstat a JKstat object
     * @param standalone a boolean, true if the demo is a standalone
     * application.
     */
    public JIOstat(JKstat jkstat, boolean standalone) {
	super("jiostat", standalone);

        // create main display panel
        JPanel mainPanel = new JPanel(new BorderLayout());

	/*
	 * Filter on all IO kstats.
	 */
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.setFilterType(KstatType.KSTAT_TYPE_IO);
	// ignore usba statistics
	ksf.addNegativeFilter("usba:::");
	KstatSet kss = new KstatSet(jkstat, ksf);
	iotable = new IOstatTable(kss, DEFAULT_INTERVAL, jkstat);
	mainPanel.add(new JScrollPane(iotable));
	setContentPane(mainPanel);

	addInfoPanel(mainPanel, VERSION);

	setIconImage(new ImageIcon(this.getClass().getClassLoader()
			.getResource("pixmaps/jiostat.png")).getImage());

	setSize(620, 250);
	validate();
	setVisible(true);
    }

    @Override
    public void stopLoop() {
	iotable.stopLoop();
    }

    @Override
    public void setDelay(int i) {
	iotable.setDelay(i);
	setLabelDelay(i);
    }

    /**
     * Create a JIOstat application from the command line.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	if (args.length == 0) {
	    new JIOstat();
	} else if (args.length == 2 && "-s".equals(args[0])) {
	    new JIOstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	} else if (args.length == 2 && "-S".equals(args[0])) {
	    new JIOstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	} else {
	    System.err.println("Usage: iostat [-s|-S url]");
	    System.exit(1);
	}
    }
}
