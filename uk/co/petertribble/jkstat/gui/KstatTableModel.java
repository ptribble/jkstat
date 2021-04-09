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

import javax.swing.table.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.JKstat;

/**
 * The Table Model underlying a KstatTable.
 *
 * @author Peter Tribble
 */
public class KstatTableModel extends AbstractTableModel
	implements ActionListener {

    private static final String[] columnNames = { "Name", "Value", "Rate" };

    /**
     * A Timer, to update the model in a loop.
     */
    protected Timer timer;

    /**
     * The initial update delay, in milliseconds.
     */
    protected int delay = 1000;

    private JKstat jkstat;
    private Kstat ks;
    private Kstat oldks;
    private String[] rowNames;

    /**
     * Create a Table Model from the given kstat, updating at the specified
     * interval. If the interval is zero then the table won't be updated. If
     * the interval is less than zero, then rates won't be shown.
     *
     * @param ks the {@code Kstat} to be represented by this model
     * @param interval the desired update interval, in seconds
     * @param jkstat a {@code JKstat}
     */
    public KstatTableModel(Kstat ks, int interval, JKstat jkstat) {
	this.ks = ks;
	delay = interval*1000;
	this.jkstat = jkstat;
	update();
	rowNames = ks.statistics().toArray(new String[ks.statistics().size()]);
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
	return (delay < 0) ? columnNames.length - 1 : columnNames.length;
    }

    @Override
    public int getRowCount() {
	return rowNames.length;
    }

    @Override
    public String getColumnName(int col) {
	return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
	if (col == 0) {
	    return rowNames[row];
	} else if (col == 1) {
	    return ks.getData(rowNames[row]);
	} else {
	    if (ks.isNumeric(rowNames[row])) {
		long ll = ks.longData(rowNames[row]) -
						oldks.longData(rowNames[row]);
		if (ll == 0) {
		    return Long.valueOf(0L);
		}
		long snapdelta = ks.getSnaptime() - oldks.getSnaptime();
		return (snapdelta == 0) ? new Double(Double.NaN)
		    : new Double(ll*(1000000000.0/snapdelta));
	    } else {
		return "-";
	    }
	}
    }

    @Override
    public Class<?> getColumnClass(int c) {
	return getValueAt(0, c).getClass();
    }
}
