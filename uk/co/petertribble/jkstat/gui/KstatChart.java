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
public class KstatChart extends KstatBaseChart {

    private ChartableKstat cks;
    private Kstat ks;
    private TimeSeriesCollection dataset;
    private Map <String, TimeSeries> tsmap;

    /**
     * Create a Chart of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} supplying the data
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatChart(JKstat jkstat, Kstat ks, String statistic,
		boolean showdelta) {
	this(jkstat, ks, new ChartableKstat(jkstat, ks), statistic, showdelta);
    }

    /**
     * Create a Chart of the given statistic.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} supplying the data
     * @param cks the {@code ChartableKstat} generating rates from the data
     * @param statistic the statistic to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatChart(JKstat jkstat, Kstat ks, ChartableKstat cks,
		String statistic, boolean showdelta) {
	this.jkstat = jkstat;
	this.ks = ks;
	this.cks = cks;
	this.showdelta = showdelta;
	init(statistic);
    }

    /**
     * Create a Chart of the given statistics.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} supplying the data
     * @param statistics the statistics to be charted
     * @param showdelta if true, show rates, else show absolute values
     */
    public KstatChart(JKstat jkstat, Kstat ks, List <String> statistics,
		boolean showdelta) {
	this.jkstat = jkstat;
	this.ks = ks;
	cks = new ChartableKstat(jkstat, ks);
	this.showdelta = showdelta;
	init(statistics);
    }

    private void init(String statistic) {
	List <String> statistics = new ArrayList <String> ();
	statistics.add(statistic);
	init(statistics);
    }

    private void init(List <String> statistics) {
	tsmap = new HashMap <String, TimeSeries> ();
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
    public void addStatistic(String statistic) {
	dataset.addSeries(tsmap.get(statistic));
    }

    @Override
    public void removeStatistic(String statistic) {
	dataset.removeSeries(tsmap.get(statistic));
    }

    @Override
    public void setMaxAge(int maxage) {
	this.maxage = maxage;
	for (String s : tsmap.keySet()) {
	    tsmap.get(s).setMaximumItemAge(maxage);
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
	// loop over all statistics
	for (String statistic : tsmap.keySet()) {
	    tsmap.get(statistic).add(ms, showdelta ? cks.getRate(statistic)
				: (double) cks.getValue(statistic));
	}
    }
}
