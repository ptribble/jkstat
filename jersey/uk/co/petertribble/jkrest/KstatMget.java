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

package uk.co.petertribble.jkrest;
import javax.ws.rs.*;
import uk.co.petertribble.jkstat.api.*;

@Path("mget/{module}/{instance}/{namespecifier}")

public class KstatMget {

    static final JKstat JKSTAT = new NativeJKstat();

    /**
     * Get all the kstats matching the supplied pattern. You can use "*"
     * to match all, or a semicolon-separated list for multiple values.
     *
     * @param module the desired module
     * @param instance the desired instance, as a String
     * @param name the desired name
     *
     * @return a list of JSON formatted kstats
     */
    @GET
    @Produces("application/json")
    public String getKstats(@PathParam("module") final String module,
			   @PathParam("instance") final String instance,
			   @PathParam("namespecifier") final String name) {
	StringBuilder sb = new StringBuilder();
	sb.append('{');
	boolean first = true;
	for (String iname : name.split(";")) {
	    KstatFilter ksf = new KstatFilter(JKSTAT);
	    if ("*".equals(instance)) {
		ksf.addFilter(module + "::" + iname);
	    } else {
		ksf.addFilter(module + ":" + instance + ":" + iname);
	    }
	    // split separate lists
	    if (first) {
		first = false;
	    } else {
		sb.append(',');
	    }
	    sb.append('\"').append(iname).append("\":[");
	    boolean kfirst = true;
	    for (Kstat ks : ksf.getKstats()) {
		if (kfirst) {
		    kfirst = false;
		} else {
		    sb.append(',');
		}
		Kstat ks2 = JKSTAT.getKstat(ks);
		sb.append(ks2.toJSON());
	    }
	    sb.append(']');
	}
	sb.append('}');
	return sb.toString();
    }
}
