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
 * A graphical chart of a kstat, showing one or multiple statistics.
 *
 * @author Peter Tribble
 */
public final class KstatChart extends KstatBaseChart {

    private ChartableKstat cks;
    private Kstat ks;
    private TimeSeriesCollection dataset;
    private Map<String, TimeSeries> tsmap;

    /**
     * Create a Chart of the given statistic.
     *
     * @param njkstat a {@code JKstat}
     * @param nks the {@code Kstat} supplying the data
     * @param statistic the statistic to be charted
     * @param sdelta if true, show rates, else show absolute values
     */
    public KstatChart(final JKstat njkstat, final Kstat nks,
		      final String statistic, final boolean sdelta) {
	this(njkstat, nks, new ChartableKstat(njkstat, nks), statistic, sdelta);
    }

    /**
     * Create a Chart of the given statistic.
     *
     * @param njkstat a {@code JKstat}
     * @param nks the {@code Kstat} supplying the data
     * @param ncks the {@code ChartableKstat} generating rates from the data
     * @param statistic the statistic to be charted
     * @param sdelta if true, show rates, else show absolute values
     */
    public KstatChart(final JKstat njkstat, final Kstat nks,
		      final ChartableKstat ncks, final String statistic,
		      final boolean sdelta) {
	jkstat = njkstat;
	ks = nks;
	cks = ncks;
	showdelta = sdelta;
	init(statistic);
    }

    /**
     * Create a Chart of the given statistics.
     *
     * @param njkstat a {@code JKstat}
     * @param nks the {@code Kstat} supplying the data
     * @param statistics the statistics to be charted
     * @param sdelta if true, show rates, else show absolute values
     */
    public KstatChart(final JKstat njkstat, final Kstat nks,
		      final List<String> statistics, final boolean sdelta) {
	jkstat = njkstat;
	ks = nks;
	cks = new ChartableKstat(jkstat, ks);
	showdelta = sdelta;
	init(statistics);
    }

    private void init(final String statistic) {
	List<String> statistics = new ArrayList<>();
	statistics.add(statistic);
	init(statistics);
    }

    private void init(final List<String> statistics) {
	tsmap = new HashMap<>();
	dataset = new TimeSeriesCollection();

	// this is all the statistics
	for (String statistic : cks.getStatistics()) {
	    tsmap.put(statistic, new TimeSeries(statistic));
	}

	// just display these
	// FIXME what if the statistic isn't valid and isn't present in tsmap?
	for (String statistic : statistics) {
	    addStatistic(statistic);
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
		cks.toString(),
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
	dataset.addSeries(tsmap.get(statistic));
    }

    @Override
    public void removeStatistic(final String statistic) {
	dataset.removeSeries(tsmap.get(statistic));
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
    private void readOne(final Millisecond ms) {
	cks.update();
	// loop over all statistics
	for (String statistic : tsmap.keySet()) {
	    tsmap.get(statistic).add(ms, showdelta ? cks.getRate(statistic)
				: (double) cks.getValue(statistic));
	}
    }
}
