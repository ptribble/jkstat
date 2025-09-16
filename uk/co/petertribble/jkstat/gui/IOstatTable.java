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

import javax.swing.*;
import java.awt.event.*;
import uk.co.petertribble.jkstat.api.KstatSet;
import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.ChartableKstat;

/**
 * A tabular representation of iostat.
 *
 * @author Peter Tribble
 */
public final class IOstatTable extends JTable {

    private static final long serialVersionUID = 1L;

    /**
     * The underlying data model.
     */
    private IOstatTableModel ktm;
    transient JKstat jkstat;

    /**
     * Wraps a {@code KstatSet} in a {@code JTable}, adding a right-click
     * popup menu to allow extra functionality such as creating a chart
     * so that the statistics can be displayed over time.
     *
     * @param kss a {@code KstatSet}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     */
    public IOstatTable(final KstatSet kss, final int interval,
		       final JKstat jkstat) {
	this.jkstat = jkstat;
	ktm = new IOstatTableModel(kss, interval, jkstat);
	setModel(ktm);
	setAutoCreateRowSorter(true);
	addMouseListener((MouseListener) new PopupListener());
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
		// need to get which row of the model we're on
		int irow = convertRowIndexToModel(rowAtPoint(e.getPoint()));
		// and the statistic name from the column
		String s = getColumnName(columnAtPoint(e.getPoint()));
		// can't chart the device
		if (!"device".equals(s)) {
		    createChartMenu(s, irow).show(
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
     * @param irow the row in the underlying model
     *
     * @return a {@code JPopupMenu}
     */
    public JPopupMenu createChartMenu(final String s, final int irow) {
	JPopupMenu jpm = new JPopupMenu();
	final ChartableKstat cks = ktm.getChartableKstat(irow);
	JMenuItem showChartItem = new JMenuItem(
		KstatResources.getString("TABLE.CHART.TEXT") + " " + s
		+ " for device " + cks);
	showChartItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(final ActionEvent e) {
		new KstatChartFrame(jkstat, cks.getKstat(), cks, s);
	    }
	});
	jpm.add(showChartItem);
	return jpm;
    }

    /**
     * Stop the table updating.
     */
    public void stopLoop() {
	ktm.stopLoop();
    }

    /**
     * Set the update delay.
     *
     * @param i the desired update delay, in seconds
     */
    public void setDelay(final int i) {
	ktm.setDelay(i);
    }
}
