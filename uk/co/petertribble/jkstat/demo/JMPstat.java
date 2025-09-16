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
import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.NativeJKstat;
import uk.co.petertribble.jkstat.gui.MPstatTable;
import uk.co.petertribble.jkstat.client.*;

/**
 * A tabular representation of the mpstat command.
 *
 * @author Peter Tribble
 */
public final class JMPstat extends JKdemo {

    private static final long serialVersionUID = 1L;

    /**
     * The underlying data table.
     */
    private MPstatTable mptable;

    private static final String VERSION = "JMPstat version 1.2";

    /**
     * Construct a client JMPstat application.
     *
     * @param kcc the client configuration
     */
    public JMPstat(final KClientConfig kcc) {
	this(new RemoteJKstat(kcc), true);
    }

    /**
     * Construct a new JMPstat application.
     */
    public JMPstat() {
	this(new NativeJKstat(), true);
    }

    /**
     * Construct a new JMPstat application.
     *
     * @param jkstat a JKstat object
     * @param standalone a boolean, true if the demo is a standalone
     * application.
     */
    public JMPstat(final JKstat jkstat, final boolean standalone) {
	super("jmpstat", standalone);

        // create main display panel
        JPanel mainPanel = new JPanel(new BorderLayout());

	mptable = new MPstatTable(jkstat, DEFAULT_INTERVAL);
	mainPanel.add(new JScrollPane(mptable));
	setContentPane(mainPanel);

	addInfoPanel(mainPanel, VERSION);

	setIconImage(new ImageIcon(this.getClass().getClassLoader()
			.getResource("pixmaps/jmpstat.png")).getImage());

	setSize(620, 250);
	validate();
	setVisible(true);
    }

    @Override
    public void stopLoop() {
	mptable.stopLoop();
    }

    @Override
    public void setDelay(final int i) {
	mptable.setDelay(i);
	setLabelDelay(i);
    }

    /**
     * Create a JMPstat application from the command line.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
	if (args.length == 0) {
	    new JMPstat();
	} else if (args.length == 2 && "-s".equals(args[0])) {
	    new JMPstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	} else if (args.length == 2 && "-S".equals(args[0])) {
	    new JMPstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	} else {
	    System.err.println("Usage: mpstat [-s|-S url]");
	    System.exit(1);
	}
    }
}
