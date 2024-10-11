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

package uk.co.petertribble.jkstat.demo;

import javax.swing.*;
import java.awt.BorderLayout;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.gui.IOstatTable;
import uk.co.petertribble.jkstat.client.*;

/**
 * A graphical tabular display of I/O statistics.
 *
 * @author Peter Tribble
 */
public class JIOstat extends JKdemo {

    private static final long serialVersionUID = 1L;

    private IOstatTable iotable;

    private static final String VERSION = "JIOstat version 1.2";

    /**
     * Construct a new JIOstat application.
     *
     * @param kcc the client configuration
     */
    public JIOstat(KClientConfig kcc) {
	this(new RemoteJKstat(kcc), true);
    }

    /**
     * Construct a new JIOstat application.
     */
    public JIOstat() {
	this(new NativeJKstat(), true);
    }

    /**
     * Construct a new JIOstat application.
     *
     * @param jkstat a JKstat object
     * @param standalone a boolean, true if the demo is a standalone
     * application.
     */
    public JIOstat(JKstat jkstat, boolean standalone) {
	super("jiostat", standalone);

        // create main display panel
        JPanel mainPanel = new JPanel(new BorderLayout());

	/*
	 * Filter on all IO kstats.
	 */
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.setFilterType(KstatType.KSTAT_TYPE_IO);
	// ignore usba statistics
	ksf.addNegativeFilter("usba:::");
	KstatSet kss = new KstatSet(jkstat, ksf);
	iotable = new IOstatTable(kss, DEFAULT_INTERVAL, jkstat);
	mainPanel.add(new JScrollPane(iotable));
	setContentPane(mainPanel);

	addInfoPanel(mainPanel, VERSION);

	setIconImage(new ImageIcon(this.getClass().getClassLoader()
			.getResource("pixmaps/jiostat.png")).getImage());

	setSize(620, 250);
	validate();
	setVisible(true);
    }

    @Override
    public void stopLoop() {
	iotable.stopLoop();
    }

    @Override
    public void setDelay(int i) {
	iotable.setDelay(i);
	setLabelDelay(i);
    }

    /**
     * Create a JIOstat application from the command line.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	if (args.length == 0) {
	    new JIOstat();
	} else if (args.length == 2 && "-s".equals(args[0])) {
	    new JIOstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	} else if (args.length == 2 && "-S".equals(args[0])) {
	    new JIOstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	} else {
	    System.err.println("Usage: iostat [-s|-S url]");
	    System.exit(1);
	}
    }
}
