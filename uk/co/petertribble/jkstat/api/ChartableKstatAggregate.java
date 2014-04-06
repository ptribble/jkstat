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

package uk.co.petertribble.jkstat.api;

/**
 * A class to get rates of change from a Kstat aggregate (designed to be used
 * in charts).
 *
 * @author Peter Tribble
 */
public class ChartableKstatAggregate extends ChartableKstat {

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
	    rateMap.put(statistic, 1000000000.0*d/dt);
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
