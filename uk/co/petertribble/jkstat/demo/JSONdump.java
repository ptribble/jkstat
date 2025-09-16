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

import uk.co.petertribble.jkstat.api.NativeJKstat;
import uk.co.petertribble.jkstat.api.JKstat;
import uk.co.petertribble.jkstat.api.Kstat;

/**
 * Dump out the entire kstat hierarchy in JSON format.
 *
 * @author Peter Tribble
 */
public final class JSONdump {

    private JSONdump() {
    }

    /**
     * Create the application.
     *
     * @param args command line arguments, ignored
     */
    public static void main(final String[] args) {
	boolean firstentry = true;
	JKstat jkstat = new NativeJKstat();
	System.out.println("[");
	for (Kstat ks : jkstat.getKstats()) {
	    Kstat nks = jkstat.getKstat(ks);
	    if (nks != null) {
		if (firstentry) {
		    firstentry = false;
		} else {
		    System.out.println(",");
		}
		System.out.println(jkstat.getKstat(ks).toJSON());
	    }
	}
	System.out.println("]");
    }
}
