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
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.JKstat;

/**
 * A tabular representation of a Kstat.
 *
 * @author Peter Tribble
 */
public class KstatTable extends JTable {

    private KstatTableModel ktm;
    private JKstat jkstat;
    private Kstat ks;

    /**
     * Wraps a {@code Kstat} in a {@code JTable}, adding a right-click
     * popup menu to allow extra functionality such as creating a chart
     * so that the statistics can be displayed over time.
     *
     * @param module the kstat module
     * @param instance the kstat instance
     * @param name the kstat name
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     */
    public KstatTable(String module, String instance, String name,
		int interval, JKstat jkstat) {
	this(jkstat.getKstat(module, Integer.parseInt(instance), name),
		interval, jkstat);
    }

    /**
     * Wraps a {@code Kstat} in a {@code JTable}, adding a right-click
     * popup menu to allow extra functionality such as creating a chart
     * so that the statistics can be displayed over time.
     *
     * @param ks a {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     */
    public KstatTable(Kstat ks, int interval, JKstat jkstat) {
	this.jkstat = jkstat;
	this.ks = ks;
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
	public void mousePressed(MouseEvent e) {
	    showPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    showPopup(e);
	}

	private void showPopup(MouseEvent e) {
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
	    public void actionPerformed(ActionEvent e) {
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
    public void setDelay(int interval) {
	if (ktm != null) {
	    ktm.setDelay(interval);
	}
    }
}
