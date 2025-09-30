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

package uk.co.petertribble.jkstat.browser;

import javax.swing.*;
import java.awt.event.*;
import uk.co.petertribble.jingle.JingleMultiFrame;
import uk.co.petertribble.jingle.JingleInfoFrame;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.client.*;
import uk.co.petertribble.jkstat.parse.ParseableJSONZipJKstat;
import uk.co.petertribble.jkstat.gui.KstatResources;
import uk.co.petertribble.jkstat.demo.*;

/**
 * A graphical Kstat browser, showing the available kstats as a tree.
 *
 * @author Peter Tribble
 */
public final class KstatBrowser extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private transient JKstat jkstat;
    /**
     * The panel with the tree.
     */
    private KstatTreePanel ktp;

    /**
     * A menu item to exit the browser.
     */
    private JMenuItem exitItem;
    /**
     * A menu item to clone the browser.
     */
    private JMenuItem cloneItem;
    /**
     * A menu item to close this instance of the browser.
     */
    private JMenuItem closeItem;
    /**
     * A menu item to show the information summary.
     */
    private JMenuItem infoItem;
    /**
     * A menu item to show the help.
     */
    private JMenuItem helpItem;
    /**
     * A menu item to show the license.
     */
    private JMenuItem licenseItem;

    /**
     * A radio item to select a 1s update interval.
     */
    private JRadioButtonMenuItem sleepItem1;
    /**
     * A radio item to select a 2s update interval.
     */
    private JRadioButtonMenuItem sleepItem2;
    /**
     * A radio item to select a 5s update interval.
     */
    private JRadioButtonMenuItem sleepItem5;
    /**
     * A radio item to select a 10s update interval.
     */
    private JRadioButtonMenuItem sleepItem10;

    /**
     * Constructs a client KstatBrowser.
     *
     * @param kcc The client configuration
     */
    public KstatBrowser(final KClientConfig kcc) {
	this(new RemoteJKstat(kcc));
    }

    /**
     * Constructs a local KstatBrowser.
     */
    public KstatBrowser() {
	this(new NativeJKstat());
    }

    /**
     * Constructs a local KstatBrowser.
     *
     * @param njkstat a JKstat object
     */
    public KstatBrowser(final JKstat njkstat) {
	super(KstatResources.getString("BROWSERUI.NAME.TEXT"));
	jkstat = njkstat;
	addWindowListener(new WindowExit());

	ktp = new KstatTreePanel(jkstat);
	setContentPane(ktp);

	JMenuBar jm = new JMenuBar();

	JMenu jme = new JMenu(KstatResources.getString("FILE.TEXT"));
	jme.setMnemonic(KeyEvent.VK_F);
	cloneItem = new JMenuItem(
			KstatResources.getString("FILE.NEWBROWSER.TEXT"),
			KeyEvent.VK_B);
	cloneItem.addActionListener(this);
	jme.add(cloneItem);
	closeItem = new JMenuItem(
			KstatResources.getString("FILE.CLOSEWIN.TEXT"),
			KeyEvent.VK_W);
	closeItem.addActionListener(this);
	jme.add(closeItem);
	exitItem = new JMenuItem(KstatResources.getString("FILE.EXIT.TEXT"),
			KeyEvent.VK_X);
	exitItem.addActionListener(this);
	jme.add(exitItem);

	JingleMultiFrame.register(this, closeItem);

	JMenu jmi = new JMenu(KstatResources.getString("INFO.TEXT"));
	jmi.setMnemonic(KeyEvent.VK_I);
	infoItem = new JMenuItem(
			KstatResources.getString("INFO.STATISTICS.TEXT"),
			KeyEvent.VK_S);
	infoItem.addActionListener(this);
	jmi.add(infoItem);

	JMenu jms = new JMenu(KstatResources.getString("SLEEP.TEXT"));
	jms.setMnemonic(KeyEvent.VK_U);
	sleepItem1 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.1"));
	sleepItem1.addActionListener(this);
	sleepItem2 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.2"));
	sleepItem2.addActionListener(this);
	sleepItem5 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.5"), true);
	sleepItem5.addActionListener(this);
	sleepItem10 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.10"));
	sleepItem10.addActionListener(this);
	jms.add(sleepItem1);
	jms.add(sleepItem2);
	jms.add(sleepItem5);
	jms.add(sleepItem10);

	ButtonGroup sleepGroup = new ButtonGroup();
	sleepGroup.add(sleepItem1);
	sleepGroup.add(sleepItem2);
	sleepGroup.add(sleepItem5);
	sleepGroup.add(sleepItem10);

	JMenu jmh = new JMenu(KstatResources.getString("HELP.TEXT"));
	jmh.setMnemonic(KeyEvent.VK_H);
	helpItem = new JMenuItem(KstatResources.getString("HELP.ABOUT.TEXT")
				+ " kstatbrowser", KeyEvent.VK_A);
	helpItem.addActionListener(this);
	jmh.add(helpItem);
	licenseItem = new JMenuItem(
				KstatResources.getString("HELP.LICENSE.TEXT"),
				KeyEvent.VK_L);
	licenseItem.addActionListener(this);
	jmh.add(licenseItem);

	jm.add(jme);
	jm.add(jmi);
	jm.add(jms);
	// add the demo menu if we're reading live statistics
	if (!(jkstat instanceof SequencedJKstat)) {
	    jm.add(new KstatToolsMenu(jkstat, false));
	}
	jm.add(jmh);

	setJMenuBar(jm);

	setIconImage(new ImageIcon(this.getClass().getClassLoader()
			.getResource("pixmaps/jkstat.png")).getImage());

	setSize(540, 600);
	setVisible(true);
    }

    class WindowExit extends WindowAdapter {
	@Override
	public void windowClosing(final WindowEvent we) {
	    JingleMultiFrame.unregister(KstatBrowser.this);
	}
    }

    /**
     * Set the update interval of the currently displayed kstat.
     *
     * @param i the desired update interval in seconds
     */
    public void setDelay(final int i) {
	ktp.setDelay(i);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
	if (e.getSource() == cloneItem) {
	    if (jkstat instanceof SequencedJKstat) {
		new KstatBrowser(((SequencedJKstat) jkstat).newInstance());
	    } else {
		new KstatBrowser(jkstat);
	    }
	}
	if (e.getSource() == closeItem) {
	    JingleMultiFrame.unregister(this);
	}
	if (e.getSource() == exitItem) {
	    System.exit(0);
	}
	if (e.getSource() == infoItem) {
	    ktp.showStats();
	}
	if (e.getSource() == helpItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/index.html", "text/html");
	}
	if (e.getSource() == licenseItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/CDDL.txt", "text/plain");
	}
	if (e.getSource() == sleepItem1) {
	    setDelay(1);
	}
	if (e.getSource() == sleepItem2) {
	    setDelay(2);
	}
	if (e.getSource() == sleepItem5) {
	    setDelay(5);
	}
	if (e.getSource() == sleepItem10) {
	    setDelay(10);
	}
    }

    private static void usage() {
	System.err.println("Usage: browser [-s|-S [url]]");
	System.err.println("       browser [-m]");
	System.err.println("       browser [-z zipfile]");
	System.exit(1);
    }

    /**
     * Create a KstatBrowser.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
	if (args.length == 0) {
	    new KstatBrowser();
	} else if (args.length == 1) {
	    if ("-s".equals(args[0]) || "-S".equals(args[0])) {
		KClientDialog kcd = new KClientDialog("-s".equals(args[0])
					? KClientConfig.CLIENT_XMLRPC
					: KClientConfig.CLIENT_REST);
		KClientConfig kcc = kcd.getConfig();
		if (kcc.isConfigured()) {
		    new KstatBrowser(kcc);
		} else {
		    System.err.println("No servers");
		    usage();
		}
	    } else if ("-m".equals(args[0])) {
		KBrowseDialog kbd = new KBrowseDialog();
		KClientConfig kcc = kbd.getConfig();
		if (kcc.isConfigured()) {
		    new KstatBrowser(kcc);
		} else {
		    System.err.println("No servers");
		    usage();
		}
	    } else {
		usage();
	    }
	} else if (args.length == 2) {
	    if ("-s".equals(args[0])) {
		new KstatBrowser(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	    } else if ("-S".equals(args[0])) {
		new KstatBrowser(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	    } else if ("-z".equals(args[0])) {
		try {
		    new KstatBrowser(new ParseableJSONZipJKstat(args[1], true));
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
