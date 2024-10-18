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

import java.util.Set;
import java.util.HashSet;

/**
 * Represents an aggregated Set of Kstats, so that we can retrieve both
 * aggregated and average statistics across the Set.
 *
 * @author Peter Tribble
 */
public class KstatAggregate {

    private KstatSet kss;
    private Set <Kstat> inkstats;
    private Set <Kstat> kstats;
    private JKstat jkstat;
    private boolean isdynamic;
    private String title;

    /**
     * Allocates a {@code KstatAggregate} comprising a Set of kstats.
     *
     * @param jkstat a {@code JKstat}
     * @param kss a {@code KstatSet} containing the Kstats to be aggregated
     */
    public KstatAggregate(JKstat jkstat, KstatSet kss) {
	this(jkstat, kss, "Aggregate");
    }

    /**
     * Allocates a {@code KstatAggregate} comprising a Set of kstats.
     *
     * @param jkstat a {@code JKstat}
     * @param kss a {@code KstatSet} containing the Kstats to be aggregated
     * @param title a String that can be used for presentation
     */
    public KstatAggregate(JKstat jkstat, KstatSet kss, String title) {
	this.jkstat = jkstat;
	this.kss = kss;
	this.title = title;
	inkstats = kss.getKstats();
	isdynamic = true;
	kstats = inkstats;
    }

    /**
     * Allocates a {@code KstatAggregate} comprising a Set of kstats.
     *
     * @param jkstat a {@code JKstat}
     * @param inkstats a Set of Kstats to be aggregated
     */
    public KstatAggregate(JKstat jkstat, Set <Kstat> inkstats) {
	this(jkstat, inkstats, "Aggregate");
    }

    /**
     * Allocates a {@code KstatAggregate} comprising a Set of kstats.
     *
     * @param jkstat a {@code JKstat}
     * @param inkstats a Set of Kstats to be aggregated
     * @param title a String that can be used for presentation
     */
    public KstatAggregate(JKstat jkstat, Set <Kstat> inkstats, String title) {
	this.jkstat = jkstat;
	this.inkstats = inkstats;
	this.title = title;
	isdynamic = false;
	kstats = inkstats;
    }

    /**
     * Connect to a different {@code JKstat} object. This is probably a bug,
     * but the charts create a new instance of a {@code SequencedJKstat}. This
     * allows them to resync the aggregate jkstat with the one in the chart.
     *
     * @param jkstat a {@code JKstat}
     */
    public void setJKstat(JKstat jkstat) {
	this.jkstat = jkstat;
	if (isdynamic) {
	    kss.setJKstat(jkstat);
	}
    }

    /**
     * Read the kstats. Updates the kstat chain and reads the data.
     */
    public void read() {
	if (isdynamic) {
	    kss.chainupdate();
	    inkstats = kss.getKstats();
	}
	kstats = new HashSet<>();
	for (Kstat ks : inkstats) {
	    kstats.add(jkstat.getKstat(ks));
	}
    }

    /**
     * Get the aggregate of the given statistic. Simply adds up the values from
     * those kstats that contain the given statistic.
     *
     * @param s the desired statistic
     *
     * @return the aggregated value of the statistic
     */
    public long aggregate(String s) {
	long l = 0;
	for (Kstat ks : kstats) {
	    if (ks.isNumeric(s)) {
		l += ks.longData(s);
	    }
	}
	return l;
    }

    /**
     * Get the average of the given statistic. Simply adds up the values from
     * those kstats that contain the given statistic, divided by the number of
     * kstats that contain the given statistic. If none of the kstats contain
     * the statistic, then return zero.
     *
     * @param s the desired statistic
     *
     * @return the average value of the statistic
     */
    public float average(String s) {
	long l = 0;
	long n = 0;
	for (Kstat ks : kstats) {
	    if (ks.isNumeric(s)) {
		l += ks.longData(s);
		n++;
	    }
	}
	if (n == 0) {
	    return (float) 0;
	}
	return l/(float) n;
    }

    /**
     * Get the creation time of this KstatAggregate, defined as the creation
     * time of the oldest constituent Kstat. The oldest is used to try and
     * gain stability as Kstats are added and removed.
     *
     * @return the creation time of the oldest Kstat in this KstatAggregate
     */
    public long getCrtime() {
	long l = Long.MAX_VALUE;
	for (Kstat ks : kstats) {
	    if (ks.getCrtime() < l) {
		l = ks.getCrtime();
	    }
	}
	return l;
    }

    /**
     * Get the snap time of this KstatAggregate, defined as the most recent
     * snap time of the constituent Kstats.
     *
     * @return the most recent snap time of the Kstats in this KstatAggregate
     */
    public long getSnaptime() {
	long l = 0;
	for (Kstat ks : kstats) {
	    if (ks.getSnaptime() > l) {
		l = ks.getSnaptime();
	    }
	}
	return l;
    }

    /**
     * Returns the Kstats contained in this aggregate.
     *
     * @return the Set of Kstats contained in this aggregate
     */
    public Set <Kstat> getKstats() {
	return kstats;
    }

    /**
     * Returns a String representation of this KstatAggregate. If a title was
     * supplied in the constructor, then use that, else a standard String is
     * used.
     *
     * @return a String representation of this KstatAggregate
     */
    @Override
    public String toString() {
	return title;
    }
}
