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

package uk.co.petertribble.jkstat.api;

/**
 * A class to get rates of change from a Kstat aggregate (designed to be used
 * in charts).
 *
 * @author Peter Tribble
 */
public final class ChartableKstatAggregate extends ChartableKstat {

    private KstatAggregate ksa;

    /**
     * Create a ChartableKstatAggregate using the given KstatAggregate as the
     * data source.
     *
     * @param jkstat a {@code JKstat}
     * @param ksa the {@code KstatAggregate} supplying the data
     */
    public ChartableKstatAggregate(JKstat jkstat, KstatAggregate ksa) {
	this.ksa = ksa;
	ksa.read();
	lastsnap = ksa.getCrtime();
	for (String statistic : KstatUtil.numericStatistics(jkstat, ksa)) {
	    valueMap.put(statistic, 0L);
	}
	update();
    }

    @Override
    public void setJKstat(JKstat jkstat) {
	ksa.setJKstat(jkstat);
    }

    /**
     * Update with new data.
     *
     * @return always returns true
     */
    @Override
    public boolean update() {
	ksa.read();
	double dt = ksa.getSnaptime() - lastsnap;
	lastsnap = ksa.getSnaptime();
	for (String statistic : valueMap.keySet()) {
	    long newvalue = ksa.aggregate(statistic);
	    double d = (double) (newvalue - valueMap.get(statistic));
	    rateMap.put(statistic, 1000000000.0 * d / dt);
	    valueMap.put(statistic, newvalue);
	}
	return true;
    }

    /**
     * Return a String representation of this ChartableKstatAggregate suitable
     * for use as a graph label.
     */
    @Override
    public String toString() {
	return ksa.toString();
    }
}
