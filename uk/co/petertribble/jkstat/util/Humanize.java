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

package uk.co.petertribble.jkstat.util;

import java.text.DecimalFormat;

/**
 * A class to convert numbers into human-readable versions with an appropriate
 * suffix.
 *
 * @author Peter Tribble
 */
public final class Humanize {

    private static final double KSCALE = 1024.0;
    private static final char[] NAMES = {'k', 'm', 'g', 't', 'p', 'e'};
    private static final DecimalFormat DF = new DecimalFormat("##0.0#");

    /**
     * Hide the constructor.
     */
    private Humanize() {
    }

    /**
     * Return a human readable version of the input number, with an extra
     * letter to denote k/m/g/t/p/e. The number is scaled by 1024 as many
     * times as necessary.
     *
     * @param l the number to Humanize
     *
     * @return a String representing the given number in human readable form
     */
    public static String scale(final long l) {
	return scale((double) l, "");
    }

    /**
     * Return a human readable version of the input number, with an extra
     * letter to denote k/m/g/t/p/e. The number is scaled by 1024 as many
     * times as necessary.
     *
     * @param l the number to Humanize
     * @param suffix a suffix appended to the String produced
     *
     * @return a String representing the given number in human readable form
     */
    public static String scale(final long l, final String suffix) {
	return scale((double) l, suffix);
    }

    /**
     * Return a human readable version of the input number, with an extra
     * letter to denote k/m/g/t/p/e. The number is scaled by 1024 as many
     * times as necessary.
     *
     * @param d the number to Humanize
     *
     * @return a String representing the given number in human readable form
     */
    public static String scale(final double d) {
	return scale(d, "");
    }

    /**
     * Return a human readable version of the input number, with an extra
     * letter to denote k/m/g/t/p/e. The number is scaled by 1024 as many
     * times as necessary.
     *
     * @param d the number to Humanize
     * @param suffix a suffix appended to the String produced
     *
     * @return a String representing the given number in human readable form
     */
    public static String scale(final double d, final String suffix) {
	double dvalue = d;
	/*
	 * This would be a lot easier if I could define an empty character
	 * in the first position of the names array. So if we are less than
	 * 1024, just return the number.
	 */
	if (dvalue <= KSCALE) {
	    return DF.format(dvalue) + " " + suffix;
	}
	/*
	 * Need to start at -1. We know from the test above that we have to
	 * scale at least once, so we always scale by 1024 and set i to zero,
	 * which points to k in the names array.
	 */
	int i = -1;
	while ((dvalue > KSCALE) && (i < 5)) {
	    dvalue /= KSCALE;
	    i++;
	}
	return DF.format(dvalue) + " " + NAMES[i] + suffix;
    }
}
