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

package uk.co.petertribble.jkstat.demo;

import javax.swing.*;
import java.awt.event.*;
import uk.co.petertribble.jkstat.api.*;
import uk.co.petertribble.jkstat.gui.KstatResources;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
/**
 * Show the 1, 5, and 15 minute averages on a chart.
 *
 * @author Peter Tribble
 */
public final class JLoadChart extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    /**
     * Save the 1-minute load averages.
     */
    private TimeSeries ts1;
    /**
     * Save the 5-minute load averages.
     */
    private TimeSeries ts5;
    /**
     * Save the 15-minute load averages.
     */
    private TimeSeries ts15;

    private transient JKstat jkstat;
    /**
     * A menu item to exit the demo.
     */
    private JMenuItem exitItem;

    /**
     * A standalone demo charting load averages.
     */
    public JLoadChart() {
	super("JLoadChart");

	jkstat = new NativeJKstat();
	int maxage = 600000;

	String lavetext = KstatResources.getString("LOAD.AVERAGE.TEXT");
	ts1 = new TimeSeries("1min " + lavetext);
	ts1.setMaximumItemAge(maxage);
	ts5 = new TimeSeries("5min " + lavetext);
	ts5.setMaximumItemAge(maxage);
	ts15 = new TimeSeries("15min " + lavetext);
	ts15.setMaximumItemAge(maxage);

	updateAccessory();
	TimeSeriesCollection dataset = new TimeSeriesCollection();
	dataset.addSeries(ts1);
	dataset.addSeries(ts5);
	dataset.addSeries(ts15);

	JFreeChart chart = ChartFactory.createTimeSeriesChart(
		lavetext,
		KstatResources.getString("CHART.TIME"),
		KstatResources.getString("LOAD.LOAD.TEXT"),
		dataset,
		true,
		true,
		false);

	XYPlot xyplot = chart.getXYPlot();

	NumberAxis loadaxis = new NumberAxis(
				KstatResources.getString("LOAD.LOAD.TEXT"));
	loadaxis.setAutoRange(true);
	loadaxis.setAutoRangeIncludesZero(true);
	xyplot.setRangeAxis(loadaxis);

	DateAxis daxis = new DateAxis(KstatResources.getString("CHART.TIME"));
	daxis.setAutoRange(true);
	daxis.setFixedAutoRange(maxage);
	xyplot.setDomainAxis(daxis);

	addWindowListener(new WindowExit());
	setContentPane(new ChartPanel(chart));

	JMenuBar jm = new JMenuBar();
	JMenu jme = new JMenu(KstatResources.getString("FILE.TEXT"));
	jme.setMnemonic(KeyEvent.VK_F);
	exitItem = new JMenuItem(KstatResources.getString("FILE.EXIT.TEXT"),
				KeyEvent.VK_X);
	exitItem.addActionListener(this);
	jme.add(exitItem);
	jm.add(jme);

	setJMenuBar(jm);

	pack();
	setVisible(true);
	Timer timer = new Timer(5000, this);
	timer.start();
    }

    private void updateAccessory() {
	Kstat ksl = jkstat.getKstat("unix", 0, "system_misc");
	Millisecond ms = new Millisecond();
	ts1.add(ms, ksl.longData("avenrun_1min") / 256.0);
	ts5.add(ms, ksl.longData("avenrun_5min") / 256.0);
	ts15.add(ms, ksl.longData("avenrun_15min") / 256.0);
    }

    static class WindowExit extends WindowAdapter {
	@Override
	public void windowClosing(final WindowEvent we) {
	    System.exit(0);
	}
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
	if (e.getSource() == exitItem) {
	    System.exit(0);
	} else {
	    updateAccessory();
	}
    }

    /**
     * Create the application.
     *
     * @param args command line arguments, ignored
     */
    public static void main(final String[] args) {
	new JLoadChart();
    }
}
