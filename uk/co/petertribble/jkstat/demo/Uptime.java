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
 * Copyright 2026 Peter Tribble
 *
 */

package uk.co.petertribble.jkstat.demo;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import uk.co.petertribble.jkstat.api.Kstat;
import uk.co.petertribble.jkstat.api.NativeJKstat;

/**
 * Print out the 1, 5, and 15 minute load averages in the style of uptime.
 *
 * @author Peter Tribble
 */
public final class Uptime {

    /**
     * The number of seconds in a minute.
     */
    private static final long MINSEC = 60;
    /**
     * The number of seconds in an hour.
     */
    private static final long HRSEC = 60 * 60;
    /**
     * The number of seconds in a day.
     */
    private static final long DAYSEC = 60 * 60 * 24;

    private Uptime() {
    }

    /**
     * Create the application.
     *
     * @param args command line arguments, ignored
     */
    public static void main(final String[] args) {
        DecimalFormat df = new DecimalFormat("##0.00");

	Kstat ks = new NativeJKstat().getKstat("unix", 0, "system_misc");

	System.out.print(LocalTime.now()
	    .format(DateTimeFormatter.ofPattern("H:mm:ss")) + " up ");

	long l = System.currentTimeMillis() / 1000 - ks.longData("boot_time");

	long days = l / DAYSEC;
	l %= DAYSEC;
	long hrs = l / HRSEC;
	l %= HRSEC;
	long mins = l / MINSEC;

	if (days > 1) {
	    System.out.print(days + " days ");
	} else if (days == 1) {
	    System.out.print("1 day ");
	}

	if (hrs > 1) {
	    System.out.print(hrs + " hrs ");
	} else if (hrs == 1) {
	    System.out.print("1 hr ");
	}

	if (mins > 1) {
	    System.out.print(mins + " mins, ");
	} else {
	    System.out.print(mins + " min, ");
	}

	System.out.print("load average: ");
	System.out.print(df.format(ks.longData("avenrun_1min") / 256.0) + ", ");
	System.out.print(df.format(ks.longData("avenrun_5min") / 256.0) + ", ");
	System.out.println(df.format(ks.longData("avenrun_15min") / 256.0));
    }
}
