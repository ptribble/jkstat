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

@Path("getkcid")

public class KstatGetkcid {

    static final JKstat JKSTAT = new NativeJKstat();

    /**
     * Get the current kstat chain id.
     *
     * @return the current chain id, as a String
     */
    @GET
    @Produces("application/json")
    public String getKstat() {
	return Integer.toString(JKSTAT.getKCID());
    }
}
