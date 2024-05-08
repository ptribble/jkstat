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
import uk.co.petertribble.jingle.JingleMultiFrame;
import uk.co.petertribble.jingle.JingleInfoFrame;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.client.*;
import uk.co.petertribble.jkstat.parse.ParseableJSONZipJKstat;
import uk.co.petertribble.jkstat.gui.KstatResources;
import uk.co.petertribble.jkstat.demo.*;

/**
 * A graphical Kstat browser, showing the available kstats as a tree.
 *
 * @author Peter Tribble
 */
public class KstatBrowser extends JFrame implements ActionListener {

    private JKstat jkstat;
    private KstatTreePanel ktp;

    private JMenuItem exitItem;
    private JMenuItem cloneItem;
    private JMenuItem closeItem;
    private JMenuItem infoItem;
    private JMenuItem helpItem;
    private JMenuItem licenseItem;

    private JRadioButtonMenuItem sleepItem1;
    private JRadioButtonMenuItem sleepItem2;
    private JRadioButtonMenuItem sleepItem5;
    private JRadioButtonMenuItem sleepItem10;

    /**
     * Constructs a client KstatBrowser.
     *
     * @param kcc The client configuration
     */
    public KstatBrowser(KClientConfig kcc) {
	this(new RemoteJKstat(kcc));
    }

    /**
     * Constructs a local KstatBrowser.
     */
    public KstatBrowser() {
	this(new NativeJKstat());
    }

    /**
     * Constructs a local KstatBrowser.
     *
     * @param jkstat a JKstat object
     */
    public KstatBrowser(JKstat jkstat) {
	super(KstatResources.getString("BROWSERUI.NAME.TEXT"));
	this.jkstat = jkstat;
	addWindowListener(new winExit());

	ktp = new KstatTreePanel(jkstat);
	setContentPane(ktp);

	JMenuBar jm = new JMenuBar();

	JMenu jme = new JMenu(KstatResources.getString("FILE.TEXT"));
	jme.setMnemonic(KeyEvent.VK_F);
	cloneItem = new JMenuItem(
			KstatResources.getString("FILE.NEWBROWSER.TEXT"),
			KeyEvent.VK_B);
	cloneItem.addActionListener(this);
	jme.add(cloneItem);
	closeItem = new JMenuItem(
			KstatResources.getString("FILE.CLOSEWIN.TEXT"),
			KeyEvent.VK_W);
	closeItem.addActionListener(this);
	jme.add(closeItem);
	exitItem = new JMenuItem(KstatResources.getString("FILE.EXIT.TEXT"),
			KeyEvent.VK_X);
	exitItem.addActionListener(this);
	jme.add(exitItem);

	JingleMultiFrame.register(this, closeItem);

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
				+ " kstatbrowser", KeyEvent.VK_A);
	helpItem.addActionListener(this);
	jmh.add(helpItem);
	licenseItem = new JMenuItem(
				KstatResources.getString("HELP.LICENSE.TEXT"),
				KeyEvent.VK_L);
	licenseItem.addActionListener(this);
	jmh.add(licenseItem);

	jm.add(jme);
	jm.add(jmi);
	jm.add(jms);
	// add the demo menu if we're reading live statistics
	if (!(jkstat instanceof SequencedJKstat)) {
	    jm.add(new KstatToolsMenu(jkstat, false));
	}
	jm.add(jmh);

	setJMenuBar(jm);

	setIconImage(new ImageIcon(this.getClass().getClassLoader()
			.getResource("pixmaps/jkstat.png")).getImage());

	setSize(540, 600);
	setVisible(true);
    }

    class winExit extends WindowAdapter {
	@Override
	public void windowClosing(WindowEvent we) {
	    JingleMultiFrame.unregister(KstatBrowser.this);
	}
    }

    /**
     * Set the update interval of the currently displayed kstat.
     *
     * @param i the desired update interval in seconds
     */
    public void setDelay(int i) {
	ktp.setDelay(i);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == cloneItem) {
	    if (jkstat instanceof SequencedJKstat) {
		new KstatBrowser(((SequencedJKstat) jkstat).newInstance());
	    } else {
		new KstatBrowser(jkstat);
	    }
	}
	if (e.getSource() == closeItem) {
	    JingleMultiFrame.unregister(this);
	}
	if (e.getSource() == exitItem) {
	    System.exit(0);
	}
	if (e.getSource() == infoItem) {
	    ktp.showStats();
	}
	if (e.getSource() == helpItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/index.html", "text/html");
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

    private static void usage() {
	System.err.println("Usage: browser [-s|-S [url]]");
	System.err.println("       browser [-m]");
	System.err.println("       browser [-z zipfile]");
	System.exit(1);
    }

    /**
     * Create a KstatBrowser.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	if (args.length == 0) {
	    new KstatBrowser();
	} else if (args.length == 1) {
	    if ("-s".equals(args[0]) || "-S".equals(args[0])) {
		KClientDialog kcd = new KClientDialog("-s".equals(args[0]) ?
					KClientConfig.CLIENT_XMLRPC :
					KClientConfig.CLIENT_REST);
		KClientConfig kcc = kcd.getConfig();
		if (kcc.isConfigured()) {
		    new KstatBrowser(kcc);
		} else {
		    System.err.println("No servers");
		    usage();
		}
	    } else if ("-m".equals(args[0])) {
		KBrowseDialog kbd = new KBrowseDialog();
		KClientConfig kcc = kbd.getConfig();
		if (kcc.isConfigured()) {
		    new KstatBrowser(kcc);
		} else {
		    System.err.println("No servers");
		    usage();
		}
	    } else {
		usage();
	    }
	} else if (args.length == 2) {
	    if ("-s".equals(args[0])) {
		new KstatBrowser(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	    } else if ("-S".equals(args[0])) {
		new KstatBrowser(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	    } else if ("-z".equals(args[0])) {
		try {
		    new KstatBrowser(new ParseableJSONZipJKstat(args[1], true));
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
