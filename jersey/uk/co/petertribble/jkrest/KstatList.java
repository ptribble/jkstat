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

@Path("list")

public class KstatList {

    static final JKstat JKSTAT = new NativeJKstat();

    /**
     * Get the list of current kstats. Just returns metadata, without any data.
     *
     * @return a JSON formatted list of kstats
     */
    @GET
    @Produces("application/json")
    public String getKstat() {
	KstatSet kss = new KstatSet(JKSTAT);
	return kss.toJSON();
    }
}
