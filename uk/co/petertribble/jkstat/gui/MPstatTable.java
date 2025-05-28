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
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jingle.TableSorter;

/**
 * A tabular representation of mpstat data.
 *
 * @author Peter Tribble
 */
public final class MPstatTable extends JTable {

    private static final long serialVersionUID = 1L;

    private MPstatTableModel ktm;
    TableSorter sortedModel;
    transient JKstat jkstat;

    /**
     * Create a new MPstatTable.
     *
     * @param jkstat a JKstat object
     * @param interval the desired update interval
     */
    public MPstatTable(JKstat jkstat, int interval) {
	this.jkstat = jkstat;
	/*
	 * Filter on all cpu kstats.
	 */
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.addFilter("cpu::sys:");

	ktm = new MPstatTableModel(new KstatSet(jkstat, ksf), interval, jkstat);
	sortedModel = new TableSorter(ktm);
	setModel(sortedModel);
	sortedModel.setTableHeader(getTableHeader());
	addMouseListener((MouseListener) new PopupListener());
    }

    /**
     * Inner class to handle mouse popups.
     */
    class PopupListener extends MouseAdapter {

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
		// need to get which row of the model we're on
		int irow = sortedModel.modelIndex(rowAtPoint(e.getPoint()));
		// and the statistic name from the column
		String s = getColumnName(columnAtPoint(e.getPoint()));
		// can't chart the CPU column
		if (!"CPU".equals(s)) {
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
		+ " for cpu " + cks);
	showChartItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
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
    public void setDelay(int i) {
	ktm.setDelay(i);
    }
}
