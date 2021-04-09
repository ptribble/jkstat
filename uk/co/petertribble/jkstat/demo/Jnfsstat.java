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
import javax.swing.event.*;
import java.awt.BorderLayout;
import java.awt.Component;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.gui.KstatTable;
import java.util.*;
import uk.co.petertribble.jkstat.client.*;

/**
 * A tabular representation of nfs activity.
 *
 * @author Peter Tribble
 */
public class Jnfsstat extends JKdemo implements ChangeListener {

    private static final String VERSION = "Jnfsstat version 1.2";

    private JTabbedPane nfsstatPane;
    private List <KstatTable> activeTables;

    /**
     * Construct a new Jnfsstat application.
     *
     * @param kcc the client configuration
     */
    public Jnfsstat(KClientConfig kcc) {
	this(new RemoteJKstat(kcc), true);
    }

    /**
     * Construct a new Jnfsstat application.
     */
    public Jnfsstat() {
	this(new NativeJKstat(), true);
    }

    /**
     * Construct a new Jnfsstat application.
     *
     * @param jkstat a JKstat object
     * @param standalone a boolean, true if the demo is a standalone
     * application.
     */
    public Jnfsstat(JKstat jkstat, boolean standalone) {
	super("jnfsstat", standalone);

	// create main display panel
	JPanel mainPanel = new JPanel(new BorderLayout());
	setContentPane(mainPanel);

	/*
	 * This Set contains the list of tables we have present.
	 * We can then iterate over all the tables that exist.
	 */
	activeTables = new ArrayList <KstatTable> ();

	/*
	 * We have panels for the following: server and client;
	 * nfs, rpc, and nfs_acl; for nfs version 2,3, and 4
	 * They are presented in a nested hierarchy of tab panes.
	 */
	nfsstatPane = new JTabbedPane();
	JTabbedPane rpcPane = new JTabbedPane();
	JTabbedPane nfsClientPane = new JTabbedPane();
	JTabbedPane nfsServerPane = new JTabbedPane();
	JTabbedPane nfsAclPane = new JTabbedPane();

	/*
	 * Add a listener to the tabbed panes. The aim is to work out which tab
	 * is currently selected.
	 */
	nfsstatPane.addChangeListener(this);
	rpcPane.addChangeListener(this);
	nfsClientPane.addChangeListener(this);
	nfsServerPane.addChangeListener(this);
	nfsAclPane.addChangeListener(this);

	/*
	 * We first check to see if the kstats exist and then
	 * create the corresponding tables.
	 */
	Kstat kstat;
	// rpc, connection oriented and connectionless
	kstat = jkstat.getKstat("unix", 0, "rpc_cots_client");
	if (kstat != null) {
	    KstatTable rpccotsClientTable = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(rpccotsClientTable);
	    rpcPane.add("Client, connection oriented",
			new JScrollPane(rpccotsClientTable));
	}
	kstat = jkstat.getKstat("unix", 0, "rpc_cots_server");
	if (kstat != null) {
	    KstatTable rpccotsServerTable = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(rpccotsServerTable);
	    rpcPane.add("Server, connection oriented",
			new JScrollPane(rpccotsServerTable));
	}
	kstat = jkstat.getKstat("unix", 0, "rpc_clts_client");
	if (kstat != null) {
	    KstatTable rpccltsClientTable = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(rpccltsClientTable);
	    rpcPane.add("Client, connectionless",
			new JScrollPane(rpccltsClientTable));
	}
	kstat = jkstat.getKstat("unix", 0, "rpc_clts_server");
	if (kstat != null) {
	    KstatTable rpccltsServerTable = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(rpccltsServerTable);
	    rpcPane.add("Server, connectionless",
			new JScrollPane(rpccltsServerTable));
	}
	nfsstatPane.add("rpc", rpcPane);

	// nfs client, version 2 3 4
	kstat = jkstat.getKstat("nfs", 0, "rfsreqcnt_v2");
	if (kstat != null) {
	    KstatTable nfsClientTable2 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsClientTable2);
	    nfsClientPane.add("Version 2", new JScrollPane(nfsClientTable2));
	}
	kstat = jkstat.getKstat("nfs", 0, "rfsreqcnt_v3");
	if (kstat != null) {
	    KstatTable nfsClientTable3 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsClientTable3);
	    nfsClientPane.add("Version 3", new JScrollPane(nfsClientTable3));
	}
	kstat = jkstat.getKstat("nfs", 0, "rfsreqcnt_v4");
	if (kstat != null) {
	    KstatTable nfsClientTable4 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsClientTable4);
	    nfsClientPane.add("Version 4", new JScrollPane(nfsClientTable4));
	}
	nfsstatPane.add("NFS client", nfsClientPane);

	// nfs server, version 2 3 4
	kstat = jkstat.getKstat("nfs", 0, "rfsproccnt_v2");
	if (kstat != null) {
	    KstatTable nfsServerTable2 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsServerTable2);
	    nfsServerPane.add("Version 2", new JScrollPane(nfsServerTable2));
	}
	kstat = jkstat.getKstat("nfs", 0, "rfsproccnt_v3");
	if (kstat != null) {
	    KstatTable nfsServerTable3 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsServerTable3);
	    nfsServerPane.add("Version 3", new JScrollPane(nfsServerTable3));
	}
	kstat = jkstat.getKstat("nfs", 0, "rfsproccnt_v4");
	if (kstat != null) {
	    KstatTable nfsServerTable4 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsServerTable4);
	    nfsServerPane.add("Version 4", new JScrollPane(nfsServerTable4));
	}
	nfsstatPane.add("NFS server", nfsServerPane);

	// nfs_acl
	kstat = jkstat.getKstat("nfs_acl", 0, "aclreqcnt_v2");
	if (kstat != null) {
	    KstatTable nfsClientAclTable2 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsClientAclTable2);
	    nfsAclPane.add("Client, v2",
			new JScrollPane(nfsClientAclTable2));
	}
	kstat = jkstat.getKstat("nfs_acl", 0, "aclreqcnt_v3");
	if (kstat != null) {
	    KstatTable nfsClientAclTable3 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsClientAclTable3);
	    nfsAclPane.add("Client, v3",
			new JScrollPane(nfsClientAclTable3));
	}
	kstat = jkstat.getKstat("nfs_acl", 0, "aclreqcnt_v4");
	if (kstat != null) {
	    KstatTable nfsClientAclTable4 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsClientAclTable4);
	    nfsAclPane.add("Client, v4",
			new JScrollPane(nfsClientAclTable4));
	}
	kstat = jkstat.getKstat("nfs_acl", 0, "aclproccnt_v2");
	if (kstat != null) {
	    KstatTable nfsServerAclTable2 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsServerAclTable2);
	    nfsAclPane.add("Server, v2",
			new JScrollPane(nfsServerAclTable2));
	}
	kstat = jkstat.getKstat("nfs_acl", 0, "aclproccnt_v3");
	if (kstat != null) {
	    KstatTable nfsServerAclTable3 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsServerAclTable3);
	    nfsAclPane.add("Server, v3",
			new JScrollPane(nfsServerAclTable3));
	}
	kstat = jkstat.getKstat("nfs_acl", 0, "aclproccnt_v4");
	if (kstat != null) {
	    KstatTable nfsServerAclTable4 = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(nfsServerAclTable4);
	    nfsAclPane.add("Server, v4",
			new JScrollPane(nfsServerAclTable4));
	}
	nfsstatPane.add("nfs_acl", nfsAclPane);

	mainPanel.add(nfsstatPane);

	addInfoPanel(mainPanel, VERSION);

	/*
	 * The update interval for all the components above was set to zero
	 * so that they wouldn't all start updating. Now we set the delay
	 * to a valid value, and the display logic updates the visible panel.
	 */
	setDelay(DEFAULT_INTERVAL);

	setIconImage(new ImageIcon(this.getClass().getClassLoader()
			.getResource("pixmaps/jnfsstat.png")).getImage());

	setSize(620, 360);
	validate();
	setVisible(true);
    }

    @Override
    public void setDelay(int i) {
	for (KstatTable kst : activeTables) {
	    kst.setDelay(i);
	}
	setLabelDelay(i);
    }

    @Override
    public void stopLoop() {
	for (KstatTable kst : activeTables) {
	    kst.stopLoop();
	}
    }

    /**
     * Create a Jnfsstat application from the command line.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	if (args.length == 0) {
	    new Jnfsstat();
	} else if (args.length == 2 && args[0].equals("-s")) {
	    new Jnfsstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	} else if (args.length == 2 && args[0].equals("-S")) {
	    new Jnfsstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_REST));
	} else {
	    System.err.println("Usage: nfsstat [-s|-S url]");
	    System.exit(1);
	}
    }

    private void activateCurrentTab() {
	Component c = nfsstatPane.getSelectedComponent();
	if (c == null) {
	    return;
	}
	JTabbedPane ctp = (JTabbedPane) c;
	c = ctp.getSelectedComponent();
	if (c == null) {
	    return;
	}
	JScrollPane js = (JScrollPane) c;
	c = js.getViewport().getView();
	if (c == null) {
	    return;
	}
	((KstatTable) c).startLoop();
    }

    // for ChangeListener
    @Override
    public void stateChanged(ChangeEvent ce) {
	stopLoop();
	activateCurrentTab();
    }
}
