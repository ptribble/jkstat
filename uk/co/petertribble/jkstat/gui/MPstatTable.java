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

import javax.swing.*;
import java.awt.event.*;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jingle.TableSorter;

/**
 * A tabular representation of mpstat data.
 *
 * @author Peter Tribble
 */
public class MPstatTable extends JTable {

    private static final long serialVersionUID = 1L;

    private MPstatTableModel ktm;
    TableSorter sortedModel;
    JKstat jkstat;

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
