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

package uk.co.petertribble.jkstat.demo;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import uk.co.petertribble.jkstat.api.NativeJKstat;
import uk.co.petertribble.jkstat.api.Kstat;

/**
 * Print out the 1, 5, and 15 minute load averages in the style of uptime.
 *
 * @author Peter Tribble
 */
public final class Uptime {

    private Uptime() {
    }

    /**
     * Create the application.
     *
     * @param args command line arguments, ignored
     */
    public static void main(String[] args) {
        DecimalFormat df = new DecimalFormat("##0.00");

	Kstat ks = new NativeJKstat().getKstat("unix", 0, "system_misc");

	System.out.print(DateFormat.getTimeInstance(DateFormat.SHORT)
			.format(new Date()) + " up ");

	long l = System.currentTimeMillis() / 1000 - ks.longData("boot_time");

	long days = l / (60 * 60 * 24);
	l %= (60 * 60 * 24);
	long hrs = l / (60 * 60);
	l %= (60 * 60);
	long mins = l / 60;

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
