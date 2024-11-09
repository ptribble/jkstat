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

    private static final long serialVersionUID = 1L;

    private static final String VERSION = "Jnfsstat version 1.2";

    private JTabbedPane nfsstatPane;
    private List<KstatTable> activeTables;

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
	activeTables = new ArrayList<>();

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
	 * Add a listener to each of the tabbed panes. The aim is to work
	 * out which tab is currently selected.
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

	// rpc, connection oriented and connectionless
	addTable(jkstat, jkstat.getKstat("unix", 0, "rpc_cots_client"),
		 "Client, connection oriented", rpcPane);
	addTable(jkstat, jkstat.getKstat("unix", 0, "rpc_cots_server"),
		 "Server, connection oriented", rpcPane);
	addTable(jkstat, jkstat.getKstat("unix", 0, "rpc_clts_client"),
		 "Client, connectionless", rpcPane);
	addTable(jkstat, jkstat.getKstat("unix", 0, "rpc_clts_server"),
		 "Server, connectionless", rpcPane);
	nfsstatPane.add("rpc", rpcPane);

	// nfs client, version 2 3 4
	addTable(jkstat, jkstat.getKstat("nfs", 0, "rfsreqcnt_v2"),
		 "Version 2", nfsClientPane);
	addTable(jkstat, jkstat.getKstat("nfs", 0, "rfsreqcnt_v3"),
		 "Version 3", nfsClientPane);
	addTable(jkstat, jkstat.getKstat("nfs", 0, "rfsreqcnt_v4"),
		 "Version 4", nfsClientPane);
	nfsstatPane.add("NFS client", nfsClientPane);

	// nfs server, version 2 3 4
	addTable(jkstat, jkstat.getKstat("nfs", 0, "rfsproccnt_v2"),
		 "Version 2", nfsServerPane);
	addTable(jkstat, jkstat.getKstat("nfs", 0, "rfsproccnt_v3"),
		 "Version 3", nfsServerPane);
	addTable(jkstat, jkstat.getKstat("nfs", 0, "rfsproccnt_v4"),
		 "Version 4", nfsServerPane);
	nfsstatPane.add("NFS server", nfsServerPane);

	// nfs_acl
	addTable(jkstat, jkstat.getKstat("nfs_acl", 0, "aclreqcnt_v2"),
		 "Client, v2", nfsAclPane);
	addTable(jkstat, jkstat.getKstat("nfs_acl", 0, "aclreqcnt_v3"),
		 "Client, v3", nfsAclPane);
	addTable(jkstat, jkstat.getKstat("nfs_acl", 0, "aclreqcnt_v4"),
		 "Client, v4", nfsAclPane);
	addTable(jkstat, jkstat.getKstat("nfs_acl", 0, "aclproccnt_v2"),
		 "Server, v2", nfsAclPane);
	addTable(jkstat, jkstat.getKstat("nfs_acl", 0, "aclproccnt_v3"),
		 "Server, v3", nfsAclPane);
	addTable(jkstat, jkstat.getKstat("nfs_acl", 0, "aclproccnt_v4"),
		 "Server, v4", nfsAclPane);
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

    /*
     * Convenience method to add a table
     */
    private void addTable(JKstat jkstat, Kstat kstat, String title,
			  JTabbedPane parentPane) {
	if (kstat != null) {
	    KstatTable newTable = new KstatTable(kstat, 0, jkstat);
	    activeTables.add(newTable);
	    parentPane.add(title, new JScrollPane(newTable));
	}
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
	} else if (args.length == 2 && "-s".equals(args[0])) {
	    new Jnfsstat(
		    new KClientConfig(args[1], KClientConfig.CLIENT_XMLRPC));
	} else if (args.length == 2 && "-S".equals(args[0])) {
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
