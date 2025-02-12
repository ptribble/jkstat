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

import java.util.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import uk.co.petertribble.jkstat.api.*;

/**
 * A Table Model representing an IO Kstat.
 *
 * @author Peter Tribble
 */
public final class IOstatTableModel extends AbstractTableModel
		implements ActionListener {

    private static final long serialVersionUID = 1L;

    private String[] columnNames = {"r/s", "w/s", "kr/s", "kw/s", "wait",
				"actv", "svc_t", "%w", "%b", "device"};

    private List<ChartableIOKstat> iodata;
    private Timer timer;
    private int delay;
    private JKstat jkstat;
    private KstatSet kss;

    /**
     * Create a Table Model from the given Kstats.
     *
     * @param kss the {@code KstatSet} to be represented by this model
     * @param interval the desired update interval, in seconds
     * @param jkstat a {@code JKstat}
     */
    public IOstatTableModel(KstatSet kss, int interval, JKstat jkstat) {
	this.kss = kss;
	this.jkstat = jkstat;

	iodata = new ArrayList<>();
	for (Kstat ks : kss.getKstats(true)) {
	    iodata.add(new ChartableIOKstat(jkstat, ks));
	}

	delay = interval * 1000;
	fireTableDataChanged();
	startLoop();
    }

    /**
     * Start the loop that updates the model.
     */
    public void startLoop() {
	if (delay > 0) {
	    if (timer == null) {
		timer = new Timer(delay, this);
	    }
	    timer.start();
	}
    }

    /**
     * Stop the loop that updates the model.
     */
    public void stopLoop() {
	if (timer != null) {
	    timer.stop();
	}
    }

    /**
     * Set the loop delay to be the specified number of seconds. If a zero or
     * negative delay is requested, stop the updates and remember the previous
     * delay.
     *
     * @param interval the desired delay, in seconds
     */
    public void setDelay(int interval) {
	if (interval <= 0) {
	    stopLoop();
	} else {
	    delay = interval * 1000;
	    if (timer != null) {
		timer.setDelay(delay);
	    }
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	updateKstat();
    }

    /**
     * Update the statistics. Iterates through the current list updating each
     * one. If a kstat disappears, it is removed.
     */
    public void updateKstat() {
	/*
	 * If any new statistics, add them.
	 */
	if (kss.chainupdate() != 0) {
	    for (Kstat ks : kss.getAddedKstats()) {
		iodata.add(new ChartableIOKstat(jkstat, ks));
	    }
	}
	Iterator<ChartableIOKstat> vki = iodata.iterator();
	while (vki.hasNext()) {
	    ChartableIOKstat cks = vki.next();
	    if (!cks.update()) {
		vki.remove();
	    }
	}
	fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
	return columnNames.length;
    }

    @Override
    public int getRowCount() {
	return iodata.size();
    }

    @Override
    public String getColumnName(int col) {
	return columnNames[col];
    }

    /**
     * Retrieve the ChartableKstat at the given row.
     *
     * @param row the row to look at
     *
     * @return the ChartableKstat at the given row
     */
    public ChartableKstat getChartableKstat(int row) {
	return iodata.get(row);
    }

    /**
     * Return the appropriate data.
     */
    @Override
    public Object getValueAt(int row, int col) {
	return (col == columnNames.length - 1)
	    ? iodata.get(row).toString()
	    : iodata.get(row).getRate(columnNames[col]);
    }

    @Override
    public Class<?> getColumnClass(int c) {
	return (c == columnNames.length - 1) ? String.class : Double.class;
    }
}
