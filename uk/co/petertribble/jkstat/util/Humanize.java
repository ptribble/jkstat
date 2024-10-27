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
    private static final char[] NAMES = { 'k', 'm', 'g', 't', 'p', 'e'};
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
    public static String scale(long l) {
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
    public static String scale(long l, String suffix) {
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
    public static String scale(double d) {
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
    public static String scale(double d, String suffix) {
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
