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
import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.NativeJKstat;
import uk.co.petertribble.jkstat.gui.MPstatTable;
import uk.co.petertribble.jkstat.client.*;

/**
 * A tabular representation of the mpstat command.
 *
 * @author Peter Tribble
 */
public class JMPstat extends JKdemo {

    private MPstatTable mptable;

    private static final String VERSION = "JMPstat version 1.2";

    /**
     * Construct a client JMPstat application.
     *
     * @param kcc the client configuration
     */
    public JMPstat(KClientConfig kcc) {
	this(new RemoteJKstat(kcc), true);
    }

    /**
     * Construct a new JMPstat application.
     */
    public JMPstat() {
	this(new NativeJKstat(), true);
    }

    /**
     * Construct a new JMPstat application.
     *
     * @param jkstat a JKstat object
     * @param standalone a boolean, true if the demo is a standalone
     * application.
     */
    public JMPstat(JKstat jkstat, boolean standalone) {
	super("jmpstat", standalone);

        // create main display panel
        JPanel mainPanel = new JPanel(new BorderLayout());

	mptable = new MPstatTable(jkstat, DEFAULT_INTERVAL);
	mainPanel.add(new JScrollPane(mptable));
	setContentPane(mainPanel);

	addInfoPanel(mainPanel, VERSION);

	setIconImage(new ImageIcon(this.getClass().getClassLoader()
			.getResource("pixmaps/jmpstat.png")).getImage());

	setSize(620, 250);
	validate();
	setVisible(true);
    }

    @Override
    public void stopLoop() {
	mptable.stopLoop();
    }

    @Override
    public void setDelay(int i) {
	mptable.setDelay(i);
	setLabelDelay(i);
    }

    /**
     * Create a JMPstat application from the command line.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	if (args.length == 0) {
	    new JMPstat();
	} else if (args.length == 2 && args[0].equals("-s")) {
	    new JMPstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	} else if (args.length == 2 && args[0].equals("-S")) {
	    new JMPstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	} else {
	    System.err.println("Usage: mpstat [-s|-S url]");
	    System.exit(1);
	}
    }
}
