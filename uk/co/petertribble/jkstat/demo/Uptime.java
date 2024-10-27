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

	long l = System.currentTimeMillis()/1000 - ks.longData("boot_time");

	long days = l/(60*60*24);
	l %= (60*60*24);
	long hrs = l/(60*60);
	l %= (60*60);
	long mins = l/60;

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
	System.out.print(df.format(ks.longData("avenrun_1min")/256.0) + ", ");
	System.out.print(df.format(ks.longData("avenrun_5min")/256.0) + ", ");
	System.out.println(df.format(ks.longData("avenrun_15min")/256.0));
    }
}
