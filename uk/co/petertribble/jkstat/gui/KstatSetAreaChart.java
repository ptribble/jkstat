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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import uk.co.petertribble.jkstat.api.*;

import org.jfree.chart.ChartFactory;
import org.jfree.data.time.*;
import org.jfree.chart.plot.PlotOrientation;

/**
 * A graphical chart of a KstatSet, showing one or multiple statistics. The
 * statistic is charted by area, and multiple statistics are stacked.
 *
 * @author Peter Tribble
 */
public final class KstatSetAreaChart extends KstatBaseChart {

    private KstatSet kss;
    private TimeTableXYDataset dataset;
    private List<String> statlist;
    private Map<Kstat, ChartableKstat> kMap;

    /**
     * Create an area Chart of the rate of change of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param kss the {@code KstatSet} supplying the data
     * @param statistic the statistic to be charted
     */
    public KstatSetAreaChart(JKstat jkstat, KstatSet kss, String statistic) {
	this(jkstat, kss, statistic, true);
    }

    /**
     * Create an area Chart of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param kss the {@code KstatSet} supplying the data
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatSetAreaChart(JKstat jkstat, KstatSet kss, String statistic,
		boolean showdelta) {
	this.jkstat = jkstat;
	this.kss = kss;
	this.showdelta = showdelta;
	init(statistic);
    }

    /**
     * Create an area Chart of the given statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param kss the {@code KstatSet} supplying the data
     * @param statistics the statistics to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatSetAreaChart(JKstat jkstat, KstatSet kss,
		List<String> statistics, boolean showdelta) {
	this.jkstat = jkstat;
	this.kss = kss;
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
	kMap = new HashMap<>();

	for (Kstat ks : kss.getKstats()) {
	    kMap.put(ks, new ChartableKstat(jkstat, ks));
	}

	if (jkstat instanceof SequencedJKstat) {
	    readAll(((SequencedJKstat) jkstat).newInstance());
	} else {
	    updateAccessory();
	}

	String ylabel = showdelta ? KstatResources.getString("CHART.RATE")
	    : KstatResources.getString("CHART.VALUE");

	chart = ChartFactory.createStackedXYAreaChart(
		kss.toString() == null ? "statistic" : kss.toString(),
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
    }

    /*
     * read all the data from the kstat sequence
     */
    private void readAll(SequencedJKstat sjkstat) {
	for (ChartableKstat ck : kMap.values()) {
	    ck.setJKstat(sjkstat);
	}
	do {
	    // FIXME update the KstatSet if it changes
	    for (Kstat ks : kss.getKstats()) {
		if (sjkstat.getKstat(ks) != null) {
		    readOne(ks, new Millisecond(new Date(sjkstat.getTime())));
		}
	    }
	} while (sjkstat.next());
    }

    @Override
    public void updateAccessory() {
	for (Kstat ks : kss.getKstats()) {
	    readOne(ks, new Millisecond());
	}
    }

    /*
     * Get and update the appropriate data.
     */
    private void readOne(Kstat ks, Millisecond ms) {
	ChartableKstat cks = kMap.get(ks);
	cks.update();
	for (String statistic : statlist) {
	    dataset.add(ms, showdelta ? cks.getRate(statistic)
			: (double) cks.getValue(statistic),
			ks.getTriplet() + ":" + statistic);
	}
    }
}
