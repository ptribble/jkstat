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

/**
 * A graphical chart of a KstatSet, showing one or multiple statistics.
 *
 * @author Peter Tribble
 */
public final class KstatSetChart extends KstatBaseChart {

    private KstatSet kss;
    private TimeSeriesCollection dataset;
    private Map<String, TimeSeries> tsmap;
    private Map<Kstat, ChartableKstat> kMap;

    /**
     * Create a Chart of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param kss the {@code KstatSet} supplying the data
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatSetChart(final JKstat jkstat, final KstatSet kss,
			 final String statistic, final boolean showdelta) {
	this.jkstat = jkstat;
	this.kss = kss;
	this.showdelta = showdelta;
	init(statistic);
    }

    /**
     * Create a Chart of the given statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param kss the {@code KstatSet} supplying the data
     * @param statistics the statistics to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatSetChart(final JKstat jkstat, final KstatSet kss,
		    final List<String> statistics, final boolean showdelta) {
	this.jkstat = jkstat;
	this.kss = kss;
	this.showdelta = showdelta;
	init(statistics);
    }

    private void init(final String statistic) {
	List<String> statistics = new ArrayList<>();
	statistics.add(statistic);
	init(statistics);
    }

    private void init(final List<String> statistics) {
	dataset = new TimeSeriesCollection();
	tsmap = new HashMap<>();
	kMap = new HashMap<>();

	for (Kstat ks : kss.getKstats()) {
	    kMap.put(ks, new ChartableKstat(jkstat, ks));

	    for (String statistic : KstatUtil.numericStatistics(jkstat, ks)) {
		String s = ks.getTriplet() + ":" + statistic;
		tsmap.put(s, new TimeSeries(s));
	    }
	    for (String statistic : statistics) {
		dataset.addSeries(tsmap.get(ks.getTriplet() + ":" + statistic));
	    }
	}

	if (jkstat instanceof SequencedJKstat) {
	    readAll(((SequencedJKstat) jkstat).newInstance());
	} else {
	    setMaxAge(maxage);
	    updateAccessory();
	}

	String ylabel = showdelta ? KstatResources.getString("CHART.RATE")
	    : KstatResources.getString("CHART.VALUE");

	chart = ChartFactory.createTimeSeriesChart(
		kss.toString() == null ? "statistic" : kss.toString(),
		KstatResources.getString("CHART.TIME"),
		ylabel,
		dataset,
		true,
		true,
		false);

	setAxes();

	if (!(jkstat instanceof SequencedJKstat)) {
	    startLoop();
	}
    }

    @Override
    public void addStatistic(final String statistic) {
	for (Kstat ks : kss.getKstats()) {
	    dataset.addSeries(tsmap.get(ks.getTriplet() + ":" + statistic));
	}
    }

    @Override
    public void removeStatistic(final String statistic) {
	for (Kstat ks : kss.getKstats()) {
	    dataset.removeSeries(tsmap.get(ks.getTriplet() + ":" + statistic));
	}
    }

    @Override
    public void setMaxAge(final int maxage) {
	this.maxage = maxage;
	for (String s : tsmap.keySet()) {
	    tsmap.get(s).setMaximumItemAge(maxage);
	}
    }

    /*
     * read all the data from the kstat sequence
     */
    private void readAll(final SequencedJKstat sjkstat) {
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
    private void readOne(final Kstat ks, final Millisecond ms) {
	ChartableKstat cks = kMap.get(ks);
	cks.update();
	for (String statistic : KstatUtil.numericStatistics(jkstat, ks)) {
	    tsmap.get(ks.getTriplet() + ":" + statistic).add(ms,
			showdelta ? cks.getRate(statistic)
			: (double) cks.getValue(statistic));
	}
    }
}
