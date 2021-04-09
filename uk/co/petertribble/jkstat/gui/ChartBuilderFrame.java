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

import uk.co.petertribble.jkstat.api.*;
import javax.swing.*;
import java.awt.event.*;
import uk.co.petertribble.jkstat.client.*;
import uk.co.petertribble.jkstat.parse.ParseableJSONZipJKstat;

/**
 * A graphical chart builder. Allows the user to build a chart by selecting
 * a kstat or kstats and defining how the chart should look.
 *
 * @author Peter Tribble
 */
public class ChartBuilderFrame extends JFrame implements ActionListener {

    private JMenuItem exitItem;

    /**
     * Create a ChartBuilderFrame.
     */
    public ChartBuilderFrame() {
	this(new NativeJKstat());
    }

    /**
     * Create a client ChartBuilderFrame.
     *
     * @param kcc the client configuration
     */
    public ChartBuilderFrame(KClientConfig kcc) {
	this(new RemoteJKstat(kcc));
    }

    /**
     * Create a ChartBuilderFrame.
     *
     * @param jkstat a {@code JKstat}
     */
    public ChartBuilderFrame(JKstat jkstat) {
	setTitle(KstatResources.getString("CHART.BUILDER"));
	addWindowListener(new winExit());
	JMenuBar jm = new JMenuBar();
	jm.add(fileMenu());
	setJMenuBar(jm);

	setContentPane(new ChartBuilderPanel(jkstat));

	setSize(480, 420);
	validate();
	setVisible(true);
    }

    private JMenu fileMenu() {
	JMenu jme = new JMenu(KstatResources.getString("FILE.TEXT"));
	jme.setMnemonic(KeyEvent.VK_F);

	jme.addSeparator();
	exitItem = new JMenuItem(KstatResources.getString("FILE.CLOSE.TEXT"),
				KeyEvent.VK_C);
	exitItem.addActionListener(this);
	jme.add(exitItem);
	return jme;
    }

    class winExit extends WindowAdapter {
	@Override
	public void windowClosing(WindowEvent we) {
	    dispose();
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == exitItem) {
	    setVisible(false);
	    dispose();
	}
    }

    private static void usage() {
	System.err.println("Usage: chartbuilder [-s|-S [url]]");
	System.err.println("       chartbuilder [-z zipfile]");
	System.exit(1);
    }

    /**
     * Create the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
	if (args.length == 0) {
	    new ChartBuilderFrame();
	} else if (args.length == 1) {
	    if (args[0].equals("-s") || args[0].equals("-S")) {
		KClientDialog kcd = new KClientDialog(args[0].equals("-s") ?
					KClientConfig.CLIENT_XMLRPC :
					KClientConfig.CLIENT_REST);
		KClientConfig kcc = kcd.getConfig();
		if (kcc.isConfigured()) {
		    new ChartBuilderFrame(kcc);
		}
	    } else {
		usage();
	    }
	} else if (args.length == 2) {
	    if (args[0].equals("-s")) {
		new ChartBuilderFrame(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	    } else if (args[0].equals("-S")) {
		new ChartBuilderFrame(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	    } else if (args[0].equals("-z")) {
		try {
		    new ChartBuilderFrame(new ParseableJSONZipJKstat(args[1],
					true));
		} catch (Exception e) {
		    e.printStackTrace();
		    usage();
		}
	    } else {
		usage();
	    }
	} else {
	    usage();
	}
    }
}
