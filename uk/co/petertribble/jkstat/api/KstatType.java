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
 * Stores information about kstat types. The kstat type is always stored and
 * manipulated as an int, so this class simply provides a mechanism to map
 * those ints to their names, and to provide a textual representation of the
 * kstat type.
 *
 * @author Peter Tribble
 */
public class KstatType {

    /**
     * Anything. If known, massaged to name/value form.
     */
    public static final int KSTAT_TYPE_RAW = 0;

    /**
     * Statistics stored as name/value pairs.
     */
    public static final int KSTAT_TYPE_NAMED = 1;

    /**
     * Interrupt statistics.
     */
    public static final int KSTAT_TYPE_INTR = 2;

    /**
     * I/O statistics.
     */
    public static final int KSTAT_TYPE_IO = 3;

    /**
     * Event timer.
     */
    public static final int KSTAT_TYPE_TIMER = 4;

    /**
     * Returns the kstat type as a String.
     *
     * @param type the type of kstat to be converted to a String
     *
     * @return the String representation of the requested kstat type
     */
    public static String getTypeAsString(int type) {
	String t;
	switch (type) {
	    case KSTAT_TYPE_RAW:
		t = "KSTAT_TYPE_RAW";
		break;
	    case KSTAT_TYPE_NAMED:
		t = "KSTAT_TYPE_NAMED";
		break;
	    case KSTAT_TYPE_INTR:
		t = "KSTAT_TYPE_INTR";
		break;
	    case KSTAT_TYPE_IO:
		t = "KSTAT_TYPE_IO";
		break;
	    case KSTAT_TYPE_TIMER:
		t = "KSTAT_TYPE_TIMER";
		break;
	    default:
		t = "UNKNOWN";
	}
	return t;
    }

    /**
     * Returns the kstat type as an int.
     *
     * @param type the name of the kstat type to be converted to an int
     *
     * @return the int representation of the requested kstat type
     */
    public static int getTypeAsInt(String type) {
	int t = 99;
	if ("KSTAT_TYPE_RAW".equals(type)) {
	    t = KSTAT_TYPE_RAW;
	} else if ("KSTAT_TYPE_NAMED".equals(type)) {
	    t = KSTAT_TYPE_NAMED;
	} else if ("KSTAT_TYPE_INTR".equals(type)) {
	    t = KSTAT_TYPE_INTR;
	} else if ("KSTAT_TYPE_IO".equals(type)) {
	    t = KSTAT_TYPE_IO;
	} else if ("KSTAT_TYPE_TIMER".equals(type)) {
	    t = KSTAT_TYPE_TIMER;
	}
	return t;
    }
}
