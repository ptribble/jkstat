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

import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import java.text.DecimalFormat;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.JKstat;

/**
 * The Table Model underlying a KstatTable.
 *
 * @author Peter Tribble
 */
public final class KstatTableModel extends AbstractTableModel
	implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMNS = {"Name", "Value", "Rate"};
    private static final DecimalFormat DF = new DecimalFormat("##0.0###");

    /**
     * A Timer, to update the model in a loop.
     */
    private Timer timer;

    /**
     * The initial update delay, in milliseconds.
     */
    private int delay;

    private transient JKstat jkstat;
    /**
     * The current Kstat.
     */
    private Kstat ks;
    /**
     * The previous snapshot of the current Kstat.
     */
    private Kstat oldks;
    /**
     * The names of the displayed rows.
     */
    private String[] rowNames;

    /**
     * Create a Table Model from the given kstat, updating at the specified
     * interval. If the interval is zero then the table won't be updated. If
     * the interval is less than zero, then rates won't be shown.
     *
     * @param nks the {@code Kstat} to be represented by this model
     * @param interval the desired update interval, in seconds
     * @param njkstat a {@code JKstat}
     */
    public KstatTableModel(final Kstat nks, final int interval,
			   final JKstat njkstat) {
	ks = nks;
	delay = interval * 1000;
	jkstat = njkstat;
	update();
	rowNames = ks.statistics().toArray(new String[0]);
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
    public void setDelay(final int interval) {
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
    public void actionPerformed(final ActionEvent e) {
	update();
    }

    /**
     * Update the Kstat and the Model.
     */
    public void update() {
	oldks = ks;
	ks = jkstat.getKstat(oldks);
	// handle the kstat disappearing
	if (ks == null) {
	    stopLoop();
	    return;
	}
	fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
	return (delay < 0) ? COLUMNS.length - 1 : COLUMNS.length;
    }

    @Override
    public int getRowCount() {
	return rowNames.length;
    }

    @Override
    public String getColumnName(final int col) {
	return COLUMNS[col];
    }

    @Override
    public Object getValueAt(final int row, final int col) {
	if (col == 0) {
	    return rowNames[row];
	} else if (col == 1) {
	    return ks.getData(rowNames[row]);
	} else {
	    if (ks.isNumeric(rowNames[row])) {
		long ll = ks.longData(rowNames[row])
						- oldks.longData(rowNames[row]);
		if (ll == 0) {
		    return 0L;
		}
		long snapdelta = ks.getSnaptime() - oldks.getSnaptime();
		return (snapdelta == 0) ? Double.NaN
		    : DF.format(ll * (1000000000.0 / snapdelta));
	    } else {
		return "-";
	    }
	}
    }

    @Override
    public Class<?> getColumnClass(final int c) {
	return getValueAt(0, c).getClass();
    }
}
