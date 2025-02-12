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
import java.util.TreeSet;

/**
 * Some utility methods to manpulate Kstat classes.
 *
 * @author Peter Tribble
 */
public final class KstatUtil {

    private KstatUtil() {
    }

    /**
     * Get a Set of the numerical statistics in the given Kstat. If the Kstat
     * hasn't been read, read it.
     *
     * @param jkstat a JKstat object
     * @param ks the Kstat of interest
     *
     * @return a Set of those statistics that are of a numeric type
     */
    public static Set<String> numericStatistics(JKstat jkstat, Kstat ks) {
	if (ks == null) {
	    return null;
	}
	Set<String> ss = ks.statistics();
	if (ss.isEmpty()) {
	    ks = jkstat.getKstat(ks);
	    if (ks == null) {
		return null;
	    }
	    ss = ks.statistics();
	}
	Set<String> ns = new TreeSet<>();
	for (String s : ss) {
	    if (ks.isNumeric(s)) {
		ns.add(s);
	    }
	}
	return ns;
    }

    /**
     * Get a Set of the numerical statistic in the given KstatAggregate.
     *
     * @param jkstat a JKstat object
     * @param ksa the KstatAggregate of interest
     *
     * @return a Set of those statistics that are of a numeric type
     */
    public static Set<String> numericStatistics(JKstat jkstat,
						KstatAggregate ksa) {
	if (ksa == null) {
	    return null;
	}
	Set<String> ns = new TreeSet<>();
	for (Kstat ks : ksa.getKstats()) {
	    ns.addAll(numericStatistics(jkstat, ks));
	}
	return ns;
    }

    /**
     * Utility routine to construct a set. The set is constructed from the
     * module:instance:name triplet, any of which can be empty.
     *
     * @param jkstat a JKstat object
     * @param s0 the kstat module
     * @param s1 the kstat instance
     * @param s2 the kstat name
     *
     * @return a KstatSet matching the supplied parameters
     */
    public static KstatSet makeSet(JKstat jkstat, String s0,
				String s1, String s2) {
	KstatFilter ksf = new KstatFilter(jkstat);
	ksf.addFilter(s0 + ":" + s1 + ":" + s2);
	return new KstatSet(jkstat, ksf);
    }

    /**
     * Utility routine to construct an aggregate. The aggregate is constructed
     * from the module:instance:name triplet, any of which can be empty.
     *
     * @param jkstat a JKstat object
     * @param s0 the kstat module
     * @param s1 the kstat instance
     * @param s2 the kstat name
     *
     * @return a KstatAggregate matching the supplied parameters
     */
    public static KstatAggregate makeAggr(JKstat jkstat, String s0,
				String s1, String s2) {
	return new KstatAggregate(jkstat, makeSet(jkstat, s0, s1, s2),
				s0 + ":" + s1 + ":" + s2);
    }

    /**
     * Utility routine to construct a Kstat.
     *
     * @param s0 the kstat module
     * @param s1 the kstat instance
     * @param s2 the kstat name
     *
     * @return a Kstat of the appropriate module, instance, and name
     */
    public static Kstat makeKstat(String s0, String s1, String s2) {
	try {
	    return new Kstat(s0, Integer.parseInt(s1), s2);
	} catch (NumberFormatException e) {
	    return null;
	}
    }
}
