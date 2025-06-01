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
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.gui.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import uk.co.petertribble.jingle.SpringUtilities;
import uk.co.petertribble.jkstat.client.*;

/**
 * A java implementation of xcpustate.
 *
 * @author Peter Tribble
 */
public final class JCpuState extends JKdemo implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final int STYLE_BASIC = 0;
    private static final int STYLE_CHART = 1;
    private static int style = STYLE_BASIC;
    private static int orientation = SwingConstants.HORIZONTAL;

    private transient JKstat jkstat;
    private transient KstatAccessorySet kas;

    /**
     * The number of cpus being displayed.
     */
    private int ncpus;

    /**
     * Menu Items that will create a popup window about each cpu.
     */
    private JMenuItem[] aboutCpuItem;
    /**
     * Menu Items that will create a popup window with extended information
     * for each cpu.
     */
    private JMenuItem[] extendedCpuItem;
    /**
     * Strings for labels on each cpu.
     */
    private String[] cpuID;

    /**
     * Create a new JCpuState application.
     *
     * @param kcc the client configuration
     */
    public JCpuState(KClientConfig kcc) {
	this(new RemoteJKstat(kcc), true);
    }

    /**
     * Create a new JCpuState application.
     */
    public JCpuState() {
	this(new NativeJKstat(), true);
    }

    /**
     * Create a new JCpuState application.
     *
     * @param jkstat a JKstat object
     * @param standalone a boolean, true if the demo is a standalone
     * application.
     */
    public JCpuState(JKstat jkstat, boolean standalone) {
	super("jcpustate", 1, standalone, false, false);

	this.jkstat = jkstat;

	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.setFilterClass("misc");
	ksf.addFilter("cpu_stat:::");

	Set<Kstat> kstats = ksf.getKstats(true);

	ncpus = kstats.size();
	aboutCpuItem = new JMenuItem[ncpus];
	extendedCpuItem = new JMenuItem[ncpus];
	cpuID = new String[ncpus];
	List<KstatAccessoryPanel> acplist = new ArrayList<>();

	// create main display panel
	JPanel mainPanel = new JPanel(new SpringLayout());
	setContentPane(mainPanel);

	/*
	 * If vertical, the bars are short and squat; if horizontal they
	 * are wide and thin.
	 */
	Dimension dcpu = (orientation == SwingConstants.VERTICAL)
	    ? new Dimension(16, 50) : new Dimension(200, 20);
	/*
	 * Add the kstats to the panel. SpringUtilities assumes we populate
	 * the grid left to right, row by row. So in the vertical layout we
	 * follow a label with its accessory; in the horizontal layout we
	 * add all the accessories in order, then all the labels go in the
	 * row underneath. Note that the orientation refers to the accessory,
	 * and the layout orientation is the opposite of that.
	 */
	int ncpu = 0;
	for (Kstat ks : kstats) {
	    String scpu = ks.getInstance();
	    if (orientation != SwingConstants.VERTICAL) {
		mainPanel.add(new JLabel("Cpu " + scpu));
	    }
	    KstatAccessoryPanel acp = (style == STYLE_CHART)
		? new AccessoryCpuChart(ks, -1, jkstat)
		: new AccessoryCpuPanel(ks, -1, jkstat, orientation);
	    acplist.add(acp);
	    acp.setMinimumSize(dcpu);
	    acp.setPreferredSize(dcpu);
	    mainPanel.add(acp);
	    // add a popup menu to each one
	    JPopupMenu jpm = new JPopupMenu();
	    cpuID[ncpu] = scpu;
	    aboutCpuItem[ncpu] = new JMenuItem(
			KstatResources.getString("CPUSTATE.ABOUT.TEXT")
				+ " " + scpu);
	    aboutCpuItem[ncpu].addActionListener(this);
	    jpm.add(aboutCpuItem[ncpu]);
	    extendedCpuItem[ncpu] = new JMenuItem(
			KstatResources.getString("CPUSTATE.EXT.TEXT"));
	    extendedCpuItem[ncpu].addActionListener(this);
	    jpm.add(extendedCpuItem[ncpu]);
	    acp.addMouseListener((MouseListener) new PopupListener(jpm));
	    ncpu++;
	}

	if (orientation == SwingConstants.VERTICAL) {
	    for (Kstat ks : kstats) {
		mainPanel.add(new JLabel(ks.getInstance(), JLabel.CENTER));
	    }
	    SpringUtilities.makeCompactGrid(mainPanel, 2, ncpus, 6, 3, 3, 3);
	} else {
	    SpringUtilities.makeCompactGrid(mainPanel, ncpus, 2, 6, 3, 3, 3);
	}

	setIconImage(new ImageIcon(this.getClass().getClassLoader()
			.getResource("pixmaps/jcpustate.png")).getImage());

	pack();
	setVisible(true);
	kas = new KstatAccessorySet(acplist, 1);
    }

    @Override
    public void setDelay(int i) {
	kas.setDelay(i);
    }

    @Override
    public void stopLoop() {
	kas.stopLoop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	super.actionPerformed(e);
	for (int i = 0; i < ncpus; i++) {
	    if (e.getSource() == aboutCpuItem[i]) {
		new KstatTableFrame("cpu_info", cpuID[i], "cpu_info" + cpuID[i],
				    -1, jkstat);
	    }
	    if (e.getSource() == extendedCpuItem[i]) {
		new KstatTableFrame("cpu_stat", cpuID[i], "cpu_stat" + cpuID[i],
				    1, jkstat);
	    }
	}
    }

    /*
     * Inner class to handle mouse popups.
     */
    static class PopupListener extends MouseAdapter {
	private JPopupMenu popup;

	PopupListener(JPopupMenu popup) {
	    this.popup = popup;
	}

	@Override
	public void mousePressed(MouseEvent e) {
	    showPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    showPopup(e);
	}

	private void showPopup(MouseEvent e) {
	    if (e.isPopupTrigger()) {
		popup.show(e.getComponent(), e.getX(), e.getY());
	    }
	}
    }

    /**
     * Create a standalone JCpuState demo application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	for (String s : args) {
	    if ("chart".equals(s)) {
		style = STYLE_CHART;
	    }
	    if ("vertical".equals(s)) {
		orientation = SwingConstants.VERTICAL;
	    }
	}
	if (args.length >= 2 && "-s".equals(args[0])) {
	    new JCpuState(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	} else if (args.length >= 2 && "-S".equals(args[0])) {
	    new JCpuState(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	} else {
	    new JCpuState();
	}
    }
}
