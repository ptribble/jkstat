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

package uk.co.petertribble.jkstat.browser;

import javax.swing.*;
import java.awt.event.*;
import uk.co.petertribble.jingle.JingleInfoFrame;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.client.*;
import uk.co.petertribble.jkstat.gui.KstatResources;
import uk.co.petertribble.jkstat.demo.*;

/**
 * A graphical Kstat browser, showing the available kstats as a tree.
 * Applet version to run in a browser, connecting back to the same host
 * the applet is deployed from.
 *
 * @author Peter Tribble
 */
public class KstatApplet extends JApplet implements ActionListener {

    private JKstat jkstat;
    private KstatTreePanel ktp;

    private JMenuItem infoItem;
    private JMenuItem helpItem;
    private JMenuItem licenseItem;

    private JRadioButtonMenuItem sleepItem1;
    private JRadioButtonMenuItem sleepItem2;
    private JRadioButtonMenuItem sleepItem5;
    private JRadioButtonMenuItem sleepItem10;

    /**
     * Build the applet interface.
     */
    public void init() {
	jkstat = new RemoteJKstat(
		new KClientConfig(getCodeBase().toString() + "kstat",
		KClientConfig.CLIENT_REST));

	ktp = new KstatTreePanel(jkstat);
	setContentPane(ktp);

	JMenuBar jm = new JMenuBar();

	JMenu jmi = new JMenu(KstatResources.getString("INFO.TEXT"));
	jmi.setMnemonic(KeyEvent.VK_I);
	infoItem = new JMenuItem(
			KstatResources.getString("INFO.STATISTICS.TEXT"),
			KeyEvent.VK_S);
	infoItem.addActionListener(this);
	jmi.add(infoItem);

	JMenu jms = new JMenu(KstatResources.getString("SLEEP.TEXT"));
	jms.setMnemonic(KeyEvent.VK_U);
	sleepItem1 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.1"));
	sleepItem1.addActionListener(this);
	sleepItem2 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.2"));
	sleepItem2.addActionListener(this);
	sleepItem5 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.5"), true);
	sleepItem5.addActionListener(this);
	sleepItem10 = new JRadioButtonMenuItem(
				KstatResources.getString("SLEEP.10"));
	sleepItem10.addActionListener(this);
	jms.add(sleepItem1);
	jms.add(sleepItem2);
	jms.add(sleepItem5);
	jms.add(sleepItem10);

	ButtonGroup sleepGroup = new ButtonGroup();
	sleepGroup.add(sleepItem1);
	sleepGroup.add(sleepItem2);
	sleepGroup.add(sleepItem5);
	sleepGroup.add(sleepItem10);

	JMenu jmh = new JMenu(KstatResources.getString("HELP.TEXT"));
	jmh.setMnemonic(KeyEvent.VK_H);
	helpItem = new JMenuItem(KstatResources.getString("HELP.ABOUT.TEXT")
				+ " the kstat applet", KeyEvent.VK_A);
	helpItem.addActionListener(this);
	jmh.add(helpItem);
	licenseItem = new JMenuItem(
				KstatResources.getString("HELP.LICENSE.TEXT"),
				KeyEvent.VK_L);
	licenseItem.addActionListener(this);
	jmh.add(licenseItem);

	jm.add(jmi);
	jm.add(jms);
	jm.add(new KstatToolsMenu(jkstat, false));
	jm.add(jmh);

	setJMenuBar(jm);

	setSize(540, 600);
	setVisible(true);
    }

    /**
     * Set the update interval of the currently displayed kstat.
     *
     * @param i the desired update interval in seconds
     */
    public void setDelay(int i) {
	ktp.setDelay(i);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == infoItem) {
	    ktp.showStats();
	}
	if (e.getSource() == helpItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/applet.html", "text/html");
	}
	if (e.getSource() == licenseItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/CDDL.txt", "text/plain");
	}
	if (e.getSource() == sleepItem1) {
	    setDelay(1);
	}
	if (e.getSource() == sleepItem2) {
	    setDelay(2);
	}
	if (e.getSource() == sleepItem5) {
	    setDelay(5);
	}
	if (e.getSource() == sleepItem10) {
	    setDelay(10);
	}
    }
}
