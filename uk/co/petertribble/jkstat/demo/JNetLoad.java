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
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.gui.*;
import uk.co.petertribble.jkstat.client.*;

/**
 * A graphical display of network traffic.
 *
 * @author Peter Tribble
 */
public final class JNetLoad extends JKdemo implements ActionListener {

    private static final long serialVersionUID = 1L;

    private transient JKstat jkstat;
    private transient KstatAccessorySet kas;

    // to identify menu popups
    private transient Map<JMenuItem, Kstat> itemMap;

    /**
     * Construct a new JNetLoad application.
     *
     * @param kcc the client configuration
     */
    public JNetLoad(final KClientConfig kcc) {
	this(new RemoteJKstat(kcc), true);
    }

    /**
     * Construct a new JNetLoad application.
     */
    public JNetLoad() {
	this(new NativeJKstat(), true);
    }

    /**
     * Construct a new JNetLoad application.
     *
     * @param jkstat a JKstat object
     * @param standalone a boolean, true if the demo is a standalone
     * application.
     */
    public JNetLoad(final JKstat jkstat, final boolean standalone) {
	super("JNetLoad", 1, standalone);

	this.jkstat = jkstat;

	itemMap = new HashMap<>();

	List<KstatAccessoryPanel> kaplist = new ArrayList<>();

	// create a main panel
	JPanel mainPanel = new JPanel();
	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

	setContentPane(mainPanel);

	String iflabel = KstatResources.getString("NETLOAD.IF.TEXT") + " ";
	String grlabel = KstatResources.getString("NETLOAD.GRAPH.TEXT") + " ";

	// filter the kstats we need
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.setFilterClass("net");
	ksf.addFilter(":::rbytes64");
	ksf.addNegativeFilter("::mac");

	// add the kstats to the panel
	for (Kstat ks : ksf.getKstats(true)) {
	    KstatAccessoryPanel kap = new AccessoryNetPanel(ks, -1, jkstat);
	    kap.setMinimumSize(new Dimension(300, 50));
	    kap.setPreferredSize(new Dimension(300, 80));
	    kap.setBorder(BorderFactory.createTitledBorder(
				  iflabel + ks.getName()));
	    mainPanel.add(kap);
	    kaplist.add(kap);

	    // popup menu for graph
	    JPopupMenu jpm = new JPopupMenu();
	    JMenuItem jmi = new JMenuItem(grlabel + ks.getName());
	    jmi.addActionListener(this);
	    jpm.add(jmi);
	    kap.addMouseListener((MouseListener) new PopupListener(jpm));
	    itemMap.put(jmi, ks);
	}

	setIconImage(new ImageIcon(this.getClass().getClassLoader()
			.getResource("pixmaps/jnetload.png")).getImage());

	pack();
	setVisible(true);
	kas = new KstatAccessorySet(kaplist, 1);
    }

    @Override
    public void stopLoop() {
	kas.stopLoop();
    }

    @Override
    public void setDelay(final int i) {
	kas.setDelay(i);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
	super.actionPerformed(e);
	Kstat ks = itemMap.get((JMenuItem) e.getSource());
	if (ks != null) {
	    new KstatChartFrame(jkstat, ks,
				Arrays.asList("rbytes64", "obytes64"));
	}
    }

    /*
     * Inner class to handle mouse popups.
     */
    static class PopupListener extends MouseAdapter {
	private JPopupMenu popup;

	PopupListener(final JPopupMenu popup) {
	    this.popup = popup;
	}

	@Override
	public void mousePressed(final MouseEvent e) {
	    showPopup(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	    showPopup(e);
	}

	private void showPopup(final MouseEvent e) {
	    if (e.isPopupTrigger()) {
		popup.show(e.getComponent(), e.getX(), e.getY());
	    }
	}
    }

    /**
     * Create a JNetLoad application from the command line.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
	if (args.length == 0) {
	    new JNetLoad();
	} else if (args.length == 2 && "-s".equals(args[0])) {
	    new JNetLoad(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	} else if (args.length == 2 && "-S".equals(args[0])) {
	    new JNetLoad(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	} else {
	    System.err.println("Usage: netload [-s|-S url]");
	    System.exit(1);
	}
    }
}
