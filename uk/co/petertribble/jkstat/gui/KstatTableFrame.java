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
import java.awt.BorderLayout;
import java.awt.event.*;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.JKstat;

/**
 * A JFrame that wraps a KstatTable.
 *
 * @author Peter Tribble
 */
public class KstatTableFrame extends JFrame implements ActionListener {

    private KstatTable kt;

    /**
     * Wrap a {@code KstatTable} in a {@code JFrame} so that it can be viewed
     * in a separate window.
     *
     * @param module the kstat module
     * @param instance the kstat instance
     * @param name the kstat name
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     */
    public KstatTableFrame(String module, String instance, String name,
		int interval, JKstat jkstat) {
	this(jkstat.getKstat(module, Integer.parseInt(instance), name),
		interval, jkstat);
    }

    /**
     * Wrap a {@code KstatTable} in a {@code JFrame} so that it can be viewed
     * in a separate window.
     *
     * @param ks the {@code Kstat}
     * @param interval the update interval in seconds
     * @param jkstat a {@code JKstat}
     */
    public KstatTableFrame(Kstat ks, int interval, JKstat jkstat) {
	setTitle(ks.getTriplet());
	kt = new KstatTable(ks, interval, jkstat);
	addWindowListener(new winExit());
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	JPanel p = new JPanel(new BorderLayout());

	p.add(new JScrollPane(kt));

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	buttonPanel.add(Box.createHorizontalGlue());
	JButton closeButton = new JButton(
				KstatResources.getString("WINDOW.CLOSE.TEXT"));
	closeButton.addActionListener(this);
	buttonPanel.add(closeButton);
	p.add(buttonPanel, BorderLayout.SOUTH);

	setContentPane(p);
	setSize(420, 320);
	validate();
	setVisible(true);
    }

    /**
     * On closure, stop the table updating.
     */
    class winExit extends WindowAdapter {
	@Override
	public void windowClosing(WindowEvent we) {
	    kt.stopLoop();
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	kt.stopLoop();
	setVisible(false);
	dispose();
    }
}
