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
import java.awt.BorderLayout;
import java.awt.event.*;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.JKstat;

/**
 * A JFrame that wraps a KstatTable.
 *
 * @author Peter Tribble
 */
public final class KstatTableFrame extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    KstatTable kt;

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
	addWindowListener(new WindowExit());
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
    class WindowExit extends WindowAdapter {
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
