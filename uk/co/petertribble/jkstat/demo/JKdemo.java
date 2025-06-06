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
import java.awt.event.*;
import uk.co.petertribble.jkstat.gui.KstatResources;
import uk.co.petertribble.jingle.JingleInfoFrame;

/**
 * A base class for simple demo applications.
 *
 * @author Peter Tribble
 */
public abstract class JKdemo extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    /**
     * A menu item to exit the demo.
     */
    protected JMenuItem exitItem;

    /**
     * A menu item to show some help information.
     */
    protected JMenuItem helpItem;

    /**
     * A menu item to show license information.
     */
    protected JMenuItem licenseItem;

    /**
     * A menu item to sleep for 1s.
     */
    protected JRadioButtonMenuItem sleepItem1;

    /**
     * A menu item to sleep for 2s.
     */
    protected JRadioButtonMenuItem sleepItem2;

    /**
     * A menu item to sleep for 5s.
     */
    protected JRadioButtonMenuItem sleepItem5;

    /**
     * A menu item to sleep for 10s.
     */
    protected JRadioButtonMenuItem sleepItem10;

    /**
     * The menubar common to all demos.
     */
    protected JMenuBar jm;

    /**
     * The File and Exit menu.
     */
    protected JMenu jme;

    /**
     * The Sleep menu.
     */
    protected JMenu jms;

    /**
     * The Help menu.
     */
    protected JMenu jmh;

    /**
     * A label to show current state.
     */
    private JLabel infoLabel;

    /**
     * The default update interval, 5s.
     */
    protected static final int DEFAULT_INTERVAL = 5;

    /**
     * The name of this demo.
     */
    private String demoname;
    /**
     * The current update interval.
     */
    private int interval;
    /**
     * Whether this demo is a child or running standalone.
     */
    private boolean standalone;

    /**
     * Constructs a JKdemo object.
     *
     * @param demoname a String used as the demo title
     */
    public JKdemo(String demoname) {
	this(demoname, DEFAULT_INTERVAL, true);
    }

    /**
     * Constructs a JKdemo object.
     *
     * @param demoname a String used as the demo title
     * @param standalone a boolean, true if the demo is a standalone
     * application
     */
    public JKdemo(String demoname, boolean standalone) {
	this(demoname, DEFAULT_INTERVAL, standalone);
    }

    /**
     * Constructs a JKdemo object.
     *
     * @param demoname a String used as the demo title
     * @param interval the update delay in seconds
     */
    public JKdemo(String demoname, int interval) {
	this(demoname, interval, true);
    }

    /**
     * Constructs a JKdemo object.
     *
     * @param demoname a String used as the demo title
     * @param interval the update delay in seconds
     * @param standalone a boolean, true if the demo is a standalone
     * application
     */
    public JKdemo(String demoname, int interval, boolean standalone) {
	this(demoname, interval, standalone, true, true);
    }

    /**
     * Constructs a JKdemo object.
     *
     * @param demoname a String used as the demo title
     * @param interval the update delay in seconds
     * @param standalone a boolean, true if the demo is a standalone
     * application
     * @param showdelay a boolean determining whether the sleep menu is shown
     * @param showhelp a boolean determining whether the help menu is shown
     */
    public JKdemo(String demoname, int interval, boolean standalone,
		  boolean showdelay, boolean showhelp) {
	super(demoname);
	this.demoname = demoname;
	this.interval = interval;
	this.standalone = standalone;

	jm = new JMenuBar();
	setJMenuBar(jm);
	addWindowListener(new WindowExit());
	addFileMenu();
	if (showdelay) {
	    addSleepMenu();
	}
	if (showhelp) {
	    addHelpMenu();
	}
    }

    /**
     * Add the File menu.
     */
    public void addFileMenu() {
	jme = new JMenu(KstatResources.getString("FILE.TEXT"));
	jme.setMnemonic(KeyEvent.VK_F);
	if (standalone) {
	    exitItem = new JMenuItem(KstatResources.getString("FILE.EXIT.TEXT"),
				KeyEvent.VK_X);
	} else {
	    exitItem = new JMenuItem(
				KstatResources.getString("FILE.CLOSEWIN.TEXT"),
				KeyEvent.VK_W);
	}
	exitItem.addActionListener(this);
	jme.add(exitItem);
	jm.add(jme);
    }

    /**
     * Add the Sleep menu.
     */
    public void addSleepMenu() {
	jms = new JMenu(KstatResources.getString("SLEEP.TEXT"));
	jms.setMnemonic(KeyEvent.VK_U);

	sleepItem1 = new JRadioButtonMenuItem(
					KstatResources.getString("SLEEP.1"),
					interval == 1);
	sleepItem1.addActionListener(this);
	sleepItem2 = new JRadioButtonMenuItem(
					KstatResources.getString("SLEEP.2"),
					interval == 2);
	sleepItem2.addActionListener(this);
	sleepItem5 = new JRadioButtonMenuItem(
					KstatResources.getString("SLEEP.5"),
					interval == 5);
	sleepItem5.addActionListener(this);
	sleepItem10 = new JRadioButtonMenuItem(
					KstatResources.getString("SLEEP.10"),
					interval == 10);
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

	jm.add(jms);
    }

    /**
     * Add the Help menu.
     */
    public void addHelpMenu() {
	jmh = new JMenu(KstatResources.getString("HELP.TEXT"));
	jmh.setMnemonic(KeyEvent.VK_H);
	helpItem = new JMenuItem(KstatResources.getString("HELP.ABOUT.TEXT")
		+ " " + demoname, KeyEvent.VK_A);
	helpItem.addActionListener(this);
	jmh.add(helpItem);
	licenseItem = new JMenuItem(
		KstatResources.getString("HELP.LICENSE.TEXT"),
		KeyEvent.VK_L);
	licenseItem.addActionListener(this);
	jmh.add(licenseItem);
	jm.add(jmh);
    }

    /**
     * Add an informational panel. Shows some text and the current update
     * interval.
     *
     * @param mainPanel a JPanel object to add the informational panel to
     * @param sinfo a String used as text in the informational panel
     */
    public void addInfoPanel(JPanel mainPanel, String sinfo) {
	JPanel infoPanel = new JPanel(new BorderLayout());
	infoLabel = new JLabel();
	setLabelDelay(DEFAULT_INTERVAL);
	infoPanel.add(new JLabel(sinfo, SwingConstants.LEFT));
	infoPanel.add(infoLabel, BorderLayout.EAST);
	mainPanel.add(infoPanel, BorderLayout.SOUTH);
    }

    /**
     * Set the text on the information to show the current update delay.
     *
     * @param i the update delay in seconds
     */
    protected void setLabelDelay(int i) {
	if (infoLabel != null) {
	    infoLabel.setText(KstatResources.getString("SLEEP.TEXT") + " " + i
		+ KstatResources.getString("SLEEP.SEC"));
	}
    }

    /**
     * Add a menu.
     *
     * @param menu the menu to add
     */
    protected void addMenu(JMenu menu) {
	jm.add(menu);
    }

    class WindowExit extends WindowAdapter {
	@Override
	public void windowClosing(WindowEvent we) {
	    kaboom();
	}
    }

    /**
     * Exit the demo appropriately for the context. If standalone, exit the
     * entire VM, otherwise just shut down the individual demo.
     */
    void kaboom() {
	if (standalone) {
	    System.exit(0);
	} else {
	    stopLoop();
	    dispose();
	}
    }

    /**
     * Set the desired update delay. Subclasses should use implement this
     * to propagate the delay to any accessories or gadgets they contain.
     *
     * @param i the update delay in seconds
     */
    public abstract void setDelay(int i);

    /**
     * Stop the application updating itself. Implementing classes should
     * implement this to stop any accessories or gadgets that are updating.
     */
    public abstract void stopLoop();

    /**
     * The main action handler. This handles both menu items (exit,
     * license, help) and updating the delay.
     *
     * The help handler uses the demoname passed in the constructor
     * to find the right help file.
     *
     * Subclasses wishing to override this method should call
     * super.actionPerformed() to get the delay update handling,
     * and then use their own logic to handle any other changes.
     *
     * @param e the ActionEvent which will trigger this action
     */
    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == exitItem) {
	    kaboom();
	} else if (e.getSource() == helpItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/" + demoname + ".html", "text/html");
	} else if (e.getSource() == licenseItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/CDDL.txt", "text/plain");
	} else if (e.getSource() == sleepItem1) {
	    setDelay(1);
	} else if (e.getSource() == sleepItem2) {
	    setDelay(2);
	} else if (e.getSource() == sleepItem5) {
	    setDelay(5);
	} else if (e.getSource() == sleepItem10) {
	    setDelay(10);
	}
    }
}
