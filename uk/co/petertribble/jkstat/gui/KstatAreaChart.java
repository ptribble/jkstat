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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Date;
import uk.co.petertribble.jkstat.api.*;

import org.jfree.chart.ChartFactory;
import org.jfree.data.time.*;
import org.jfree.chart.plot.PlotOrientation;

/**
 * A graphical chart of a kstat, showing one or multiple statistics. The
 * statistic is charted by area, and multiple statistics are stacked.
 *
 * @author Peter Tribble
 */
public class KstatAreaChart extends KstatBaseChart {

    private ChartableKstat cks;
    private Kstat ks;
    private TimeTableXYDataset dataset;
    private List<String> statlist;
    private Set<String> allstats;

    /**
     * Create an area Chart of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} supplying the data
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatAreaChart(JKstat jkstat, Kstat ks, String statistic,
		boolean showdelta) {
	this.jkstat = jkstat;
	this.ks = ks;
	this.showdelta = showdelta;
	init(statistic);
    }

    /**
     * Create an area Chart of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} supplying the data
     * @param cks the {@code ChartableKstat} generating rates from the data
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatAreaChart(JKstat jkstat, Kstat ks, ChartableKstat cks,
		String statistic, boolean showdelta) {
	this.jkstat = jkstat;
	this.ks = ks;
	this.cks = cks;
	this.showdelta = showdelta;
	init(statistic);
    }

    /**
     * Create an area Chart of the given statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} supplying the data
     * @param statistics the statistics to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatAreaChart(JKstat jkstat, Kstat ks, List<String> statistics,
		boolean showdelta) {
	this.jkstat = jkstat;
	this.ks = ks;
	cks = new ChartableKstat(jkstat, ks);
	this.showdelta = showdelta;
	init(statistics);
    }

    private void init(String statistic) {
	List<String> statistics = new ArrayList<>();
	statistics.add(statistic);
	init(statistics);
    }

    private void init(List<String> statistics) {
	statlist = statistics;
	dataset = new TimeTableXYDataset();

	allstats = cks.getStatistics();

	if (jkstat instanceof SequencedJKstat) {
	    readAll(((SequencedJKstat) jkstat).newInstance());
	} else {
	    updateAccessory();
	}

	String ylabel = showdelta ? KstatResources.getString("CHART.RATE")
	    : KstatResources.getString("CHART.VALUE");

	chart = ChartFactory.createStackedXYAreaChart(
		cks.toString(),
		KstatResources.getString("CHART.TIME"),
		ylabel,
		dataset,
		PlotOrientation.VERTICAL,
		true,
		true,
		false);

	setAxes();

	if (!(jkstat instanceof SequencedJKstat)) {
	    startLoop();
	}
    }

    @Override
    public void addStatistic(String statistic) {
	statlist.add(statistic);
    }

    @Override
    public void removeStatistic(String statistic) {
	statlist.remove(statistic);
	for (int i = 0; i < dataset.getItemCount(); i++) {
	    dataset.remove(dataset.getTimePeriod(i), statistic);
	}
    }

    /*
     * read all the data from the kstat sequence
     */
    private void readAll(SequencedJKstat sjkstat) {
	cks.setJKstat(sjkstat);
	do {
	    if (sjkstat.getKstat(ks) != null) {
		readOne(new Millisecond(new Date(sjkstat.getTime())));
	    }
	} while (sjkstat.next());
    }

    @Override
    public void updateAccessory() {
	readOne(new Millisecond());
    }

    /*
     * Get and update the appropriate data.
     */
    private void readOne(Millisecond ms) {
	cks.update();
	for (String statistic : statlist) {
	    if (allstats.contains(statistic)) {
		dataset.add(ms, showdelta ? cks.getRate(statistic)
				: (double) cks.getValue(statistic), statistic);
	    }
	}
    }
}
