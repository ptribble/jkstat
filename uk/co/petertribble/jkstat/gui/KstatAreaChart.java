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
