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
    private Set<Kstat> inkstats;
    private Set<Kstat> kstats;
    private JKstat jkstat;
    private boolean isdynamic;
    private String title;

    /**
     * Allocates a {@code KstatAggregate} comprising a Set of kstats.
     *
     * @param jkstat a {@code JKstat}
     * @param kss a {@code KstatSet} containing the Kstats to be aggregated
     */
    public KstatAggregate(final JKstat jkstat, final KstatSet kss) {
	this(jkstat, kss, "Aggregate");
    }

    /**
     * Allocates a {@code KstatAggregate} comprising a Set of kstats.
     *
     * @param jkstat a {@code JKstat}
     * @param kss a {@code KstatSet} containing the Kstats to be aggregated
     * @param title a String that can be used for presentation
     */
    public KstatAggregate(final JKstat jkstat, final KstatSet kss,
			  final String title) {
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
    public KstatAggregate(final JKstat jkstat, final Set<Kstat> inkstats) {
	this(jkstat, inkstats, "Aggregate");
    }

    /**
     * Allocates a {@code KstatAggregate} comprising a Set of kstats.
     *
     * @param jkstat a {@code JKstat}
     * @param inkstats a Set of Kstats to be aggregated
     * @param title a String that can be used for presentation
     */
    public KstatAggregate(final JKstat jkstat, final Set<Kstat> inkstats,
			  final String title) {
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
    public void setJKstat(final JKstat jkstat) {
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
    public long aggregate(final String s) {
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
    public float average(final String s) {
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
	return l / (float) n;
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
    public Set<Kstat> getKstats() {
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
