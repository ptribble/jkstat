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
import uk.co.petertribble.jkstat.api.KstatSet;
import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.ChartableKstat;
import uk.co.petertribble.jingle.TableSorter;

/**
 * A tabular representation of iostat.
 *
 * @author Peter Tribble
 */
public class IOstatTable extends JTable {

    private IOstatTableModel ktm;
    private TableSorter sortedModel;
    private JKstat jkstat;

    /**
     * Wraps a {@code KstatSet} in a {@code JTable}, adding a right-click
     * popup menu to allow extra functionality such as creating a chart
     * so that the statistics can be displayed over time.
     *
     * @param kss a {@code KstatSet}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     */
    public IOstatTable(KstatSet kss, int interval, JKstat jkstat) {
	this.jkstat = jkstat;
	ktm = new IOstatTableModel(kss, interval, jkstat);
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
