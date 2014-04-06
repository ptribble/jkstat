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
import java.util.TreeSet;

/**
 * Some utility methods to manpulate Kstat classes.
 *
 * @author Peter Tribble
 */
public class KstatUtil {

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
    public static Set <String> numericStatistics(JKstat jkstat, Kstat ks) {
	if (ks == null) {
	    return null;
	}
	Set <String> ss = ks.statistics();
	if (ss.isEmpty()) {
	    ks = jkstat.getKstat(ks);
	    if (ks == null) {
		return null;
	    }
	    ss = ks.statistics();
	}
	Set <String> ns = new TreeSet <String> ();
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
    public static Set <String> numericStatistics(JKstat jkstat,
						KstatAggregate ksa) {
	if (ksa == null) {
	    return null;
	}
	Set <String> ns = new TreeSet <String> ();
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
