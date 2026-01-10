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
 * Copyright 2026 Peter Tribble
 *
 */

package uk.co.petertribble.jkstat.gui;

import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.browser.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/**
 * A graphical chart builder. Allows the user to build a chart by selecting
 * a kstat or kstats and defining how the chart should look.
 *
 * @author Peter Tribble
 */
public final class ChartBuilderPanel extends JPanel
    implements TreeSelectionListener, ActionListener {

    private static final long serialVersionUID = 1L;

    private transient JKstat jkstat;

    /**
     * The holding tabbed pane.
     */
    private JTabbedPane jtp;
    private static final int KTAB_ID = 0;
    private static final int STAB_ID = 1;
    /**
     * A panel to hold the stats display.
     */
    private JPanel statsPanel;

    /**
     * A check box to select all instances.
     */
    private JCheckBox allInstanceButton;
    /**
     * A check box to select aggregate instances.
     */
    private JCheckBox aggrInstanceButton;
    /**
     * A radio button to toggle values vs rates.
     */
    private JRadioButton rateButton;
    /**
     * A radio button to toggle the line style.
     */
    private JRadioButton lineStyle;
    /**
     * A button to start the chart.
     */
    private JButton goButton;

    /**
     * The selected Kstat.
     */
    private Kstat myKstat;
    /**
     * A label to show the selected Kstat.
     */
    private JLabel kstatLabel;

    /**
     * Create a ChartBuilderPanel.
     *
     * @param njkstat a JKstat object
     */
    public ChartBuilderPanel(final JKstat njkstat) {
	jkstat = njkstat;
	/*
	 * The panel contains the following:
	 *  a tree to select a kstat
	 *  a list of selected kstats
	 *  select show rate or value
	 *  select all instances
	 */
	setLayout(new BorderLayout());

	statsPanel = new JPanel();
	statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.PAGE_AXIS));

	jtp = new JTabbedPane();
	jtp.insertTab(KstatResources.getString("CHART.KTAB"), null,
		new JScrollPane(buildLeftTree()), null, KTAB_ID);
	jtp.insertTab(KstatResources.getString("CHART.STAB"), null,
		new JScrollPane(statsPanel), null, STAB_ID);
	jtp.setEnabledAt(STAB_ID, false);

	JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					jtp,
					buildRightPanel());
	jsp.setOneTouchExpandable(true);
	add(jsp);
    }

    /*
     * It makes no sense to graph the kstats listed below.
     */
    private JTree buildLeftTree() {
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.addNegativeFilter("::fm");
	ksf.addNegativeFilter("ufs directio::");
	ksf.addNegativeFilter("s1394::");
	ksf.addNegativeFilter("mm::phys_installed");
	ksf.addNegativeFilter("sockfs::sock_unix_list");
	ksf.addNegativeFilter("unix:0:kstat_types");
	ksf.addNegativeFilter("unix:0:kstat_headers");
	ksf.addNegativeFilter("unix:0:page_retire_list");
	KstatTreeMap ksm = new KstatTreeMap(new KstatSet(jkstat, ksf));
	JTree ktree = new JTree(new KstatTreeModel(ksm.getKstatMap()));
	ktree.setRootVisible(false);
	ktree.setShowsRootHandles(true);
	ktree.addTreeSelectionListener(this);
	return ktree;
    }

    private JPanel buildRightPanel() {
	// this holds all the panels
	JPanel rpanel = new JPanel();
	rpanel.setLayout(new GridLayout(0, 1));
	// vpanel holds the rate/value toggles
	JPanel vpanel = new JPanel();
	vpanel.setLayout(new BoxLayout(vpanel, BoxLayout.PAGE_AXIS));

	ButtonGroup bgval = new ButtonGroup();
	rateButton = new JRadioButton(
				KstatResources.getString("CHART.SHOWRATE"));
	rateButton.setSelected(true);
	bgval.add(rateButton);
	vpanel.add(rateButton);
	JRadioButton valueButton =
	    new JRadioButton(KstatResources.getString("CHART.SHOWVAL"));
	bgval.add(valueButton);
	vpanel.add(valueButton);
	vpanel.setBorder(BorderFactory.createTitledBorder(
				KstatResources.getString("CHART.RATEVAL")));
	rpanel.add(vpanel);

	// spanel holds the style toggles
	JPanel spanel = new JPanel();
	spanel.setLayout(new BoxLayout(spanel, BoxLayout.PAGE_AXIS));

	ButtonGroup bgstyle = new ButtonGroup();
	lineStyle = new JRadioButton(KstatResources.getString("CHART.LINE"));
	lineStyle.setSelected(true);
	bgstyle.add(lineStyle);
	spanel.add(lineStyle);
	JRadioButton stackedStyle =
	    new JRadioButton(KstatResources.getString("CHART.STACK"));
	bgstyle.add(stackedStyle);
	spanel.add(stackedStyle);

	spanel.setBorder(BorderFactory.createTitledBorder(
				KstatResources.getString("CHART.GSTYLE")));
	rpanel.add(spanel);

	// ipanel holds the instance toggles
	JPanel ipanel = new JPanel();
	ipanel.setLayout(new BoxLayout(ipanel, BoxLayout.PAGE_AXIS));
	allInstanceButton = new JCheckBox(
				KstatResources.getString("CHART.ALLINST"));
	allInstanceButton.addActionListener(this);
	ipanel.add(allInstanceButton);

	// place below and indented ?
	aggrInstanceButton = new JCheckBox(
				KstatResources.getString("CHART.AGGRINST"));
	aggrInstanceButton.setEnabled(false);
	aggrInstanceButton.addActionListener(this);
	ipanel.add(aggrInstanceButton);
	rpanel.add(ipanel);

	JPanel gpanel = new JPanel();
	gpanel.setLayout(new BoxLayout(gpanel, BoxLayout.LINE_AXIS));
	goButton = new JButton(KstatResources.getString("CHART.START"));
	goButton.setEnabled(false);
	goButton.addActionListener(this);
	gpanel.add(Box.createHorizontalGlue());
	gpanel.add(goButton);
	gpanel.add(Box.createRigidArea(new Dimension(6, 0)));
	rpanel.add(gpanel);

	kstatLabel = new JLabel(" ");
	rpanel.add(kstatLabel);

	return rpanel;
    }

    /*
     * This is called when a Kstat is picked from the tree. It saves the
     * selected Kstat and enables the Statistics tab and the Create button.
     */
    private void pickKstat(final DefaultMutableTreeNode node) {
	if ((node != null) && (node.getUserObject() instanceof Kstat)) {
	    myKstat = (Kstat) node.getUserObject();
	    updateLabel();
	    statsPanel.removeAll();
	    for (String stat : KstatUtil.numericStatistics(jkstat, myKstat)) {
		statsPanel.add(new JCheckBox(stat));
	    }
	    jtp.setSelectedIndex(STAB_ID);
	    jtp.setEnabledAt(STAB_ID, true);
	    goButton.setEnabled(true);
	}
    }

    /*
     * Update the label showing which Kstat we're going to chart
     */
    private void updateLabel() {
	if (myKstat != null) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(KstatResources.getString("CHART.WORKINGON"))
		.append(' ')
		.append(allInstanceButton.isSelected() ? getSpecifier()
			  : myKstat.getTriplet());
	    kstatLabel.setText(sb.toString());
	}
    }

    private String getSpecifier() {
	return myKstat.getModule() + "::" + myKstat.getName();
    }

    /*
     * Walk through the list of JCheckBoxes on the statsPanel, and produce
     * a List of Strings representing the selected statistics.
     */
    private List<String> selectedStats() {
	List<String> stats = new ArrayList<>();
	for (Component c : statsPanel.getComponents()) {
	    if (c instanceof JCheckBox) {
		JCheckBox jcb = (JCheckBox) c;
		if (jcb.isSelected()) {
		    stats.add(jcb.getText());
		}
	    }
	}
	return stats;
    }

    /*
     * This creates a Chart.
     *
     * Check if the list of statistics is empty, and pop up a message if it is.
     *
     * If 'all instances' then we need to construct a KstatSet based on the
     * module::name
     *
     * If we've also selected 'aggregate instances', we need to convert the Set
     * to an aggregate
     */
    private void createChart() {
	List<String> stats = selectedStats();
	if (stats.isEmpty()) {
	    JOptionPane.showMessageDialog(this,
		    KstatResources.getString("CHART.SELECT.MSG"),
		    KstatResources.getString("CHART.SELECT.TITLE"),
		    JOptionPane.WARNING_MESSAGE);
	} else {
	    boolean showRate = rateButton.isSelected();
	    if (allInstanceButton.isSelected()) {
		KstatFilter ksf = new KstatFilter(jkstat);
		String statSpecifier = getSpecifier();
		ksf.addFilter(statSpecifier);
		KstatSet kss = new KstatSet(jkstat, ksf, statSpecifier);
		if (aggrInstanceButton.isSelected()) {
		    /*
		     * NOTE: the chart creates a new instance of a
		     * SequencedJKstat, which then decouples it from the
		     * instance that the aggregate has.
		     */
		    KstatAggregate ksa = new KstatAggregate(jkstat, kss,
						statSpecifier + " aggregate");
		    if (lineStyle.isSelected()) {
			new KstatChartFrame(jkstat, ksa, stats, showRate);
		    } else {
			new KstatAreaChartFrame(jkstat, ksa, stats, showRate);
		    }
		} else {
		    if (stats.size() == 1) {
			String stat = stats.get(0);
			if (lineStyle.isSelected()) {
			    new KstatChartFrame(jkstat, kss, stat, showRate);
			} else {
			    new KstatAreaChartFrame(jkstat, kss, stat,
						showRate);
			}
		    } else {
			JOptionPane.showMessageDialog(this,
			    KstatResources.getString("CHART.INVOPT.MSG"),
			    KstatResources.getString("CHART.INVOPT.TITLE"),
			    JOptionPane.WARNING_MESSAGE);
		    }
		}
	    } else {
		if (lineStyle.isSelected()) {
		    new KstatChartFrame(jkstat, myKstat, stats, showRate);
		} else {
		    new KstatAreaChartFrame(jkstat, myKstat, stats, showRate);
		}
	    }
	}
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
	if (e.getSource() == allInstanceButton) {
	    aggrInstanceButton.setEnabled(allInstanceButton.isSelected());
	    updateLabel();
	} else if (e.getSource() == goButton) {
	    createChart();
	}
    }

    // handle TreeSelectionListener events
    @Override
    public void valueChanged(final TreeSelectionEvent e) {
	TreePath tpth = e.getNewLeadSelectionPath();
	if (tpth != null) {
	    pickKstat((DefaultMutableTreeNode) tpth.getLastPathComponent());
	}
    }
}
