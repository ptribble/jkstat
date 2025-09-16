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
import java.awt.event.*;
import uk.co.petertribble.jkstat.client.*;
import uk.co.petertribble.jkstat.parse.ParseableJSONZipJKstat;

/**
 * A graphical chart builder. Allows the user to build a chart by selecting
 * a kstat or kstats and defining how the chart should look.
 *
 * @author Peter Tribble
 */
public final class ChartBuilderFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    /**
     * A menu item to exit the application.
     */
    private JMenuItem exitItem;

    /**
     * Create a ChartBuilderFrame.
     */
    public ChartBuilderFrame() {
	this(new NativeJKstat());
    }

    /**
     * Create a client ChartBuilderFrame.
     *
     * @param kcc the client configuration
     */
    public ChartBuilderFrame(final KClientConfig kcc) {
	this(new RemoteJKstat(kcc));
    }

    /**
     * Create a ChartBuilderFrame.
     *
     * @param jkstat a {@code JKstat}
     */
    public ChartBuilderFrame(final JKstat jkstat) {
	setTitle(KstatResources.getString("CHART.BUILDER"));
	addWindowListener(new WindowExit());
	JMenuBar jm = new JMenuBar();
	jm.add(fileMenu());
	setJMenuBar(jm);

	setContentPane(new ChartBuilderPanel(jkstat));

	setSize(480, 420);
	validate();
	setVisible(true);
    }

    private JMenu fileMenu() {
	JMenu jme = new JMenu(KstatResources.getString("FILE.TEXT"));
	jme.setMnemonic(KeyEvent.VK_F);

	jme.addSeparator();
	exitItem = new JMenuItem(KstatResources.getString("FILE.CLOSE.TEXT"),
				KeyEvent.VK_C);
	exitItem.addActionListener(this);
	jme.add(exitItem);
	return jme;
    }

    class WindowExit extends WindowAdapter {
	@Override
	public void windowClosing(final WindowEvent we) {
	    dispose();
	}
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
	if (e.getSource() == exitItem) {
	    setVisible(false);
	    dispose();
	}
    }

    private static void usage() {
	System.err.println("Usage: chartbuilder [-s|-S [url]]");
	System.err.println("       chartbuilder [-z zipfile]");
	System.exit(1);
    }

    /**
     * Create the application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
	if (args.length == 0) {
	    new ChartBuilderFrame();
	} else if (args.length == 1) {
	    if ("-s".equals(args[0]) || "-S".equals(args[0])) {
		KClientDialog kcd = new KClientDialog("-s".equals(args[0])
					? KClientConfig.CLIENT_XMLRPC
					: KClientConfig.CLIENT_REST);
		KClientConfig kcc = kcd.getConfig();
		if (kcc.isConfigured()) {
		    new ChartBuilderFrame(kcc);
		}
	    } else {
		usage();
	    }
	} else if (args.length == 2) {
	    if ("-s".equals(args[0])) {
		new ChartBuilderFrame(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	    } else if ("-S".equals(args[0])) {
		new ChartBuilderFrame(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	    } else if ("-z".equals(args[0])) {
		try {
		    new ChartBuilderFrame(new ParseableJSONZipJKstat(args[1],
					true));
		} catch (Exception e) {
		    e.printStackTrace();
		    usage();
		}
	    } else {
		usage();
	    }
	} else {
	    usage();
	}
    }
}
