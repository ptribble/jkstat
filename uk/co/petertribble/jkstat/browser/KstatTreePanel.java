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

package uk.co.petertribble.jkstat.browser;

import java.util.Map;
import java.util.SortedMap;
import java.util.Date;
import java.text.DateFormat;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.*;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.gui.*;
import uk.co.petertribble.jingle.JingleTextPane;

/**
 * A panel showing the kstats in a tree structure in a left pane with
 * details of the selected kstat in a right pane.
 *
 * @author Peter Tribble
 */
public final class KstatTreePanel extends JPanel
    implements TreeSelectionListener, ActionListener {

    private static final long serialVersionUID = 1L;

    private transient JKstat jkstat;

    // switch behaviour if using a SequencedJKstat
    private boolean stepping;
    private boolean stopped;
    // controls for SequencedJKstat
    private JButton startB;
    private JButton beginB;
    private JButton backB;
    private JButton forwardB;
    private JButton pauseB;
    private DateFormat df;
    private JProgressBar spb;

    private JingleTextPane tp;
    private JPanel ktpanel;
    private JPanel rpanel;

    private KstatAccessoryPanel kap;
    private KstatTable kt;
    private Timer timer;
    private int interval = 5;

    private transient KstatSet kss;
    private transient KstatTreeMap ksm;
    private transient KstatModuleMap kstatMap;
    private transient SortedMap<String, KstatModuleMap> kstatClassMap;
    private transient SortedMap<String, KstatModuleMap> kstatTypeMap;

    private final KstatTreeModel moduleModel;
    private final KstatTreeModel classModel;
    private final KstatTreeModel typeModel;

    /**
     * Constructs a KstatTreePanel.
     */
    public KstatTreePanel() {
	this(new NativeJKstat());
    }

    /**
     * Constructs a KstatTreePanel.
     *
     * @param jkstat a JKstat object
     */
    public KstatTreePanel(JKstat jkstat) {
	this(jkstat, new KstatSet(jkstat));
    }

    /**
     * Constructs a KstatTreePanel.
     *
     * @param jkstat a JKstat object
     * @param kss a KstatSet object
     */
    public KstatTreePanel(JKstat jkstat, KstatSet kss) {
	this.jkstat = jkstat;
	this.kss = kss;

	stepping = (jkstat instanceof SequencedJKstat);

	ksm = new KstatTreeMap(kss);

	/*
	 * Create SortedMaps to store the Kstats in a hierarchical structure
	 * and populate them with the contents of the list.
	 */
	kstatMap = ksm.getKstatMap();
	kstatTypeMap = ksm.getKstatTypeMap();
	kstatClassMap = ksm.getKstatClassMap();

	setLayout(new BorderLayout());

	JTabbedPane jtp = new JTabbedPane();

	// create the data models
	moduleModel = new KstatTreeModel(kstatMap);
	classModel = new KstatTreeModel(kstatClassMap);
	typeModel = new KstatTreeModel(kstatTypeMap);

	// Hierarchical display by module
	JTree tree1 = new JTree(moduleModel);
	tree1.setRootVisible(false);
	tree1.setShowsRootHandles(true);
	jtp.add(KstatResources.getString("BROWSERUI.MODULETAB"),
		new JScrollPane(tree1));
	tree1.addTreeSelectionListener(this);

	// Hierarchical display by Kstat class
	JTree tree2 = new JTree(classModel);
	tree2.setRootVisible(false);
	tree2.setShowsRootHandles(true);
	jtp.add(KstatResources.getString("BROWSERUI.CLASSTAB"),
		new JScrollPane(tree2));
	tree2.addTreeSelectionListener(this);

	/*
	 * Hierarchical display by Kstat type, if we have multiple types.
	 */
	if (kstatTypeMap.size() > 1) {
	    JTree tree3 = new JTree(typeModel);
	    tree3.setRootVisible(false);
	    tree3.setShowsRootHandles(true);
	    jtp.add(KstatResources.getString("BROWSERUI.TYPETAB"),
			new JScrollPane(tree3));
	    tree3.addTreeSelectionListener(this);
	}

	// text panel on right for metadata
	tp = new JingleTextPane();

	// intermediate panel to hold the data table
	ktpanel = new JPanel(new BorderLayout());

	// right hand panel to hold text panel and accessory widget
	rpanel = new JPanel(new BorderLayout());

	// split pane to hold the lot
	JSplitPane psplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					   jtp, rpanel);
	psplit.setOneTouchExpandable(true);
	psplit.setDividerLocation(180);
	add(psplit);

	// if a SequencedJKstat, show a control/status panel at the top
	if (stepping) {
	    df = DateFormat.getDateTimeInstance();
	    JPanel cpanel = new JPanel();
	    cpanel.setLayout(new BoxLayout(cpanel, BoxLayout.LINE_AXIS));
	    beginB = new JButton("|<");
	    cpanel.add(beginB);
	    beginB.addActionListener(this);
	    beginB.setEnabled(false);
	    cpanel.add(Box.createRigidArea(new Dimension(4, 0)));
	    backB = new JButton("<");
	    cpanel.add(backB);
	    backB.addActionListener(this);
	    backB.setEnabled(false);
	    cpanel.add(Box.createRigidArea(new Dimension(4, 0)));
	    startB = new JButton(">>");
	    cpanel.add(startB);
	    startB.addActionListener(this);
	    cpanel.add(Box.createRigidArea(new Dimension(4, 0)));
	    forwardB = new JButton(">");
	    cpanel.add(forwardB);
	    forwardB.addActionListener(this);
	    cpanel.add(Box.createRigidArea(new Dimension(4, 0)));
	    pauseB = new JButton("||");
	    cpanel.add(pauseB);
	    pauseB.addActionListener(this);
	    pauseB.setEnabled(false);
	    cpanel.add(Box.createHorizontalGlue());
	    spb = new JProgressBar(0, ((SequencedJKstat) jkstat).size());
	    spb.setValue(0);
	    spb.setPreferredSize(new Dimension(200, 32));
	    spb.setMinimumSize(new Dimension(120, 24));
	    spb.setStringPainted(true);
	    spb.setString(KstatResources.getString("BROWSERUI.TIME"));
	    cpanel.add(spb);
	    cpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    add(cpanel, BorderLayout.NORTH);
	}

	// start off by showing the statistics
	showStats();

	// start a timer loop to look for updates
	if (!stepping) {
	    startLoop();
	}
    }

    private void setKstatPanel(DefaultMutableTreeNode node) {
	if ((node != null) && (node.getUserObject() instanceof Kstat)) {
	    showKstat((Kstat) node.getUserObject());
	}
    }

    private void showKstat(Kstat ks) {
	rpanel.removeAll();
	rpanel.validate();
	ks = jkstat.getKstat(ks);
	setKstatInfo(ks);
	setKstatTable(ks);
	setKstatAccessory(ks);
	rpanel.validate();
    }

    /**
     * Start the Timer so that the panel will continuously update.
     */
    public void startLoop() {
	if (timer == null) {
	    timer = new Timer(interval * 1000, this);
	}
	timer.start();
    }

    /**
     * Stop the timer loop, so that the panel will no longer be updated.
     */
    public void stopLoop() {
	if (timer != null) {
	    timer.stop();
	}
    }

    /**
     * Set the update interval.
     *
     * @param i an int specifying the delay value
     */
    public void setDelay(int i) {
	interval = i;
	if (timer != null) {
	    timer.setDelay(interval * 1000);
	}
    }

    /*
     * This is where we update any accessories. If we run out of input data,
     * stop updates completely.
     */
    private void updateAccessories() {
	if (stepping) {
	    stopped = !((SequencedJKstat) jkstat).next();
	    spb.setValue(spb.getValue() + 1);
	    spb.setString(df.format(new Date(jkstat.getTime())));
	}
	if (stopped) {
	    // we ought to stop one step earlier
	    forwardB.setEnabled(false);
	    stopLoop();
	} else {
	    if (kap != null) {
		kap.updateAccessory();
	    }
	    if (kt != null) {
		kt.update();
	    }
	}
    }

    /*
     * This is where we update any accessories. If we run out of input data,
     * stop updates completely.
     */
    private void backAccessories() {
	if (stepping) {
	    stopped = !((SequencedJKstat) jkstat).previous();
	    spb.setValue(spb.getValue() - 1);
	    spb.setString(df.format(new Date(jkstat.getTime())));
	}
	if (stopped) {
	    // we ought to stop one step earlier
	    backB.setEnabled(false);
	    beginB.setEnabled(false);
	    stopLoop();
	} else {
	    if (kap != null) {
		kap.updateAccessory();
	    }
	    if (kt != null) {
		kt.update();
	    }
	}
    }

    /*
     * Go back to the beginning.
     */
    private void resetToStart() {
	if (stepping) {
	    ((SequencedJKstat) jkstat).begin();
	    spb.setValue(0);
	    spb.setString(df.format(new Date(jkstat.getTime())));
	    if (kap != null) {
		kap.updateAccessory();
	    }
	    if (kt != null) {
		kt.update();
	    }
	}
    }

    /*
     * This is where we check for updates to the kstat chain, and then reflect
     * those changes in the gui. We need to update the maps, because the
     * statistics page is based on those, and if we're showing the statistics
     * page we need to recalculate the numbers and redisplay it.
     *
     * We also need to update the Models used by the visible trees.
     * It is the responsibility of the models to sort out the changes.
     */
    private void checkForTreeUpdates() {
	if (kss.chainupdate() != 0) {
	    for (Kstat ks : kss.getAddedKstats()) {
		addKstat(ks);
	    }
	    for (Kstat ks : kss.getDeletedKstats()) {
		removeKstat(ks);
	    }
	}
    }

    /*
     * Add an accessory panel that shows a graphical display of the kstat.
     */
    private void setKstatAccessory(Kstat ks) {
	kap = KstatAccessoryRegistry.getAccessoryPanel(ks, -1, jkstat);
	if (kap != null) {
	    rpanel.add(kap, BorderLayout.SOUTH);
	}
    }

    /*
     * Add a JTable showing the kstat data in tabular form.
     */
    private void setKstatTable(Kstat ks) {
	ktpanel.removeAll();
	kt = new KstatTable(ks, 0, jkstat);
	ktpanel.add(new JScrollPane(kt));
	rpanel.add(ktpanel);
    }

    private void setKstatInfo(Kstat ks) {
	StringBuilder sb = new StringBuilder(34);
	if (ks == null) {
	    sb.append("Invalid kstat");
	} else {
	    ksm.addKstat(ks);
	    sb.append("<h2>").append(ks.getTriplet())
		.append("</h2>\nclass: ").append(ks.getKstatClass())
		.append("; type: ").append(ks.getTypeAsString());
	}
	setInfoText(sb.toString());
	rpanel.add(tp, BorderLayout.NORTH);
    }

    private void setInfoText(String s) {
	tp.setText(s);
    }

    /*
     * Return the number of items (leaf nodes) in a Map, recursively descending
     * into contained Maps. We do not care about types, really.
     */
    @SuppressWarnings("rawtypes")
    private int countEntries(Map m) {
	int i = 0;
	for (Object o : m.values()) {
	    if (o instanceof Map) {
		i += countEntries((Map) o);
	    } else {
		i++;
	    }
	}
	return i;
    }

    /*
     * Add a new Kstat to the Maps and to the models used by the trees.
     */
    private void addKstat(Kstat ks) {
	addToModels(ks);
	ksm.addKstat(ks);
    }

    /*
     * Removes a Kstat from the Maps and from the models used by the trees.
     */
    private void removeKstat(Kstat ks) {
	removeFromModels(ks);
	ksm.removeKstat(ks);
    }

    /*
     * Add a Kstat to the models.
     */
    private void addToModels(Kstat ks) {
	moduleModel.addKstat((String) null, ks);
	classModel.addKstat(ks.getKstatClass(), ks);
	typeModel.addKstat(ks.getTypeAsString(), ks);
    }

    /*
     * Remove a Kstat from the models.
     */
    private void removeFromModels(Kstat ks) {
	moduleModel.removeKstat((String) null, ks);
	classModel.removeKstat(ks.getKstatClass(), ks);
	typeModel.removeKstat(ks.getTypeAsString(), ks);
    }

    /**
     * Display summary statistics in the right-hand frame.
     */
    public void showStats() {
	StringBuilder sb = new StringBuilder(256);
	sb.append("<h2>")
	    .append(KstatResources.getString("BROWSERUI.STATISTICS.TEXT"))
	    .append("</h2>")
	    .append(KstatResources.getString("BROWSERUI.TOTAL.TEXT"))
	    .append(' ')
	    .append(countEntries(kstatMap))
	    .append("<table border=\"1\"><tr><th colspan=\"2\">")
	    .append(kstatMap.size())
	    .append(" Modules</th></tr>\n");
	for (String s : kstatMap.keySet()) {
	    sb.append("<tr><td>").append(s).append("</td><td>")
		.append(countEntries(kstatMap.get(s)))
		.append("</td></tr>\n");
	}

	sb.append("<tr><th colspan=\"2\">").append(kstatClassMap.size())
	    .append(" Classes</th></tr>\n");
	for (String s : kstatClassMap.keySet()) {
	    sb.append("<tr><td>").append(s).append("</td><td>")
		.append(countEntries(kstatClassMap.get(s)))
		.append("</td></tr>\n");
	}

	if (kstatTypeMap.size() > 1) {
	    sb.append("<tr><th colspan=\"2\">").append(kstatTypeMap.size())
		.append(" Types</th></tr>\n");
	    for (String s : kstatTypeMap.keySet()) {
		sb.append("<tr><td>").append(s).append("</td><td>")
		    .append(countEntries(kstatTypeMap.get(s)))
		    .append("</td></tr>\n");
	    }
	}

	sb.append("</table>\n");
	// make sure the accessory and the table are nulled so we
	// don't update them again
	kap = null;
	kt = null;
	setInfoText(sb.toString());
	rpanel.removeAll();
	rpanel.add(new JScrollPane(tp));
    }

    // handle TreeSelectionListener events
    @Override
    public void valueChanged(TreeSelectionEvent e) {
	TreePath tpth = e.getNewLeadSelectionPath();
	if (tpth != null) {
	    setKstatPanel((DefaultMutableTreeNode) tpth.getLastPathComponent());
	}
    }

    // handle timer events
    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == startB) {
	    startLoop();
	    backB.setEnabled(false);
	    beginB.setEnabled(false);
	    startB.setEnabled(false);
	    pauseB.setEnabled(true);
	    forwardB.setEnabled(false);
	} else if (e.getSource() == pauseB) {
	    stopLoop();
	    beginB.setEnabled(true);
	    backB.setEnabled(true);
	    startB.setEnabled(true);
	    pauseB.setEnabled(false);
	    forwardB.setEnabled(true);
	} else if (e.getSource() == backB) {
	    forwardB.setEnabled(true);
	    checkForTreeUpdates();
	    backAccessories();
	} else if (e.getSource() == beginB) {
	    forwardB.setEnabled(true);
	    backB.setEnabled(false);
	    beginB.setEnabled(false);
	    resetToStart();
	} else if (e.getSource() == forwardB) {
	    beginB.setEnabled(true);
	    backB.setEnabled(true);
	    checkForTreeUpdates();
	    updateAccessories();
	} else {
	    checkForTreeUpdates();
	    updateAccessories();
	}
    }
}
