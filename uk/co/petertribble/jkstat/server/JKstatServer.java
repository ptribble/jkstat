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

package uk.co.petertribble.jkstat.server;

import uk.co.petertribble.jkstat.api.*;

/**
 * This is the core of the Kstat server.
 *
 * XML-RPC has significant limitations on available data types. Using JSON as
 * the serialized form means we just pass Strings, avoiding the limitations.
 *
 * @author Peter Tribble
 */
public class JKstatServer {

    private static final JKstat JKSTAT = new NativeJKstat();

    /**
     * Return the current Kstat chain ID.
     *
     * @return an int representing the Kstat chain ID
     */
    public int getKCID() {
	return JKSTAT.getKCID();
    }

    /**
     * Return a list of available kstats, as a serialized JSON String.
     *
     * @return the List of available Kstats
     */
    public String listKstats() {
	return new KstatSet(JKSTAT).toJSON();
    }

    /**
     * Return the requested Kstat as a serialized JSON String.
     *
     * @param module the requested Kstat module
     * @param instance the requested Kstat instance
     * @param name the requested Kstat name
     *
     * @return JSON describing the Kstat, or an empty String if the Kstat
     * doesn't exist
     */
    public String kstat(final String module, final int instance,
			final String name) {
	Kstat ks = JKSTAT.getKstat(module, instance, name);
	return (ks == null) ? "" : ks.toJSON();
    }
}
