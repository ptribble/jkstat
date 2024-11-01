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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class to get rates of change from a Kstat (designed to be used in charts).
 *
 * @author Peter Tribble
 */
public class ChartableKstat {

    /**
     * A JKstat object to query for data.
     */
    protected JKstat jkstat;

    /**
     * A Kstat.
     */
    protected Kstat ks;

    /**
     * The snaptime of the last measurement.
     */
    protected long lastsnap;

    /**
     * A Map storing the last recorded values.
     */
    protected Map<String, Long> valueMap = new HashMap<>();

    /**
     * A Map storing the most recently calculated rates. This is calculated
     * upon update, rather than calculating rates on demand.
     */
    protected Map<String, Double> rateMap = new HashMap<>();

    /**
     * Create a new ChartableKstat. Required for inheritance.
     */
    protected ChartableKstat() {
    }

    /**
     * Create a ChartableKstat using the given Kstat as the data source.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} supplying the data
     */
    public ChartableKstat(JKstat jkstat, Kstat ks) {
	this(jkstat, ks, true);
    }

    /**
     * Create a ChartableKstat using the given Kstat as the data source.
     *
     * @param jkstat a {@code JKstat}
     * @param ks the {@code Kstat} supplying the data
     * @param init true if the list of values should be initialized to zero
     */
    public ChartableKstat(JKstat jkstat, Kstat ks, boolean init) {
	this.jkstat = jkstat;
	this.ks = ks;
	ks = jkstat.getKstat(ks);
	lastsnap = ks.getCrtime();
	if (init) {
	    for (String statistic : KstatUtil.numericStatistics(jkstat, ks)) {
		valueMap.put(statistic, 0L);
	    }
	}
	// necessary to initialize rateMap
	update();
    }

    /**
     * Resync the JKstat instance.
     *
     * @param jkstat the new jkstat instance to use
     */
    public void setJKstat(JKstat jkstat) {
	this.jkstat = jkstat;
    }

    /**
     * Update with new data. If there's a problem, return false. This indicates
     * that the underlying Kstat has disappeared.
     *
     * @return whether the update succeeded
     */
    public boolean update() {
	ks = jkstat.getKstat(ks);
	if (ks == null) {
	    return false;
	}
	double dt = ks.getSnaptime() - lastsnap;
	lastsnap = ks.getSnaptime();
	for (String statistic : valueMap.keySet()) {
	    long newvalue = ks.longData(statistic);
	    double d = (double) (newvalue - valueMap.get(statistic));
	    rateMap.put(statistic, 1000000000.0*d/dt);
	    valueMap.put(statistic, newvalue);
	}
	return true;
    }

    /**
     * Return the available statistics.
     *
     * @return the list of available statistics
     */
    public Set<String> getStatistics() {
	return new TreeSet<>(rateMap.keySet());
    }

    /**
     * Return the rate of change of the given statistic.
     *
     * @param s the statistic to get the rate of change of
     *
     * @return the rate of change of the given statistic
     */
    public double getRate(String s) {
	return rateMap.get(s);
    }

    /**
     * Return the value of the given statistic, or zero if the statistic isn't
     * present in the Map. This avoids the need to explicitly zero the values
     * ahead of time.
     *
     * @param s the statistic to get the value of
     *
     * @return the value of the given statistic
     */
    public long getValue(String s) {
	Long l = valueMap.get(s);
	return (l == null) ? 0L : l;
    }

    /**
     * Return the underlying Kstat.
     *
     * @return this ChartableKstat's underlying Kstat
     */
    public Kstat getKstat() {
	return ks;
    }

    /**
     * Return a String representation of this ChartableKstat suitable for use
     * as a graph label.
     */
    @Override
    public String toString() {
	return ks.getTriplet();
    }
}
