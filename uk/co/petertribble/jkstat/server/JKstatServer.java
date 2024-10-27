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
    public String kstat(String module, int instance, String name) {
	Kstat ks = JKSTAT.getKstat(module, instance, name);
	return (ks == null) ? "" : ks.toJSON();
    }
}
