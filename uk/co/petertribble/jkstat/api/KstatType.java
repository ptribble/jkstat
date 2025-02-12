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
 * Stores information about kstat types. The kstat type is always stored and
 * manipulated as an int, so this class simply provides a mechanism to map
 * those ints to their names, and to provide a textual representation of the
 * kstat type.
 *
 * @author Peter Tribble
 */
public final class KstatType {

    private KstatType() {
    }

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
