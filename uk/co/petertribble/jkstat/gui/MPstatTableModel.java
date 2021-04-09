/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

package uk.co.petertribble.jkstat.gui;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import uk.co.petertribble.jkstat.api.ChartableMPstat;
import uk.co.petertribble.jkstat.api.ChartableKstat;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.KstatSet;

/**
 * A TableModel to implement mpstat.
 *
 * @author Peter Tribble
 */
public final class MPstatTableModel extends AbstractTableModel
	implements ActionListener {

    /*
     * The columns remove wt from the normal list, and add "cpu" on at the
     * beginning.
     */
    private String[] columnNames = { "CPU", "minf", "mjf", "xcal", "intr",
		"ithr", "csw", "icsw", "migr", "smtx", "srw", "syscl", "usr",
		"sys", "idl"};

    private List <ChartableMPstat> mpdata;
    private Timer timer;
    private int delay = 1000;
    private JKstat jkstat;
    private KstatSet kss;

    /**
     * Create a Table Model from the given Kstats.
     *
     * @param kss the {@code KstatSet} to be represented by this model
     * @param interval the desired update interval, in seconds
     * @param jkstat a {@code JKstat}
     */
    public MPstatTableModel(KstatSet kss, int interval, JKstat jkstat) {
	this.kss = kss;
	this.jkstat = jkstat;

	mpdata = new ArrayList <ChartableMPstat> ();
	for (Kstat ks : kss.getKstats()) {
	    mpdata.add(new ChartableMPstat(jkstat, ks));
	}

	delay = interval*1000;
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
	    delay = interval*1000;
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
     * Update the statistics. Iterates through the current list
     * updating each one. If a kstat disappears, it is removed.
     */
    public void updateKstat() {
	/*
	 * If any new statistics, add them.
	 */
	if (kss.chainupdate() != 0) {
	    for (Kstat ks : kss.getAddedKstats()) {
		mpdata.add(new ChartableMPstat(jkstat, ks));
	    }
	}
	Iterator <ChartableMPstat> vki = mpdata.iterator();
	while (vki.hasNext()) {
	    ChartableMPstat cks = vki.next();
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
	return mpdata.size();
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
	return mpdata.get(row);
    }

    /**
     * Return the appropriate data.
     */
    @Override
    public Object getValueAt(int row, int col) {
	return (col == 0) ?
	    mpdata.get(row).toString() :
	    (long) mpdata.get(row).getRate(columnNames[col]);
	/*
	 * The cast above is necessary to force it to be displayed by the
	 * table as a long. getRate() returns a double, and the table uses
	 * that and ignores getColumnClass(), so would display it with a
	 * decimal point.
	 */
    }

    @Override
    public Class<?> getColumnClass(int c) {
	return (c == 0) ? String.class : Long.class;
    }
}
