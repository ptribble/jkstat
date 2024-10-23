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
import uk.co.petertribble.jkstat.client.*;

/**
 * Display kernel memory allocation statistics.
 */
public class JKmemAlloc extends JKdemo {

    private static final long serialVersionUID = 1L;

    private JKmemPanel kmPanel;

    private static final String VERSION = "JKmemAlloc version 1.2";

    /**
     * Create a standalone JKmemAlloc application.
     *
     * @param kcc the client configuration
     */
    public JKmemAlloc(KClientConfig kcc) {
	this(new RemoteJKstat(kcc));
    }

    /**
     * Create a standalone JKmemAlloc application.
     */
    public JKmemAlloc() {
	this(new NativeJKstat());
    }

    /**
     * Create a standalone JKmemAlloc application.
     *
     * @param jkstat a JKstat object
     */
    public JKmemAlloc(JKstat jkstat) {
	super("jkmemalloc");

	// create main display panel
	JPanel mainPanel = new JPanel(new BorderLayout());

	// this is the main panel
	kmPanel = new JKmemPanel(jkstat, DEFAULT_INTERVAL);

	mainPanel.add(new JScrollPane(kmPanel));
	setContentPane(mainPanel);

	addInfoPanel(mainPanel, VERSION);

	setSize(620, 550);
	validate();
	setVisible(true);
    }

    @Override
    public void setDelay(int i) {
	kmPanel.setDelay(i);
	setLabelDelay(i);
    }

    @Override
    public void stopLoop() {
	kmPanel.stopLoop();
    }

    /**
     * Create a standalone JKmemAlloc application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	if (args.length == 0) {
	    new JKmemAlloc();
	} else if (args.length == 2 && "-s".equals(args[0])) {
	    new JKmemAlloc(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	} else if (args.length == 2 && "-S".equals(args[0])) {
	    new JKmemAlloc(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	} else {
	    System.err.println("Usage: kmemalloc [-s|-S url]");
	    System.exit(1);
	}
    }
}
