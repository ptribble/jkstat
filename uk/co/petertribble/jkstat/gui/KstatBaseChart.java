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

package uk.co.petertribble.jkstat.gui;

import java.awt.Color;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import uk.co.petertribble.jkstat.api.*;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 * A graphical chart of a Kstat.
 *
 * @author Peter Tribble
 */
public abstract class KstatBaseChart implements ActionListener {

    /**
     * A reference to a Jkstat object.
     */
    protected JKstat jkstat;

    /**
     * The chart to draw.
     */
    protected JFreeChart chart;

    /**
     * A Timer, to update the chart in a loop.
     */
    protected Timer timer;

    /**
     * The initial update delay, in milliseconds.
     */
    protected int delay = 5000;

    /**
     * The maximum age of the data, in milliseconds. Data older than this
     * will be removed.
     */
    protected int maxage = 600000;

    /**
     * A flag to determine whether we show rates (if true) or raw values.
     */
    protected boolean showdelta;

    /**
     * Set up the X and Y axes.
     */
    public void setAxes() {
	XYPlot xyplot = chart.getXYPlot();

	String ylabel = showdelta ? KstatResources.getString("CHART.RATE")
	    : KstatResources.getString("CHART.VALUE");
	NumberAxis loadaxis = new NumberAxis(ylabel);
	loadaxis.setAutoRange(true);
	loadaxis.setAutoRangeIncludesZero(true);
	xyplot.setRangeAxis(loadaxis);

	DateAxis daxis = new DateAxis(KstatResources.getString("CHART.TIME"));
	daxis.setAutoRange(true);
	// let a sequence show its full date range
	if (!(jkstat instanceof SequencedJKstat)) {
	    daxis.setFixedAutoRange(maxage);
	}
	xyplot.setDomainAxis(daxis);
    }

    /**
     * Set the colours used in the chart.
     *
     * @param colors a List of Colors to be used to render the series of
     * data charts.
     */
    public void setColors(List<Color> colors) {
	XYPlot plot = chart.getXYPlot();
	XYItemRenderer renderer = plot.getRenderer();
	int n = 0;
	for (Color color : colors) {
	    renderer.setSeriesPaint(n, color);
	    n++;
	}
    }

    /**
     * Return the chart that is created.
     *
     * @return The created chart
     */
    public JFreeChart getChart() {
	return chart;
    }

    /**
     * Set the maximum age of the chart. Only statistics younger than this
     * age will be shown.
     *
     * @param maxage the required maximum age in milliseconds
     */
    public void setMaxAge(int maxage) {
	this.maxage = maxage;
    }

    /**
     * Update the statistics. This method must be implemented in order to do
     * anything useful.
     */
    public abstract void updateAccessory();

    /**
     * Add a statistic to the list of those being charted. This method must
     * be implemented in order to do anything useful.
     *
     * @param statistic the statistic to be added to the chart
     */
    public abstract void addStatistic(String statistic);

    /**
     * Remove a statistic from the list of those being charted. This method
     * must be implemented in order to do anything useful.
     *
     * @param statistic the statistic to be removed from the chart
     */
    public abstract void removeStatistic(String statistic);

    /**
     * Start the loop that updates the chart regularly.
     */
    public void startLoop() {
	if (timer == null) {
	    timer = new Timer(delay, this);
	}
	timer.start();
    }

    /**
     * Stop the loop that updates the chart.
     */
    public void stopLoop() {
	if (timer != null) {
	    timer.stop();
	}
    }

    /**
     * Set the loop delay to be the specified number of seconds. If a zero or
     * negative delay is requested, stop the updates and remember the previous
     * delay.
     *
     * @param interval the desired delay, in seconds
     */
    public void setDelay(int interval) {
	delay = interval * 1000;
	if (timer != null) {
	    timer.setDelay(delay);
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	updateAccessory();
    }
}
