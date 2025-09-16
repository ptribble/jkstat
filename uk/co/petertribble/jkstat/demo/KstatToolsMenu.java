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

import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.gui.KstatResources;
import uk.co.petertribble.jkstat.browser.KstatBrowser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.*;

/**
 * Creates a Menu, allowing access to a kstat browser and a number of
 * useful utilities from other applications.
 *
 * @author Peter Tribble
 */
public final class KstatToolsMenu extends JMenu implements ActionListener {

    private static final long serialVersionUID = 1L;

    /**
     * A menu item to launch a new jkstat browser.
     */
    private JMenuItem kstatbrowserItem;
    /**
     * A menu item to launch jcpustate.
     */
    private JMenuItem jcpustateItem;
    /**
     * A menu item to launch jiostat.
     */
    private JMenuItem jiostatItem;
    /**
     * A menu item to launch jmpstat.
     */
    private JMenuItem jmpstatItem;
    /**
     * A menu item to launch jnetload.
     */
    private JMenuItem jnetloadItem;
    /**
     * A menu item to launch jnfsstat.
     */
    private JMenuItem jnfsstatItem;
    private transient JKstat jkstat;

    /**
     * Constructs a KstatToolsMenu object.
     *
     * @param jkstat a JKstat object
     */
    public KstatToolsMenu(final JKstat jkstat) {
	this(jkstat, true);
    }

    /**
     * Constructs a KstatToolsMenu object.
     *
     * @param jkstat a JKstat object
     * @param showbrowser a boolean -- true if the kstat browser is to be shown
     */
    public KstatToolsMenu(final JKstat jkstat, final boolean showbrowser) {
	super(KstatResources.getString("DEMO.TEXT"));
	setMnemonic(KeyEvent.VK_D);

	this.jkstat = jkstat;

	if (showbrowser) {
	    kstatbrowserItem = new JMenuItem(
			KstatResources.getString("BROWSERUI.NAME.TEXT"));
	    add(kstatbrowserItem);
	    kstatbrowserItem.addActionListener(this);
	    addSeparator();
	}
	jcpustateItem = new JMenuItem("Cpustate");
	add(jcpustateItem);
	jcpustateItem.addActionListener(this);
	jiostatItem = new JMenuItem("iostat");
	add(jiostatItem);
	jiostatItem.addActionListener(this);
	jmpstatItem = new JMenuItem("mpstat");
	add(jmpstatItem);
	jmpstatItem.addActionListener(this);
	jnetloadItem = new JMenuItem("Network load");
	add(jnetloadItem);
	jnetloadItem.addActionListener(this);
	jnfsstatItem = new JMenuItem("nfsstat");
	add(jnfsstatItem);
	jnfsstatItem.addActionListener(this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
	if (e.getSource() == kstatbrowserItem) {
	    new KstatBrowser();
	}
	if (e.getSource() == jcpustateItem) {
	    new JCpuState(jkstat, false);
	}
	if (e.getSource() == jiostatItem) {
	    new JIOstat(jkstat, false);
	}
	if (e.getSource() == jmpstatItem) {
	    new JMPstat(jkstat, false);
	}
	if (e.getSource() == jnetloadItem) {
	    new JNetLoad(jkstat, false);
	}
	if (e.getSource() == jnfsstatItem) {
	    new Jnfsstat(jkstat, false);
	}
    }
}
