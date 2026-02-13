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
 * Copyright 2026 Peter Tribble
 *
 */

package uk.co.petertribble.jkstat.gui;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import java.awt.event.*;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.JKstat;

/**
 * A tabular representation of a Kstat.
 *
 * @author Peter Tribble
 */
public class KstatTable extends JTable {

    private static final long serialVersionUID = 1L;

    /**
     * The underlying model.
     */
    private KstatTableModel ktm;
    transient JKstat jkstat;
    /**
     * The kstat being displayed.
     */
    Kstat ks;

    /**
     * Wraps a {@code Kstat} in a {@code JTable}, adding a right-click
     * popup menu to allow extra functionality such as creating a chart
     * so that the statistics can be displayed over time.
     *
     * @param module the kstat module
     * @param instance the kstat instance
     * @param name the kstat name
     * @param interval the update interval in seconds
     * @param njkstat a {@code JKstat}
     */
    public KstatTable(final String module, final String instance,
		final String name, final int interval, final JKstat njkstat) {
	this(njkstat.getKstat(module, Integer.parseInt(instance), name),
		interval, njkstat);
    }

    /**
     * Wraps a {@code Kstat} in a {@code JTable}, adding a right-click
     * popup menu to allow extra functionality such as creating a chart
     * so that the statistics can be displayed over time.
     *
     * @param nks a {@code Kstat}
     * @param interval the update interval in seconds
     * @param njkstat a {@code JKstat}
     */
    public KstatTable(final Kstat nks, final int interval,
		      final JKstat njkstat) {
	jkstat = njkstat;
	ks = nks;
	if (ks != null) {
	    ktm = new KstatTableModel(ks, interval, jkstat);
	    setModel(ktm);

	    /*
	     * We only show the popup menus if the table is going to show the
	     * rates. If the interval is negative, then the KstatTableModel
	     * won't show the rates column, so we shouldn't show popups either.
	     */
	    if (interval >= 0) {
		addMouseListener((MouseListener) new PopupListener());
	    }
	}
    }

    /**
     * Inner class to handle mouse popups.
     */
    class PopupListener extends MouseAdapter {

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
		// the statistic name is always shown in the first column
		String s = (String) getValueAt(rowAtPoint(e.getPoint()), 0);
		if (ks.isNumeric(s)) {
		    createChartMenu(s).show(
					e.getComponent(), e.getX(), e.getY());
		}
	    }
	}
    }

    /**
     * Create a popup menu allowing the user to create a chart of the selected
     * statistic.
     *
     * @param s the statistic that should be charted
     *
     * @return a {@code JPopupMenu}
     */
    public JPopupMenu createChartMenu(final String s) {
	JPopupMenu jpm = new JPopupMenu();
	JMenuItem showChartItem = new JMenuItem(
		KstatResources.getString("TABLE.CHART.TEXT") + " " + s);
	showChartItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		new KstatChartFrame(jkstat, ks, s);
	    }
	});
	jpm.add(showChartItem);
	return jpm;
    }

    /**
     * Tell the model to update itself.
     */
    public void update() {
	if (ktm != null) {
	    ktm.update();
	}
    }

    /**
     * Stop the table updating.
     */
    public void stopLoop() {
	if (ktm != null) {
	    ktm.stopLoop();
	}
    }

    /**
     * Start the table updating.
     */
    public void startLoop() {
	if (ktm != null) {
	    ktm.startLoop();
	}
    }

    /**
     * Set the update delay of the table.
     *
     * @param interval the desired update delay, in seconds
     */
    public void setDelay(final int interval) {
	if (ktm != null) {
	    ktm.setDelay(interval);
	}
    }
}
